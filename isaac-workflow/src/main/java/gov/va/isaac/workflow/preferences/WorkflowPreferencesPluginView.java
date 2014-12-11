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
package gov.va.isaac.workflow.preferences;

import java.io.IOException;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * WorkflowPreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class WorkflowPreferencesPluginView implements PreferencesPluginViewI {
	private Logger logger = LoggerFactory.getLogger(WorkflowPreferencesPluginView.class);

	private GridPane gridPane = null;
	protected ValidBooleanBinding allValid_ = null;
	
	private final StringProperty workflowServerDeploymentIdProperty = new SimpleStringProperty();

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
	public Node getNode() {
		if (gridPane == null) {
			gridPane = new GridPane();
			
			allValid_ = new ValidBooleanBinding() {
				{
					bind(workflowServerDeploymentIdProperty);
					setComputeOnInvalidate(true);
				}
				
				@Override
				protected boolean computeValue() {
					if (workflowServerDeploymentIdProperty.get() == null || workflowServerDeploymentIdProperty.get().length() == 0) {
						this.setInvalidReason("Null/unset/unselected workflowServerDeploymentId");

						return false;
					}

					this.clearInvalidReason();
					return true;
				}
			};
			
			Label workflowServerDeploymentIdlabel = new Label("Workflow Server Deployment ID");
			TextField workflowServerDeploymentIdTextField = new TextField();
			workflowServerDeploymentIdTextField.textProperty().bindBidirectional(workflowServerDeploymentIdProperty);

			// TODO load/set current preferences values
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			workflowServerDeploymentIdProperty.set(loggedIn.getWorkflowServerDeploymentId());

			gridPane.addRow(0, workflowServerDeploymentIdlabel, workflowServerDeploymentIdTextField);
		}
		
		return gridPane;
	}

	public ReadOnlyStringProperty currentWorkflowServerDeploymentId() {
		return workflowServerDeploymentIdProperty;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public String getName() {
		return "Workflow";
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		logger.debug("Saving {} preferences", getName());
		
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		loggedIn.setWorkflowServerDeploymentId(workflowServerDeploymentIdProperty.get());
		try {
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile";
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
}
