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

import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.ProcessInstanceCreationRequest;
import gov.va.isaac.workflow.persistence.ProcessInstanceCreationRequestsAPI;
import java.util.List;

/**
 *
 * @author alo
 */
public class ProcessInstanceSynchronizationTester {

    public static void main(String[] args) throws Exception {
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
        ProcessInstanceCreationRequestsAPI procApi = (ProcessInstanceCreationRequestsAPI) wfEngine.getProcessInstanceService();
        //procApi.dropSchema();
        //procApi.createSchema();
        
        ProcessInstanceCreationRequest newInstance = wfEngine.getProcessInstanceService().createRequest("terminology-authoring.test1", "56968009", "Wood asthma (disorder)", "test-user");
        System.out.println("New instance: " + newInstance.getId());
        
        List<ProcessInstanceCreationRequest> pending = wfEngine.getProcessInstanceService().getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequest.RequestStatus.REQUESTED);

        System.out.println("Pending count: " + pending.size());
        for (ProcessInstanceCreationRequest loopP : pending) {
            System.out.println("Pending instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }
        
        List<ProcessInstanceCreationRequest> completed = wfEngine.getProcessInstanceService().getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequest.RequestStatus.CREATED);
        System.out.println("Complete count: " + completed.size());
        for (ProcessInstanceCreationRequest loopP : completed) {
            System.out.println("Complete instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }

        System.out.print("Synchronizing: ");
        for (ProcessInstanceCreationRequest loopP : pending) {
            System.out.print(".");
            wfEngine.requestProcessInstanceCreationToServer(loopP);
        }
        System.out.println(" done!");
        pending = wfEngine.getProcessInstanceService().getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequest.RequestStatus.REQUESTED);
        System.out.println("Pending count: " + pending.size());
        for (ProcessInstanceCreationRequest loopP : pending) {
            System.out.println("Pending instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }
        
        completed = wfEngine.getProcessInstanceService().getOwnedRequestsByStatus("test-user", ProcessInstanceCreationRequest.RequestStatus.CREATED);
        System.out.println("Complete count: " + completed.size());
        for (ProcessInstanceCreationRequest loopP : completed) {
            System.out.println("Complete instance: " + loopP.getId() + " " + loopP.getComponentId() + " " + loopP.getStatus().name() + " " + loopP.getWfId() + " " + loopP.getSyncMessage());
        }

    }

}
