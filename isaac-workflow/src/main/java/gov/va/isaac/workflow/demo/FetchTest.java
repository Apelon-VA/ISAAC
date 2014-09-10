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

import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.persistence.*;
import gov.va.isaac.workflow.sync.TasksFetcher;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alo
 */
public class FetchTest {
    
    public static void main(String[] args) {
        LocalTasksApi tapi = new LocalTasksApi();
        tapi.dropSchema();
        tapi.createSchema();
        String userId = "alejandro";
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        
        TasksFetcher tf = new TasksFetcher(wfEngine.getRemoteTaskService(), tapi);
        
        try {
            System.out.println("Reading Local Tasks Repository (no filters): ");
            List<LocalTask> openTasks = tapi.getTasks();
            System.out.println("count = " + openTasks.size());
            for (LocalTask lt : openTasks) {
                System.out.println("Local: " + lt.getId() + " - " + lt.getName() + " - " + lt.getStatus() + " - " + lt.getOwner() + " - " + lt.getComponentId()+ " - " + lt.getComponentName());
            }
            System.out.println("SYNC - Fetching from remote server...");
            tf.fetchTasks(userId);
            System.out.println("Reading Local Tasks Repository (no filters) after sync: ");
            List<LocalTask> openTasks1 = tapi.getTasks();
            System.out.println("count = " + openTasks1.size());
            for (LocalTask lt : openTasks1) {
                System.out.println("Local: " + lt.getId() + " - " + lt.getName() + " - " + lt.getStatus() + " - " + lt.getOwner() + " - " + lt.getComponentId()+ " - " + lt.getComponentName());
            }
            List<LocalTask> openOwnedTasks = tapi.getOpenOwnedTasks(userId);
            System.out.println("Testing getOpenOwnedTasks (" + userId + "): " + openOwnedTasks.size());
            
            List<LocalTask> ownedTasksForAsthma = tapi.getOpenOwnedTasksByComponentId(userId, "0ca5c7c0-9e6a-11e3-a5e2-0800200c9a66");
            System.out.println("Testing getOpenOwnedTasksByComponentId (Asthma, " + userId + "): " + ownedTasksForAsthma.size());
            
            List<LocalTask> ownedTasksByStatus = tapi.getOwnedTasksByStatus(userId, "Reserved");
            System.out.println("Testing ownedTasksByStatus (" + userId + ", Reserved): " + ownedTasksByStatus.size());
            
        } catch (Exception ex) {
            Logger.getLogger(FetchTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
