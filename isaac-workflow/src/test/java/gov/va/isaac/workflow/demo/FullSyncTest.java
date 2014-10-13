package gov.va.isaac.workflow.demo;

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

import gov.va.isaac.AppContext;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.engine.RemoteSynchronizer;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.io.IOException;

/**
 *
 * @author alo
 */
public class FullSyncTest extends BaseTest {
    
   public static void main(String[] args) throws DatastoreException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
       NoSuchFieldException, SecurityException, IOException, InterruptedException {
        setup();

        LocalTasksServiceBI localTasksService = AppContext.getService(LocalTasksServiceBI.class);
        ProcessInstanceServiceBI processService = AppContext.getService(ProcessInstanceServiceBI.class);
        RemoteSynchronizer remoteSyncService = AppContext.getService(RemoteSynchronizer.class);
        remoteSyncService.loadRequested();
        
        localTasksService.dropSchema();
        localTasksService.createSchema();
        processService.dropSchema();
        processService.createSchema();
        
//        assertTrue(localTasksService.getOpenOwnedTasks(userid).size() == 0);
        
        remoteSyncService.blockingSynchronize();
        
        //assertTrue(localTasksService.getOpenOwnedTasks(userid).size() > 0);

        //Long lastTaskId = null;
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks()) {
            System.out.println("TaskId: " + loopTask.getId() + " " + loopTask.getName());
            System.out.println("Component: " + loopTask.getComponentId() + " " + loopTask.getComponentName());
            //lastTaskId = loopTask.getId();
            if (loopTask.getInputVariables() != null) {
                for (String key : loopTask.getInputVariables().keySet()) {
                    System.out.println("Input variable: " + key + ": " + loopTask.getInputVariables().get(key));
                }
            }
        }

        //localTasksService.setAction(29L, "COMPLETE", "pending");
//       System.out.println("LastTaskId: " + lastTaskId);
//       HashMap<String, String> variables = new HashMap<String, String>();
//       variables.put("out_evaluation", "value1");
//       localTasksService.setAction(lastTaskId, "COMPLETE", "pending", variables);
//        //localTasksService.setAction(25L, "RELEASE", "pending");
//
//        wfEngine.synchronizeWithRemote();
//
//        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
//            System.out.println("TaskId: " + loopTask.getId());
//        }
//
//
//       wfEngine.claim(1, userid);
//       System.out.println("After claim");
//        wfEngine.synchronizeWithRemote();
//
//        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
//            System.out.println("TaskId: " + loopTask.getId());
//        }
//        Map<String,String> variables2 = new HashMap<String, String>();
//        processService.createRequest("terminology-authoring.test1", "56968009", "Guillermo Wood asthma (disorder)", "alejandro", variables2);
////        processService.createRequest("terminology-authoring.test1", "56968009", "Guillermo 2 Wood asthma (disorder)", "alejandro");
//
//        System.out.println("After new instances");
//        wfEngine.synchronizeWithRemote();
//
//        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
//            System.out.println("TaskId: " + loopTask.getId());
//        }

    }
}
