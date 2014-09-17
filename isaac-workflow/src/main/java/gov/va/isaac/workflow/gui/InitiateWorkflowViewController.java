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
import gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.interfaces.workflow.WorkflowProcess;
import gov.va.isaac.util.WBUtility;

import java.util.HashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link InitiateWorkflowViewI}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class InitiateWorkflowViewController {
	private static final Logger LOG = LoggerFactory.getLogger(InitiateWorkflowViewController.class);
	
	enum WorkflowProcessREVIEW3InputVariablesMapValue {
		//component_id, // Passed through API
		//component_name, // Passed through API
		instructions,
		edit_coordinate,
		edit_coordinate_promotion
	}

	enum ComponentType {
		Component,
		Concept
	}

	@FXML private BorderPane mainBorderPane;
	
	@FXML private Label workflowProcessesComboBoxLabel;
	@FXML private ComboBox<WorkflowProcess> workflowProcessesComboBox;
	
	@FXML private Button cancelButton;
	@FXML private Button initiateButton;

	@FXML private TextField componentDescriptionTextField;
	@FXML private Label componentTypeLabel; // componentOrConcept vs concept

	private TextField instructionsTextField;
	private Label instructionsTextFieldLabel;
	
	private TextField editCoordinateTextField;
	private Label editCoordinateTextFieldLabel;
	
	private TextField promotionCoordinateTextField;
	private Label promotionCoordinateTextFieldLabel;
	
	@FXML private GridPane variablesGridPane;

	private InitiateWorkflowView initiateWorkflowView;
	private ComponentWorkflowServiceI workflowService;

	private ComponentVersionBI componentOrConcept;

	private String getUserName() {
		// TODO: replace hard-coded username
		return "alejandro";
	}
	
	public Pane getRoot() {
		return mainBorderPane;
	}

	public ComponentVersionBI getComponent() {
		return componentOrConcept;
	}

	private ComponentType getComponentType() {
		return componentOrConcept instanceof ConceptVersionBI ? ComponentType.Concept : ComponentType.Component;
	}

	private static String getComponentDescription(ComponentVersionBI componentOrConceptVersion) {
		if (componentOrConceptVersion instanceof ConceptVersionBI) {
			return WBUtility.getDescription(componentOrConceptVersion.getNid());
		} else {
			if (componentOrConceptVersion instanceof DescriptionVersionBI) {
				return ((DescriptionVersionBI<?>)componentOrConceptVersion).getText();
			} else {
				return componentOrConceptVersion.toUserString();
			}
		}
	}

	void setView(InitiateWorkflowView initiateWorkflowView) {
		this.initiateWorkflowView = initiateWorkflowView;
	}

	public void setComponent(ComponentVersionBI passedComponentOrConcept) {
		if (componentOrConcept != null) {
			String msg = "Cannot reset componentOrConcept from " + componentOrConcept.getNid() + " to " + passedComponentOrConcept.getNid();
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		componentOrConcept = passedComponentOrConcept;
		if (componentOrConcept instanceof ConceptVersionBI) {
			LOG.debug("Set concept nid=" + passedComponentOrConcept.getNid() + ", uuid=" + passedComponentOrConcept.getPrimordialUuid() + ", desc=" + passedComponentOrConcept.toUserString());
		} else {
			LOG.debug("Set componentOrConcept nid=" + passedComponentOrConcept.getNid() + ", uuid=" + passedComponentOrConcept.getPrimordialUuid() + ", desc=" + WBUtility.getDescription(passedComponentOrConcept.getNid()));
		}
		
		loadContents();
	}

	public void setComponent(int componentOrConceptNid) {
		ComponentVersionBI componentVersion = WBUtility.getComponentVersion(componentOrConceptNid);
		if (componentVersion == null) {
			// May be a concept
			componentVersion = WBUtility.getConceptVersion(componentOrConceptNid);
		}

		if (componentVersion == null) {
			String msg = "No componentOrConcept or concept retrieved for nid " + componentOrConceptNid;
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		setComponent(componentVersion);
	}

	@FXML
	public void initialize() {
		assert mainBorderPane != null : "fx:id=\"mainBorderPane\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert initiateButton != null : "fx:id=\"initiateButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert workflowProcessesComboBoxLabel != null : "fx:id=\"workflowProcessesComboBoxLabel\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert workflowProcessesComboBox != null : "fx:id=\"workflowProcessesComboBox\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert componentDescriptionTextField != null : "fx:id=\"componentDescriptionTextField\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert componentTypeLabel != null : "fx:id=\"componentTypeLabel\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";

		cancelButton.setText("Cancel");
		cancelButton.setOnAction((e) -> doCancel());

		initiateButton.setText("Initiate");
		initiateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initiateWorkflow();
			}
		});

		// TODO: must move to model to handle other WorkflowProcess types
		variablesGridPane.getChildren().clear();
		int row = 0;
		
		instructionsTextFieldLabel = new Label();
		instructionsTextFieldLabel.setText("Instructions");
		instructionsTextFieldLabel.setPadding(new Insets(5));
		instructionsTextField = new TextField();
		instructionsTextField.setPadding(new Insets(5));
		variablesGridPane.addRow(row, instructionsTextFieldLabel, instructionsTextField);
		//variablesGridPane.getRowConstraints().get(row).setVgrow(Priority.NEVER);
		row++;
		
		editCoordinateTextFieldLabel = new Label();
		editCoordinateTextFieldLabel.setText("Edit Coordinate");
		editCoordinateTextFieldLabel.setPadding(new Insets(5));
		editCoordinateTextField = new TextField();
		editCoordinateTextField.setPadding(new Insets(5));
		variablesGridPane.addRow(row++, editCoordinateTextFieldLabel, editCoordinateTextField);

		promotionCoordinateTextFieldLabel = new Label();
		promotionCoordinateTextFieldLabel.setText("Promotion Coordinate");
		promotionCoordinateTextFieldLabel.setPadding(new Insets(5));
		promotionCoordinateTextField = new TextField();
		promotionCoordinateTextField.setPadding(new Insets(5));
		variablesGridPane.addRow(row++, promotionCoordinateTextFieldLabel, promotionCoordinateTextField);

		variablesGridPane.getColumnConstraints().get(0).setPercentWidth(30);
		variablesGridPane.getColumnConstraints().get(0).setFillWidth(true);
		variablesGridPane.getColumnConstraints().get(1).setPercentWidth(70);
		variablesGridPane.getColumnConstraints().get(1).setFillWidth(true);

		initializeWorkflowProcessesComboBox();
	}

	private void loadContents() {
		loadWorkflowProcessesComboBox();

		componentTypeLabel.setText(getComponentType().name());
		componentDescriptionTextField.setText(getComponentDescription(componentOrConcept));
	}

	private void initializeWorkflowProcessesComboBox() {
		workflowProcessesComboBoxLabel.setText("Workflow Process");
		workflowProcessesComboBoxLabel.setPadding(new Insets(5));
		
		workflowProcessesComboBox.setEditable(false);
		workflowProcessesComboBox.getSelectionModel().selectFirst();
		workflowProcessesComboBox.setCellFactory((p) -> {
			final ListCell<WorkflowProcess> cell = new ListCell<WorkflowProcess>() {
				@Override
				protected void updateItem(WorkflowProcess a, boolean bln) {
					super.updateItem(a, bln);

					if(a != null){
						setText(a.getText());
					} else {
						setText(null);
					}
				}
			};

			return cell;
		});
	}

	private void loadWorkflowProcessesComboBox() {
		workflowProcessesComboBox.getItems().clear();
		workflowProcessesComboBox.getItems().addAll(WorkflowProcess.REVIEW3);
		workflowProcessesComboBox.getSelectionModel().selectFirst();
	}

	/**
	 * Handler for cancel button.
	 */
	public void doCancel() {
		initiateWorkflowView.close();
	}

	private boolean validate(String title) {
		if (instructionsTextField.getText() == null || instructionsTextField.getText().length() == 0) {
			String msg = "Instructions text field is empty";
			String details = "Must enter instructions into instructions text field";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			return false;
		}

		if (workflowProcessesComboBox.getSelectionModel().getSelectedItem() == WorkflowProcess.REVIEW3) {
			if (editCoordinateTextField.getText() == null
					|| editCoordinateTextField.getText().length() == 0) {
				String msg = "Edit view coordinate UUID text field is empty";
				String details = "Must enter edit view coordinate UUID into edit coordinate text field";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
				return false;
			}
			if (promotionCoordinateTextField.getText() == null || promotionCoordinateTextField.getText().length() == 0) {
				String msg = "Edit view coordinate UUID text field is empty";
				String details = "Must enter edit view coordinate UUID into edit coordinate text field";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
				return false;
			}
		} else {
			String msg = "Unsupported WorkflowProcess: " + workflowProcessesComboBox.getSelectionModel().getSelectedItem();
			String details = "Only WorkflowProcess." + WorkflowProcess.REVIEW3 + " currently supported";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			return false;
		}
		
		return true;
	}
	
	private void initiateWorkflow() {
		if (! validate("Failed initiating workflow")) {
			return;
		}

		String description = componentDescriptionTextField.getText();
		WorkflowProcess process = workflowProcessesComboBox.getSelectionModel().getSelectedItem();
		
		Map<String, String> map = new HashMap<>();
		if (process == WorkflowProcess.REVIEW3) {
			map.put(WorkflowProcessREVIEW3InputVariablesMapValue.instructions.name(), instructionsTextField.getText());
			map.put(WorkflowProcessREVIEW3InputVariablesMapValue.edit_coordinate.name(), editCoordinateTextField.getText());
			map.put(WorkflowProcessREVIEW3InputVariablesMapValue.edit_coordinate_promotion.name(), promotionCoordinateTextField.getText());
		} else {
			// TODO: handle other WorkflowProcess values
		}

		LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + description + "\", conceptUuid=\"" + componentOrConcept.getPrimordialUuid().toString() + "\", user=\"" + getUserName() + "\", processName=\"" + process + "\")");
		ProcessInstanceCreationRequestI createdRequest = getWorkflowService().createNewComponentWorkflowRequest(description, componentOrConcept.getPrimordialUuid(), getUserName(), process.getText(), map);
		
		if (createdRequest == null) {
			String title = "Workflow Initiation Failed";
			String msg = "Failed creating WorkflowProcess " + workflowProcessesComboBox.getSelectionModel().getSelectedItem() + " (service call returned null)";
			String details = "Component: " + description + "\n" + map;
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
		} else {
			LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
			AppContext.getCommonDialogs().showInformationDialog("Workflow initiation succeeded", "Created " + workflowProcessesComboBox.getSelectionModel().getSelectedItem() + " task id " + createdRequest.getWfId() + ":\n" + createdRequest);	

			doCancel();
		}
		
		getWorkflowService().synchronizeWithRemote();
	}

	private ComponentWorkflowServiceI getWorkflowService() {
		if (workflowService == null) {
			workflowService = AppContext.getService(ComponentWorkflowServiceI.class);
		}

		assert workflowService != null;

		return workflowService;
	}
}
