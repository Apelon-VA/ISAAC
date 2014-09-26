/* 
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.workflow.engine;

import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.*;
import gov.va.isaac.workflow.persistence.LocalTasksApi;
import gov.va.isaac.workflow.persistence.ProcessInstanceCreationRequestsAPI;
import gov.va.isaac.workflow.sync.TasksFetcher;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.RemoteRestRuntimeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alo
 */
public class LocalWfEngine implements LocalWorkflowRuntimeEngineBI {

    private URL url;
    private String userId;
    private String password;
    private String deploymentId;
    public static RuntimeEngine remoteEngine;
    public static ProcessInstanceCreationRequestsAPI processRequestsApi;
    public static LocalTasksServiceBI localTasksService;
    
    private static final Logger log = LoggerFactory.getLogger(LocalWfEngine.class);

    public LocalWfEngine(URL url, String userId, String password, String deploymentId) {
        this.url = url;
        this.userId = userId;
        this.password = password;
        this.deploymentId = deploymentId;
    }

    public URL getUrl() {
        return url;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    @Override
    public void setRemoteData(URL url, String userId, String password, String deploymentId) {
        this.url = url;
        this.userId = userId;
        this.password = password;
        this.deploymentId = deploymentId;
    }

    @Override
    public void synchronizeWithRemote() {
        try {
            // Upload pending actions
            LocalTasksServiceBI ltapi = getLocalTaskService();
            TaskService remoteService = getRemoteTaskService();
            ProcessInstanceServiceBI procApi = getProcessInstanceService();
            int countActions = 0;
            List<LocalTask> actions = ltapi.getOwnedTasksByActionStatus(TaskActionStatus.Pending);
            for (LocalTask loopTask : actions) {
                Task remoteTask = remoteService.getTaskById(loopTask.getId());
                if (remoteTask != null) {
                    remoteTask.getTaskData().getStatus();
                    if (remoteTask.getTaskData().getStatus().equals(Status.Completed)) {
                        // too late, task not available
                    } else if (remoteTask.getTaskData().getStatus().equals(Status.Reserved)) {
                        // start and action
                        if (loopTask.getAction().equals(Action.COMPLETE)) {
                            remoteService.start(loopTask.getId(), userId);
                            remoteService.complete(loopTask.getId(), userId, toObjectValueMap(loopTask.getOutputVariables()));
                            ltapi.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Complete,  loopTask.getOutputVariables());
                        } else if (loopTask.getAction().equals(Action.RELEASE)) {
                            remoteService.release(loopTask.getId(), userId);
                            ltapi.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Canceled,  loopTask.getOutputVariables());
                        }
                    }  else if (remoteTask.getTaskData().getStatus().equals(Status.InProgress)) {
                        // action
                        if (loopTask.getAction().equals(Action.COMPLETE)) {
                            remoteService.complete(loopTask.getId(), userId, toObjectValueMap(loopTask.getOutputVariables()));
                            ltapi.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Complete,  loopTask.getOutputVariables());
                        } else if (loopTask.getAction().equals(Action.RELEASE)) {
                            remoteService.release(loopTask.getId(), userId);
                            ltapi.setAction(loopTask.getId(), loopTask.getAction(), TaskActionStatus.Canceled,  loopTask.getOutputVariables());
                        }
                    }
                }
                
                countActions++;
            }
            ltapi.commit();

            // Upload pending requests
            int countInstances = 0;
            List<ProcessInstanceCreationRequestI> pendingRequests = procApi.getOpenOwnedRequests(userId);
            for (ProcessInstanceCreationRequestI loopP : pendingRequests) {
                requestProcessInstanceCreationToServer(loopP);
                countInstances++;
            }
            
            // Sync tasks
            TasksFetcher tf = new TasksFetcher(remoteService, ltapi);
            String result = tf.fetchTasks(userId);
            
            log.info("Sync finished");
            log.debug("   - Actions processed: {}", countActions);
            log.debug("   - Instances processed: {}", countInstances);
            log.debug("   - {}", result);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error synchronizing", ex);
        }
    }

    @Override
    public void requestProcessInstanceCreationToServer(ProcessInstanceCreationRequestI instanceRequest) {
        KieSession session = getRemoteEngine().getKieSession();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("component_id", instanceRequest.getComponentId());
        params.put("component_name", instanceRequest.getComponentName());
        params.put("created_by", instanceRequest.getUserId());
        params.putAll(instanceRequest.getVariables());
        if (instanceRequest.getParams() != null) {
            params.putAll(instanceRequest.getParams());
        }
        ProcessInstance newInstance = session.startProcess(instanceRequest.getProcessName(), params);
        processRequestsApi.updateRequestStatus(instanceRequest.getId(),
                ProcessInstanceCreationRequestI.RequestStatus.CREATED,
                "Instance created on KIE Server: " + getUrl().toString(), newInstance.getId());
    }

    @Override
    public LocalTasksServiceBI getLocalTaskService() {
        if (LocalWfEngine.localTasksService != null) {
            return LocalWfEngine.localTasksService;
        } else {
            LocalWfEngine.localTasksService = new LocalTasksApi(userId);
            return LocalWfEngine.localTasksService;
        }
    }

    @Override
    public TaskService getRemoteTaskService() {
        return getRemoteEngine().getTaskService();
    }

    private RuntimeEngine getRemoteEngine() {
        if (LocalWfEngine.remoteEngine != null) {
            return LocalWfEngine.remoteEngine;
        } else {
            RemoteRestRuntimeFactory restSessionFactory = new RemoteRestRuntimeFactory(deploymentId, url, userId, password);
            LocalWfEngine.remoteEngine = restSessionFactory.newRuntimeEngine();
            return LocalWfEngine.remoteEngine;
        }
    }

    @Override
    public Map<String, Object> getVariablesMapForTaskId(Long taskId) {
        Task task = getRemoteEngine().getTaskService().getTaskById(taskId);
        Content contentById = getRemoteEngine().getTaskService().getContentById(task.getTaskData().getDocumentContentId());
        JaxbContent jaxbTaskContent = (JaxbContent) contentById;
        return jaxbTaskContent.getContentMap();
        //return new HashMap<>();
    }

    @Override
    public ProcessInstanceServiceBI getProcessInstanceService() {
        if (LocalWfEngine.processRequestsApi != null) {
            return LocalWfEngine.processRequestsApi;
        } else {
            LocalWfEngine.processRequestsApi = new ProcessInstanceCreationRequestsAPI();
            return LocalWfEngine.processRequestsApi;
        }
    }

    @Override
    public void claim(Integer count, String userId) {
        TaskService remoteService = getRemoteTaskService();
        List<TaskSummary> availableTasks = remoteService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        log.debug("Available {}", availableTasks.size());
        int claimed = 0;
        for (TaskSummary loopTask : availableTasks) {
            if (loopTask.getActualOwner() ==  null || !loopTask.getActualOwner().getId().equals(userId)) {
                remoteService.claim(loopTask.getId(), userId);
                claimed++;
            }
            if (claimed >= count) break;
        }
    }

private HashMap<String, Object> toObjectValueMap(Map<String, String> sourceMap) {
    HashMap<String, Object> result = new HashMap<String, Object>();
    for (String loopSourceKey : sourceMap.keySet()) {
        result.put(loopSourceKey, sourceMap.get(loopSourceKey));
    }
    return result;
}


}
