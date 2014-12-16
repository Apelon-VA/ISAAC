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
 * ViewCoordinatePreferencesPlugin
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences.plugins;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginCheckBoxProperty;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginComboBoxProperty;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginLabelProperty;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginProperty;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginTextFieldProperty;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginProperty.StringConverter;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javafx.scene.control.Control;

import javax.inject.Singleton;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
//import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExampleAbstractPreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

//@Service
@Singleton
public class ExampleAbstractPreferencesPluginView extends AbstractPreferencesPluginView {
	private static Logger logger = LoggerFactory.getLogger(ExampleAbstractPreferencesPluginView.class);
	
	private static Collection<PreferencesPluginProperty<?, ? extends Control>> createProperties() {
		List<PreferencesPluginProperty<?, ? extends Control>> properties = new ArrayList<>();

		PreferencesPluginLabelProperty syncUserProperty = new PreferencesPluginLabelProperty("Sync User") {
			@Override
			public String readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getSyncUsername();
			}

			@Override
			public String readFromDefaults() {
				return null;
			}
			
		};
		properties.add(syncUserProperty);

		PreferencesPluginCheckBoxProperty displayFSNProperty =
				new PreferencesPluginCheckBoxProperty("Display FSN") {

			@Override
			public Boolean readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getDisplayFSN();
			}

			@Override
			public Boolean readFromDefaults() {
				return UserProfileDefaults.getDefaultDisplayFSN();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setDisplayFSN(getProperty().getValue());
			}
		};
		properties.add(displayFSNProperty);

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

		PreferencesPluginComboBoxProperty<UUID> viewCoordinatePathProperty = new PreferencesPluginComboBoxProperty<UUID>(
				"View Coordinate Path",
				new StringConverter<UUID>() {
					@Override
					public String convertToString(UUID value) {
						return value != null ? WBUtility.getDescription(value) : null;
					}
				}) {
			@Override
			public UUID readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getViewCoordinatePath();
			}

			@Override
			public UUID readFromDefaults() {
				return UserProfileDefaults.getDefaultViewCoordinatePath();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setViewCoordinatePath(getProperty().getValue());
			}
		};
		List<UUID> list = new ArrayList<>();

		try {
			List<ConceptChronicleBI> pathConcepts = WBUtility.getPathConcepts();
			for (ConceptChronicleBI cc : pathConcepts) {
				list.add(cc.getPrimordialUuid());
			}
		} catch (IOException | ContradictionException e) {
			logger.error("Failed loading path concepts. Caught {} {}", e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
		}
		UUID current = viewCoordinatePathProperty.readFromPersistedPreferences();
		if (current != null && ! list.contains(current)) {
			list.add(current);
		}
		viewCoordinatePathProperty.getControl().getItems().addAll(list);
		properties.add(viewCoordinatePathProperty);

		return properties;
	}
	
	/**
	 * @param name
	 * @param properties
	 */
	protected ExampleAbstractPreferencesPluginView() {
		super("Example", createProperties());
	}
}
