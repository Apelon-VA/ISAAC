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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.engine.RemoteSynchronizer;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ComponentWorkflowService
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@Singleton
public class ComponentWorkflowService implements ComponentWorkflowServiceI {
	
	private final Logger LOG = LoggerFactory.getLogger(ComponentWorkflowService.class);

	@Inject
	private RemoteSynchronizer syncService_;
	
	@Inject
	private ProcessInstanceServiceBI pis_;
	
	@Inject
	private LocalTasksServiceBI lts_;
	
	private ComponentWorkflowService() 
	{
		//For HK2 to construct
	}

	/**
	 * @see {@link ComponentWorkflowServiceI#synchronizeWithRemote()}
	 */
	@Override
	public void synchronizeWithRemote(){
		syncService_.synchronize(null);
	}
	
	/**
	 * reads the preferred description from the concept, and calls 
	 * {@link ComponentWorkflowServiceI#createNewComponentWorkflowRequest(String, UUID, String, Map)}
	 */
	public ProcessInstanceCreationRequestI createNewComponentWorkflowRequest(ConceptVersionBI conceptVersion, String processName, 
			Map<String,String> variables) throws IOException, ContradictionException {
		String preferredDescription = conceptVersion.getPreferredDescription().getText();

		return createNewComponentWorkflowRequest(preferredDescription, conceptVersion.getPrimordialUuid(), processName, variables);
	}
	
	/**
	 * @see {@link ComponentWorkflowServiceI#createNewComponentWorkflowRequest(String, UUID, String, Map)}
	 */
	@Override
	public ProcessInstanceCreationRequestI createNewComponentWorkflowRequest(String preferredDescription, UUID uuid, String processName, 
			Map<String,String> variables) throws IOException {
		try
		{
			LOG.debug("Invoking ProcessInstanceCreationRequestsAPI().createRequest(processName=\"" + processName + "\", conceptUuid=\"" + uuid.toString() 
					+ "\", prefDesc=\"" + preferredDescription + "\")");
			if (variables == null) {
				variables = new HashMap<String,String>();
			}
			
			String user = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername();
			
			ProcessInstanceCreationRequestI createdRequest = pis_.createRequest(processName, uuid, preferredDescription, user, variables);
			LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
			
			return createdRequest;
		}
		catch (DatastoreException e)
		{
			throw new IOException("Error creating request", e);
		}
	}
	
	/**
	 * @see gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI#releaseTask(java.lang.Long)
	 */
	@Override
	public void releaseTask(Long taskId) throws IOException
	{
		try
		{
			lts_.releaseTask(taskId);
		}
		catch (DatastoreException e)
		{
			throw new IOException(e);
		}
		
	}
}
