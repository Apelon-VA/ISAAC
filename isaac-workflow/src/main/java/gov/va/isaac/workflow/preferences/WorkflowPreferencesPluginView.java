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
package gov.va.isaac.workflow.preferences;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.gui.preferences.plugins.AbstractPreferencesPluginView;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginCheckBoxProperty;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginComboBoxProperty;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginLabelProperty;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginProperty;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginTextFieldProperty;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javafx.scene.control.Control;

import javax.inject.Singleton;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkflowPreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class WorkflowPreferencesPluginView extends AbstractPreferencesPluginView {
	private static Logger logger = LoggerFactory.getLogger(WorkflowPreferencesPluginView.class);
	
	private static Collection<PreferencesPluginProperty<?, ? extends Control>> createProperties() {
		List<PreferencesPluginProperty<?, ? extends Control>> properties = new ArrayList<>();

		PreferencesPluginLabelProperty workflowUserNameProperty = new PreferencesPluginLabelProperty("Workflow User") {
			@Override
			public String readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getWorkflowUsername();
			}

			@Override
			public String readFromDefaults() {
				return null;
			}
			
		};
		properties.add(workflowUserNameProperty);

		PreferencesPluginCheckBoxProperty launchWorkflowForEachCommitProperty =
				new PreferencesPluginCheckBoxProperty("Launch Workflow for Each Commit") {

			@Override
			public Boolean readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.isLaunchWorkflowForEachCommit();
			}

			@Override
			public Boolean readFromDefaults() {
				return UserProfileDefaults.getDefaultLaunchWorkflowForEachCommit();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setLaunchWorkflowForEachCommit(getProperty().getValue());
			}
		};
		properties.add(launchWorkflowForEachCommitProperty);

		PreferencesPluginTextFieldProperty workflowServerDeploymentIdProperty = 
				new PreferencesPluginTextFieldProperty("Workflow Server Deployment ID") {
			@Override
			public String readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getWorkflowServerDeploymentId();
			}

			@Override
			public String readFromDefaults() {
				return UserProfileDefaults.getDefaultWorkflowServerDeploymentId();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setWorkflowServerDeploymentId(getProperty().getValue());
			}
		};
		properties.add(workflowServerDeploymentIdProperty);

		PreferencesPluginTextFieldProperty workflowServerUrlProperty = 
				new PreferencesPluginTextFieldProperty("Workflow Server URL") {
			@Override
			public String readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getWorkflowServerUrl();
			}

			@Override
			public String readFromDefaults() {
				return UserProfileDefaults.getDefaultWorkflowServerUrl();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setWorkflowServerUrl(getProperty().getValue());
			}
		};
		properties.add(workflowServerUrlProperty);

		PreferencesPluginComboBoxProperty<UUID> workflowPromotionPathProperty = new PreferencesPluginComboBoxProperty<UUID>(
				"Workflow Promotion Path",
				new PreferencesPluginProperty.StringConverter<UUID>() {
					@Override
					public String convertToString(UUID value) {
						return value != null ? OTFUtility.getDescription(value) : null;
					}
				}) {
			@Override
			public UUID readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getWorkflowPromotionPath();
			}

			@Override
			public UUID readFromDefaults() {
				return UserProfileDefaults.getDefaultWorkflowPromotionPath();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setWorkflowPromotionPath(getProperty().getValue());
			}
		};
		
		List<UUID> list = new ArrayList<>();

		try {
			List<ConceptChronicleBI> pathConcepts = OTFUtility.getPathConcepts();
			for (ConceptChronicleBI cc : pathConcepts) {
				list.add(cc.getPrimordialUuid());
			}
		} catch (IOException | ContradictionException e) {
			logger.error("Failed loading path concepts. Caught {} {}", e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
		}
		UUID current = workflowPromotionPathProperty.readFromPersistedPreferences();
		if (current != null && ! list.contains(current)) {
			list.add(current);
		}
		workflowPromotionPathProperty.getControl().getItems().addAll(list);
		properties.add(workflowPromotionPathProperty);

		return properties;
	}
	
	/**
	 * @param name
	 * @param properties
	 */
	protected WorkflowPreferencesPluginView() {
		super("Workflow", createProperties());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getTabOrder()
	 */
	@Override
	public int getTabOrder() {
		return 30;
	}
}