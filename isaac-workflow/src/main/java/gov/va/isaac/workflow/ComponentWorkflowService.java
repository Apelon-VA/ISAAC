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
 * ComponentWorkflowService
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.persistence.ProcessInstanceCreationRequestsAPI;

/**
 * ComponentWorkflowService
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@PerLookup
public class ComponentWorkflowService implements ComponentWorkflowServiceI {
	private static final Logger LOG = LoggerFactory.getLogger(ComponentWorkflowService.class);

	private final LocalWorkflowRuntimeEngineBI wfEngine_;
	
	public ComponentWorkflowService() {
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
	public ProcessInstanceCreationRequestI createNewComponentWorkflowRequest(ConceptVersionBI conceptVersion, String userName, String processName, Map<String,String> variables) throws IOException, ContradictionException {
		String preferredDescription = conceptVersion.getPreferredDescription().getText();

		return createNewComponentWorkflowRequest(preferredDescription, conceptVersion.getPrimordialUuid(), userName, processName, variables);
	}
	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.ConceptWorkflowServiceI#createNewConceptWorkflowRequest(java.lang.String, java.util.UUID, java.lang.String, java.lang.String)
	 */
	@Override
	public ProcessInstanceCreationRequestI createNewComponentWorkflowRequest(String preferredDescription, UUID uuid, String userName, String processName, Map<String,String> variables) {
		ProcessInstanceCreationRequestsAPI popi = new ProcessInstanceCreationRequestsAPI();
		LOG.debug("Invoking ProcessInstanceCreationRequestsAPI().createRequest(processName=\"" + processName + "\", conceptUuid=\"" + uuid.toString() + "\", prefDesc=\"" + preferredDescription + "\", user=\"" + userName + "\")");
        if (variables == null) {
            variables = new HashMap<String,String>();
        }
		ProcessInstanceCreationRequestI createdRequest = popi.createRequest(processName, uuid, preferredDescription, userName, variables);
		LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
		
		return createdRequest;
	}
}
