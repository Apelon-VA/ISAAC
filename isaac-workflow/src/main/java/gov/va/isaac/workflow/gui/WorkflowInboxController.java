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
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowTaskDetailsViewI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusTaskIdProvider;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalTasksServiceBI.ActionEvent;
import gov.va.isaac.workflow.LocalTasksServiceBI.ActionEventListener;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.engine.RemoteSynchronizer;
import gov.va.isaac.workflow.engine.SynchronizeResult;
import gov.va.isaac.workflow.exceptions.DatastoreException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WorkflowInboxController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class WorkflowInboxController
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowInboxController.class);
	
	@FXML BorderPane rootBorderPane;
	@FXML ResourceBundle resources;
	@FXML URL location;
	@FXML Button claimTasksButton;
	@FXML Label userName;
	@FXML TableView<LocalTask> taskTable;

	@Inject private LocalWorkflowRuntimeEngineBI wfEngine_;
	@Inject private LocalTasksServiceBI taskService_;
	@Inject private RemoteSynchronizer remoteSyncService_;
	
	private final ActionEventListener actionEventListener = new ActionEventListener() {
		@Override
		public void handle(ActionEvent actionEvent) {
			if (actionEvent.getActionStatus() == TaskActionStatus.Pending) {
				List<LocalTask> displayedItems = new ArrayList<>(taskTable.getItems());
				for (LocalTask taskInTable : displayedItems) {
					if (taskInTable.getId() == actionEvent.getTaskId()) {
						Platform.runLater(() -> {
							taskTable.getItems().remove(taskInTable);
							LOG.debug("ActionEventListener removed {} task id #{} due to pending {} action", taskInTable.getName(), taskInTable.getId(), taskInTable.getAction());
						});
					}
				}
			}
		}
	};
	
	public static WorkflowInboxController init() throws IOException {
		// Load FXML
		URL resource = WorkflowInboxController.class.getResource("WorkflowInbox.fxml");
		LOG.debug("FXML for " + WorkflowInboxController.class + ": " + resource);
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}
	
	@FXML
	void initialize()
	{
		AppContext.getServiceLocator().inject(this);
		assert claimTasksButton != null : "fx:id=\"claimTasksButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert userName != null : "fx:id=\"userName\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert taskTable != null : "fx:id=\"taskTable\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert rootBorderPane != null : "fx:id=\"rootBorderPane\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";

		//TODO (artf231896) maybe use the preferred name instead of their login name?
		userName.setText(ExtendedAppContext.getCurrentlyLoggedInUser());

		taskTable.setTableMenuButtonVisible(true);

		// BEGIN Task name
		TableColumn<LocalTask, String> tCol = new TableColumn<>();
		tCol.setText("Status");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getName());
		});
		taskTable.getColumns().add(tCol);
		// END Task name

		// BEGIN Task id
		tCol = new TableColumn<>();
		tCol.setText("Id");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getId() + "");
		});
		taskTable.getColumns().add(tCol);
		// END Task id
		
		// BEGIN Component Type
		tCol = new TableColumn<>();
		tCol.setText("Component Type");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(getCompType(value.getValue().getComponentName()));
		});
		taskTable.getColumns().add(tCol);
		// END Component name

		// BEGIN Component name
		tCol = new TableColumn<>();
		tCol.setText("Component");
		tCol.setCellValueFactory((value) -> {
			return new SimpleStringProperty(value.getValue().getComponentName());
		});
		taskTable.getColumns().add(tCol);
		// END Component name

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
			ComponentChronicleBI<?> componentChronicle = OTFUtility.getComponentChronicle(componentUuid);
			if (componentChronicle == null) {
				LOG.warn("Component ID for task " + value.getValue().getId() + " retrieved a null componentChronicle");

				containingConcept = OTFUtility.getConceptVersion(componentUuid);
				if (containingConcept == null) {
					LOG.error("Component ID for task " + value.getValue().getId() + " retrieved a null concept");

					return new SimpleStringProperty();
				}
			} else {
				try {
					containingConcept = componentChronicle.getEnclosingConcept().getVersion(OTFUtility.getViewCoordinate());
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
					wfEngine_.claim(10);
					SynchronizeResult sr = remoteSyncService_.blockingSynchronize();
					if (sr.hasError())
					{
						AppContext.getCommonDialogs().showErrorDialog("Claim Error", "There was a problem running sync after the task claim", sr.getErrorSummary());
					}
				}
				catch (Exception e)
				{
					LOG.error("Unexpected error claiming tasks", e);
					AppContext.getCommonDialogs().showErrorDialog("There was a problem claiming tasks", e);
				} finally {
					cleanup(claimPopover, claimTasksButton, this);
				}
			});
		});

		// TODO (artf231897): not sure we should be loading content in init(). Maybe move to getView()?
		loadContent();
	}

	private String getCompType(String componentName) {
		String[] s = componentName.split(" ");
		return s[0];
	}

	public Region getView() {
		return rootBorderPane;
	}
	
	/*
	 * Need this method as workaround for compiler/JVM bug:
	 * { @link http://stackoverflow.com/questions/13219297/bad-type-on-operand-stack-using-jdk-8-lambdas-with-anonymous-inner-classes }
	 * { @link http://mail.openjdk.java.net/pipermail/lambda-dev/2012-September/005938.html }
	 */
	public static void cleanup(BusyPopover popover, Button button, WorkflowInboxController toRefresh) {
		Platform.runLater(() -> {
			popover.hide();
			button.setDisable(false);
			toRefresh.refreshContent();
		});
	}
	
	protected void loadContent()
	{
		refreshContent();
	}

	private void refreshContent()
	{
		// Remove actionEventListener to prevent concurrent modification of list
		taskService_.removeActionEventListener(actionEventListener);
		taskTable.getItems().clear();
		try
		{
			List<LocalTask> tasksForTable = taskService_.getOpenOwnedTasks();
			// Remove (do not display) tasks with TaskActionStatus.Pending
			List<LocalTask> tasksToRemove = new ArrayList<>();
			for (LocalTask task : tasksForTable) {
				if (task.getActionStatus() == TaskActionStatus.Pending) {
					tasksToRemove.add(task);
				}
				if (task.getComponentName() == null)
				{
					LOG.error("Task {} is missing its component name!", task.getId());
					tasksToRemove.add(task);
				}
			}

			tasksForTable.removeAll(tasksToRemove);
			taskTable.getItems().addAll(tasksForTable);
		}
		catch (DatastoreException e)
		{
			LOG.error("Unexpected error refreshing tasks", e);
			AppContext.getCommonDialogs().showErrorDialog("There was a problem reading the tasks", e);
		} finally {
			taskService_.addActionEventListener(actionEventListener);
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
					if (event.getButton() == MouseButton.SECONDARY) {
						@SuppressWarnings("unchecked")
						TableCell<LocalTask, T> c = (TableCell<LocalTask, T>) event.getSource();

						if (c != null && c.getIndex() < c.getTableView().getItems().size()) {
							CommonMenusDataProvider dp = new CommonMenusDataProvider() {
								@Override
								public String[] getStrings() {
									List<String> items = new ArrayList<>();
									for (Integer index : c.getTableView().getSelectionModel().getSelectedIndices()) {
										items.add(c.getTableColumn().getCellData(index).toString());
									}

									String[] itemArray = items.toArray(new String[items.size()]);

									return itemArray;
								}
							};
							CommonMenusTaskIdProvider taskIdProvider = new CommonMenusTaskIdProvider() {
								@Override
								public Set<Long> getTaskIds() {
									Set<Long> taskIds = new HashSet<Long>();
									for (LocalTask r : (ObservableList<LocalTask>)c.getTableView().getSelectionModel().getSelectedItems()) {
										taskIds.add(r.getId());
									}

									return taskIds;
								}
							};

							ContextMenu cm = new ContextMenu();
							CommonMenus.addCommonMenus(cm, dp, taskIdProvider);

							c.setContextMenu(cm);
						}
					} else {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

						if (event.getClickCount() == 1) {
							//LOG.debug(event.getButton() + " single clicked. Cell text: " + c.getText());
						} else if (event.getClickCount() > 1) {
							LOG.debug(event.getButton() + " double clicked. Cell text: " + c.getText());
							int cellIndex = c.getIndex();
							LocalTask task = taskTable.getItems().get(cellIndex);

							WorkflowTaskDetailsViewI view = AppContext.getService(WorkflowTaskDetailsViewI.class);
							view.setTask(task.getId());
							view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
						}
					}
				}
			});

			return newCell;
		}
	};
}
