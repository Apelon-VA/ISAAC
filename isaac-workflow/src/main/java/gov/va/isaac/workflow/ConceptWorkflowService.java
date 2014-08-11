/**
 * Copyright Notice
 * 
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ConceptWorkflowService
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow;

import java.io.IOException;
import java.util.UUID;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.interfaces.workflow.ConceptWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.persistence.ProcessInstanceCreationRequestsAPI;

/**
 * ConceptWorkflowService
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@PerLookup
public class ConceptWorkflowService implements ConceptWorkflowServiceI {
	private static final Logger LOG = LoggerFactory.getLogger(ConceptWorkflowService.class);

	private final LocalWorkflowRuntimeEngineBI wfEngine_;
	
	public ConceptWorkflowService() {
		// TODO: determine if LocalWorkflowRuntimeEngineBI wfEngine_ should be static
		wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.ConceptWorkflowServiceI#synchronizeWithRemote()
	 */
	@Override
	public void synchronizeWithRemote() {
		wfEngine_.synchronizeWithRemote();
	}
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.ConceptWorkflowServiceI#createNewConceptWorkflowRequest(org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI, java.lang.String, java.lang.String)
	 */
	public ProcessInstanceCreationRequestI createNewConceptWorkflowRequest(ConceptVersionBI conceptVersion, String userName, String processName) throws IOException, ContradictionException {
		String preferredDescription = conceptVersion.getPreferredDescription().getText();

		return createNewConceptWorkflowRequest(preferredDescription, conceptVersion.getPrimordialUuid(), userName, processName);
	}
	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.ConceptWorkflowServiceI#createNewConceptWorkflowRequest(java.lang.String, java.util.UUID, java.lang.String, java.lang.String)
	 */
	@Override
	public ProcessInstanceCreationRequestI createNewConceptWorkflowRequest(String preferredDescription, UUID UUID, String userName, String processName) {
		ProcessInstanceCreationRequestsAPI popi = new ProcessInstanceCreationRequestsAPI();
		LOG.debug("Invoking ProcessInstanceCreationRequestsAPI().createRequest(processName=\"" + processName + "\", conceptUuid=\"" + UUID.toString() + "\", prefDesc=\"" + preferredDescription + "\", user=\"" + userName + "\")");
		ProcessInstanceCreationRequestI createdRequest = popi.createRequest(processName, UUID.toString(), preferredDescription, userName);
		LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
		
		return createdRequest;
	}
}
