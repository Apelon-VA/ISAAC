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
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowAdvancementViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowHistoryViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowTaskDetailsViewI;
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.exceptions.DatastoreException;

import java.util.Map;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link WorkflowTaskDetailsViewI}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class WorkflowTaskDetailsViewController {
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowTaskDetailsViewController.class);
	
	enum MapVariable {
		in_instructions("Instructions"),
		TaskName("Task Name"),
		NodeName("Workflow Process Node Name"),
		in_component_name("Component Name"),
		in_edit_coordinate(), // Don't display
		in_component_id("Component Id"),
		GroupId(); // Don't display
		
		private final String displayName;
		
		private MapVariable() {
			this(null);
		}

		private MapVariable(String dispName) {
			displayName = dispName;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		public boolean shouldDisplay() {
			return getDisplayName() != null;
		}
		
		public static boolean shouldDisplay(String str) {
			MapVariable var = valueOfIfExists(str);
			
			if (var != null) {
				return var.shouldDisplay();
			} else {
				return true;
			}
		}
		
		public static MapVariable valueOfIfExists(String str) {
			try {
				MapVariable var = MapVariable.valueOf(str);
				
				return var;
			} catch (Throwable e) {
				return null;
			}
		}
	}
	
	@FXML private BorderPane mainBorderPane;

	@FXML private Button closeButton;
	@FXML private Button openConceputButton;
	@FXML private Button releaseTaskButton;
	@FXML private Button advanceWfButton;
	@FXML private Button viewWfHxButton;

	@FXML private Label generatedComponentSummary;

	@FXML private Label taskIdLabel;
	@FXML private Label statusLabel;
	@FXML private Label componentLabel;	
	@FXML private Label componentIdLabel;
	
	@FXML private TextArea instructionsTextArea;

	private WorkflowTaskDetailsView workflowTaskDetailsView;
	private LocalTask task;	
	
	@Inject
	private LocalWorkflowRuntimeEngineBI workflowEngine;
	@Inject
	private LocalTasksServiceBI localTasksService;

	private int conceptId;
	
	public Pane getRoot() {
		return mainBorderPane;
	}
	
	public LocalTask getTask() {
		return task;
	}

	public void setTask(LocalTask passedTask) {
		if (passedTask == null) {
			String msg = "Cannot set task to null";
			LOG.error(msg);
			throw new RuntimeException(msg);
		}
		if (task != null) {
			String msg = "Cannot reset task to " + passedTask.getId() + " when task is already set to " + this.task.getId();
			LOG.error(msg);
			throw new RuntimeException(msg);
		}
		
		task = passedTask;

		LOG.debug("Set task to " + task);

		loadContents();
	}
	
	public void setTask(long taskId) throws DatastoreException {
		LocalTask retrievedTask = localTasksService.getTask(taskId);
		
		setTask(retrievedTask);
	}
	
	void setView(WorkflowTaskDetailsView workflowTaskDetailsView) {
		this.workflowTaskDetailsView = workflowTaskDetailsView;
	}

	@FXML
	public void initialize() {
		
		AppContext.getServiceLocator().inject(this);
		assert mainBorderPane != null : "fx:id=\"mainBorderPane\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert closeButton != null : "fx:id=\"closeButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		
		closeButton.setOnAction((e) -> doCancel());
		advanceWfButton.setOnAction((e) -> doAdvanceWorkflow());
		releaseTaskButton.setOnAction((e) -> 
		{
			try
			{
				doReleaseTask();
			}
			catch (DatastoreException e1)
			{
				LOG.error("Error releasing task", e1);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error releasing task", e1);
			}
		});
		openConceputButton.setOnAction((e) -> openConceptPanel());
		viewWfHxButton.setOnAction((e) -> viewWorkflowHistory());
	}

	private void loadContents() {
		UUID componentId = UUID.fromString(task.getComponentId());
		conceptId = OTFUtility.getComponentChronicle(componentId).getConceptNid();
		generatedComponentSummary.setText(ComponentDescriptionHelper.getComponentDescription(componentId));

		taskIdLabel.setText(Long.toString(task.getId()));

		if (task.getActionStatus() == TaskActionStatus.Pending) {
			advanceWfButton.setDisable(true);
			releaseTaskButton.setDisable(true);
		}

		if (task.getInputVariables() != null) {
			if (task.getInputVariables().size() > 0) {
				
				for (Map.Entry<String, String> entry: task.getInputVariables().entrySet()) {
					
					if (MapVariable.shouldDisplay(entry.getKey())) {
						if (entry.getKey().equals("NodeName")) {
							statusLabel.setText(entry.getValue());
						} else if (entry.getKey().equals("in_component_id")) {
							componentIdLabel.setText(entry.getValue());
						} else if (entry.getKey().equals("in_instructions")) {
							instructionsTextArea.setText(entry.getValue());
						}
					} else {
						LOG.debug("Not displaying excluded input variables map entry: {}", entry);
					}
				}
			}
		}
	}

	public void doReleaseTask(long taskId) throws DatastoreException {
		workflowEngine.release(taskId);
	}
	
	private void doCancel() {
		workflowTaskDetailsView.close();
	}
	
	private void doAdvanceWorkflow() {
		WorkflowAdvancementViewI view = AppContext.getService(WorkflowAdvancementViewI.class);
		view.setTask(task.getId());
		view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());

		workflowTaskDetailsView.close();
	}
	
	private void openConceptPanel() {
		PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, SharedServiceNames.MODERN_STYLE);
		cv.setConcept(conceptId);
		cv.showView(null);
	}

	private void doReleaseTask() throws DatastoreException {
		workflowEngine.release(task.getId());
		workflowTaskDetailsView.close();
	}

	private void viewWorkflowHistory() {
		WorkflowHistoryViewI view = AppContext.getService(WorkflowHistoryViewI.class);
		view.setTask(task.getId());
		view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());

		//		workflowTaskDetailsView.close();
	}
}
