/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.isaac.isaac.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;

/**
 *
 * @author alo
 */
public class WorkflowApiDemo {
    
    public static void main(String[] args) {
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        
        String userId = "test-user";
        
        List<Status> openStatuses = new ArrayList<Status>();
        openStatuses.add(Status.Ready);
        
        // Get tasks for user from local task cache
        wfEngine.getLocalTaskService().getTasksOwnedByStatus(userId, openStatuses, "en-US");
        
        // List tasks in remote service that I could reclaim
        wfEngine.getRemoteTaskService().getTasksAssignedAsPotentialOwner(userId, "en-US");
        
        // Claim one of those tasks, bu taskId = 16
        wfEngine.getRemoteTaskService().claim(16, userId);
        
        // Synchronize with remote server
        wfEngine.synchronizeWithRemote();
        
        // Get tasks for user from local task cache again, it will contain task 16 now
        wfEngine.getLocalTaskService().getTasksOwnedByStatus(userId, openStatuses, "en-US");
        
        // Get Task 16
        Task task16 = wfEngine.getLocalTaskService().getTaskById(16);
        
        // Load workflow data in params
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("status", "Rejected");
        params.put("Comments", "There are errors in A and B, please resolve them.");
        
        // Complete task, using params and signed by user
        wfEngine.getLocalTaskService().complete(16, userId, params);
        
        // Get tasks for user from local task cache again, it will not contain task 16 as it does not have an open status
        wfEngine.getLocalTaskService().getTasksOwnedByStatus(userId, openStatuses, "en-US");
        
        // Synchronize with remote server, completed task will be uploaded, new tasks will be downloaded
        wfEngine.synchronizeWithRemote();
        
    }
    
}
