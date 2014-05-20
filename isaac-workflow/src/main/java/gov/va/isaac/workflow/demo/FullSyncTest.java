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
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alo
 */
public class FullSyncTest {
    
   public static void main(String[] args) {
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        
        String userid = "alejandro";
        
        LocalTasksServiceBI localTasksService = wfEngine.getLocalTaskService();
        ProcessInstanceServiceBI processService = wfEngine.getProcessInstanceService();
        
        //localTasksService.dropSchema();
        //localTasksService.createSchema();
        //processService.dropSchema();
        //processService.createSchema();
        
        //assertTrue(localTasksService.getOpenOwnedTasks(userid).size() == 0);
        
        wfEngine.synchronizeWithRemote();
        
        //assertTrue(localTasksService.getOpenOwnedTasks(userid).size() > 0);
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
            System.out.println("TaskId: " + loopTask.getId());
        }
        
        //localTasksService.setAction(29L, "COMPLETE", "pending");
        localTasksService.setAction(27L, "RELEASE", "pending");
        //localTasksService.setAction(25L, "RELEASE", "pending");
        
        wfEngine.synchronizeWithRemote();
        
        assertTrue(localTasksService.getOpenOwnedTasks(userid).size() > 0);
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
            System.out.println("TaskId: " + loopTask.getId());
        }
        
        
        //wfEngine.claim(5, userid);
        
        wfEngine.synchronizeWithRemote();
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
            System.out.println("TaskId: " + loopTask.getId());
        }
        
        processService.createRequest("terminology-authoring.test1", "56968009", "Guillermo Wood asthma (disorder)", "alejandro");
        processService.createRequest("terminology-authoring.test1", "56968009", "Guillermo 2 Wood asthma (disorder)", "alejandro");
        
        wfEngine.synchronizeWithRemote();
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks(userid)) {
            System.out.println("TaskId: " + loopTask.getId());
        }

    }
}
