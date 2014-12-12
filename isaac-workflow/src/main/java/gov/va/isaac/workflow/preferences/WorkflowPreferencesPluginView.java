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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
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
public class WorkflowPreferencesPluginView implements PreferencesPluginViewI {
	private Logger logger = LoggerFactory.getLogger(WorkflowPreferencesPluginView.class);

	private GridPane gridPane = null;
	protected ValidBooleanBinding allValid_ = null;
	
	private final StringProperty workflowServerDeploymentIdProperty = new SimpleStringProperty();
	private final StringProperty workflowServerUrlProperty = new SimpleStringProperty();
	private final ObjectProperty<UUID> workflowPromotionPathProperty = new SimpleObjectProperty<>();
	private final BooleanProperty launchWorkflowForEachCommitProperty = new SimpleBooleanProperty();

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

			Label workflowUserNameLabelLabel = new Label("Workflow User");
			workflowUserNameLabelLabel.setPadding(new Insets(5, 5, 5, 5));
			Label workflowUserNameLabel = new Label();
			workflowUserNameLabel.setPadding(new Insets(5, 5, 5, 5));
			
			Label workflowServerDeploymentIdLabel = new Label("Workflow Server Deployment ID");
			workflowServerDeploymentIdLabel.setPadding(new Insets(5, 5, 5, 5));
			TextField workflowServerDeploymentIdTextField = new TextField();
			workflowServerDeploymentIdTextField.setPadding(new Insets(5, 5, 5, 5));
			workflowServerDeploymentIdTextField.setMaxWidth(Double.MAX_VALUE);
			workflowServerDeploymentIdTextField.setTooltip(new Tooltip("Default is " + UserProfileDefaults.getDefaultWorkflowServerDeploymentId()));
			workflowServerDeploymentIdTextField.textProperty().bindBidirectional(workflowServerDeploymentIdProperty);

			Label workflowServerUrlLabel = new Label("Workflow Server URL");
			workflowServerUrlLabel.setPadding(new Insets(5, 5, 5, 5));
			TextField workflowServerUrlTextField = new TextField();
			workflowServerUrlTextField.setPadding(new Insets(5, 5, 5, 5));
			workflowServerUrlTextField.setMaxWidth(Double.MAX_VALUE);
			workflowServerUrlTextField.setTooltip(new Tooltip("Default is " + UserProfileDefaults.getDefaultWorkflowServerUrl()));
			workflowServerUrlTextField.textProperty().bindBidirectional(workflowServerUrlProperty);

			Label launchWorkflowForEachCommitCheckBoxLabel = new Label("Launch Workflow For Each Commit");
			launchWorkflowForEachCommitCheckBoxLabel.setPadding(new Insets(5, 5, 5, 5));
			CheckBox launchWorkflowForEachCommitCheckBox = new CheckBox();
			launchWorkflowForEachCommitCheckBox.setPadding(new Insets(5, 5, 5, 5));
			launchWorkflowForEachCommitCheckBox.setMaxWidth(Double.MAX_VALUE);
			launchWorkflowForEachCommitCheckBox.setTooltip(new Tooltip("Default is " + UserProfileDefaults.getDefaultLaunchWorkflowForEachCommit()));
			launchWorkflowForEachCommitCheckBox.selectedProperty().bindBidirectional(launchWorkflowForEachCommitProperty);
			
			Label workflowPromotionPathComboBoxLabel = new Label("Workflow Promotion Path");
			workflowPromotionPathComboBoxLabel.setPadding(new Insets(5, 5, 5, 5));
			ComboBox<UUID> workflowPromotionPathComboBox = new ComboBox<>();
			workflowPromotionPathComboBox.setPadding(new Insets(5, 5, 5, 5));
			workflowPromotionPathComboBox.setCellFactory(new Callback<ListView<UUID>, ListCell<UUID>> () {
				@Override
				public ListCell<UUID> call(ListView<UUID> param) {
					final ListCell<UUID> cell = new ListCell<UUID>() {
						@Override
						protected void updateItem(UUID c, boolean emptyRow) {
							super.updateItem(c, emptyRow);

							if(c == null) {
								setText(null);
							}else {
								String desc = WBUtility.getDescription(c);
								setText(desc);
							}
						}
					};

					return cell;
				}
			});
			workflowPromotionPathComboBox.setButtonCell(new ListCell<UUID>() {
				@Override
				protected void updateItem(UUID c, boolean emptyRow) {
					super.updateItem(c, emptyRow); 
					if (emptyRow) {
						setText("");
					} else {
						String desc = WBUtility.getDescription(c);
						setText(desc);
					}
				}
			});
			workflowPromotionPathComboBox.setTooltip(new Tooltip("Default is " + WBUtility.getDescription(UserProfileDefaults.getDefaultWorkflowPromotionPath())));
			workflowPromotionPathComboBox.getItems().addAll(getPathOptions());
			workflowPromotionPathProperty.bind(workflowPromotionPathComboBox.getSelectionModel().selectedItemProperty());
			
			// load/set current preferences values
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			workflowUserNameLabel.setText(loggedIn.getWorkflowUsername());
			workflowServerDeploymentIdTextField.textProperty().set(loggedIn.getWorkflowServerDeploymentId());
			workflowServerUrlTextField.textProperty().set(loggedIn.getWorkflowServerUrl());
			launchWorkflowForEachCommitCheckBox.selectedProperty().set(loggedIn.isLaunchWorkflowForEachCommit());
			workflowPromotionPathComboBox.getSelectionModel().select(loggedIn.getWorkflowPromotionPath());

			// Format GridPane
			int row = 0;
			gridPane.setMaxWidth(Double.MAX_VALUE);

			gridPane.addRow(row++, workflowUserNameLabelLabel, workflowUserNameLabel);
			GridPane.setHgrow(workflowUserNameLabelLabel, Priority.NEVER);
			GridPane.setFillWidth(workflowUserNameLabel, true);
			GridPane.setHgrow(workflowUserNameLabel, Priority.NEVER);
			
			gridPane.addRow(row++, workflowServerDeploymentIdLabel, workflowServerDeploymentIdTextField);
			GridPane.setHgrow(workflowServerDeploymentIdLabel, Priority.NEVER);
			GridPane.setFillWidth(workflowServerDeploymentIdTextField, true);
			GridPane.setHgrow(workflowServerDeploymentIdTextField, Priority.ALWAYS);
			
			gridPane.addRow(row++, workflowServerUrlLabel, workflowServerUrlTextField);
			GridPane.setHgrow(workflowServerUrlLabel, Priority.NEVER);
			GridPane.setFillWidth(workflowServerUrlTextField, true);
			GridPane.setHgrow(workflowServerUrlTextField, Priority.ALWAYS);

			gridPane.addRow(row++, launchWorkflowForEachCommitCheckBoxLabel, launchWorkflowForEachCommitCheckBox);
			GridPane.setHgrow(launchWorkflowForEachCommitCheckBoxLabel, Priority.NEVER);
			GridPane.setFillWidth(launchWorkflowForEachCommitCheckBox, true);
			GridPane.setHgrow(launchWorkflowForEachCommitCheckBox, Priority.ALWAYS);
			
			gridPane.addRow(row++, workflowPromotionPathComboBoxLabel, workflowPromotionPathComboBox);
			GridPane.setHgrow(workflowPromotionPathComboBoxLabel, Priority.NEVER);
			GridPane.setFillWidth(workflowPromotionPathComboBox, true);
			GridPane.setHgrow(workflowPromotionPathComboBox, Priority.ALWAYS);
			
			allValid_ = new ValidBooleanBinding() {
				{
					bind(workflowServerDeploymentIdProperty, workflowServerUrlProperty, workflowPromotionPathProperty);
					setComputeOnInvalidate(true);
				}
				
				@Override
				protected boolean computeValue() {
					if (StringUtils.isBlank(workflowServerDeploymentIdProperty.get())) {
						this.setInvalidReason("Null/unset/unselected workflowServerDeploymentId");

						TextErrorColorHelper.setTextErrorColor(workflowServerDeploymentIdLabel);
						
						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(workflowServerDeploymentIdLabel);
					}
					if (StringUtils.isBlank(workflowServerUrlProperty.get())) {
						this.setInvalidReason("Null/unset/unselected workflowServerUrl");
						
						TextErrorColorHelper.setTextErrorColor(workflowServerUrlLabel);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(workflowServerUrlLabel);
					}
					if (workflowPromotionPathProperty.get() == null) {
						this.setInvalidReason("Null/unset/unselected workflowPromotionPathProperty");

						TextErrorColorHelper.setTextErrorColor(workflowPromotionPathComboBoxLabel);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(workflowPromotionPathComboBoxLabel);
					}
					if (WBUtility.getConceptVersion(workflowPromotionPathProperty.get()) == null) {
						this.setInvalidReason("Invalid workflowPromotionPathProperty (no corresponding concept)");

						TextErrorColorHelper.setTextErrorColor(workflowPromotionPathComboBoxLabel);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(workflowPromotionPathComboBoxLabel);
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
		loggedIn.setWorkflowServerUrl(workflowServerUrlProperty.get());
		loggedIn.setLaunchWorkflowForEachCommit(this.launchWorkflowForEachCommitProperty.get());
		loggedIn.setWorkflowPromotionPath(workflowPromotionPathProperty.get());

		try {
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile";
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
	
	private Collection<UUID> getPathOptions() {
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
		// Add currently-stored value to list of options, if not already there
		UUID storedPath = getStoredPath();
		if (storedPath != null && ! list.contains(storedPath)) {
			list.add(storedPath);
		}

		return list;
	}
	
	private UUID getStoredPath() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		return loggedIn.getWorkflowPromotionPath();
	}
}
