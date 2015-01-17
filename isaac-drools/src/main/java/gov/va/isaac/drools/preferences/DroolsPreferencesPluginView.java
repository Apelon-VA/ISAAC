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
package gov.va.isaac.drools.preferences;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;
import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javax.inject.Singleton;
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
public class DroolsPreferencesPluginView implements PreferencesPluginViewI {
	private Logger logger = LoggerFactory.getLogger(DroolsPreferencesPluginView.class);

	private GridPane gridPane = null;
	protected ValidBooleanBinding allValid_ = null;
	
	private final BooleanProperty runDroolsBeforeEachCommitProperty = new SimpleBooleanProperty();

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
			
			allValid_ = new ValidBooleanBinding() {
				{
					setComputeOnInvalidate(true);
				}
				
				@Override
				protected boolean computeValue() {
					this.clearInvalidReason();
					return true;
				}
			};
			
			Label runDroolsBeforeEachCommitCheckBoxLabel = new Label("Run Drools Before Each Commit");
			runDroolsBeforeEachCommitCheckBoxLabel.setPadding(new Insets(5, 5, 5, 5));
			CheckBox runDroolsBeforeEachCommitCheckBox = new CheckBox();
			runDroolsBeforeEachCommitCheckBox.setPadding(new Insets(5, 5, 5, 5));
			runDroolsBeforeEachCommitCheckBox.setMaxWidth(Double.MAX_VALUE);
			runDroolsBeforeEachCommitCheckBox.setTooltip(new Tooltip("Default is " + UserProfileDefaults.getDefaultRunDroolsBeforeEachCommit()));
			runDroolsBeforeEachCommitCheckBox.selectedProperty().bindBidirectional(runDroolsBeforeEachCommitProperty);
			
			// load/set current preferences values
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			runDroolsBeforeEachCommitCheckBox.selectedProperty().set(loggedIn.isRunDroolsBeforeEachCommit());

			// Format GridPane
			int row = 0;
			gridPane.setMaxWidth(Double.MAX_VALUE);

			gridPane.addRow(row++, runDroolsBeforeEachCommitCheckBoxLabel, runDroolsBeforeEachCommitCheckBox);
			GridPane.setHgrow(runDroolsBeforeEachCommitCheckBoxLabel, Priority.NEVER);
			GridPane.setFillWidth(runDroolsBeforeEachCommitCheckBox, true);
			GridPane.setHgrow(runDroolsBeforeEachCommitCheckBox, Priority.ALWAYS);
		}
		
		return gridPane;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public String getName() {
		return "Drools";
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		logger.debug("Saving {} preferences", getName());
		
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		loggedIn.setRunDroolsBeforeEachCommit(this.runDroolsBeforeEachCommitProperty.get());

		try {
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile";
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getTabOrder()
	 */
	@Override
	public int getTabOrder()
	{
		return 60;
	}
}
