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
import gov.va.isaac.gui.listview.operations.OperationResult;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.CommonMenuBuilderI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.UpdateableDoubleBinding;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.OTFUtility;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
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
	private enum LocalMenuItem {
		REMOVE_FROM_LIST("Remove from List", Images.DELETE),
		COMMIT_CONCEPT_CHANGE("Commit Concept Change", Images.COMMIT),
		CANCEL_CONCEPT_CHANGE("Cancel Concept Change", Images.CANCEL);
		
		final String text;
		final Images image;

		private LocalMenuItem(String text, Images image) {
			this.text = text;
			this.image = image;
		}

		public String getText() {
			return text;
		}
		public Images getImage() {
			return image;
		}
	}
	
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
	@FXML private Button commitButton;
	@FXML private Button cancelButton;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Button saveListButton;
	@FXML private AnchorPane rootPane;

	private UpdateableBooleanBinding allOperationsReady_;

	StringProperty reasonWhyExecuteDisabled_ = new SimpleStringProperty();
	StringProperty noChangeReasonWhyExecuteDisabled_ = new SimpleStringProperty();
	
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	private int uncommittedCount = 0;

	private PopupConceptViewI conceptView;

	protected static ListBatchViewController init() throws IOException
	{
		// Load from FXML.
		URL resource = ListBatchViewController.class.getResource("ListBatchView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	private static MenuItem findMenuItem(List<MenuItem> itemsToSearch, LocalMenuItem itemToFind) {
		for (MenuItem itemToSearch : itemsToSearch) {
			if (itemToSearch.getText().equals(itemToFind.getText())) {
				return itemToSearch;
			}
		}
		
		return null;
	}
	
	@FXML
	public void initialize()
	{
		conceptView = AppContext.getService(PopupConceptViewI.class, SharedServiceNames.MODERN_STYLE);
		
		operationsList.getChildren().add(new OperationNode(this));

		final ConceptNode cn = new ConceptNode(null, false);
		cn.setPromptText("Type, drop or select a concept to add");
		HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
		HBox.setMargin(cn.getNode(), new Insets(6, 5, 6, 5));
		conceptTableFooter.getChildren().clear();
		conceptTableFooter.getChildren().add(cn.getNode());

		cn.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				if (newValue != null)
				{
					SimpleDisplayConcept sdc = new SimpleDisplayConcept(newValue);
					if (!conceptTable.getItems().contains(sdc)) {
						updateTableItem(sdc, sdc.isUncommitted());
					}
					
					cn.clear();
				}
			}
		});

		conceptTable.setPlaceholder(new Label("Drop Concepts Here"));
		TableColumn<SimpleDisplayConcept, String> col1 = new TableColumn<SimpleDisplayConcept, String>();
		col1.setText("Concept");
		col1.prefWidthProperty().bind(conceptTable.widthProperty().subtract(3.0));
		col1.setCellValueFactory(new PropertyValueFactory<SimpleDisplayConcept, String>("description"));

		col1.setCellFactory(new Callback<TableColumn<SimpleDisplayConcept,String>, TableCell<SimpleDisplayConcept,String>>() {
			
			@Override
			public TableCell<SimpleDisplayConcept, String> call(TableColumn<SimpleDisplayConcept, String> param) {
				final TableCell<SimpleDisplayConcept, String> cell = new TableCell<SimpleDisplayConcept, String>() {
					private final Background uncommittedBackground = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(15), new Insets(2)));
					private final Background defaultBackground = new Background(new BackgroundFill(null, null, null));

					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
	
						TableRow<?> currentRow = getTableRow();
						if (!empty) {
							if (currentRow != null && currentRow.getItem() != null) {
								setText(item);
								setTextFill(Color.BLACK);
								if (((SimpleDisplayConcept)currentRow.getItem()).isUncommitted()) {
									setBackground(uncommittedBackground );
									findMenuItem(currentRow.getContextMenu().getItems(), LocalMenuItem.COMMIT_CONCEPT_CHANGE).setDisable(false);
									findMenuItem(currentRow.getContextMenu().getItems(), LocalMenuItem.CANCEL_CONCEPT_CHANGE).setDisable(false);
								} else {
									findMenuItem(currentRow.getContextMenu().getItems(), LocalMenuItem.COMMIT_CONCEPT_CHANGE).setDisable(true);
									findMenuItem(currentRow.getContextMenu().getItems(), LocalMenuItem.CANCEL_CONCEPT_CHANGE).setDisable(true);
									setBackground(defaultBackground );
								}
							}
						} else {
							if (getText() != null) {
								if (getBackground() != null && 
									getBackground().getFills() != null && 
									getBackground().getFills().size() > 0) {
									setText(null);
									setTextFill(null);
									setBackground(null);
								}
							}
						}
					}
				};

				//TODO this is the wrong listener... what if they keyboard up and down?  
				cell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (((TableCell<?,?>)event.getSource()).getIndex() < conceptTable.getItems().size()) {
							SimpleDisplayConcept con = (SimpleDisplayConcept)conceptTable.getItems().get(((TableCell<?,?>)event.getSource()).getIndex());
							conceptView.setConcept(con.getNid());
							conceptDisplayTab.setContent(conceptView.getView());
						}
					}
				});
				
				return cell;
			}
		});
		
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
			
/*			private UpdateableBooleanBinding isUncommittedConcept_ = new UpdateableBooleanBinding() {
				@Override
				protected boolean computeValue()
				{
					return true;
				}
			};
			
			public BooleanExpression isUncommittedForMenuItem()
			{
				return isUncommittedConcept_; 
			}
*/			


			@Override
			public TableRow<SimpleDisplayConcept> call(TableView<SimpleDisplayConcept> param)
			{
				final TableRow<SimpleDisplayConcept> row = new TableRow<SimpleDisplayConcept>();
				final ContextMenu rowMenu = new ContextMenu();
//				MenuItem viewItem = new MenuItem("View Concept");
//				viewItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
//				viewItem.setOnAction(new EventHandler<ActionEvent>()
//				{
//					@Override
//					public void handle(ActionEvent event)
//					{
//						PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, SharedServiceNames.MODERN_STYLE);
//						cv.setConcept(row.getItem().getNid());
//						cv.showView(rootPane.getScene().getWindow());
//					}
//				});
				MenuItem removeItem = new MenuItem(LocalMenuItem.REMOVE_FROM_LIST.getText());
				removeItem.setGraphic(LocalMenuItem.REMOVE_FROM_LIST.getImage().createImageView());
				removeItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						if (row.getItem().isUncommitted()) {
							AppContext.getCommonDialogs().showErrorDialog("Remove From List Error", "Cannot remove uncomitted Concept.  Cancel change on concept or commit concept first.\n\r", null);
						} else {
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
					}
				});
				MenuItem commitItem = new MenuItem(LocalMenuItem.COMMIT_CONCEPT_CHANGE.getText());
				commitItem.setGraphic(LocalMenuItem.COMMIT_CONCEPT_CHANGE.getImage().createImageView());
				commitItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						try
						{
							OTFUtility.commit(row.getItem().getNid());
							updateTableItem(row.getItem(), false);
						}
						catch (IOException ex)
						{
							logger_.error("Unexpected error during commit", ex);
							AppContext.getCommonDialogs().showErrorDialog("Error committing concept", ex);
						}
					}
				});

				MenuItem cancelItem = new MenuItem(LocalMenuItem.CANCEL_CONCEPT_CHANGE.getText());
				cancelItem.setGraphic(LocalMenuItem.CANCEL_CONCEPT_CHANGE.getImage().createImageView());
				cancelItem.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						//TODO this should be presented to the user... not silently logged
						try {
							ExtendedAppContext.getDataStore().forget(ExtendedAppContext.getDataStore().getConceptVersion(OTFUtility.getViewCoordinate(), row.getItem().getNid()));
							updateTableItem(row.getItem(), false);
						} catch (IOException e) {
							logger_.error("Unable to cancel comp: " + row.getItem().getNid(), e);
						}
					}
				});
				
//				commitItem.visibleProperty().bind(isUncommittedConcept_);
//				cancelItem.visibleProperty().bind(isUncommittedConcept_);
				
				rowMenu.getItems().addAll(/* viewItem, */ removeItem, commitItem, cancelItem);

				row.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getButton() == MouseButton.SECONDARY) {
							@SuppressWarnings("unchecked")
							TableRow<SimpleDisplayConcept> r = (TableRow<SimpleDisplayConcept>)event.getSource();
							if (r.getIndex() < r.getTableView().getItems().size()) {
								CommonMenusDataProvider dp = new CommonMenusDataProvider() {
									@Override
									public String[] getStrings() {
										List<SimpleDisplayConcept> selected = r.getTableView().getSelectionModel().getSelectedItems();
										String descs[] = new String[selected.size()];
										for (int i = 0; i < selected.size(); ++i) {
											descs[i] = selected.get(i).getDescription();
										}

										logger_.debug(selected.size() + " item(s) selected: " + Arrays.toString(descs));

										return descs;
									}
								};

								CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider() {
									@Override
									public Set<Integer> getNIds() {
										List<SimpleDisplayConcept> selected = r.getTableView().getSelectionModel().getSelectedItems();
										Set<Integer> nids = new HashSet<>();
										for (SimpleDisplayConcept concept : selected) {
											nids.add(concept.getNid());
										}

										logger_.debug(selected.size() + " item(s) selected: " + nids);

										return nids;
									}
								};
								CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
								menuBuilder.setMenuItemsToExclude(CommonMenus.CommonMenuItem.LIST_VIEW);
								CommonMenus.addCommonMenus(r.getContextMenu(), menuBuilder, dp, nidProvider);
							}
						}
					}

				});
				
				// only display context menu for non-null items:
				row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
				return row;
			}
		});

		// remove it, wrap it, readd it.
		executeOperationsToolbar.getItems().remove(executeOperationsButton);
		executeOperationsToolbar.getItems().add(ErrorMarkerUtils.setupDisabledInfoMarker(executeOperationsButton, reasonWhyExecuteDisabled_));

		executeOperationsButton.setOnAction((a) -> executeOperations());
		
		reasonWhyExecuteDisabled_.set("No changes have occured from executing the List Operations");
		cancelButton.setDisable(true);
		executeOperationsToolbar.getItems().remove(cancelButton);
		executeOperationsToolbar.getItems().add(ErrorMarkerUtils.setupDisabledInfoMarker(cancelButton, noChangeReasonWhyExecuteDisabled_));
		
		commitButton.setDisable(true);
		executeOperationsToolbar.getItems().remove(commitButton);
		executeOperationsToolbar.getItems().add(ErrorMarkerUtils.setupDisabledInfoMarker(commitButton, noChangeReasonWhyExecuteDisabled_));

		commitButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					OTFUtility.commit();
					uncommitAllTableItems();
				}
				catch (IOException ex)
				{
					logger_.error("Unexpected error during commit", ex);
					AppContext.getCommonDialogs().showErrorDialog("Error committing concept", ex);
				}
			}
		});

		cancelButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				OTFUtility.cancel();
				uncommitAllTableItems();
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
				DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Clear List Question", "Will only remove concepts without uncommitted change.  Continue?");

				if (response == DialogResponse.YES) {
					Set<SimpleDisplayConcept> removalList = new HashSet<>();
					
					for (SimpleDisplayConcept con : conceptTable.getItems()) {
						if (!con.isUncommitted()) {
							removalList.add(con);
						}
					}
					
					for (SimpleDisplayConcept con : removalList) {
						conceptTable.getItems().remove(con);
					}
				
					commitButton.setDisable(uncommittedCount == 0);
					cancelButton.setDisable(uncommittedCount == 0);
				}
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
								DialogResponse dr = AppContext.getCommonDialogs().showYesNoDialog("Overwrite existing file?", 
										"The file '" + f.getName() + "' already exists.  Overwrite?");
								if (DialogResponse.YES != dr)
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
								fileWriter.writeNext(new String[] {OTFUtility.getConceptVersion(c.getNid()).getPrimordialUuid().toString(), c.getDescription()});
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
													ConceptVersionBI c = OTFUtility.getConceptVersion(uuid);
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
												addMultipleItems(newConcepts);
												AppContext.getCommonDialogs().showInformationDialog("Concept Import Completed", "The Concept List import is complete" 
															+ (readErrors.length() > 0 ? "\r\n\r\nThere were some errors:\r\n" + readErrors.toString() : ""));
											}
										});
										
									}
									catch (final Exception e)
									{
										logger_.error("Unexpected error loading concept file", e);
										AppContext.getCommonDialogs().showErrorDialog("Unexpected error exporting concepts", e);
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
							//TODO replace this with either a Java 8 (u40) Dialog, or just build this little thing - this is the only place
							//in the entire code base where we use controlsFx...
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
								SimpleDisplayConcept newCon = new SimpleDisplayConcept(OTFUtility.getDescription(c.getVersion(OTFUtility.getViewCoordinate())), c.getNid());
								newCon.setUncommitted(true);
								concepts.add(newCon);
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
								addMultipleItems(concepts);
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
				@Override
				public void handle(WindowEvent we)
				{
					// disable close button
					we.consume();
				}
			});

			final BooleanProperty cancelRequested = new SimpleBooleanProperty(false);

			final ListBatchOperationsRunnerController lborc = ListBatchOperationsRunnerController.init(cancelRequested, conceptTable.getItems());
			s.setScene(new Scene(lborc.getRoot()));

			final List<CustomTask<OperationResult>> operationsToExecute = new ArrayList<>(operationsList.getChildren().size());
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
						OperationResult result = (OperationResult)event.getSource().getValue();
						taskSummary.append(result.getOperationMsg());
						
						for (SimpleDisplayConcept oldCon: result.getModifiedConcepts()) {
							updateTableItem(oldCon, true);
						}
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

			for (Task<OperationResult> task : operationsToExecute)
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
						for (CustomTask<OperationResult> task : operationsToExecute)
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

	private void updateTableItem(SimpleDisplayConcept oldCon, boolean isUncommitted) {
		ConceptVersionBI con = OTFUtility.getConceptVersion(oldCon.getNid());
		SimpleDisplayConcept newCon = new SimpleDisplayConcept(con);

		int idx = conceptTable.getItems().indexOf(oldCon);
		if (idx >= 0) {
			conceptTable.getItems().remove(idx);
		}
		
		if (isUncommitted) {
			try
			{
				OTFUtility.addUncommitted(con);
				newCon.setUncommitted(true);
				
				if (isUncommitted && (!oldCon.isUncommitted() || idx < 0)) {
					uncommittedCount++;
				}
			}
			catch (IOException ex)
			{
				logger_.error("Unexpected error during add uncommited", ex);
				AppContext.getCommonDialogs().showErrorDialog("Error committing concept", ex);
			}
		} else {
			//TODO this should be shown to the user, not silently logged
			//TODO Jesse - this is broken, and I have no idea why it is being called here.  I don't know why the above code is doing things with 
			//committed / uncommitted either... all very strange.
//			try {
//				ExtendedAppContext.getDataStore().forget(con);
//				newCon.setUncommitted(false);
//
//				if (!isUncommitted && oldCon.isUncommitted()) {
//					uncommittedCount--;
//				}
//			} catch (IOException e) {
//				logger_.error("Unable to cancel concept: " + con.getNid(), e);
//			}
		}

		if (idx >= 0) {
			conceptTable.getItems().add(idx, newCon);
		} else {
			conceptTable.getItems().add(newCon);
		}
		
		commitButton.setDisable(uncommittedCount  == 0);
		cancelButton.setDisable(uncommittedCount  == 0);
	}

	private void uncommitAllTableItems() {
		final ArrayList<SimpleDisplayConcept> newItems = new ArrayList<>();

		for (SimpleDisplayConcept origCon : conceptTable.getItems()) {
			SimpleDisplayConcept displayCon = new SimpleDisplayConcept(OTFUtility.getConceptVersion(origCon.getNid()));
			
			displayCon.setUncommitted(false);
			newItems.add(displayCon);
		}
		
		conceptTable.getItems().clear(); 
		conceptTable.getItems().addAll(newItems);		

		uncommittedCount = 0;
		commitButton.setDisable(true);
		cancelButton.setDisable(true);
	}

	private void addMultipleItems(List<SimpleDisplayConcept> concepts) {
		for (SimpleDisplayConcept con : concepts) {
			updateTableItem(con, con.isUncommitted());
		}
	}
	
	protected void addConcept(int id) {
		List<Integer> idList = new ArrayList<>();
		
		idList.add(id);
		
		addConcepts(idList);
	}

	protected void addConcepts(List<Integer> nids) {
		List<SimpleDisplayConcept> displayConcepts = new ArrayList<>();
		
		for (int nid : nids) {
			ConceptVersionBI concept = OTFUtility.getConceptVersion(nid);
			displayConcepts.add(new SimpleDisplayConcept(concept));
		}
		
		addMultipleItems(displayConcepts);
	}
}
