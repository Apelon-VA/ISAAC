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
package gov.va.isaac.workflow.demo;


/**
 *
 * @author alo
 */
public class WorkflowApiDemo {
    
    public static void main(String[] args) {
//        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        
//        String userId = "test-user";
        
//        List<Status> openStatuses = new ArrayList<Status>();
//        openStatuses.add(Status.Ready);
//        
//        // Get tasks for user from local task cache
//        wfEngine.getLocalTaskService().getTasksOwnedByStatus(userId, openStatuses, "en-US");
//        
//        // List tasks in remote service that I could reclaim
//        wfEngine.getRemoteTaskService().getTasksAssignedAsPotentialOwner(userId, "en-US");
//        
//        // Claim one of those tasks, bu taskId = 16
//        wfEngine.getRemoteTaskService().claim(16, userId);
//        
//        // Synchronize with remote server
//        wfEngine.synchronizeWithRemote();
//        
//        // Get tasks for user from local task cache again, it will contain task 16 now
//        wfEngine.getLocalTaskService().getTasksOwnedByStatus(userId, openStatuses, "en-US");
//        
//        // Get Task 16
//        Task task16 = wfEngine.getLocalTaskService().getTaskById(16);
//        
//        // Load workflow data in params
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("status", "Rejected");
//        params.put("Comments", "There are errors in A and B, please resolve them.");
//        
//        // Complete task, using params and signed by user
//        wfEngine.getLocalTaskService().complete(16, userId, params);
//        
//        // Get tasks for user from local task cache again, it will not contain task 16 as it does not have an open status
//        wfEngine.getLocalTaskService().getTasksOwnedByStatus(userId, openStatuses, "en-US");
        
        // Synchronize with remote server, completed task will be uploaded, new tasks will be downloaded
//        wfEngine.synchronizeWithRemote();
        
    }
    
}
