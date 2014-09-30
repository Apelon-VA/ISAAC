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
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.interfaces.gui.views.PopupConceptViewI;
import gov.va.isaac.interfaces.gui.views.WorkflowTaskViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.taskmodel.TaskModel;
import gov.va.isaac.workflow.taskmodel.TaskModelFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link WorkflowAdvancementViewController}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class WorkflowAdvancementViewController
{	
	private final static Logger logger = LoggerFactory.getLogger(WorkflowAdvancementViewController.class);
	
	enum ActionOutputVariables {
		out_comment
	}

	// Underlying concept for loading detail pane
	private ConceptVersionBI conceptVersion;

	// Embedded concept detail pane
	PopupConceptViewI conceptView;
	
	@FXML private BorderPane borderPane;
	@FXML private TabPane centralTabPane;
	@FXML private Button saveActionButton;

	@FXML private Label taskLabel;
	@FXML private ComboBox<Action> actionComboBox;
	
	@FXML private Button viewTaskDetailsButton;
	
	private WorkflowAdvancementView stage;

	private GridPane inputTabGridPane;
	
	private ScrollPane conceptScrollPane;

	private LocalTask initialTask = null;
	private TaskModel taskModel = null;
	
	// Workflow Engine and Task Service
	private LocalWorkflowRuntimeEngineBI wfEngine_;
	private LocalTasksServiceBI taskService_;

	// Initialize GUI (invoked by FXML)
	@FXML
	void initialize()
	{
		assert saveActionButton != null : "fx:id=\"saveActionButton\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
		assert actionComboBox != null : "fx:id=\"actionComboBox\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
		assert taskLabel != null : "fx:id=\"taskLabel\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
		assert centralTabPane != null : "fx:id=\"centralTabPane\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";

		initializeWorkflowEngine();
		initializeTaskService();

		conceptScrollPane = new ScrollPane();
		conceptScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		conceptScrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		Tab conceptTab = new Tab("Concept Details");
		conceptTab.setContent(conceptScrollPane);
		conceptTab.setClosable(false);
		centralTabPane.getTabs().add(conceptTab);
		
		Tab inputTab = new Tab("Input");
		inputTabGridPane = new GridPane();
		inputTab.setContent(inputTabGridPane);
		centralTabPane.getTabs().add(inputTab);
		
		// Disabling saveActionButton until dependencies met 
		saveActionButton.setDisable(true);

		viewTaskDetailsButton.setOnAction((e) -> {
			WorkflowTaskViewI view = AppContext.getService(WorkflowTaskViewI.class);
			view.setTask(initialTask.getId());
			view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
		});

		// This code only for embedded concept detail view
		// Use H2K to find and initialize conceptView as a ConceptView
		conceptView = AppContext.getService(PopupConceptViewI.class, "ModernStyle");

		// Force single selection
		actionComboBox.getSelectionModel().selectFirst();
		// Use setCellFactory() and setButtonCell() of ComboBox to customize list entry display
		// TODO: possibly remove: these calls to setCellFactory() and setButtonCell() are only necessary if WorkflowAction().toString() is not appropriate for this display
		actionComboBox.setCellFactory(new Callback<ListView<Action>,ListCell<Action>>(){
			@Override
			public ListCell<Action> call(ListView<Action> p) {

				final ListCell<Action> cell = new ListCell<Action>(){

					@Override
					protected void updateItem(Action a, boolean bln) {
						super.updateItem(a, bln);

						if(a != null){
							setText(a.toString());
						}else{
							setText(null);
						}
					}

				};

				return cell;
			}
		});
		actionComboBox.setButtonCell(new ListCell<Action>() {
			@Override
			protected void updateItem(Action t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.toString());
					taskModel.setAction(t);
				}

			}
		});

		// Activation of save depends on taskModel.isSavable()
		saveActionButton.setOnAction((action) -> {
			if (taskModel.isSavable()) {
				Platform.runLater(() -> 
				{
					unbindSaveActionButtonFromModelIsSavableProperty();
					saveActionButton.setDisable(true);
				});
				final BusyPopover claimPopover = BusyPopover.createBusyPopover("Saving action...", saveActionButton);

				Utility.execute(() -> {
					try
					{
						final LocalTask currentlySelectedTask = initialTask;
						final Action currentlySelectedAction = actionComboBox.getValue();

						Map<String, String> variableMap = new HashMap<>();
						for (Map.Entry<String, StringProperty> entry : taskModel.getOutputVariables().entrySet()) {
							variableMap.put(entry.getKey(), entry.getValue().get());
						}
						
						taskService_.setAction(currentlySelectedTask.getId(), currentlySelectedAction, TaskActionStatus.Pending, variableMap);
						Platform.runLater(() -> 
						{
							claimPopover.hide();
							refreshContent();

							if (stage != null) {
								stage.close();
							}
						});
					}
					catch (Exception e)
					{
						claimPopover.hide();
						logger.error("Error saving task: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
					}
				});
			} else { // ! this.taskModel.isSavable()
				// This should never happen, if saveActionButton.setDisable(true) used in proper places
				Action selectedAction = actionComboBox.getSelectionModel().getSelectedItem();
				logger.error("Error saving task: fields not set: task=" + initialTask + ", action=" + selectedAction);
			}
		});
	}
	
	void setStage(WorkflowAdvancementView stage) {
		this.stage = stage;
	}
	
	private void loadTaskLabel() {
		taskLabel.setText(initialTask.getId() + ": " + initialTask.getComponentName() + ": " + initialTask.getName());
	}

//	private Node configureNode(Node node) {
//		if (node instanceof Label) {
//			Label label = (Label)node;
//			label.setPadding(new Insets(5));
//			label.setStyle("-fx-font-weight: bold");
//		} else if (node instanceof TextArea) {
//			TextArea textArea = (TextArea)node;
//			textArea.setPadding(new Insets(5));
//		}
//		
//		return node;
//	}

	public LocalTask getTask() {
		return initialTask;
	}

	public void setTask(long taskId) {
		if (initialTask != null) {
			String msg = "Cannot reset initialTask from " + initialTask.getId() + " to " + taskId;
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		if (conceptVersion != null) {
			String msg = "Cannot set initialTask to " + taskId + " when conceptVersion is already set to " + new SimpleDisplayConcept(conceptVersion);
			logger.error(msg);
			throw new RuntimeException(msg);
		}
				
		initializeServices();
		
		initialTask = taskService_.getTask(taskId);

		if (initialTask == null) {
			logger.error("Task retrieved by id {} is null", taskId);

			return;
		}
		if (initialTask.getComponentId() == null) {
			logger.error("Component ID for task {} is null", initialTask.getId());

			return;
		}
		UUID componentUuid = null;
		try {
			componentUuid = UUID.fromString(initialTask.getComponentId());
		} catch (IllegalArgumentException e) {
			logger.error("Component ID for task {} is not a valid UUID", initialTask.getId());

			return;
		}

		ConceptVersionBI containingConcept = null;
		ComponentChronicleBI<? extends ComponentVersionBI> componentChronicle = WBUtility.getComponentChronicle(componentUuid);
		if (componentChronicle == null) {
			logger.warn("Component ID for task " + initialTask.getId() + " retrieved a null componentChronicle");

			containingConcept = WBUtility.getConceptVersion(componentUuid);
			if (containingConcept == null) {
				logger.error("Component ID for task " + initialTask.getId() + " retrieved a null concept");

				return;
			}
		} else {
			try {
				containingConcept = componentChronicle.getEnclosingConcept().getVersion(WBUtility.getViewCoordinate());
			} catch (Exception e) {
				logger.error("Failed getting version from ComponentChronicleBI task " + initialTask.getId() + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
				e.printStackTrace();
			}

			if (containingConcept == null) {
				logger.error("ComponentChronicleBI task " + initialTask.getId() + " contained a null enclosing concept");

				return;
			}
		}

		if (componentChronicle == null) {
			logger.warn("Component id " + componentUuid + " for task " + initialTask.getId() + " is a concept, not just a component.");
		}
		
		conceptVersion = containingConcept;
		
		taskModel = TaskModelFactory.newTaskModel(initialTask);
				
		loadContent();
	}

	ConceptVersionBI getConcept() {
		return conceptVersion;
	}
	
	private void initializeServices() {
		initializeWorkflowEngine();
		initializeTaskService();
	}
	
	private void initializeTaskService() {
		if (taskService_ == null) {
			taskService_ = wfEngine_.getLocalTaskService();
		}
	}
	
	private void initializeWorkflowEngine() {
		// TODO: determine if LocalWorkflowRuntimeEngineBI wfEngine_ should be static
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
			// TODO: determine if wfEngine_.synchronizeWithRemote() needs to be called more than once
			
			Utility.submit(() -> wfEngine_.synchronizeWithRemote());
		}
	}
	
	private void bindSaveActionButtonToModelIsSavableProperty() {
		saveActionButton.disableProperty().bind(taskModel.getIsSavableProperty().not());
	}
	private void unbindSaveActionButtonFromModelIsSavableProperty() {
		saveActionButton.disableProperty().unbind();
	}
	
	private void loadTaskModel() {
		
		int rowIndex = 0;
		for (String outputVariable : taskModel.getOutputVariables().keySet()) {
			this.inputTabGridPane.addRow(rowIndex, new Label(taskModel.getLabelName(outputVariable)), taskModel.createOutputNode(outputVariable));
		}
		
		for (Node child : inputTabGridPane.getChildren()) {
			VariableGridPaneNodeConfigurationHelper.configureNode(child);
		}
	}

	// Load data into GUI components
	protected void loadContent()
	{
		loadTaskLabel();
		
		initializeServices();
		
		loadTaskModel();
		
		loadConcept();
		
		loadActions();
		
		refreshContent();
	}

	private void loadConcept() {
		// loadConcept() must not be called before setTask()
		conceptView.setConcept(conceptVersion.getNid());
		conceptScrollPane.setContent(conceptView.getView());
	}
	
	// Refresh all content
	private void refreshContent()
	{
		unbindSaveActionButtonFromModelIsSavableProperty();
		bindSaveActionButtonToModelIsSavableProperty();
	}
	
	// Helper to refresh action list in actionComboBox
	private void loadActions() {
		actionComboBox.getItems().clear();
		
		for (Action action : Action.values()) {
			if (action != Action.NONE) {
				actionComboBox.getItems().add(action);
			}
		}
	}
	
	public Region getRootNode() {
		return borderPane;
	}
}
