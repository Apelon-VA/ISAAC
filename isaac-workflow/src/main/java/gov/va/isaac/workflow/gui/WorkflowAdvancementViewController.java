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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
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

	@FXML private ComboBox<LocalTask> taskComboBox;
	@FXML private ComboBox<Action> actionComboBox;
	
	@FXML private Button viewTaskDetailsButton;

	private GridPane inputTabGridPane;
	
	//private WorkflowProcessNode currentWorkflowProcessNode;
	
	private TextArea outCommentTextArea;

	private ScrollPane conceptScrollPane;

	private LocalTask initialTask = null;
	
	// Workflow Engine and Task Service
	private LocalWorkflowRuntimeEngineBI wfEngine_;
	private LocalTasksServiceBI taskService_;

	// Initialize GUI (invoked by FXML)
	@FXML
	void initialize()
	{
		assert saveActionButton != null : "fx:id=\"saveActionButton\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
		assert actionComboBox != null : "fx:id=\"actionComboBox\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
		assert taskComboBox != null : "fx:id=\"taskComboBox\" was not injected: check your FXML file 'WorkflowAdvancementView.fxml'.";
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
		Label outCommentTextAreaLabel = new Label("Comment");
		configureNode(outCommentTextAreaLabel);
		outCommentTextArea = new TextArea();
		outCommentTextArea.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
			@Override
			public void handle(InputEvent event) {
				saveActionButton.setDisable(! isDataRequiredForSaveOk());
			}
		});
		configureNode(outCommentTextArea);
		inputTabGridPane.addRow(0, outCommentTextAreaLabel, outCommentTextArea);
		inputTab.setContent(inputTabGridPane);
		centralTabPane.getTabs().add(inputTab);
		
		// Disabling saveActionButton until dependencies met 
		saveActionButton.setDisable(true);
		viewTaskDetailsButton.setDisable(true);
		viewTaskDetailsButton.setOnAction((e) -> {
			WorkflowTaskViewI view = AppContext.getService(WorkflowTaskViewI.class);
			view.setTask(taskComboBox.getSelectionModel().getSelectedItem().getId());
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
				}

			}
		});

		// Force single selection
		taskComboBox.getSelectionModel().selectFirst();
		// Use setCellFactory() and setButtonCell() of ComboBox to customize list entry display
		taskComboBox.setCellFactory(new Callback<ListView<LocalTask>, ListCell<LocalTask>>(){
			@Override
			public ListCell<LocalTask> call(ListView<LocalTask> p) {
				final ListCell<LocalTask> cell = new ListCell<LocalTask>(){
					@Override
					protected void updateItem(LocalTask t, boolean bln) {
						super.updateItem(t, bln);
						if(t != null){
							setText(t.getId() + ": " + t.getComponentName() + ": " + t.getName());
						}else{
							setText(null);
						}
					}
				};

				return cell;
			}
		});
		taskComboBox.setButtonCell(new ListCell<LocalTask>() {
			@Override
			protected void updateItem(LocalTask t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
					viewTaskDetailsButton.setDisable(true);
				} else {
					setText(t.getId() + ": " + t.getComponentName() + ": " + t.getName());
					viewTaskDetailsButton.setDisable(false);
				}

			}
		});
		taskComboBox.setOnAction((event) -> {
			viewTaskDetailsButton.setDisable(! isDataRequiredToViewTaskDetails());
		});

		// Activation of save depends on isDataRequiredForSaveOk()
		saveActionButton.setOnAction((action) -> {
			if (isDataRequiredForSaveOk()) {
				saveActionButton.setDisable(true);
				final BusyPopover claimPopover = BusyPopover.createBusyPopover("Saving action...", saveActionButton);

				Utility.execute(() -> {
					try
					{
						final LocalTask currentlySelectedTask = taskComboBox.getValue();
						final Action currentlySelectedAction = actionComboBox.getValue();
						final String outComment = outCommentTextArea.getText();

						Map<String, String> variableMap = new HashMap<>();
						variableMap.put(ActionOutputVariables.out_comment.name(), outComment);
						taskService_.setAction(currentlySelectedTask.getId(), currentlySelectedAction, TaskActionStatus.Pending, variableMap);
						Platform.runLater(() -> 
						{
							claimPopover.hide();
							refreshContent();
							if (this.isDataRequiredForSaveOk()) {
								saveActionButton.setDisable(false);
							}
						});
					}
					catch (Exception e)
					{
						claimPopover.hide();
						logger.error("Error saving task: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
					}
				});
			} else { // ! this.isDataRequiredForSaveOk()
				// This should never happen, if saveActionButton.setDisable(true) used in proper places
				LocalTask selectedTask = taskComboBox.getSelectionModel().getSelectedItem();
				Action selectedAction = actionComboBox.getSelectionModel().getSelectedItem();
				logger.error("Error saving task: fields not set: task=" + selectedTask + ", action=" + selectedAction);
			}
		});
	}
	
	private Node configureNode(Node node) {
		if (node instanceof Label) {
			Label label = (Label)node;
			label.setPadding(new Insets(5));
			label.setStyle("-fx-font-weight: bold");
		} else if (node instanceof TextArea) {
			TextArea textArea = (TextArea)node;
			textArea.setPadding(new Insets(5));
		}
		
		return node;
	}
	
	// handler to disable/enable saveActionButton based on validity of required data
	// This method is used, but currently referenced only in FXML
	@FXML 
	private void handleChangeInDataRequiredForSaveAction() {
		if (isDataRequiredForSaveOk()) {
			saveActionButton.setDisable(false);
		} else {
			saveActionButton.setDisable(true);
		}
	}

	// TODO: This should be replaced by call to framework
	private final String getUserName() {
		return "alejandro";
	}

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
		
		//NodeType nodeType = NodeType.valueOf(initialTask.getName());
		
		//currentWorkflowProcessNode = WorkflowProcessNodeFactory.newWorkflowProcessNode(nodeType);
		
		loadContent();
	}

	ConceptVersionBI getConcept() {
		return conceptVersion;
	}
	
	// Private helper method to test validity of data required for save
	private boolean isDataRequiredForSaveOk() {
		LocalTask selectedTask = taskComboBox.getSelectionModel().getSelectedItem();
		Action selectedAction = actionComboBox.getSelectionModel().getSelectedItem();
		String outComment = outCommentTextArea.getText();

		return selectedTask != null && selectedAction != null && outComment != null && outComment.length() > 0;
	}

	private boolean isDataRequiredToViewTaskDetails() {
		return taskComboBox.getSelectionModel().getSelectedItem() != null;
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
	
	// Load data into GUI components
	protected void loadContent()
	{
		initializeServices();
		
		refreshContent();
	}

	private void loadConcept() {
		// The following is example code for testing
		//		int nid = 0;
//		// This code is a hack to populate the conceptView with a test concept
//		if (concept == null) {
//			try {
//				nid = Snomed.ORGANISM.getNid();
//				setConcept(ExtendedAppContext.getDataStore().getConcept(nid));
//			} catch (ValidationException e1) {
//				logger.error("Error: getting Nid from concept " + Snomed.ORGANISM + ": caught " + e1.getClass().getName() + " \"" + e1.getLocalizedMessage() + "\"");
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				logger.error("Error: getting Nid from concept " + Snomed.ORGANISM + ": caught " + e1.getClass().getName() + " \"" + e1.getLocalizedMessage() + "\"");
//				e1.printStackTrace();
//			}
//		}
			
		// loadConcept() must not be called before setConcept().  conceptVersion must not be null  
		conceptView.setConcept(conceptVersion.getNid());
		conceptScrollPane.setContent(conceptView.getView());
	}
	
	// Refresh all content
	private void refreshContent()
	{
		refreshTasks();
		refreshActions();
		loadConcept();
		
		// Ensure that saveActionButton is properly disabled/enabled based on dependencies
		if (this.isDataRequiredForSaveOk()) {
			saveActionButton.setDisable(false);
		} else {
			saveActionButton.setDisable(true);
		}
	}

	// helper to refresh task list in taskComboBox
	private void refreshTasks() {
		/// Example tasks
//		LocalTask [id=38, name=Step 2, componentId=56968009, componentName=Guillermo 2 Wood asthma (disorder), status=Reserved, owner=alejandro, action=NONE, actionStatus=]
//		LocalTask [id=37, name=Step 2, componentId=56968009, componentName=Guillermo Wood asthma (disorder), status=Reserved, owner=alejandro, action=NONE, actionStatus=]
//		LocalTask [id=36, name=Step 2, componentId=56968009, componentName=Guillermo 2 Wood asthma (disorder), status=Reserved, owner=alejandro, action=NONE, actionStatus=]
//		LocalTask [id=34, name=Step 1, componentId=56968009, componentName=Guillermo Wood asthma (disorder), status=Reserved, owner=alejandro, action=NONE, actionStatus=]
//		LocalTask [id=30, name=Step 1, componentId=56968009, componentName=Guillermo Wood asthma (disorder), status=Reserved, owner=alejandro, action=NONE, actionStatus=]
//		LocalTask [id=1, name=Step 1, componentId=12314442, componentName=Component 1, status=Reserved, owner=alejandro, action=NONE, actionStatus=]
		
		taskComboBox.getItems().clear();
		if (initialTask != null) {
			taskComboBox.getItems().add(initialTask);
		} else {
			List<LocalTask> tasks = taskService_.getOpenOwnedTasksByComponentId(conceptVersion.getPrimordialUuid().toString());
			Collections.sort(tasks, LocalTask.ID_COMPARATOR);
			taskComboBox.getItems().addAll(tasks);
		}
		
		try {
			logger.debug("DEBUG: Loaded " + taskComboBox.getItems().size() + " open tasks for user \"" + getUserName() + "\" for concept UUID \"" + conceptVersion.getPrimordialUuid() + "\" (" + conceptVersion.getPreferredDescription().getText() + ")");
		} catch (IOException | ContradictionException e1) {
			String title = "Failed getting preferred description";
			String msg = "Unexpected error calling getPreferredDescription() of conceptVersion: caught " + e1.getClass().getName();
			logger.error(title, e1);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	// Helper to refresh action list in actionComboBox
	private void refreshActions() {
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
