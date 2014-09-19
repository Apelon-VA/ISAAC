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
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
		NodeName("Node Name"),
		in_component_name("Component Name"),
		in_edit_coordinate(null),
		in_component_id("Component Id"),
		GroupId("Group Id");
		
		private final String displayName;
		
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
			try {
				MapVariable var = MapVariable.valueOf(str);
				
				return var.shouldDisplay();
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	@FXML private BorderPane mainBorderPane;
	
	@FXML private Button closeButton;
	
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

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(variableMapGridPane);
		scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		mainBorderPane.setCenter(scrollPane);
	}

	private void initializeVariableMapGridPane() {
		variableMapGridPane = new GridPane();
	}

	private Node configureNode(Node node) {
		if (node instanceof Label) {
			Label label = (Label)node;
			
			label.setPadding(new Insets(5));

			int columnIndex = GridPane.getColumnIndex(label);
			if (columnIndex == 0) {
				label.setStyle("-fx-font-weight: bold");
			}
			
			label.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.SECONDARY) {
						CommonMenusDataProvider dp = new CommonMenusDataProvider() {
							@Override
							public String[] getStrings() {
								List<String> items = new ArrayList<>();
								items.add(label.getText());


								String[] itemArray = items.toArray(new String[items.size()]);

								// TODO: determine why we are getting here multiple (2 or 3) times for each selection
								//System.out.println("Selected strings: " + Arrays.toString(itemArray));

								return itemArray;
							}
						};

						ContextMenu cm = new ContextMenu();
						CommonMenus.addCommonMenus(cm, dp);

						label.setContextMenu(cm);
					} 
				}
			});
		}

		return node;
	}

	private void loadContents() {
		int rowIndex = 0;
		variableMapGridPane.addRow(rowIndex++, new Label("Task Id"), new Label(Long.toString(task.getId())));
		variableMapGridPane.addRow(rowIndex++, new Label("Component Id"), new Label(task.getComponentId()));
		variableMapGridPane.addRow(rowIndex++, new Label("Component"), new Label(task.getComponentName()));

		if (task.getInputVariables() != null) {
			if (task.getInputVariables().size() > 0) {
				//variableMapGridPane.addRow(rowIndex++, new Label("Input Variables"));
				
				for (Map.Entry<String, String> entry: task.getInputVariables().entrySet()) {
					if (MapVariable.shouldDisplay(entry.getKey())) {
						variableMapGridPane.addRow(rowIndex++, new Label(MapVariable.valueOf(entry.getKey()).getDisplayName()), new Label(entry.getValue()));
					} else {
						LOG.debug("Not displaying unexpected input variables map entry: {}", entry);
					}
				}
			}
		}
		if (task.getOutputVariables() != null) {
			if (task.getOutputVariables().size() > 0) {
				//variableMapGridPane.addRow(rowIndex++, new Label("Output Variables"));
				
				for (Map.Entry<String, String> entry: task.getOutputVariables().entrySet()) {
					if (MapVariable.shouldDisplay(entry.getKey())) {
						variableMapGridPane.addRow(rowIndex++, new Label(MapVariable.valueOf(entry.getKey()).getDisplayName()), new Label(entry.getValue()));
					} else {
						LOG.debug("Not displaying unexpected output variables map entry: {}", entry);
					}
				}
			}
		}
		
		for (Node node : variableMapGridPane.getChildren()) {
			configureNode(node);
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
