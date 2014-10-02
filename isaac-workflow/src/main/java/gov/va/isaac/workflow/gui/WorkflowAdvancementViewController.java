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

import javafx.beans.property.ObjectProperty;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

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
	@FXML private Label actionComboBoxLabel;
	
	private WorkflowAdvancementView stage;

	private GridPane inputTabGridPane;
	private Tab inputTab;
	private Label inputTabLabel;
	private ScrollPane conceptScrollPane;

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
		assert actionComboBoxLabel != null : "fx:id=\"actionComboBoxLabel\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
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
		
		inputTabLabel = new Label("Input");
		inputTab = new Tab();
		inputTab.setGraphic(inputTabLabel);
		
		inputTabGridPane = new GridPane();
		inputTab.setContent(inputTabGridPane);
		centralTabPane.getTabs().add(inputTab);
		
		// Disabling saveActionButton until dependencies met 
		saveActionButton.setDisable(true);

		viewTaskDetailsButton.setOnAction((e) -> {
			WorkflowTaskViewI view = AppContext.getService(WorkflowTaskViewI.class);
			view.setTask(taskModel.getTask().getId());
			view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
		});

		// This code only for embedded concept detail view
		// Use H2K to find and initialize conceptView as a ConceptView
		conceptView = AppContext.getService(PopupConceptViewI.class, "ModernStyle");

		// Force single selection
		actionComboBox.getSelectionModel().selectFirst();
		actionComboBox.setButtonCell(new ListCell<Action>() {
			@Override
			protected void updateItem(Action t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
					taskModel.setAction(Action.NONE);
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
						taskService_.setAction(taskModel.getTask().getId(), actionComboBox.getValue(), TaskActionStatus.Pending, taskModel.getCurrentOutputVariables());
						Platform.runLater(() -> 
						{
							claimPopover.hide();
							refreshSaveActionButtonBinding();

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
				logger.error("Error saving task: fields not set: task=" + taskModel.getTask() + ", action=" + selectedAction);
			}
		});
	}
	
	void setStage(WorkflowAdvancementView stage) {
		this.stage = stage;
	}
	
	private void loadTaskLabel() {
		taskLabel.setText(taskModel.getTask().getId() + ": " + taskModel.getTask().getComponentName() + ": " + taskModel.getTask().getName());
	}

	public LocalTask getTask() {
		return taskModel.getTask();
	}

	public void setTask(long taskId) {
		if (taskModel != null) {
			String msg = "Cannot reset initialTask from " + taskModel.getTask().getId() + " to " + taskId;
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		if (conceptVersion != null) {
			String msg = "Cannot set initialTask to " + taskId + " when conceptVersion is already set to " + new SimpleDisplayConcept(conceptVersion);
			logger.error(msg);
			throw new RuntimeException(msg);
		}
				
		initializeServices();
		
		LocalTask initialTask = taskService_.getTask(taskId);

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

		taskModel.getActionProperty().addListener(new ChangeListener<Action>() {
			@Override
			public void changed(
					ObservableValue<? extends Action> observable,
					Action oldValue,
					Action newValue) {
				setActionComboBoxLabelColorBasedOnTaskModelActionProperty();
			}});
		setActionComboBoxLabelColorBasedOnTaskModelActionProperty();

		taskModel.getOutputVariablesSavableProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue,
					Boolean newValue) {
				setInputTabColorBasedOnTaskModelOutputVariables();
			}});
		setInputTabColorBasedOnTaskModelOutputVariables();
		
		loadContent();
	}

	private void setInputTabColorBasedOnTaskModelOutputVariables() {
		if (taskModel.getOutputVariablesSavableProperty().get()) {
			inputTabLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
		} else {
			inputTabLabel.setStyle("-fx-text-fill: red;");
		}
	}
	private void setActionComboBoxLabelColorBasedOnTaskModelActionProperty() {
		ObjectProperty<Action> actionProperty = taskModel.getActionProperty();
		if (actionProperty.get() != null && actionProperty.get() != Action.NONE) {
			actionComboBoxLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
		} else {
			actionComboBoxLabel.setStyle("-fx-text-fill: red;");
		}
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
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
			
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
		for (String outputVariable : taskModel.getOutputVariableNames()) {
			this.inputTabGridPane.addRow(rowIndex++, taskModel.getOutputVariableInputNodeLabel(outputVariable), taskModel.getOutputVariableInputNode(outputVariable));
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
		
		refreshSaveActionButtonBinding();
	}

	private void loadConcept() {
		// loadConcept() must not be called before setTask()
		conceptView.setConcept(conceptVersion.getNid());
		conceptScrollPane.setContent(conceptView.getView());
	}
	
	private void refreshSaveActionButtonBinding()
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
