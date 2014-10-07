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
import gov.va.isaac.interfaces.gui.views.WorkflowAdvancementViewI;
import gov.va.isaac.interfaces.gui.views.WorkflowTaskViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.workflow.ComponentDescriptionHelper;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.util.Map;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link WorkflowTaskViewI}
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
	@FXML private Button releaseTaskButton;
	@FXML private Button advanceWfButton;

    @FXML private Label generatedComponentSummary;

    @FXML private Label taskIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label componentLabel;	
    @FXML private Label componentIdLabel;
    
    @FXML private TextArea instructionsTextArea;
    @FXML private TextArea commentsTextArea;

	private WorkflowTaskDetailsView workflowTaskDetailsView;
	private LocalTask task;	
	private LocalWorkflowRuntimeEngineBI workflowEngine;
	private LocalTasksServiceBI localTasksService;
	
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
	
	public void setTask(long taskId) {
		LocalTask retrievedTask = getLocalTasksService().getTask(taskId);
		
		setTask(retrievedTask);
	}
	
	void setView(WorkflowTaskDetailsView workflowTaskDetailsView) {
		this.workflowTaskDetailsView = workflowTaskDetailsView;
	}

	@FXML
	public void initialize() {
		assert mainBorderPane != null : "fx:id=\"mainBorderPane\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert closeButton != null : "fx:id=\"closeButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		
		closeButton.setOnAction((e) -> doCancel());
		advanceWfButton.setOnAction((e) -> doAdvanceWorkflow());
		releaseTaskButton.setOnAction((e) -> doReleaseTask());
	}

	private void loadContents() {
		generatedComponentSummary.setText(ComponentDescriptionHelper.getComponentDescription(UUID.fromString(task.getComponentId())));

		taskIdLabel.setText(Long.toString(task.getId()));

		if (task.getInputVariables() != null) {
			if (task.getInputVariables().size() > 0) {
				String[] editorComments = null;
				String[] reviewComments = null;
				String[] approveComments = null;
				
				for (Map.Entry<String, String> entry: task.getInputVariables().entrySet()) {
					
					if (MapVariable.shouldDisplay(entry.getKey())) {
						if (entry.getKey().equals("NodeName")) {
							statusLabel.setText(entry.getValue());
						} else if (entry.getKey().equals("in_component_id")) {
							componentIdLabel.setText(entry.getValue());
						} else if (entry.getKey().equals("in_instructions")) {
							instructionsTextArea.setText(entry.getValue());
						} else if (entry.getKey().equals("editor_comment") && entry.getValue().trim().length() > 0) {
							editorComments = entry.getValue().split(";");
						} else if (entry.getKey().equals("review_comment") && entry.getValue().trim().length() > 0) {
							reviewComments = entry.getValue().split(";");
						} else if (entry.getKey().equals("approval_comment") && entry.getValue().trim().length() > 0) {
							approveComments = entry.getValue().split(";");
						}
					} else {
						LOG.debug("Not displaying excluded input variables map entry: {}", entry);
					}
				}
				
				if (editorComments != null) {
					StringBuffer str = new StringBuffer();
					for (int i = 0; i < editorComments.length; i++) {
						if (i == 0) {
							str.append(editorComments[0]);
						} else {
							str.append("\r\n");
							str.append(reviewComments[i]);
						}

						
						if (reviewComments != null && reviewComments.length < i) {
							str.append("\r\n");
							str.append(reviewComments[i]);
						}

						if (approveComments != null && approveComments.length < i) {
							str.append("\r\n");
							str.append(approveComments[i]);
						}
					}
					
					commentsTextArea.setText(str.toString());
				}
			}
		}
	}

	public void doReleaseTask(long taskId) {
		LocalWorkflowRuntimeEngineFactory.getRuntimeEngine().release(taskId);
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
	
	private void doReleaseTask() {
		LocalWorkflowRuntimeEngineFactory.getRuntimeEngine().release(task.getId());
		workflowTaskDetailsView.close();
	}

	private LocalTasksServiceBI getLocalTasksService() {
		if (localTasksService == null) {
			localTasksService = getWorkflowEngine().getLocalTaskService();
		}
		
		return localTasksService;
	}
	
	private LocalWorkflowRuntimeEngineBI getWorkflowEngine() {
		if (workflowEngine == null) {
			workflowEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();

//			Utility.submit(new Runnable() {
//				@Override
//				public void run() {
//					workflowEngine.synchronizeWithRemote();
//				}
//			});
		}

		return workflowEngine;
	}
}
