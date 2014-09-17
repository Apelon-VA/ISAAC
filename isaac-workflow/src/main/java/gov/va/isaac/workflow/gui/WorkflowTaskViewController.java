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
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link WorkflowTaskViewI}
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class WorkflowTaskViewController {
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowTaskViewController.class);
	
	@FXML private BorderPane mainBorderPane;
	
	@FXML private Button closeButton;
	
	@FXML private TableView<String> variablesTableView;
	
	private WorkflowTaskView workflowTaskView;

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
	
	void setView(WorkflowTaskView workflowTaskView) {
		this.workflowTaskView = workflowTaskView;
	}

	@FXML
	public void initialize() {
		assert mainBorderPane != null : "fx:id=\"mainBorderPane\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert closeButton != null : "fx:id=\"closeButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";

		closeButton.setText("Close");
		closeButton.setOnAction((e) -> doCancel());

		initializeVariablesTableView();
	}

	private void initializeVariablesTableView() {
		variablesTableView.setTableMenuButtonVisible(true);
	}

	private void loadContents() {
		TableColumn<String, String> col = new TableColumn<>();
		col.setText("Task Id");
		col.setCellValueFactory((value) -> {
			return new SimpleStringProperty(Long.toString(task.getId()));
		});
		variablesTableView.getColumns().add(col);

		col = new TableColumn<>();
		col.setText("Component Id");
		col.setCellValueFactory((value) -> {
			return new SimpleStringProperty(task.getComponentId());
		});
		variablesTableView.getColumns().add(col);

		col = new TableColumn<>();
		col.setText("Component");
		col.setCellValueFactory((value) -> {
			return new SimpleStringProperty(task.getComponentName());
		});
		variablesTableView.getColumns().add(col);

		if (task.getInputVariables() != null) {
			TableColumn<String, String> inputVariablesColumn = new TableColumn<>();
			inputVariablesColumn.setText("Input");

			for (String key : task.getInputVariables().keySet()) {
				col = new TableColumn<>();
				col.setText(key);
				col.setCellValueFactory((value) -> {
					return new SimpleStringProperty(task.getInputVariables().get(key));
				});
				inputVariablesColumn.getColumns().add(col);
			}

			if (inputVariablesColumn.getColumns().size() > 0) {
				variablesTableView.getColumns().add(inputVariablesColumn);
			}
		}

		if (task.getOutputVariables() != null) {
			TableColumn<String, String> outputVariablesColumn = new TableColumn<>();
			outputVariablesColumn.setText("Output");

			for (String key : task.getOutputVariables().keySet()) {
				col = new TableColumn<>();
				col.setText(key);
				col.setCellValueFactory((value) -> {
					return new SimpleStringProperty(task.getOutputVariables().get(key));
				});
				outputVariablesColumn.getColumns().add(col);
			}

			if (outputVariablesColumn.getColumns().size() > 0) {
				variablesTableView.getColumns().add(outputVariablesColumn);
			}
		}
	}

	/**
	 * Handler for cancel button.
	 */
	public void doCancel() {
		workflowTaskView.close();
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

			new Thread(new Runnable() {
				@Override
				public void run() {
					workflowEngine.synchronizeWithRemote();
				}
			}).start();
		}

		return workflowEngine;
	}
}
