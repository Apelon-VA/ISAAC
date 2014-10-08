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
import gov.va.isaac.interfaces.gui.views.WorkflowInitiationViewI;
import gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.interfaces.workflow.WorkflowProcess;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.isaac.workflow.ComponentDescriptionHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link WorkflowInitiationViewI}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class WorkflowInitiationViewController {
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowInitiationViewController.class);
	
	enum WorkflowProcessREVIEW3InputVariablesMapValue {
		//component_id, // Passed through API
		//component_name, // Passed through API
		instructions("Instructions"),
		edit_coordinate(), // don't display
		edit_coordinate_promotion("Promotion Path");
		
		private final String displayName;
		
		private WorkflowProcessREVIEW3InputVariablesMapValue() {
			this(null);
		}

		private WorkflowProcessREVIEW3InputVariablesMapValue(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return displayName;
		}
	}

	enum ComponentType {
		Component,
		Concept
	}

	@FXML private BorderPane mainBorderPane;
	
	@FXML private Button cancelButton;
	@FXML private Button initiateButton;

	@FXML private Label generatedComponentDescriptionLabel;
	@FXML private Label promotionPathCoordinateLabel;
	
	@FXML private ComboBox<WorkflowProcess> workflowProcessesComboBox;
	@FXML private TextArea instructionsTextArea;


	private WorkflowInitiationView workflowInitiationView;
	private ComponentWorkflowServiceI workflowService;
	private ComponentVersionBI componentOrConcept;

	@FXML
	public void initialize() {
		initializeWorkflowProcessesComboBox();

		// TODO: must move to model to handle other WorkflowProcessModel types
		instructionsTextArea.clear();
		promotionPathCoordinateLabel.setText("");

		promotionPathCoordinateLabel.setText(getDefaultPromotionPathCoordinateTextFieldContent());
		
		instructionsTextArea.setOnKeyTyped((e) -> initiateButton.setDisable(! isDataRequiredForInitiateOk()));
		instructionsTextArea.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
			@Override
			public void handle(InputEvent event) {
				initiateButton.setDisable(! isDataRequiredForInitiateOk());
			}
		});

		cancelButton.setOnAction((e) -> doCancel());
		
		initiateButton.setDisable(! isDataRequiredForInitiateOk());
		initiateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initiateWorkflow();
			}
		});
	}

	// TODO: eliminate hard-coding of promotionPathCoordinateTextField
	private String getDefaultPromotionPathCoordinateTextFieldContent() {
		return "ISAAC Release Candidate Path";
	}
	
	// Private helper method to test validity of data required for save
	private boolean isDataRequiredForInitiateOk() {
		WorkflowProcess selectedProcess = null;
		int selectedIndex = -1;
		if (workflowProcessesComboBox != null) {
			selectedProcess = workflowProcessesComboBox.getSelectionModel().getSelectedItem();
			selectedIndex = workflowProcessesComboBox.getSelectionModel().getSelectedIndex();
		}

		String instructions = null;
		if (instructionsTextArea != null) {
			instructions = instructionsTextArea.getText();
		}
		
		if (selectedProcess != null && selectedIndex > 0 && instructions != null && instructions.length() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public Pane getRoot() {
		return mainBorderPane;
	}

	public ComponentVersionBI getComponent() {
		return componentOrConcept;
	}

	void setView(WorkflowInitiationView workflowInitiationView) {
		this.workflowInitiationView = workflowInitiationView;
	}

	public void setComponent(ComponentVersionBI passedComponentOrConcept) {
		if (componentOrConcept != null) {
			String msg = "Cannot reset componentOrConcept from " + componentOrConcept.getNid() + " to " + passedComponentOrConcept.getNid();
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		componentOrConcept = passedComponentOrConcept;
		if (componentOrConcept instanceof ConceptVersionBI) {
			LOG.debug("Set concept nid=" + passedComponentOrConcept.getNid() + ", uuid=" + passedComponentOrConcept.getPrimordialUuid() + ", desc=" 
					+ passedComponentOrConcept.toUserString());
		} else {
			LOG.debug("Set componentOrConcept nid=" + passedComponentOrConcept.getNid() + ", uuid=" + passedComponentOrConcept.getPrimordialUuid() + ", desc=" 
					+ WBUtility.getDescription(passedComponentOrConcept.getNid()));
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

	private void loadContents() {
		loadWorkflowProcessesComboBox();

		generatedComponentDescriptionLabel.setText(ComponentDescriptionHelper.getComponentDescription(componentOrConcept));
	}

	private void initializeWorkflowProcessesComboBox() {
		workflowProcessesComboBox.setEditable(false);
		workflowProcessesComboBox.setPromptText("Select Workflow Process");
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
		

		workflowProcessesComboBox.setButtonCell(new ListCell<WorkflowProcess>() {
			@Override
			protected void updateItem(WorkflowProcess t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.getText());
					initiateButton.setDisable(! isDataRequiredForInitiateOk());
				}
			}
		});
		workflowProcessesComboBox.setOnAction((event) -> {
			LOG.trace("workflowProcessesComboBox event (selected: " + workflowProcessesComboBox.getSelectionModel().getSelectedItem() + ")");

			initiateButton.setDisable(! isDataRequiredForInitiateOk());
		});
	}

	private void loadWorkflowProcessesComboBox() {
		workflowProcessesComboBox.getItems().clear();
		workflowProcessesComboBox.getItems().add(WorkflowProcess.PROMPT);
		workflowProcessesComboBox.getItems().add(WorkflowProcess.REVIEW3);
		workflowProcessesComboBox.getSelectionModel().selectFirst();
	}

	/**
	 * Handler for cancel button.
	 */
	public void doCancel() {
		workflowInitiationView.close();
	}

	private boolean validate(String title) {
		if (instructionsTextArea.getText() == null || instructionsTextArea.getText().length() == 0) {
			String msg = "Instructions text field is empty";
			String details = "Must enter instructions into instructions text field";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, workflowInitiationView);
			return false;
		}

		if (workflowProcessesComboBox.getSelectionModel().getSelectedItem() == WorkflowProcess.REVIEW3) {
//			if (editPathCoordinateTextField.getText() == null
//					|| editPathCoordinateTextField.getText().length() == 0) {
//				String msg = "Edit view coordinate UUID text field is empty";
//				String details = "Must enter edit view coordinate UUID into edit coordinate text field";
//				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
//				return false;
//			}
			if (promotionPathCoordinateLabel.getText() == null || promotionPathCoordinateLabel.getText().length() == 0) {
				String msg = "Edit view coordinate UUID text field is empty";
				String details = "Must enter edit view coordinate UUID into edit coordinate text field";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, workflowInitiationView);
				return false;
			}
		} else {
			String msg = "Unsupported WorkflowProcessModel: " + workflowProcessesComboBox.getSelectionModel().getSelectedItem();
			String details = "Only WorkflowProcessModel." + WorkflowProcess.REVIEW3 + " currently supported";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, workflowInitiationView);
			return false;
		}
		
		return true;
	}
	
	private void initiateWorkflow() {
		if (! validate("Failed initiating workflow")) {
			return;
		}

		String description = generatedComponentDescriptionLabel.getText();
		WorkflowProcess process = workflowProcessesComboBox.getSelectionModel().getSelectedItem();
		
		Map<String, String> map = new HashMap<>();
		if (process == WorkflowProcess.REVIEW3) {
			map.put(WorkflowProcessREVIEW3InputVariablesMapValue.instructions.name(), instructionsTextArea.getText());
			map.put(WorkflowProcessREVIEW3InputVariablesMapValue.edit_coordinate.name(), "");
			map.put(WorkflowProcessREVIEW3InputVariablesMapValue.edit_coordinate_promotion.name(), promotionPathCoordinateLabel.getText());
		} else {
			// TODO: handle other WorkflowProcessModel values
		}
		
		LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + description + "\", conceptUuid=\"" 
		+ componentOrConcept.getPrimordialUuid().toString() + "\", processName=\"" + process + "\")");
		ProcessInstanceCreationRequestI createdRequest = null;
		
		try
		{
			createdRequest = getWorkflowService().createNewComponentWorkflowRequest(description, componentOrConcept.getPrimordialUuid(), 
					process.getText(), map);
		}
		catch (IOException e)
		{
			LOG.error("Unexpected error creating request", e);
		}
		
		if (createdRequest == null) {
			String title = "Workflow Initiation Failed";
			String msg = "Failed creating WorkflowProcessModel " + workflowProcessesComboBox.getSelectionModel().getSelectedItem() + " (service call returned null)";
			String details = "Component: " + description + "\n" + map;
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, workflowInitiationView);
		} else {
			LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
			
			AppContext.getCommonDialogs().showInformationDialog("Workflow initiation succeeded", "Created " + workflowProcessesComboBox.getSelectionModel().getSelectedItem() 
					+ "\nFor componentId " + componentOrConcept.getPrimordialUuid());	

			Utility.submit(() -> getWorkflowService().synchronizeWithRemote());

			doCancel();
		}
	}

	private ComponentWorkflowServiceI getWorkflowService() {
		if (workflowService == null) {
			workflowService = AppContext.getService(ComponentWorkflowServiceI.class);
		}

		assert workflowService != null;

		return workflowService;
	}
}
