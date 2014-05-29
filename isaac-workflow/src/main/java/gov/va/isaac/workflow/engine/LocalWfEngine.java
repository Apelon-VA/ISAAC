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

import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceCreationRequest;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
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
            List<LocalTask> actions = ltapi.getOwnedTasksByActionStatus(userId, "pending");
            for (LocalTask loopTask : actions) {
                Task remoteTask = remoteService.getTaskById(loopTask.getId());
                if (remoteTask.getTaskData().getStatus().equals(remoteTask.getTaskData().getStatus().Completed)) {
                    // too late, task not available
                } else if (remoteTask.getTaskData().getStatus().equals(remoteTask.getTaskData().getStatus().Reserved)) {
                    // start and action
                    if (loopTask.getAction().equals("COMPLETE")) {
                        remoteService.start(loopTask.getId(), userId);
                        remoteService.complete(loopTask.getId(), userId, new HashMap<String,Object>());
                        ltapi.setAction(loopTask.getId(), loopTask.getAction(), "complete");
                    } else if (loopTask.getAction().equals("RELEASE")) {
                        remoteService.release(loopTask.getId(), userId);
                        ltapi.setAction(loopTask.getId(), loopTask.getAction(), "released");
                    }
                }  else if (remoteTask.getTaskData().getStatus().equals(remoteTask.getTaskData().getStatus().InProgress)) {
                    // action
                    if (loopTask.getAction().equals("COMPLETE")) {
                        remoteService.complete(loopTask.getId(), userId, new HashMap<String,Object>());
                        ltapi.setAction(loopTask.getId(), loopTask.getAction(), "complete");
                    } else if (loopTask.getAction().equals("RELEASE")) {
                        remoteService.release(loopTask.getId(), userId);
                        ltapi.setAction(loopTask.getId(), loopTask.getAction(), "released");
                    }
                }
                
                countActions++;
            }
            ltapi.commit();

            // Upload pending requests
            int countInstances = 0;
            List<ProcessInstanceCreationRequest> pendingRequests = procApi.getOpenOwnedRequests(userId);
            for (ProcessInstanceCreationRequest loopP : pendingRequests) {
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
            log.error("Error synchronizing", ex);
        }
    }

    @Override
    public void requestProcessInstanceCreationToServer(ProcessInstanceCreationRequest instanceRequest) {
        KieSession session = getRemoteEngine().getKieSession();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("componentId", instanceRequest.getComponentId());
        params.put("componentName", instanceRequest.getComponentName());
        params.put("createdBy", instanceRequest.getUserId());
        if (instanceRequest.getParams() != null) {
            params.putAll(instanceRequest.getParams());
        }
        ProcessInstance newInstance = session.startProcess(instanceRequest.getProcessName(), params);
        processRequestsApi.updateRequestStatus(instanceRequest.getId(),
                ProcessInstanceCreationRequest.RequestStatus.CREATED,
                "Instance created on KIE Server: " + getUrl().toString(), newInstance.getId());
    }

    @Override
    public LocalTasksServiceBI getLocalTaskService() {
        if (LocalWfEngine.localTasksService != null) {
            return LocalWfEngine.localTasksService;
        } else {
            LocalWfEngine.localTasksService = new LocalTasksApi();
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
            log.debug(loopTask.getActualOwner().getId() + " " + userId);
            if (loopTask.getActualOwner() ==  null || !loopTask.getActualOwner().getId().equals(userId)) {
                remoteService.claim(loopTask.getId(), userId);
                claimed++;
            }
            if (claimed >= count) break;
        }
    }


}
