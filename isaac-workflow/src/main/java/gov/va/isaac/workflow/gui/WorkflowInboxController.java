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
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * {@link WorkflowInboxController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class WorkflowInboxController
{
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private Button claimTasksButton;
	@FXML private Button synchronizeButton;
	@FXML private Label userName;
	@FXML private TableView<LocalTask> taskTable;

	private LocalWorkflowRuntimeEngineBI wfEngine_;
	private LocalTasksServiceBI taskService_;
	private final Logger logger = LoggerFactory.getLogger(WorkflowInboxController.class);
	//TODO figure out how we handle usernames
	private String user = "alejandro";

	@FXML
	void initialize()
	{
		assert claimTasksButton != null : "fx:id=\"claimTasksButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert synchronizeButton != null : "fx:id=\"synchronizeButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert userName != null : "fx:id=\"userName\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert taskTable != null : "fx:id=\"taskTable\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";

		userName.setText(user);

		TableColumn<LocalTask, String> tCol = new TableColumn<>();
		tCol.setText("Task");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getName());
		});
		taskTable.getColumns().add(tCol);

		tCol = new TableColumn<>();
		tCol.setText("Component");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getComponentName());
		});
		taskTable.getColumns().add(tCol);

		tCol = new TableColumn<>();
		tCol.setText("id");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getId() + "");
		});
		taskTable.getColumns().add(tCol);

		float colWidth = 1.0f / taskTable.getColumns().size();
		for (TableColumn<LocalTask, ?> col : taskTable.getColumns())
		{
			col.prefWidthProperty().bind(taskTable.widthProperty().multiply(colWidth).subtract(3.0));
		}

		claimTasksButton.setOnAction((action) -> {
			claimTasksButton.setDisable(true);
			final BusyPopover claimPopover = BusyPopover.createBusyPopover("Claiming new tasks...", claimTasksButton);

			Utility.execute(() -> {
				try
				{
					wfEngine_.claim(10, user);
					Platform.runLater(() -> 
					{
						claimPopover.hide();
						claimTasksButton.setDisable(false);
						refreshContent();
					});
				}
				catch (Exception e)
				{
					logger.error("Unexpected error claiming tasks", e);
				}
			});
		});

		synchronizeButton.setOnAction((action) -> {
			synchronizeButton.setDisable(true);
			final BusyPopover synchronizePopover = BusyPopover.createBusyPopover("Synchronizing tasks...", synchronizeButton);

			Utility.execute(() -> {
				try
				{
					wfEngine_.synchronizeWithRemote();
					Platform.runLater(() -> 
					{
						synchronizePopover.hide();
						synchronizeButton.setDisable(false);
						refreshContent();
					});
				}
				catch (Exception e)
				{
					logger.error("Unexpected error synchronizing tasks", e);
				}
			});
		});
	}

	protected void loadContent()
	{
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
			taskService_ = wfEngine_.getLocalTaskService();
			ObservableList<LocalTask> tasks = new ObservableListWrapper<LocalTask>(taskService_.getOpenOwnedTasks(user));
			taskTable.setItems(tasks);
		}
		else
		{
			refreshContent();
		}
	}

	private void refreshContent()
	{
		taskTable.getItems().clear();
		taskTable.getItems().addAll(taskService_.getOpenOwnedTasks("alejandro"));
	}
}
