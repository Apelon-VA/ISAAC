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
import gov.va.isaac.gui.conceptViews.EnhancedConceptView;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import gov.va.isaac.workflow.persistence.ProcessInstanceCreationRequestsAPI;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.UnmodifiableArrayList;

/**
 * {@link ConceptDetailWorkflowController}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class ConceptDetailWorkflowController
{	
	private final Logger logger = LoggerFactory.getLogger(ConceptDetailWorkflowController.class);

	// TODO: this should be based on a value from the API
	private final static String ACTION_STATUS = "pending";
	
	// TODO: This enum should be made freestanding and replace all other uses of these text constants everywhere. Consider eliminating selectionText then.
	private enum Action { 
		COMPLETE("Complete task"), 
		RELEASE("Release task");
		
		private final String selectionText;
		
		Action(String desc) {
			selectionText = desc;
		}
		
		public String toString() {
			return selectionText;
		}
	}
	
	// Cached Actions in list form for refreshing actionComboBox
	private final static UnmodifiableArrayList<Action> actions = new UnmodifiableArrayList<>(Action.values(), Action.values().length);

	// Underlying concept for loading detail pane
	private ConceptVersionBI conceptVersion;

	// Embedded concept detail pane
	EnhancedConceptView conceptView;
	
	@FXML private BorderPane borderPane;
	@FXML private ScrollPane conceptScrollPane;
	@FXML private Button saveActionButton;

	@FXML private ComboBox<LocalTask> taskComboBox;
	@FXML private ComboBox<Action> actionComboBox;
	@FXML private Button newWorkflowInstanceButton;

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

	// Workflow Engine and Task Service
	private LocalWorkflowRuntimeEngineBI wfEngine_;
	private LocalTasksServiceBI taskService_;

	// TODO: This should be replaced by call to framework
	private final String getUserName() {
		return "alejandro";
	}
	
	// setConcept() should only be called once
	public void setConcept(ConceptVersionBI con) {
		if (conceptVersion != null) {
			throw new RuntimeException("Cannot reset conceptVersion");
		}

		this.conceptVersion = con;
		
		// Uncomment following line only if automatically generating workflow on creation of window
		//startNewWorkflowInstance();
		
		loadContent();
	}
	
	// Private helper method to test validity of data required for save
	private boolean isDataRequiredForSaveOk() {
		LocalTask selectedTask = taskComboBox.getSelectionModel().getSelectedItem();
		Action selectedAction = actionComboBox.getSelectionModel().getSelectedItem();

		return selectedTask != null && selectedAction != null;
	}
	
	// Method for starting new workflow instance
	// TODO: need working test case and data for startNewWorkflowInstance().  Current behavior untested.
	private void startNewWorkflowInstance() {
		ProcessInstanceCreationRequestsAPI popi = new ProcessInstanceCreationRequestsAPI();
		// TODO: eliminate hard-coding of "terminology-authoring.test1"
		final String processName = "terminology-authoring.test1";
		String preferredDescription = null;
		try {
			preferredDescription = conceptVersion.getPreferredDescription().getText();
		} catch (IOException | ContradictionException e1) {
			String title = "Failed starting new workflow instance";
			String msg = "Unexpected error calling getPreferredDescription() of conceptVersion: caught " + e1.getClass().getName();
			logger.error(title, e1);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e1.getMessage());
			e1.printStackTrace();
		}
		
		// TODO: determine how creation of request should be reflected in GUI
		// TODO: replace calls to System.out with calls to logger
		System.out.println("Invoking ProcessInstanceCreationRequestsAPI().createRequest(processName=\"" + processName + "\", conceptUuid=\"" + conceptVersion.getPrimordialUuid().toString() + "\", prefDesc=\"" + preferredDescription + "\", user=\"" + getUserName() + "\")");
		ProcessInstanceCreationRequestI createdRequest = popi.createRequest(processName, conceptVersion.getPrimordialUuid().toString(), preferredDescription, getUserName());
		System.out.println("Created ProcessInstanceCreationRequest: " + createdRequest);
	}

	// Initialize GUI (invoked by FXML)
	@FXML
	void initialize()
	{
		assert saveActionButton != null : "fx:id=\"saveActionButton\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		assert newWorkflowInstanceButton != null : "fx:id=\"newWorkflowInstanceButton\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		assert actionComboBox != null : "fx:id=\"actionComboBox\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		assert taskComboBox != null : "fx:id=\"taskComboBox\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		
		// Disabling saveActionButton until dependencies met 
		saveActionButton.setDisable(true);
		
		// This code only for embedded concept detail view
		// Use H2K to find and initialize conceptView as a ConceptView
		conceptView = AppContext.getService(EnhancedConceptView.class);

		// Force single selection
		actionComboBox.getSelectionModel().selectFirst();
		// Use setCellFactory() and setButtonCell() of ComboBox to customize list entry display
		// TODO: possibly remove: these calls to setCellFactory() and setButtonCell() are only necessary if Action().toString() is not appropriate for this display
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
                } else {
                    setText(t.getId() + ": " + t.getComponentName() + ": " + t.getName());
                }

            }
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

						taskService_.setAction(currentlySelectedTask.getId(), currentlySelectedAction.toString(), ACTION_STATUS, new HashMap<String, String>());
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
		
		
		// This code only for newWorkflowInstanceButton
		newWorkflowInstanceButton.setOnAction((action) -> {
			newWorkflowInstanceButton.setDisable(true);
			final BusyPopover createNewWorkflowInstancePopover = BusyPopover.createBusyPopover("Creating new workflow instance...", newWorkflowInstanceButton);

			Utility.execute(() -> {
				try
				{
					startNewWorkflowInstance();

					Platform.runLater(() -> 
					{
						createNewWorkflowInstancePopover.hide();
						newWorkflowInstanceButton.setDisable(false);
						refreshContent();
					});
				}
				catch (Exception e)
				{
					createNewWorkflowInstancePopover.hide();
					logger.error("Error creating new workflow instance: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
				}
			});
		});
	}

	// Load data into GUI components
	protected void loadContent()
	{
		// TODO: determine if LocalWorkflowRuntimeEngineBI wfEngine_ should be static
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
			// TODO: determine if wfEngine_.synchronizeWithRemote() needs to be called more than once
			wfEngine_.synchronizeWithRemote();
		}
		
		// TODO: determine if LocalTasksServiceBI taskService_ should be static
		if (taskService_ == null) {
			taskService_ = wfEngine_.getLocalTaskService();
		}

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
		conceptScrollPane.setContent((conceptView.getConceptViewerPanel(conceptVersion.getNid())));
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
		List<LocalTask> tasks = taskService_.getOpenOwnedTasksByComponentId(getUserName(), conceptVersion.getPrimordialUuid().toString());
		Collections.sort(tasks, LocalTask.ID_COMPARATOR);
		taskComboBox.getItems().addAll(tasks);
		try {
			// TODO: replace calls to System.out with calls to logger
			System.out.println("DEBUG: Loaded " + taskComboBox.getItems().size() + " open tasks for user \"" + getUserName() + "\" for concept UUID \"" + conceptVersion.getPrimordialUuid() + "\" (" + conceptVersion.getPreferredDescription().getText() + ")");
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
		actionComboBox.getItems().addAll(actions);
	}
	
	public Region getRootNode() {
		return borderPane;
	}
}
