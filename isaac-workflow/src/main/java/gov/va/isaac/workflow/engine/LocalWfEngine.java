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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import gov.va.isaac.workflow.sync.TasksFetcher;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jvnet.hk2.annotations.Service;
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
 * {@link LocalWfEngine}
 *
 * @author alo
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class LocalWfEngine implements LocalWorkflowRuntimeEngineBI {

    private RuntimeEngine remoteEngine;
    
    @Inject
    private ProcessInstanceServiceBI procApi;
    
    @Inject
    private LocalTasksServiceBI ltapi;
    
    private static final Logger log = LoggerFactory.getLogger(LocalWfEngine.class);
    
    private LocalWfEngine()
    {
        //For HK2 to construct
    }

    @Override
    public void synchronizeWithRemote() {
        try {
            // Upload pending actions
            TaskService remoteService = getRemoteTaskService();
            int countActions = 0;
            String userId = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername();
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
            //TODO handle this error
        }
    }

    @Override
    public void requestProcessInstanceCreationToServer(ProcessInstanceCreationRequestI instanceRequest) throws RemoteException, DatastoreException {
        try
        {
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
            procApi.updateRequestStatus(instanceRequest.getId(),
                    ProcessInstanceCreationRequestI.RequestStatus.CREATED,
                    "Instance created on KIE Server: " + AppContext.getAppConfiguration().getWorkflowServerURLasURL().toString(), newInstance.getId());
        }
        catch (RuntimeException e)
        {
            throw new RemoteException("Server error", e);
        }
    }

    @Override
    public TaskService getRemoteTaskService() throws RemoteException {
        try
        {
            return getRemoteEngine().getTaskService();
        }
        catch (RuntimeException e)
        {
            throw new RemoteException("Server error", e);
        }
    }

    private RuntimeEngine getRemoteEngine() throws RemoteException {
        if (remoteEngine != null) {
            return remoteEngine;
        } else {
            try
            {
                RemoteRestRuntimeFactory restSessionFactory = new RemoteRestRuntimeFactory(AppContext.getAppConfiguration().getWorkflowServerDeploymentID(), 
                        AppContext.getAppConfiguration().getWorkflowServerURLasURL(),
                        ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername(), 
                        ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowPassword());
                remoteEngine = restSessionFactory.newRuntimeEngine();
                return remoteEngine;
            }
            catch (RuntimeException e)
            {
                throw new RemoteException("Error connecting to server", e);
            }
        }
    }

    @Override
    public Map<String, Object> getVariablesMapForTaskId(Long taskId) throws RemoteException {
        try
        {
            Task task = getRemoteEngine().getTaskService().getTaskById(taskId);
            Content contentById = getRemoteEngine().getTaskService().getContentById(task.getTaskData().getDocumentContentId());
            JaxbContent jaxbTaskContent = (JaxbContent) contentById;
            return jaxbTaskContent.getContentMap();
        }
        catch (RuntimeException e)
        {
            throw new RemoteException("Server error", e);
        }
    }

    @Override
    public void release(Long taskId) throws DatastoreException {
        Map<String, String> variableMap = new HashMap<>();
        ltapi.setAction(taskId, Action.RELEASE, TaskActionStatus.Pending, variableMap);
    }

    @Override
    public void claim(Integer count) throws RemoteException {
        TaskService remoteService = getRemoteTaskService();
        String userId = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername();
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
