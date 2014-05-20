package gov.va.isaac.workflow;

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
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
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
    
    public FullSyncTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void syncTest() {
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        
        LocalTasksServiceBI localTasksService = wfEngine.getLocalTaskService();
        ProcessInstanceServiceBI processService = wfEngine.getProcessInstanceService();
        
        localTasksService.dropSchema();
        localTasksService.createSchema();
        processService.dropSchema();
        processService.createSchema();
        
        assertTrue(localTasksService.getOpenOwnedTasks("alejandro").size() == 0);
        
        wfEngine.synchronizeWithRemote();
        
        assertTrue(localTasksService.getOpenOwnedTasks("alejandro").size() > 0);
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks("alejandro")) {
            System.out.println("TaskId: " + loopTask.getId());
        }
        
        localTasksService.setAction(22L, "COMPLETE", "pending");
        
        wfEngine.synchronizeWithRemote();
        
        assertTrue(localTasksService.getOpenOwnedTasks("alejandro").size() > 0);
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks("alejandro")) {
            System.out.println("TaskId: " + loopTask.getId());
        }
        
        wfEngine.synchronizeWithRemote();
        
        for (LocalTask loopTask : localTasksService.getOpenOwnedTasks("alejandro")) {
            System.out.println("TaskId: " + loopTask.getId());
        }
    }
}
