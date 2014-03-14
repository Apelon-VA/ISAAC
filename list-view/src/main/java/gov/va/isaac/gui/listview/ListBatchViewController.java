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
package gov.va.isaac.gui.listview;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.listview.operations.CustomTask;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.UpdateableDoubleBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * {@link ListBatchViewController}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ListBatchViewController
{
	@FXML private Button loadListButton;
	@FXML private VBox operationsList;
	@FXML private Button clearOperationsButton;
	@FXML private Tab conceptDisplayTab;
	@FXML private TableView<SimpleDisplayConcept> conceptTable;
	@FXML private HBox conceptTableFooter;
	@FXML private Button addUncommittedListButton;
	@FXML private Button addOperationButton;
	@FXML private Button clearListButton;
	@FXML private Button executeOperationsButton;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Button saveListButton;
	@FXML private AnchorPane rootPane;

	private UpdateableBooleanBinding allOperationsReady_;
	StringProperty reasonWhyExecuteDisabled_ = new SimpleStringProperty();
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

	protected static ListBatchViewController init() throws IOException
	{
		// Load from FXML.
		URL resource = ListBatchViewController.class.getResource("ListBatchView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize()
	{
		operationsList.getChildren().add(new OperationNode(this));

		final ConceptNode cn = new ConceptNode(null, false);
		cn.setPromptText("Type or select concept to add");
		HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
		HBox.setMargin(cn.getNode(), new Insets(6, 5, 6, 5));
		conceptTableFooter.getChildren().add(cn.getNode());

		cn.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				if (newValue != null)
				{
					conceptTable.getItems().add(new SimpleDisplayConcept(newValue));
					cn.clear();
				}
			}
		});

		conceptTable.setPlaceholder(new Label("Drop Concepts Here"));
		TableColumn<SimpleDisplayConcept, String> col1 = new TableColumn<SimpleDisplayConcept, String>();
		col1.setText("Concept");
		col1.prefWidthProperty().bind(conceptTable.widthProperty().subtract(3.0));
		col1.setCellValueFactory(new PropertyValueFactory<SimpleDisplayConcept, String>("description"));
		conceptTable.getColumns().add(col1);
		conceptTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		conceptTable.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode() == KeyCode.DELETE)
				{
					for (SimpleDisplayConcept sdc : conceptTable.getSelectionModel().getSelectedItems())
					{
						conceptTable.getItems().remove(sdc);
					}
					conceptTable.getSelectionModel().clearSelection();
				}
			}
		});

		conceptTable.setRowFactory(new Callback<TableView<SimpleDisplayConcept>, TableRow<SimpleDisplayConcept>>()
		{
			@Override
			public TableRow<SimpleDisplayConcept> call(TableView<SimpleDisplayConcept> param)
			{
				final TableRow<SimpleDisplayConcept> row = new TableRow<SimpleDisplayConcept>();
				final ContextMenu rowMenu = new ContextMenu();
				MenuItem viewItem = new MenuItem("View Concept");
				viewItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
				viewItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						AppContext.getCommonDialogs().showConceptDialog(row.getItem().getNid());
					}
				});
				MenuItem removeItem = new MenuItem("Delete");
				removeItem.setGraphic(Images.DELETE.createImageView());
				removeItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						if (conceptTable.getSelectionModel().getSelectedItems().size() > 1)
						{
							for (SimpleDisplayConcept sdc : conceptTable.getSelectionModel().getSelectedItems())
							{
								conceptTable.getItems().remove(sdc);
							}
							conceptTable.getSelectionModel().clearSelection();
						}
						else
						{
							conceptTable.getItems().remove(row.getItem());
						}
					}
				});
				rowMenu.getItems().addAll(viewItem, removeItem);

				// only display context menu for non-null items:
				row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
				return row;
			}
		});

		// remove it, wrap it, readd it.
		executeOperationsToolbar.getItems().remove(executeOperationsButton);
		executeOperationsToolbar.getItems().add(ErrorMarkerUtils.setupDisabledInfoMarker(executeOperationsButton, reasonWhyExecuteDisabled_));

		executeOperationsButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				executeOperations();
			}
		});

		addOperationButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				OperationNode operationNode = new OperationNode(ListBatchViewController.this);
				operationsList.getChildren().add(operationNode);
				allOperationsReady_.addBinding(operationNode.isReadyForExecution());
				allOperationsReady_.invalidate();
			}
		});

		clearOperationsButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				operationsList.getChildren().clear();
				allOperationsReady_.clearBindings();
				allOperationsReady_.invalidate();
			}
		});

		clearListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				conceptTable.getItems().clear();
			}
		});

		saveListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					FileChooser fc = new FileChooser();
					fc.setInitialFileName("ExportedConcepts.txt");
					fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
					fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
					File file = fc.showSaveDialog(rootPane.getScene().getWindow());
					if (file != null)
					{
						File f;
						String tempPath = file.getCanonicalPath().toLowerCase();
						if (!(tempPath.endsWith(".txt")))
						{
							String extension = fc.selectedExtensionFilterProperty().get().getExtensions().get(0).substring(1);
							// default to .txt, if the user had *.* selected
							if (extension.length() == 0)
							{
								extension = ".txt";
							}
							f = new File(file.getCanonicalPath() + extension);

							// Only have to check this if we muck with the file name...
							if (f.exists())
							{
								Action response = Dialogs.create().owner(rootPane.getScene().getWindow()).title("Overwrite existing file?")
										.masthead(null).nativeTitleBar().message("The file '" + f.getName() + "' already exists.  Overwrite?").showConfirm();
								if (response != Dialog.Actions.YES)
								{
									return;
								}
							}
						}
						else
						{
							f = file.getCanonicalFile();
						}
						
						if (f.getName().toLowerCase().endsWith(".txt"))
						{
							CSVWriter fileWriter = new CSVWriter(new FileWriter(f), '\t',
									CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, System.getProperty("line.separator"));
							fileWriter.writeNext(new String[] {"Primordial UUID", "Description"});
							for (SimpleDisplayConcept c : conceptTable.getItems())
							{
								fileWriter.writeNext(new String[] {WBUtility.getConceptVersion(c.getNid()).getPrimordialUuid().toString(), c.getDescription()});
							}
							fileWriter.close();
						}
						else
						{
							logger_.error("Design error - shouldn't have happened");
							AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "Unknown output format", null);
						}
					}
				}
				catch (Exception e)
				{
					logger_.error("Unexpected error exporting concepts", e);
					AppContext.getCommonDialogs().showErrorDialog("Unexpected error exporting concepts", e);
				}
			}
		});

		loadListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					FileChooser fc = new FileChooser();
					fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
					fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
					final File file = fc.showOpenDialog(rootPane.getScene().getWindow());
					
					if (file != null)
					{
						if (file.getName().toLowerCase().endsWith(".txt"))
						{
							rootPane.getScene().setCursor(Cursor.WAIT);
							
							
							Task<Void> t = new Task<Void>()
							{
								
								@Override
								public Void call()
								{
									try
									{
										updateProgress(-1, -1);
										CSVReader fileReader = new CSVReader(new FileReader(file), '\t',
												CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
										List<String[]> lines = fileReader.readAll();
										fileReader.close();
										final StringBuilder readErrors = new StringBuilder();
										final ArrayList<SimpleDisplayConcept> newConcepts = new ArrayList<>();
										int lineNumber = 0;
										for (String[] line : lines)
										{
											lineNumber++;
											if (line.length > 0)
											{
												try 
												{
													UUID uuid = UUID.fromString(line[0]);
													ConceptVersionBI c = WBUtility.getConceptVersion(uuid);
													if (c != null)
													{
														newConcepts.add(new SimpleDisplayConcept(c));
													}
													else
													{
														readErrors.append("The UUID found on line " + lineNumber + " '" + uuid.toString() + "' could not be resolved\r\n");
													}
												}
												catch (IllegalArgumentException e)
												{
													if (lineNumber > 1)
													{
														readErrors.append("No UUID found on line " + lineNumber + "\r\n");
													}
												}
											}
										}
										
										Platform.runLater(new Runnable()
										{
											@Override
											public void run()
											{
												conceptTable.getItems().addAll(newConcepts);
												Dialogs.create().title("Concept Import Completed").masthead(null)
													.message("The Concept List import is complete" 
															+ (readErrors.length() > 0 ? "\r\n\r\nThere were some errors:\r\n" + readErrors.toString() : ""))
															.owner(rootPane.getScene().getWindow()).nativeTitleBar().showInformation();
											}
										});
										
									}
									catch (final Exception e)
									{
										logger_.error("Unexpected error loading concept file", e);
										Platform.runLater(new Runnable()
										{
											@Override
											public void run()
											{
												AppContext.getCommonDialogs().showErrorDialog("Unexpected error exporting concepts", e);
											}
										});
									}
									finally
									{
										Platform.runLater(new Runnable()
										{
											
											@Override
											public void run()
											{
												rootPane.getScene().setCursor(Cursor.DEFAULT);
											}
										});
									}
									return null;
								}
							};
							Dialogs.create().title("Importing").masthead(null).message("Importing").owner(rootPane.getScene().getWindow())
								.nativeTitleBar().showWorkerProgress(t);
							Utility.execute(t);
						}
						else
						{
							logger_.error("Design error - shouldn't have happened");
							AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "Unknown output format", null);
						}
					}
				}
				catch (Exception e)
				{
					logger_.error("Unexpected error loading concepts", e);
					AppContext.getCommonDialogs().showErrorDialog("Unexpected error exporting concepts", e);
				}
			}
		});

		addUncommittedListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				addUncommittedListButton.setDisable(true);
				Runnable r = new Runnable()
				{
					@Override
					public void run()
					{
						final ArrayList<SimpleDisplayConcept> concepts = new ArrayList<>();
						for (ConceptChronicleBI c : ExtendedAppContext.getDataStore().getUncommittedConcepts())
						{
							try
							{
								concepts.add(new SimpleDisplayConcept(WBUtility.getDescription(c.getVersion(WBUtility.getViewCoordinate())), c.getNid()));
							}
							catch (ContradictionException e)
							{
								logger_.error("error adding uncommited concept '" + c + "'", e);
							}
						}
						Platform.runLater(new Runnable()
						{

							@Override
							public void run()
							{
								conceptTable.getItems().addAll(concepts);
								addUncommittedListButton.setDisable(false);
							}
						});
					}
				};
				Utility.execute(r);
			}
		});

		allOperationsReady_ = new UpdateableBooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				for (Node n : operationsList.getChildren())
				{
					if (n instanceof OperationNode)
					{
						OperationNode on = (OperationNode) n;
						if (!on.isReadyForExecution().get())
						{
							reasonWhyExecuteDisabled_.set("All errors must be corrected in the Operations List before execution");
							return false;
						}
					}
					else
					{
						logger_.error("Design error - Node isn't an operationNode!");
					}
				}
				if (operationsList.getChildren().size() == 0)
				{
					reasonWhyExecuteDisabled_.set("At least one operation must be specified");
					return false;
				}
				return true;
			}
		};

		for (Node n : operationsList.getChildren())
		{
			if (n instanceof OperationNode)
			{
				allOperationsReady_.addBinding(((OperationNode) n).isReadyForExecution());
			}
			else
			{
				logger_.error("Design error - Node isn't an operationNode!");
			}
		}
		allOperationsReady_.invalidate();

		executeOperationsButton.disableProperty().bind(allOperationsReady_.not());
	}

	protected void remove(OperationNode node)
	{
		if (!operationsList.getChildren().remove(node))
		{
			logger_.error("Unexpected error removing operation item");
		}
		allOperationsReady_.removeBinding(node.isReadyForExecution());
		allOperationsReady_.invalidate();
	}

	protected ObservableList<SimpleDisplayConcept> getConceptList()
	{
		return conceptTable.getItems();
	}

	public AnchorPane getRoot()
	{
		return rootPane;
	}

	private void executeOperations()
	{
		try
		{
			final AtomicInteger tasksCompleted = new AtomicInteger(0);
			final StringBuilder taskSummary = new StringBuilder();
			final Stage s = new Stage(StageStyle.UTILITY);
			s.initOwner(rootPane.getScene().getWindow());
			s.initModality(Modality.WINDOW_MODAL);
			s.setTitle("Batch Operation Execution");
			s.setOnCloseRequest(new EventHandler<WindowEvent>()
			{
				public void handle(WindowEvent we)
				{
					// disable close button
					we.consume();
				}
			});

			final BooleanProperty cancelRequested = new SimpleBooleanProperty(false);

			final ListBatchOperationsRunnerController lborc = ListBatchOperationsRunnerController.init(cancelRequested);
			s.setScene(new Scene(lborc.getRoot()));

			final List<CustomTask<String>> operationsToExecute = new ArrayList<>(operationsList.getChildren().size());
			for (Node n : operationsList.getChildren())
			{
				OperationNode on = (OperationNode) n;
				operationsToExecute.add(on.getOperation().createTask());
			}

			final UpdateableDoubleBinding progressComputer = new UpdateableDoubleBinding()
			{
				@Override
				protected double computeValue()
				{
					// rough progress
					double progress = ((double) tasksCompleted.get()) / ((double) operationsToExecute.size());

					for (Object binding : getDependencies())
					{
						// Should only be one of these, at most - which will be the currently executing task
						DoubleProperty db = (DoubleProperty) binding;
						progress += (db.get() / ((double) operationsToExecute.size()));
					}
					return progress;
				}
			};

			lborc.getProgressProperty().bind(progressComputer);

			s.show();

			final ExecutorService es = Executors.newFixedThreadPool(1);
			EventHandler<WorkerStateEvent> workerStateHandler = new EventHandler<WorkerStateEvent>()
			{
				@Override
				public void handle(WorkerStateEvent event)
				{
					boolean finished = false;
					if (event.getEventType() == WorkerStateEvent.WORKER_STATE_RUNNING)
					{
						progressComputer.clearBindings();
						progressComputer.addBinding(event.getSource().progressProperty());
						progressComputer.invalidate();
						lborc.setTitle("(" + (tasksCompleted.get() + 1) + " of " + operationsToExecute.size() + ") " + event.getSource().getTitle());
						lborc.getMessageProperty().bind(event.getSource().messageProperty());
					}
					else if (event.getEventType() == WorkerStateEvent.WORKER_STATE_CANCELLED || event.getEventType() == WorkerStateEvent.WORKER_STATE_FAILED)
					{
						finished = true;
						if (event.getEventType() == WorkerStateEvent.WORKER_STATE_CANCELLED)
						{
							logger_.info("Task Execution cancelled: " + event.getSource().getState(), event.getSource().getException());
						}
						else
						{
							logger_.error("Task Execution failed: " + event.getSource().getState(), event.getSource().getException());
						}
						taskSummary.append(event.getSource().getTitle() + " "
								+ (event.getSource().getException() == null ? event.getSource().getState() : "Failed: " + event.getSource().getException()));
					}
					else if (event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED)
					{
						finished = true;
						taskSummary.append((String) event.getSource().getValue());
					}
					
					taskSummary.append("\r\n");
					if (finished)
					{
						tasksCompleted.incrementAndGet();
						lborc.getSummary().setText(taskSummary.toString());
						lborc.getMessageProperty().unbind();
						lborc.getMessageProperty().setValue("");
						progressComputer.clearBindings();
						progressComputer.invalidate();
					}
				}
			};

			for (Task<String> task : operationsToExecute)
			{
				task.setOnRunning(workerStateHandler);
				task.setOnCancelled(workerStateHandler);
				task.setOnFailed(workerStateHandler);
				task.setOnSucceeded(workerStateHandler);
				es.submit(task);
			}

			cancelRequested.addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
				{
					if (cancelRequested.get())
					{
						for (CustomTask<String> task : operationsToExecute)
						{
							// Can't use task.cancel(false) because oracle mis-designed it... and the executor service doesn't
							// wait for the task to actually complete when you use the awaitCompletion() method.
							task.shutdownRequested();
						}
					}
				}
			});

			es.shutdown();

			// spawn a thread to wait for completion of the tasks
			Thread t = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					while (!es.isTerminated())
					{
						try
						{
							es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
						}
						catch (InterruptedException e)
						{
							// noop
						}
					}
					lborc.finished();
				}
			}, "Batch Execution Monitor Thread");
			t.start();
		}
		catch (Exception e)
		{
			logger_.error("Failure running Batch Operations", e);
			AppContext.getCommonDialogs().showErrorDialog("Error running operations", e);
		}
	}
}
