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
import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author alo
 */
public class ReviewWorkflowTest {

    public static void main(String[] args) {
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();

        String userid = "alejandro";

        LocalTasksServiceBI localTasksService = wfEngine.getLocalTaskService();
        ProcessInstanceServiceBI processService = wfEngine.getProcessInstanceService();

        localTasksService.dropSchema();
        localTasksService.createSchema();
        processService.dropSchema();
        processService.createSchema();

        // Create Instance
        Map<String,String> variables = new HashMap<String, String>();
        processService.createRequest("terminology-authoring.ReviewWorkflow", "56968009", "Nocturnal intermittent asthma (disorder)", "alejandro", variables);
        wfEngine.synchronizeWithRemote();

        // Claim a task
        wfEngine.claim(1, userid);
        wfEngine.synchronizeWithRemote();

        //Get 1st task
        Long taskId = localTasksService.getOpenOwnedTasks(userid).iterator().next().getId();

        //Complete 1st task
        HashMap<String, String> v1 = new HashMap<String, String>();
        v1.put("out_comment", "Edit is finished");
        localTasksService.setAction(taskId, Action.COMPLETE, "pending", v1);
        wfEngine.synchronizeWithRemote();

        // Claim next task
        wfEngine.claim(1, userid);
        wfEngine.synchronizeWithRemote();
        Long secondTaskId = localTasksService.getOpenOwnedTasks(userid).iterator().next().getId();

        //Complete 2nd task
        HashMap<String, String> v2 = new HashMap<String, String>();
        v2.put("out_comment", "The edit looks OK");
        v2.put("out_response", "approve");
        localTasksService.setAction(secondTaskId, Action.COMPLETE, "pending", v2);
        wfEngine.synchronizeWithRemote();

        // Claim next task
        wfEngine.claim(1, userid);
        wfEngine.synchronizeWithRemote();
        Long thirdTaskId = localTasksService.getOpenOwnedTasks(userid).iterator().next().getId();

        //Complete 2nd task
        HashMap<String, String> v3 = new HashMap<String, String>();
        v3.put("out_comment", "Ready to be published");
        v3.put("out_response", "approve");
        localTasksService.setAction(thirdTaskId, Action.COMPLETE, "pending", v3);
        wfEngine.synchronizeWithRemote();



        //assertTrue(localTasksService.getOpenOwnedTasks(userid).size() > 0);

//       Long lastTaskId = null;
//        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
//            System.out.println("TaskId: " + loopTask.getId() + " " + loopTask.getName());
//            lastTaskId = loopTask.getId();
//            if (loopTask.getInputVariables() != null) {
//                for (String key : loopTask.getInputVariables().keySet()) {
//                    System.out.println("Input variable: " + key + ": " + loopTask.getInputVariables().get(key));
//                }
//            }
//        }
//
//        //localTasksService.setAction(29L, "COMPLETE", "pending");
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
//
//        processService.createRequest("terminology-authoring.test1", "56968009", "Guillermo Wood asthma (disorder)", "alejandro");
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
