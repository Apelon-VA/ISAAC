/*
 * Copyright 2014 alo.
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
package gov.va.isaac.workflow.sync;

import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.persistence.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alo
 */
public class FetchTest {
    
    public static void main(String[] args) {
        LocalTasksApi tapi = new LocalTasksApi();
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        
        TasksFetcher tf = new TasksFetcher(wfEngine.getRemoteTaskService(), tapi);
        try {
            tf.fetchTasks("alejandro");
            
            tf.claimBatch("alejandro", 2);
            
            tf.fetchTasks("alejandro");
        } catch (Exception ex) {
            Logger.getLogger(FetchTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
