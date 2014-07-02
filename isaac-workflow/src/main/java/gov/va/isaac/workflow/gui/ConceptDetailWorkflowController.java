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


import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.util.Utility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.UnmodifiableArrayList;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * {@link ConceptDetailWorkflowController}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class ConceptDetailWorkflowController
{
	// TODO: this should be based on a value retrievable from the API
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
	
	@FXML private Button saveActionButton;
	
	// This code only for newWorkflowInstanceButton
	//@FXML private Button newWorkflowInstanceButton;
	
	@FXML private ComboBox<LocalTask> taskComboBox;
	@FXML private ComboBox<Action> actionComboBox;
	
	// This code only for embedded concept detail view
	//@FXML private Pane simpleConceptView;
	//private ConceptViewI conceptView;
	
	private final static UnmodifiableArrayList<Action> actions = new UnmodifiableArrayList<>(Action.values(), Action.values().length);

	// Workflow Engine and Task Service
	private LocalWorkflowRuntimeEngineBI wfEngine_;
	private LocalTasksServiceBI taskService_;
	
	private final Logger logger = LoggerFactory.getLogger(ConceptDetailWorkflowController.class);

	// TODO: This should be replaced by call to framework
	private final String getUserName() {
		return "alejandro";
	}
	

	@FXML
	void initialize()
	{
		assert saveActionButton != null : "fx:id=\"saveActionButton\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		//assert newWorkflowInstanceButton != null : "fx:id=\"newWorkflowInstanceButton\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		assert actionComboBox != null : "fx:id=\"actionComboBox\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		assert taskComboBox != null : "fx:id=\"taskComboBox\" was not injected: check your FXML file 'ConceptDetailWorkflow.fxml'.";
		
//		// This code only for embedded concept detail view
//		// Use H2K to find and initialize conceptView as a ConceptView
//		conceptView = AppContext.getService(ConceptView.class);
//		simpleConceptView.getChildren().add(conceptView.getView());
//
//		// TODO: get concept from somewhere
//		conceptView.setConcept(Snomed.ORGANISM.getUuids()[0]);

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
		
		saveActionButton.setOnAction((action) -> {
			saveActionButton.setDisable(true);
			final BusyPopover claimPopover = BusyPopover.createBusyPopover("Saving action...", saveActionButton);

			Utility.execute(() -> {
				try
				{
					final LocalTask currentlySelectedTask = taskComboBox.getValue();
					final Action currentlySelectedAction = actionComboBox.getValue();
					
					taskService_.setAction(currentlySelectedTask.getId(), currentlySelectedAction.toString(), ACTION_STATUS);
					Platform.runLater(() -> 
					{
						claimPopover.hide();
						saveActionButton.setDisable(false);
						refreshContent();
					});
				}
				catch (Exception e)
				{
					claimPopover.hide();
					logger.error("Error saving task: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
				}
			});
		});

		// This code only for newWorkflowInstanceButton
//		newWorkflowInstanceButton.setOnAction((action) -> {
//			newWorkflowInstanceButton.setDisable(true);
//			final BusyPopover createNewWorkflowInstancePopover = BusyPopover.createBusyPopover("Creating new workflow instance...", newWorkflowInstanceButton);
//
//			Utility.execute(() -> {
//				try
//				{
//					//TODO: create new workflow instance here
//					logger.error("Error creating new workflow instance: not yet implemented");
//
//					Platform.runLater(() -> 
//					{
//						createNewWorkflowInstancePopover.hide();
//						newWorkflowInstanceButton.setDisable(false);
//						refreshContent();
//					});
//				}
//				catch (Exception e)
//				{
//					createNewWorkflowInstancePopover.hide();
//					logger.error("Error creating new workflow instance: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
//				}
//			});
//		});
	}

	protected void loadContent()
	{
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
			wfEngine_.synchronizeWithRemote();
			
			taskService_ = wfEngine_.getLocalTaskService();
			
			ObservableList<LocalTask> tasks = new ObservableListWrapper<LocalTask>(taskService_.getOpenOwnedTasks(getUserName()));
			
			Collections.sort(tasks, LocalTask.ID_COMPARATOR);

			taskComboBox.setItems(tasks);
			System.out.println("Loaded " + taskComboBox.getItems().size() + " open tasks for user \"" + getUserName() + "\"");

			refreshActions();
		}
		else
		{
			refreshContent();
		}
	}

	private void refreshTasks() {
		taskComboBox.getItems().clear();
		List<LocalTask> tasks = taskService_.getOpenOwnedTasks(getUserName());
		Collections.sort(tasks, LocalTask.ID_COMPARATOR);
		taskComboBox.getItems().addAll(tasks);
		System.out.println("Loaded " + taskComboBox.getItems().size() + " open tasks for user \"" + getUserName() + "\"");
	}
	private void refreshActions() {
		actionComboBox.getItems().clear();
		actionComboBox.getItems().addAll(actions);
	}
	
	private void refreshContent()
	{
		refreshTasks();
		refreshActions();
	}
}
