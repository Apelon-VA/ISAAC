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
package gov.va.isaac.workflow.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowInitiationViewI;
import gov.va.isaac.interfaces.utility.CommitListenerI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javax.inject.Named;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyDI.CONCEPT_EVENT;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * {@link WorkflowInitiationPropertyChangeListener}
 *
 * @author jefron
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
@Named (value="Workflow Initiation")
public class WorkflowInitiationPropertyChangeListener implements PropertyChangeListener, ServicesToPreloadI, CommitListenerI
{
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private boolean enabled = false;
	private List<Integer> componentsToAdd;
	private static LocalTasksServiceBI localTasksService = null;
	private static ProcessInstanceServiceBI processService = null;

	private WorkflowInitiationPropertyChangeListener()
	{
		// for HK2 to create
	}
	
	
	/**
	 * @see gov.va.isaac.interfaces.utility.CommitListenerI#getListenerName()
	 */
	@Override
	public String getListenerName()
	{
		return "Workflow Initiation";
	}

	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.utility.CommitListenerI#disable()
	 */
	@Override
	public void disable()
	{
		if (enabled)
		{
			LOG.info("Disabling the workflow commit listener");
			//TODO (artf231898) file yet another OTF bug - this doesn't work.  We still get property change notifications after calling remove... 
			ExtendedAppContext.getDataStore().removePropertyChangeListener(this);
			enabled = false;
		}
	}
	
	/**
	 * @see gov.va.isaac.interfaces.utility.CommitListenerI#enable()
	 */
	@Override
	public void enable()
	{
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		if (!enabled && loggedIn != null && loggedIn.isLaunchWorkflowForEachCommit())
		{
			LOG.info("Enabling the workflow commit listener");
			ExtendedAppContext.getDataStore().addPropertyChangeListener(CONCEPT_EVENT.POST_COMMIT, this);
			ExtendedAppContext.getDataStore().addPropertyChangeListener(CONCEPT_EVENT.PRE_COMMIT, this);
			enabled = true;
			localTasksService = AppContext.getService(LocalTasksServiceBI.class);
			processService = AppContext.getService(ProcessInstanceServiceBI.class);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		//TODO (artf231898) this shouldn't be necessary, but OTF has a bug....
		if (!enabled)
		{
			return;
		}
		try
		{
			if (CONCEPT_EVENT.PRE_COMMIT.name().equals(evt.getPropertyName()))
			{
				LOG.debug("pre-commit triggered in workflow listener");
				int[] allConceptNids = ((NativeIdSetBI) evt.getNewValue()).getSetValues();

				componentsToAdd = identifyComponents(allConceptNids);
			} else if (CONCEPT_EVENT.POST_COMMIT.name().equals(evt.getPropertyName())) {
				LOG.debug("post-commit triggered in workflow listener");
				
				if (componentsToAdd == null)
				{
					LOG.debug("componentsToAdd was null at POST_COMMIT - perhaps the listener was registered in the middle of a commit?");
					return;
				}
				

				Platform.runLater(() -> {
					boolean showEm = componentsToAdd.size() <= 20;
					
					if (!showEm)
					{
						DialogResponse answer = AppContext.getCommonDialogs().showYesNoDialog("Show Workflow Dialog for each concept?", componentsToAdd.size() 
								+ " concepts were just committed to the DB.  Really show a workflow dialog for each concept?");
						if (answer == DialogResponse.YES)
						{
							showEm = true;
						}
					}
					if (showEm)
					{
						for (int nid : componentsToAdd)
						{
							WorkflowInitiationViewI view = AppContext.getService(WorkflowInitiationViewI.class);
							view.setComponent(nid);
							view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
						}
					}
					
					componentsToAdd.clear();
				});
			}
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error processing commit notification", e);
		}
	}

	private List<Integer> identifyComponents(int[] allConceptNids) {
		List<Integer> componentsToWf = new ArrayList<Integer>();
		
		for (int i = 0; i < allConceptNids.length; i++) {
			ConceptChronicle c = (ConceptChronicle) OTFUtility.getConceptVersion(allConceptNids[i]).getChronicle();

			int componentNid = 0;
			for (int nid : c.getUncommittedNids().getListArray()) {					
				if (componentNid == 0) {
					componentNid = nid;
				} else {
					componentNid = allConceptNids[i];
					break;
				}
			}
			
			try {
				try {
					UUID componentId = OTFUtility.getComponentChronicle(componentNid).getPrimordialUuid();
					int currentConNid = OTFUtility.getComponentVersion(componentId).getConceptNid();
					
					
					if (!conceptWithComponentInOwnedTask(currentConNid) && !conceptWithComponentInOpenRequest(currentConNid) && componentNid != 0) {
						componentsToWf.add(componentNid);
					}
				} catch (NullPointerException npe) {
					try {
						// Retired Component... send up Concept to WF
						int currentConNid = OTFUtility.getComponentChronicle(componentNid).getConceptNid();
						
						
						if (!conceptWithComponentInOwnedTask(currentConNid) && !conceptWithComponentInOpenRequest(currentConNid) && currentConNid != 0) {
							componentsToWf.add(currentConNid);
						}
					} catch (Exception ncnpe) {
						// TODO (artf231899): Handle Case of new Concept
					}
				}
			} catch (DatastoreException e) {
				LOG.error("Unexpected", e);
			}
		}

		return componentsToWf;
	}
	
	private boolean conceptWithComponentInOpenRequest(int currentConNid) throws DatastoreException {
		List<ProcessInstanceCreationRequestI> openReqList = processService.getRequests();

		// If have open task, assume that they are working within Workflow
		for (ProcessInstanceCreationRequestI t : openReqList) {
			UUID taskCompUuid = UUID.fromString(t.getComponentId());
			
			try {
				int taskConNid = OTFUtility.getComponentChronicle(taskCompUuid).getConceptNid();
		
				if (taskConNid == currentConNid) {
					return true;
				}
			} catch (Exception e) {
				LOG.info("Have to handle when concept is not in DB.  Can happen if WF sync not in line with ISAAC new component");
			}
		}

		return false;
	}


	private boolean conceptWithComponentInOwnedTask(int currentConNid) throws DatastoreException {
		List<LocalTask> openTaskList = localTasksService.getOpenOwnedTasks();

		// If have open task, assume that they are working within Workflow
		for (LocalTask t : openTaskList) {
			UUID taskCompUuid = UUID.fromString(t.getComponentId());
			
			try {
				int taskConNid = OTFUtility.getComponentChronicle(taskCompUuid).getConceptNid();
		
				if (taskConNid == currentConNid) {
					return true;
				}
			} catch (Exception e) {
				LOG.info("Have to handle when concept is not in DB.  Can happen if WF sync not in line with ISAAC new component");
			}
		}

		return false;
	}


	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		AppContext.getService(UserProfileManager.class).registerLoginCallback((user) -> {enable();});
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		// noop
	}
}
