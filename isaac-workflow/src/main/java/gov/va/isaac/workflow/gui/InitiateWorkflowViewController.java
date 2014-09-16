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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

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

	enum ComponentType {
		Component,
		Concept
	}

	@FXML private BorderPane mainBorderPane;
	@FXML private ComboBox<WorkflowProcess> workflowProcessesComboBox;
	@FXML private Button cancelButton;
	@FXML private Button initiateButton;

	@FXML private TextField componentDescriptionTextField;
	@FXML private Label componentTypeLabel; // componentOrConcept vs concept

	@FXML private TextField instructionsTextField;
	@FXML private Label instructionsTextFieldLabel;

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
		assert workflowProcessesComboBox != null : "fx:id=\"workflowProcessesComboBox\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert componentDescriptionTextField != null : "fx:id=\"componentDescriptionTextField\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert componentTypeLabel != null : "fx:id=\"componentTypeLabel\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert instructionsTextField != null : "fx:id=\"instructionsTextField\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert instructionsTextFieldLabel != null : "fx:id=\"instructionsTextFieldLabel\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";

		cancelButton.setText("Cancel");
		cancelButton.setOnAction((e) -> handleCancel());

		initiateButton.setText("Initiate");
		initiateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initiateWorkflow();
			}
		});

		instructionsTextFieldLabel.setText("Instructions");

		initializeWorkflowProcessesComboBox();
	}

	private void loadContents() {
		loadWorkflowProcessesComboBox();

		componentTypeLabel.setText(getComponentType().name());
		componentDescriptionTextField.setText(getComponentDescription(componentOrConcept));
	}

	private void initializeWorkflowProcessesComboBox() {
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
		workflowProcessesComboBox.getItems().addAll(WorkflowProcess.values());
		workflowProcessesComboBox.getSelectionModel().selectFirst();
	}

	/**
	 * Handler for cancel button.
	 */
	public void handleCancel() {
		initiateWorkflowView.close();
	}

	private void initiateWorkflow() {
		String description = componentDescriptionTextField.getText();
		WorkflowProcess process = workflowProcessesComboBox.getSelectionModel().getSelectedItem();
		
		LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + description + "\", conceptUuid=\"" + componentOrConcept.getPrimordialUuid().toString() + "\", user=\"" + getUserName() + "\", processName=\"" + process + "\")");
		ProcessInstanceCreationRequestI createdRequest = getWorkflowService().createNewComponentWorkflowRequest(description, componentOrConcept.getPrimordialUuid(), getUserName(), process.getText(), new HashMap<String,String>());
		LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);

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
