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
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
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
    public void requestProcessInstanceCreationToServer(ProcessInstanceCreationRequestI instanceRequest) throws RemoteException, DatastoreException {
    	ProcessInstance newInstance = null;
    	try
        {
            KieSession session = AppContext.getService(RemoteWfEngine.class).getRemoteEngine().getKieSession();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("component_id", instanceRequest.getComponentId());
            params.put("component_name", instanceRequest.getComponentName());
            params.put("created_by", instanceRequest.getUserId());
            params.putAll(instanceRequest.getVariables());
            if (instanceRequest.getParams() != null) {
                params.putAll(instanceRequest.getParams());
            }
            newInstance = session.startProcess(instanceRequest.getProcessName(), params);
            procApi.updateRequestStatus(instanceRequest.getId(),
                    ProcessInstanceCreationRequestI.RequestStatus.CREATED,
                    "Instance created on KIE Server: " + AppContext.getAppConfiguration().getCurrentWorkflowServerUrl().toString(), newInstance.getId());
        }
        catch (RuntimeException e)
            {
                procApi.updateRequestStatus(instanceRequest.getId(),
                        ProcessInstanceCreationRequestI.RequestStatus.REJECTED,
                        "Instance rejected by KIE Server: " + AppContext.getAppConfiguration().getCurrentWorkflowServerUrl().toString() + " - " + e.getMessage(), newInstance != null ? newInstance.getId() : 0);
                throw new RemoteException("Server error", e);
        }
    }

    @Override
    public Map<String, Object> getVariablesMapForTaskId(Long taskId) throws RemoteException {
        try
        {
            Task task = AppContext.getService(RemoteWfEngine.class).getRemoteEngine().getTaskService().getTaskById(taskId);
            Content contentById = AppContext.getService(RemoteWfEngine.class).getRemoteEngine().getTaskService().getContentById(task.getTaskData().getDocumentContentId());
            if (contentById == null)
            {
                log.error("Task {} is missing content!", task.getId());
                return new HashMap<String, Object>();
            }
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
        TaskService remoteService = AppContext.getService(RemoteWfEngine.class).getRemoteTaskService();
        String userId = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername();
        log.info("Workflow user {} requesting to claim {} tasks", userId, count);
        List<TaskSummary> availableTasks = remoteService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        log.debug("Available {}", availableTasks.size());
        int claimed = 0;
        for (TaskSummary loopTask : availableTasks) {
            if (loopTask.getStatus().equals(Status.Ready)) {
                if (loopTask.getActualOwner() == null || !loopTask.getActualOwner().getId().equals(userId)) {
                    remoteService.claim(loopTask.getId(), userId);
                    claimed++;
                }
            }
            if (claimed >= count) break;
        }
    }
}
