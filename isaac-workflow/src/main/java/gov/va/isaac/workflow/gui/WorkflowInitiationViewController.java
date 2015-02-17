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
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowInitiationViewI;
import gov.va.isaac.interfaces.workflow.ComponentWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.interfaces.workflow.WorkflowProcess;
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link WorkflowInitiationViewI}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class WorkflowInitiationViewController {
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowInitiationViewController.class);

	enum Review3WorkflowProcessVariables {
		instructions("Instructions"),
		edit_coordinate(), // don't display
		edit_coordinate_promotion(), // don't display
		skip_to_review(); // don't display

		private final String displayName;

		private Review3WorkflowProcessVariables() {
			this(null);
		}

		private Review3WorkflowProcessVariables(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	enum DualReviewWorkflowProcessVariables {
		instructions("Instructions"),
		edit_coordinate_1(), // don't display
		edit_coordinate_2(), // don't display
		edit_coordinate_adjudicator(), // don't display
		edit_coordinate_promotion(), // don't display
		skip_to_review(); // don't display

		private final String displayName;

		private DualReviewWorkflowProcessVariables() {
			this(null);
		}

		private DualReviewWorkflowProcessVariables(String displayName) {
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

	private Map<String, String> getOutputVariablesMap() {
		Map<String, String> map = new HashMap<>();
		
		switch (workflowProcessesComboBox.getSelectionModel().getSelectedItem()) {
		case REVIEW3:
			map.put(Review3WorkflowProcessVariables.instructions.name(), instructionsTextArea.getText());
			map.put(Review3WorkflowProcessVariables.edit_coordinate.name(), "default REVIEW3 edit coordinate");
			map.put(Review3WorkflowProcessVariables.edit_coordinate_promotion.name(), promotionPathCoordinateLabel.getText());
			map.put(Review3WorkflowProcessVariables.skip_to_review.name(), Boolean.toString(false));

			break;
		case DUAL_REVIEW:
			map.put(DualReviewWorkflowProcessVariables.instructions.name(), instructionsTextArea.getText());
			map.put(DualReviewWorkflowProcessVariables.edit_coordinate_1.name(), "default DUAL_REVIEW edit coordinate 1");
			map.put(DualReviewWorkflowProcessVariables.edit_coordinate_2.name(), "default DUAL_REVIEW edit coordinate 2");
			map.put(DualReviewWorkflowProcessVariables.edit_coordinate_promotion.name(), promotionPathCoordinateLabel.getText());
			map.put(DualReviewWorkflowProcessVariables.edit_coordinate_adjudicator.name(), "default DUAL_REVIEW adjudicator coordinate");
			map.put(DualReviewWorkflowProcessVariables.skip_to_review.name(), Boolean.toString(false));

			break;
		default:
			return null;
		}
		
		return map;
	}

	private WorkflowInitiationView workflowInitiationView;
	private ComponentWorkflowServiceI workflowService;
	private ComponentVersionBI componentOrConcept;

	@FXML
	public void initialize() {
		initializeWorkflowProcessesComboBox();

		// TODO (artf231901): must move to model to handle other WorkflowProcessModel types
		instructionsTextArea.clear();

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

	private String getDefaultPromotionPathCoordinateTextFieldContent() {
		UUID promotionPathUUID = AppContext.getAppConfiguration().getCurrentWorkflowPromotionPathUuidAsUUID();
		if (promotionPathUUID == null)
		{
			return "";
		}
		try {
			return OTFUtility.getConceptVersion(promotionPathUUID).getPreferredDescription().getText();
		} catch (IOException | ContradictionException e) {
			return "";
		}
	}

	// Private helper method to test validity of data required for save
	private boolean isDataRequiredForInitiateOk() {
		WorkflowProcess selectedProcess = null;
		if (workflowProcessesComboBox != null) {
			selectedProcess = workflowProcessesComboBox.getSelectionModel().getSelectedItem();
		} else {
			return false;
		}

		String instructions = null;
		if (instructionsTextArea != null) {
			instructions = instructionsTextArea.getText();
		}

		if (selectedProcess != null && instructions != null && instructions.length() > 0) {
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
					+ OTFUtility.getDescription(passedComponentOrConcept.getNid()));
		}

		loadContents();
	}

	public void setComponent(int componentOrConceptNid) {
		ComponentVersionBI componentVersion = OTFUtility.getComponentVersion(componentOrConceptNid);
		if (componentVersion == null) {
			// May be a concept
			componentVersion = OTFUtility.getConceptVersion(componentOrConceptNid);
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
					initiateButton.setDisable(true);
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
		//workflowProcessesComboBox.getItems().add(WorkflowProcess.PROMPT);
		workflowProcessesComboBox.getItems().add(WorkflowProcess.REVIEW3);
		workflowProcessesComboBox.getItems().add(WorkflowProcess.DUAL_REVIEW);
		workflowProcessesComboBox.getSelectionModel().select(null);
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
				String msg = "Promotion view coordinate is unset";
				String details = "Promotion view coordinate must be set in config file app.xml";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, workflowInitiationView);
				return false;
			}
		}
		else if (workflowProcessesComboBox.getSelectionModel().getSelectedItem() == WorkflowProcess.DUAL_REVIEW) {
			//			if (editPathCoordinateTextField.getText() == null
			//			|| editPathCoordinateTextField.getText().length() == 0) {
			//		String msg = "Edit view coordinate UUID text field is empty";
			//		String details = "Must enter edit view coordinate UUID into edit coordinate text field";
			//		AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			//		return false;
			//	}
			if (promotionPathCoordinateLabel.getText() == null || promotionPathCoordinateLabel.getText().length() == 0) {
				String msg = "Promotion view coordinate is unset";
				String details = "Promotion view coordinate must be set in config file app.xml";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, workflowInitiationView);
				return false;
			}
		}
		else
		{
			String msg = "Unsupported WorkflowProcessModel: " + workflowProcessesComboBox.getSelectionModel().getSelectedItem();
			String details = "Only WorkflowProcess." + WorkflowProcess.REVIEW3 + " and WorkflowProcess." + WorkflowProcess.DUAL_REVIEW + " currently supported";
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

		Map<String, String> map = getOutputVariablesMap();

		LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + description + "\", conceptUuid=\"" 
				+ componentOrConcept.getPrimordialUuid().toString() + "\", processName=\"" + process + "\")");
		ProcessInstanceCreationRequestI createdRequest = null;

		try
		{
			createdRequest = getWorkflowService().createNewComponentWorkflowRequest(description, componentOrConcept.getPrimordialUuid(), 
					process.getText(), map);
		}
		catch (Exception e)
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
