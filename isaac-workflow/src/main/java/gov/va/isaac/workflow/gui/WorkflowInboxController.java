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
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.interfaces.gui.views.WorkflowAdvancementViewI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusTaskIdProvider;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
	@FXML Button synchronizeButton;
	@FXML Label userName;
	@FXML TableView<LocalTask> taskTable;

	private LocalWorkflowRuntimeEngineBI wfEngine_;
	private LocalTasksServiceBI taskService_;
	private final Logger logger = LoggerFactory.getLogger(WorkflowInboxController.class);
	//TODO figure out how we handle usernames
	private String user = "alejandro";
	
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
		assert claimTasksButton != null : "fx:id=\"claimTasksButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert synchronizeButton != null : "fx:id=\"synchronizeButton\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert userName != null : "fx:id=\"userName\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert taskTable != null : "fx:id=\"taskTable\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";
		assert rootBorderPane != null : "fx:id=\"rootBorderPane\" was not injected: check your FXML file 'WorkflowInbox.fxml'.";

		userName.setText(user);

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
		tCol.setText("id");
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
			ComponentChronicleBI<?> componentChronicle = WBUtility.getComponentChronicle(componentUuid);
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
					getWorkflowEngine().claim(10, user);
					Platform.runLater(() -> 
					{
						claimPopover.hide();
						claimTasksButton.setDisable(false);
						refreshContent();
						
						synchronize(false);
					});
				}
				catch (Exception e)
				{
					logger.error("Unexpected error claiming tasks", e);
				}
			});
		});

		synchronizeButton.setOnAction((action) -> {
			synchronize(true);
		});
	}

	private String getCompType(String componentName) {
		String[] s = componentName.split(" ");
		return s[0];
	}

	public Region getView() {
		return rootBorderPane;
	}
	
	private void synchronize(final boolean displayBusyPopover) {
		synchronizeButton.setDisable(true);

		BusyPopover synchronizePopover = null;
		
		if (displayBusyPopover) {
			synchronizePopover = BusyPopover.createBusyPopover("Synchronizing tasks...", synchronizeButton);
		}

		final BusyPopover finalBusyPopover = synchronizePopover;
		
		Utility.execute(() -> {
			try
			{
				getWorkflowEngine().synchronizeWithRemote();
				Platform.runLater(() -> 
				{
					if (finalBusyPopover != null) {
						finalBusyPopover.hide();
					}
					synchronizeButton.setDisable(false);
					refreshContent();
				});
			}
			catch (Exception e)
			{
				logger.error("Unexpected error synchronizing tasks", e);
			}
		});
	}
	
	protected void loadContent()
	{
		refreshContent();
	}

	private void refreshContent()
	{
		taskTable.getItems().clear();
		taskTable.getItems().addAll(getTaskService().getOpenOwnedTasks());
	}

	private LocalWorkflowRuntimeEngineBI getWorkflowEngine() {
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
		}
		
		return wfEngine_;
	}

	private LocalTasksServiceBI getTaskService() {
		if (taskService_ == null) {
			taskService_ = getWorkflowEngine().getLocalTaskService();
		}
		
		return taskService_;
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

									// TODO: determine why we are getting here multiple (2 or 3) times for each selection
									//System.out.println("Selected strings: " + Arrays.toString(itemArray));

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

							WorkflowAdvancementViewI view = AppContext.getService(WorkflowAdvancementViewI.class);
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
