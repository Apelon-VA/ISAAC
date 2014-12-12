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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExportPreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class ExportPreferencesPluginView implements PreferencesPluginViewI {
	private Logger logger = LoggerFactory.getLogger(ExportPreferencesPluginView.class);

	private GridPane gridPane = null;
	protected ValidBooleanBinding allValid_ = null;
	
	private final StringProperty releaseVersionProperty = new SimpleStringProperty();
	private final StringProperty extensionNamespaceProperty = new SimpleStringProperty();

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getValidationFailureMessage()
	 */
	@Override
	public ReadOnlyStringProperty validationFailureMessageProperty() {
		return allValid_.getReasonWhyInvalid();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getNode()
	 */
	@Override
	public Region getContent() {
		if (gridPane == null) {
			gridPane = new GridPane();
			
			Label releaseVersionTextFieldLabel = new Label("Release Version");
			releaseVersionTextFieldLabel.setPadding(new Insets(5, 5, 5, 5));
			TextField releaseVersionTextField = new TextField();
			releaseVersionTextField.setPadding(new Insets(5, 5, 5, 5));
			releaseVersionTextField.setMaxWidth(Double.MAX_VALUE);
			releaseVersionTextField.setTooltip(new Tooltip("Default is " + UserProfileDefaults.getDefaultReleaseVersion()));
			releaseVersionTextField.textProperty().bindBidirectional(releaseVersionProperty);

			Label extensionNamespaceTextFieldLabel = new Label("Extension Namespace");
			extensionNamespaceTextFieldLabel.setPadding(new Insets(5, 5, 5, 5));
			TextField extensionNamespaceTextField = new TextField();
			extensionNamespaceTextField.setPadding(new Insets(5, 5, 5, 5));
			extensionNamespaceTextField.setMaxWidth(Double.MAX_VALUE);
			extensionNamespaceTextField.setTooltip(new Tooltip("Default is " + UserProfileDefaults.getDefaultExtensionNamespace()));
			extensionNamespaceTextField.textProperty().bindBidirectional(extensionNamespaceProperty);
			
			// load/set current preferences values
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			releaseVersionTextField.textProperty().set(loggedIn.getReleaseVersion());
			extensionNamespaceTextField.textProperty().set(loggedIn.getExtensionNamespace());

			// Format GridPane
			int row = 0;
			gridPane.setMaxWidth(Double.MAX_VALUE);

			gridPane.addRow(row++, releaseVersionTextFieldLabel, releaseVersionTextField);
			GridPane.setHgrow(releaseVersionTextFieldLabel, Priority.NEVER);
			GridPane.setFillWidth(releaseVersionTextField, true);
			GridPane.setHgrow(releaseVersionTextField, Priority.ALWAYS);
			
			gridPane.addRow(row++, extensionNamespaceTextFieldLabel, extensionNamespaceTextField);
			GridPane.setHgrow(extensionNamespaceTextFieldLabel, Priority.NEVER);
			GridPane.setFillWidth(extensionNamespaceTextField, true);
			GridPane.setHgrow(extensionNamespaceTextField, Priority.ALWAYS);
			
			allValid_ = new ValidBooleanBinding() {
				{
					bind(releaseVersionProperty, extensionNamespaceProperty);
					setComputeOnInvalidate(true);
				}
				
				@Override
				protected boolean computeValue() {
					if (StringUtils.isBlank(releaseVersionProperty.get())) {
						this.setInvalidReason("Null/empty releaseVersionProperty");

						TextErrorColorHelper.setTextErrorColor(releaseVersionTextFieldLabel);
						
						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(releaseVersionTextFieldLabel);
					}
					if (StringUtils.isBlank(extensionNamespaceProperty.get())) {
						this.setInvalidReason("Null/empty extensionNamespaceProperty");

						TextErrorColorHelper.setTextErrorColor(extensionNamespaceTextFieldLabel);
						
						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(extensionNamespaceTextFieldLabel);
					}

					this.clearInvalidReason();
					return true;
				}
			};
		}
		
		return gridPane;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public String getName() {
		return "Export";
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		logger.debug("Saving {} preferences", getName());
		
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		loggedIn.setReleaseVersion(releaseVersionProperty.get());
		loggedIn.setExtensionNamespace(extensionNamespaceProperty.get());

		try {
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile";
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
}
