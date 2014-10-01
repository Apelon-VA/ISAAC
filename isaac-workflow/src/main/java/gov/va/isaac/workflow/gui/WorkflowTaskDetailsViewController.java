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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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

	private Label componentDescriptionLabel;
	private GridPane variableMapGridPane;
	
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

		closeButton.setText("Close");
		closeButton.setOnAction((e) -> doCancel());

		initializeVariableMapGridPane();

		componentDescriptionLabel = new Label();
		componentDescriptionLabel.setPadding(new Insets(5));

		ScrollPane scrollPane = new ScrollPane();
		VBox centerVBox = new VBox();
		centerVBox.getChildren().add(componentDescriptionLabel);
		centerVBox.getChildren().add(variableMapGridPane);
		scrollPane.setContent(centerVBox);
		scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		mainBorderPane.setCenter(scrollPane);
	}

	private void initializeVariableMapGridPane() {
		variableMapGridPane = new GridPane();
	}

	private void loadContents() {
		componentDescriptionLabel.setText(ComponentDescriptionHelper.getComponentDescription(UUID.fromString(task.getComponentId())));

		int rowIndex = 0;

		variableMapGridPane.addRow(rowIndex++, new Label("Task Id"), new Label(Long.toString(task.getId())));

		if (task.getInputVariables() != null && task.getInputVariables().size() > 0) {
			//variableMapGridPane.addRow(rowIndex++, new Label("Input Variables"));

			for (Map.Entry<String, String> entry: task.getInputVariables().entrySet()) {
				MapVariable mappedVariable = MapVariable.valueOfIfExists(entry.getKey());

				if (MapVariable.shouldDisplay(entry.getKey())) {
					String text = mappedVariable != null ? MapVariable.valueOf(entry.getKey()).getDisplayName() : entry.getKey();
					variableMapGridPane.addRow(rowIndex++, new Label(text), new Label(entry.getValue()));
				} else {
					LOG.debug("Not displaying excluded input variables map entry: {}", entry);
				}
			}
		}
		if (task.getOutputVariables() != null && task.getOutputVariables().size() > 0) {
			//variableMapGridPane.addRow(rowIndex++, new Label("Output Variables"));

			for (Map.Entry<String, String> entry: task.getOutputVariables().entrySet()) {
				MapVariable mappedVariable = MapVariable.valueOfIfExists(entry.getKey());

				if (MapVariable.shouldDisplay(entry.getKey())) {
					String text = mappedVariable != null ? MapVariable.valueOf(entry.getKey()).getDisplayName() : entry.getKey();
					variableMapGridPane.addRow(rowIndex++, new Label(text), new Label(entry.getValue()));
				} else {
					LOG.debug("Not displaying excluded output variables map entry: {}", entry);
				}
			}
		}
		
		for (Node node : variableMapGridPane.getChildren()) {
			VariableGridPaneNodeConfigurationHelper.configureNode(node);
		}
	}

	/**
	 * Handler for cancel button.
	 */
	public void doCancel() {
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

			Utility.submit(new Runnable() {
				@Override
				public void run() {
					workflowEngine.synchronizeWithRemote();
				}
			});
		}

		return workflowEngine;
	}
}
