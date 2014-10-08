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

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.interfaces.workflow.WorkflowProcess;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author alo
 */
public class ProcessInstanceSynchronizationTester extends BaseTest {

    public static void main(String[] args) throws Exception {
        setup();
        LocalWorkflowRuntimeEngineBI wfEngine = AppContext.getService(LocalWorkflowRuntimeEngineBI.class);
        ProcessInstanceServiceBI procApi = AppContext.getService(ProcessInstanceServiceBI.class);
        procApi.dropSchema();
        procApi.createSchema();
        Map<String,String> variables = new HashMap<String, String>();
        variables.put("coordinateId", "16e04a1e-32a6-11e4-99ba-164230d1df67");
        variables.put("lastCommitTimeStamp", "1409665029");
        ProcessInstanceCreationRequestI newInstance = procApi.createRequest(WorkflowProcess.REVIEW3.getText(), Snomed.ASTHMA.getUuids()[0], "Asthma (disorder)", "alejandro", variables);
        System.out.println("New instance: " + newInstance.getId());
        
        List<ProcessInstanceCreationRequestI> pending = procApi.getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequestI.RequestStatus.REQUESTED);

        System.out.println("Pending count: " + pending.size());
        for (ProcessInstanceCreationRequestI loopP : pending) {
            System.out.println("Pending instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }
        
        List<ProcessInstanceCreationRequestI> completed = procApi.getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequestI.RequestStatus.CREATED);
        System.out.println("Complete count: " + completed.size());
        for (ProcessInstanceCreationRequestI loopP : completed) {
            System.out.println("Complete instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }

        System.out.print("Synchronizing: ");
        for (ProcessInstanceCreationRequestI loopP : pending) {
            System.out.print(".");
            wfEngine.requestProcessInstanceCreationToServer(loopP);
        }
        System.out.println(" done!");
        pending = procApi.getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequestI.RequestStatus.REQUESTED);
        System.out.println("Pending count: " + pending.size());
        for (ProcessInstanceCreationRequestI loopP : pending) {
            System.out.println("Pending instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }
        
        completed = procApi.getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequestI.RequestStatus.CREATED);
        System.out.println("Complete count: " + completed.size());
        for (ProcessInstanceCreationRequestI loopP : completed) {
            System.out.println("Complete instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }

    }

}
