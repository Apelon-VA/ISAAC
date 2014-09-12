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

import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.interfaces.workflow.WorkflowProcess;
import gov.va.isaac.workflow.persistence.ProcessInstanceCreationRequestsAPI;

import java.util.HashMap;
import java.util.Map;

import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;

/**
 *
 * @author alo
 */
public class ProcessInstanceCreationTester {
    
    public static void main(String[] args) {
        ProcessInstanceCreationRequestsAPI papi = new ProcessInstanceCreationRequestsAPI();
        papi.dropSchema();
        papi.createSchema();
        Map<String,String> variables = new HashMap<String, String>();
        variables.put("key 1", "value 1");
        variables.put("key 2", "value 2");
        papi.createRequest(WorkflowProcess.REVIEW.getText(), Snomed.ASTHMA.getUuids()[0], "Asthma (disorder)", "alejandro", variables);
        for (ProcessInstanceCreationRequestI loopR : papi.getRequests()) {
            System.out.println("id: " + loopR.getId() + " - pname: "  + loopR.getProcessName() + " - cid: " + loopR.getComponentId() + " - cname: " + loopR.getComponentName() + " - rtime: "  + loopR.getRequestTime() + " - status: "  + loopR.getStatus() + " - userId: "  + loopR.getUserId());
        }
        
    }
    
}
