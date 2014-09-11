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
import gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
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
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowInboxController.class);

	
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

		taskTable.setTableMenuButtonVisible(true);

		// BEGIN Task name
		TableColumn<LocalTask, String> tCol = new TableColumn<>();
		tCol.setText("Task");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getName());
		});
		taskTable.getColumns().add(tCol);
		// END Task name

		// BEGIN Task id
		tCol = new TableColumn<>();
		tCol.setText("id");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getId() + "");
		});
		taskTable.getColumns().add(tCol);
		// END Task id
		
		// BEGIN Component name
		tCol = new TableColumn<>();
		tCol.setText("Component");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getComponentName());
		});
		taskTable.getColumns().add(tCol);
		// END Component name

		// BEGIN WorkflowAction
		tCol = new TableColumn<>();
		tCol.setText("Action");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getAction().toString());
		});
		tCol.setVisible(false);
		taskTable.getColumns().add(tCol);
		// END WorkflowAction

		// BEGIN TaskActionStatus
		tCol = new TableColumn<>();
		tCol.setText("Action Status");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getActionStatus().name());
		});
		tCol.setVisible(false);
		taskTable.getColumns().add(tCol);
		// END TaskActionStatus
		
		// BEGIN Owner
		tCol = new TableColumn<>();
		tCol.setText("Owner");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getOwner());
		});
		tCol.setVisible(false);
		taskTable.getColumns().add(tCol);
		// END Owner

		// BEGIN Status
		tCol = new TableColumn<>();
		tCol.setText("Status");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getStatus().name());
		});
		tCol.setVisible(false);
		taskTable.getColumns().add(tCol);
		// END Status
		
		// BEGIN Component id (hidden)
		tCol = new TableColumn<>();
		tCol.setText("Component Id");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getComponentId());
		});
		tCol.setVisible(false);
		taskTable.getColumns().add(tCol);
		// END Component id (hidden)
				
		// BEGIN Concept
		TableColumn<LocalTask, SimpleDisplayConcept> conceptCol = new TableColumn<>();
		conceptCol.setText("Concept");
		conceptCol.setCellValueFactory((value) -> {
			if (value.getValue().getComponentId() == null) {
				LOG.error("Component ID for task {} is null", value.getValue().getId());

				return new SimpleObjectProperty<SimpleDisplayConcept>();
			}
			UUID componentUuid = null;
			try {
				componentUuid = UUID.fromString(value.getValue().getComponentId());
			} catch (IllegalArgumentException e) {
				LOG.error("Component ID for task {} is not a valid UUID", value.getValue().getId());

				return new SimpleObjectProperty<SimpleDisplayConcept>();
			}

			ConceptVersionBI containingConcept = null;
			ComponentChronicleBI componentChronicle = WBUtility.getComponentChronicle(componentUuid);
			if (componentChronicle == null) {
				LOG.warn("Component ID for task " + value.getValue().getId() + " retrieved a null componentChronicle");

				containingConcept = WBUtility.getConceptVersion(componentUuid);
				if (containingConcept == null) {
					LOG.error("Component ID for task " + value.getValue().getId() + " retrieved a null concept");

					return new SimpleObjectProperty<SimpleDisplayConcept>();
				}
			} else {
				try {
					containingConcept = componentChronicle.getEnclosingConcept().getVersion(WBUtility.getViewCoordinate());
				} catch (Exception e) {
					LOG.error("Failed getting version from ComponentChronicleBI task " + value.getValue().getId() + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
					e.printStackTrace();
				}
				if (containingConcept == null) {
					LOG.error("ComponentChronicleBI task " + value.getValue().getId() + " contained a null enclosing concept");

					return new SimpleObjectProperty<SimpleDisplayConcept>();
				}
			}

			if (componentChronicle == null) {
				LOG.warn("Component id " + componentUuid + " for task " + value.getValue().getId() + " is a concept, not just a component.");
			}
			SimpleDisplayConcept displayConcept = new SimpleDisplayConcept(containingConcept);
			return new SimpleObjectProperty<SimpleDisplayConcept>(displayConcept);
		});
		taskTable.getColumns().add(conceptCol);
		// END concept
		
		// BEGIN Concept
		TableColumn<LocalTask, String> conceptIdCol = new TableColumn<>();
		conceptIdCol.setText("Concept Id");
		conceptIdCol.setCellValueFactory((value) -> {
			if (value.getValue().getComponentId() == null) {
				LOG.error("Component ID for task {} is null", value.getValue().getId());

				return new SimpleStringProperty();
			}
			UUID componentUuid = null;
			try {
				componentUuid = UUID.fromString(value.getValue().getComponentId());
			} catch (IllegalArgumentException e) {
				LOG.error("Component ID for task {} is not a valid UUID", value.getValue().getId());

				return new SimpleStringProperty();
			}

			ConceptVersionBI containingConcept = null;
			ComponentChronicleBI componentChronicle = WBUtility.getComponentChronicle(componentUuid);
			if (componentChronicle == null) {
				LOG.warn("Component ID for task " + value.getValue().getId() + " retrieved a null componentChronicle");

				containingConcept = WBUtility.getConceptVersion(componentUuid);
				if (containingConcept == null) {
					LOG.error("Component ID for task " + value.getValue().getId() + " retrieved a null concept");

					return new SimpleStringProperty();
				}
			} else {
				try {
					containingConcept = componentChronicle.getEnclosingConcept().getVersion(WBUtility.getViewCoordinate());
				} catch (Exception e) {
					LOG.error("Failed getting version from ComponentChronicleBI task " + value.getValue().getId() + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
					e.printStackTrace();
				}

				if (containingConcept == null) {
					LOG.error("ComponentChronicleBI task " + value.getValue().getId() + " contained a null enclosing concept");

					return new SimpleStringProperty();
				}
			}

			if (componentChronicle == null) {
				LOG.warn("Component id " + componentUuid + " for task " + value.getValue().getId() + " is a concept, not just a component.");
			}
			
			UUID uuid = containingConcept.getPrimordialUuid();
			return new SimpleStringProperty(uuid.toString());
		});
		conceptIdCol.setVisible(false);
		taskTable.getColumns().add(conceptIdCol);
		// END concept ID
		
		
		
		float colWidth = 1.0f / taskTable.getColumns().size();
		for (TableColumn<LocalTask, ?> col : taskTable.getColumns())
		{
			col.prefWidthProperty().bind(taskTable.widthProperty().multiply(colWidth).subtract(3.0));
			col.setCellFactory(new MyCellFactoryCallback<>());
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

	/**
	 * If this nid is a component ref, rather than a concept ref, get the enclosing concept ref.
	 * @param nid
	 */
	private static int getComponentParentConceptNid(int nid)
	{
		ComponentChronicleBI<?> cc = WBUtility.getComponentChronicle(nid);
		if (cc != null)
		{
			return cc.getConceptNid();
		}
		else
		{
			LOG.warn("Unexpected - couldn't find component for nid {}", nid);
			return nid;
		}
	}

	private class MyCellFactoryCallback<T> implements Callback<TableColumn<LocalTask, T>, TableCell<LocalTask, T>> {
		@Override
		public TableCell<LocalTask, T> call(TableColumn<LocalTask, T> param) {
			TableCell<LocalTask, T> newCell = new TableCell<LocalTask, T>() {
				@Override
				public void updateItem(T item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty ? null : getString());
					setGraphic(null);
				}

				private String getString() {
					return getItem() == null ? "" : getItem().toString();
				}
			};
			newCell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

					if (event.getClickCount() == 1) {
						//LOG.debug(event.getButton() + " single clicked. Cell text: " + c.getText());
					} else if (event.getClickCount() > 1) {
						LOG.debug(event.getButton() + " double clicked. Cell text: " + c.getText());
						int cellIndex = c.getIndex();
						LocalTask task = taskTable.getItems().get(cellIndex);
						
						ConceptWorkflowViewI view = AppContext.getService(ConceptWorkflowViewI.class);
						view.setInitialTask(task.getId());
						view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
					}
				}
			});

			return newCell;
		}
	};
}
