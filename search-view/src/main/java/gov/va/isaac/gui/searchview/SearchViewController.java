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
package gov.va.isaac.gui.searchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.CommonMenuBuilderI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Interval;
import gov.va.isaac.util.NumberUtilities;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableListWrapper;


/**
 * Controller class for the Search View.
 * <p>
 * Logic was initially copied LEGO {@code SnomedSearchController}. 
 * Has been enhanced / rewritten much since then.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class SearchViewController implements TaskCompleteCallback
{

	private static final Logger LOG = LoggerFactory.getLogger(SearchViewController.class);

	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private BorderPane borderPane;
	@FXML private TextField searchText;
	@FXML private Button searchButton;
	@FXML private ProgressIndicator searchProgress;
	@FXML private ChoiceBox<SearchInOptions> searchIn;
	@FXML private ChoiceBox<Integer> searchLimit;
	@FXML private ListView<CompositeSearchResult> searchResults;
	@FXML private TitledPane optionsPane;
	@FXML private HBox searchInRefexHBox;
	@FXML private VBox optionsContentVBox;
	@FXML private ToolBar toolBar;
	@FXML private Label statusLabel;

	private final BooleanProperty searchRunning = new SimpleBooleanProperty(false);
	private SearchHandle ssh = null;
	private ConceptNode searchInRefex;
	private ObservableList<SimpleDisplayConcept> dynamicRefexList_ = new ObservableListWrapper<>(new ArrayList<>());
	private Tooltip tooltip = new Tooltip();
	private Integer currentlyEnteredAssemblageNid = null;
	private FlowPane searchInColumnsHolder = new FlowPane();
	private enum SearchInOptions {Descriptions, Refexes};

	public static SearchViewController init() throws IOException
	{
		// Load from FXML.
		URL resource = SearchViewController.class.getResource("SearchView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize()
	{
		assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert searchProgress != null : "fx:id=\"searchProgress\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert searchIn != null : "fx:id=\"searchIn\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert searchResults != null : "fx:id=\"searchResults\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert optionsPane != null : "fx:id=\"optionsPane\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert searchInRefexHBox != null : "fx:id=\"searchInRefexHBox\" was not injected: check your FXML file 'SearchView.fxml'.";
		assert optionsContentVBox != null : "fx:id=\"optionsContentVBox\" was not injected: check your FXML file 'SearchView.fxml'.";

		borderPane.getStylesheets().add(SearchViewController.class.getResource("/isaac-shared-styles.css").toString());
		
		searchIn.getItems().add(SearchInOptions.Descriptions);
		searchIn.getItems().add(SearchInOptions.Refexes);
		searchIn.getSelectionModel().select(0);
		
		tooltip.setText("Enter the description text to search for.  Advanced query syntax such as 'AND', 'NOT' is supported.  You may also enter UUIDs for concepts.");
		tooltip.setWrapText(true);
		tooltip.setMaxWidth(600);
		searchText.setTooltip(tooltip);
		
		optionsContentVBox.getChildren().remove(searchInRefexHBox);
		
		searchIn.valueProperty().addListener((change) ->
		{
			if (searchIn.getSelectionModel().getSelectedItem() == SearchInOptions.Descriptions)
			{
				tooltip.setText("Enter the description text to search for.  Advanced query syntax such as 'AND', 'NOT', 'OR' is supported.  You may also enter UUIDs "
						+ "or NIDs for concepts.");
				optionsContentVBox.getChildren().remove(searchInRefexHBox);
				optionsContentVBox.getChildren().remove(searchInColumnsHolder);
				searchInRefex.clear();  //make sure an invalid state here doesn't prevent the search, when the field is hidden.
			}
			else
			{
				tooltip.setText("Enter the refex value to search for.  Advanced query syntax such as 'AND', 'NOT', 'OR' is supported for refex data fields that "
						+ "are indexed as string values.  For numeric values, mathematical interval syntax is supported - such as [4,6] or (-5,10]."
						+ "  You may also search for 1 or more UUIDs and/or NIDs.");
				optionsContentVBox.getChildren().add(searchInRefexHBox);
				if (searchInColumnsHolder.getChildren().size() > 0)
				{
					optionsContentVBox.getChildren().add(searchInColumnsHolder);
				}
			}
		});
		
		searchInRefex = new ConceptNode(null, false, dynamicRefexList_, null);
		searchInRefex.getConceptProperty().addListener((ChangeListener<ConceptVersionBI>) (observable, oldValue, newValue) -> 
		{
			if (newValue != null)
			{
				searchInColumnsHolder.getChildren().clear();
				try
				{
					RefexDynamicUsageDescription rdud = RefexDynamicUsageDescription.read(newValue.getNid());
					currentlyEnteredAssemblageNid = rdud.getRefexUsageDescriptorNid();
					Integer[] indexedColumns = LuceneDynamicRefexIndexerConfiguration.readIndexInfo(currentlyEnteredAssemblageNid);
					if (indexedColumns == null || indexedColumns.length == 0)
					{
						searchInRefex.isValid().setInvalid("Refex searches can only be performed on indexed columns in the refex.  The selected "
								+ "refex does not contain any indexed data columns.  Please configure the indexes to search this refex.");
						optionsContentVBox.getChildren().remove(searchInColumnsHolder);
					}
					else
					{
						Label l = new Label("Search in Columns");
						searchInColumnsHolder.getChildren().add(l);
						l.minWidthProperty().bind(((Label)searchInRefexHBox.getChildren().get(0)).widthProperty());
						RefexDynamicColumnInfo[] rdci = rdud.getColumnInfo();
						if (rdci.length > 0)
						{
							Arrays.sort(rdci);  //We will depend on them being in the correct order later.
							HashSet<Integer> indexedColumnsSet = new HashSet<>(Arrays.asList(indexedColumns));
							int indexNumber = 0;
							for (RefexDynamicColumnInfo ci : rdci)
							{
								StackPane cbStack = new StackPane();
								CheckBox cb = new CheckBox(ci.getColumnName());
								if (ci.getColumnDataType() == RefexDynamicDataType.BYTEARRAY || !indexedColumnsSet.contains(indexNumber))
								{
									cb.setDisable(true);  //No index on this column... not searchable
									Tooltip.install(cbStack, new Tooltip("Column Datatype: " + ci.getColumnDataType().getDisplayName() + " is not indexed"));
								}
								else
								{
									cb.setSelected(true);
									cb.setTooltip(new Tooltip("Column Datatype: " + ci.getColumnDataType().getDisplayName()));
								}
								cbStack.getChildren().add(cb);
								searchInColumnsHolder.getChildren().add(cbStack);
								indexNumber++;
							}
							optionsContentVBox.getChildren().add(searchInColumnsHolder);
						}
						else
						{
							searchInRefex.isValid().setInvalid("Refex searches can only be performed on the data in the refex.  The selected "
									+ "refex does not contain any data columns.");
							optionsContentVBox.getChildren().remove(searchInColumnsHolder);
						}
					}
				}
				catch (Exception e1)
				{
					searchInRefex.isValid().setInvalid("Refex searches can only be limited to valid Dynamic Refex Assemblage concept types."
							+ "  The current value is not a Dynamic Refex Assemblage concept.");
					currentlyEnteredAssemblageNid = null;
					optionsContentVBox.getChildren().remove(searchInColumnsHolder);
					searchInColumnsHolder.getChildren().clear();
				}
			}
			else
			{
				currentlyEnteredAssemblageNid = null;
				optionsContentVBox.getChildren().remove(searchInColumnsHolder);
				searchInColumnsHolder.getChildren().clear();
			}
			
		});
		
		searchLimit.setConverter(new StringConverter<Integer>()
		{
			@Override
			public String toString(Integer object)
			{
				return object == Integer.MAX_VALUE ? "No Limit" : object.toString(); 
			}

			@Override
			public Integer fromString(String string)
			{
				// not needed
				return null;
			}
		});
		
		searchLimit.getItems().add(100);
		searchLimit.getItems().add(500);
		searchLimit.getItems().add(1000);
		searchLimit.getItems().add(10000);
		searchLimit.getItems().add(100000);
		searchLimit.getItems().add(Integer.MAX_VALUE);
		searchLimit.getSelectionModel().select(0);
		
		searchInRefexHBox.getChildren().add(searchInRefex.getNode());
		HBox.setHgrow(searchInRefex.getNode(), Priority.ALWAYS);
		
		searchInColumnsHolder.setHgap(10);
		searchInColumnsHolder.setVgap(5.0);

		searchResults.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		searchResults.setCellFactory(new Callback<ListView<CompositeSearchResult>, ListCell<CompositeSearchResult>>()
		{
			@Override
			public ListCell<CompositeSearchResult> call(ListView<CompositeSearchResult> arg0)
			{
				return new ListCell<CompositeSearchResult>()
				{
					@Override
					protected void updateItem(final CompositeSearchResult item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!empty)
						{
							VBox box = new VBox();
							box.setFillWidth(true);
							
							final ConceptVersionBI wbConcept = item.getContainingConcept();
							String preferredText = (wbConcept != null ? WBUtility.getDescription(wbConcept) : "error - see log");
							
						
							if (item.getMatchingComponents().iterator().next() instanceof RefexDynamicVersionBI<?>)
							{
								HBox hb = new HBox();
								Label concept = new Label("Referenced Concept");
								concept.getStyleClass().add("boldLabel");
								hb.getChildren().add(concept);
								hb.getChildren().add(new Label("  " + preferredText));
								
								box.getChildren().add(hb);
							
								for (ComponentVersionBI c : item.getMatchingComponents())
								{
									if (c instanceof RefexDynamicVersionBI<?>)
									{
										RefexDynamicVersionBI<?> rv = (RefexDynamicVersionBI<?>)c;
										HBox assemblageConBox = new HBox();
										Label assemblageCon = new Label("Assemblage Concept");
										assemblageCon.getStyleClass().add("boldLabel");
										HBox.setMargin(assemblageCon, new Insets(0.0, 0.0, 0.0, 10.0));
										assemblageConBox.getChildren().add(assemblageCon);
										assemblageConBox.getChildren().add(new Label("  " + WBUtility.getDescription(rv.getAssemblageNid())));
										box.getChildren().add(assemblageConBox);

										Label attachedData = new Label("Attached Data");
										attachedData.getStyleClass().add("boldLabel");
										VBox.setMargin(attachedData, new Insets(0.0, 0.0, 0.0, 10.0));
										box.getChildren().add(attachedData);
										
										try
										{
											RefexDynamicColumnInfo[] ci = rv.getRefexDynamicUsageDescription().getColumnInfo();
											int i = 0;
											
											for (RefexDynamicDataBI data : rv.getData())
											{
												Label l = new Label();
												if (data == null)  //might be an unset column, if the col is optional
												{
													continue;
												}
												if (RefexDynamicDataType.BYTEARRAY == data.getRefexDataType())
												{
													l.setText(ci[i].getColumnName() +  " - [Binary]");
												}
												else
												{
													l.setText(ci[i].getColumnName() + " - " + data.getDataObject().toString());
												}
												VBox.setMargin(l, new Insets(0.0, 0.0, 0.0, 20.0));
												box.getChildren().add(l);
												i++;
											}
										}
										catch (Exception e)
										{
											LOG.error("Unexpected error reading refex info", e);
										}
									}
								}
							}
							else
							{
								Label concept = new Label(preferredText);
								concept.getStyleClass().add("boldLabel");
								box.getChildren().add(concept);
								for (String s : item.getMatchingStrings())
								{
									if (s.equals(preferredText))
									{
										continue;
									}
									Label matchString = new Label(s);
									VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
									box.getChildren().add(matchString);
								}
							}
							setGraphic(box);

							// Also show concept details on double-click.
							setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent mouseEvent)
								{
									if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
									{
										if (mouseEvent.getClickCount() == 2)
										{
											AppContext.getCommonDialogs().showConceptDialog(wbConcept.getUUIDs().get(0));
										}
									}
								}
							});

							ContextMenu cm = new ContextMenu();
							CommonMenusDataProvider dp = new CommonMenusDataProvider()
							{
								@Override
								public String[] getStrings()
								{
									List<String> items = new ArrayList<>();
									for (CompositeSearchResult currentItem : searchResults.getSelectionModel().getSelectedItems())
									{
										//items.add(source.getTableColumn().getCellData(index).toString());
										final ConceptVersionBI currentWbConcept = currentItem.getContainingConcept();
										final String currentPreferredText = (currentWbConcept != null ? WBUtility.getDescription(currentWbConcept) : "error - see log");

										items.add(currentPreferredText);
									}

									String[] itemArray = items.toArray(new String[items.size()]);

									return itemArray;
								}
							};
							CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider()
							{
								@Override
								public Set<Integer> getNIds()
								{
									Set<Integer> nids = new HashSet<>();

									for (CompositeSearchResult r : searchResults.getSelectionModel().getSelectedItems())
									{
										nids.add(r.getContainingConcept().getNid());
									}
									return nids;
								}
							};
							CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();

							CommonMenus.addCommonMenus(cm, menuBuilder, dp, nidProvider);

							setContextMenu(cm);
						}
						else
						{
							setText("");
							setGraphic(null);
						}
					}
				};
			}
		});

		AppContext.getService(DragRegistry.class).setupDragOnly(searchResults, new SingleConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				CompositeSearchResult dragItem = searchResults.getSelectionModel().getSelectedItem();
				if (dragItem != null)
				{
					return dragItem.getContainingConcept() + "";
				}
				return null;
			}
		});

		final ValidBooleanBinding searchTextValid = new ValidBooleanBinding()
		{
			{
				bind(searchText.textProperty(), searchIn.valueProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				if ((searchIn.getValue() == SearchInOptions.Refexes && searchText.getText().length() > 0) || searchText.getText().length() > 1)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};
		
		searchProgress.visibleProperty().bind(searchRunning);
		searchButton.disableProperty().bind(searchTextValid.not().or(searchInRefex.isValid().not()));

		// Perform search or cancel when button pressed.
		searchButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (searchRunning.get() && ssh != null)
				{
					ssh.cancel();
				}
				else
				{
					search();
				}
			}
		});

		// Change button text while search running.
		searchRunning.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (searchRunning.get())
				{
					searchButton.setText("Cancel");
				}
				else
				{
					searchButton.setText("Search");
				}

			}
		});

		// Perform search on Enter keypress.
		searchText.setOnAction(e -> 
		{
			if (searchTextValid.getValue() && !searchRunning.get())
			{
				search();
			}
		});

		// Search text must be greater than one character for description searches
		searchText.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> 
		{
			
		});
	}

	public BorderPane getRoot()
	{
		//delay this
		if (dynamicRefexList_.size() == 0)
		{
			populateDynamicRefexList();
		}
		return borderPane;
	}

	@Override
	public void taskComplete(long taskStartTime, Integer taskId)
	{

		// Run on JavaFX thread.
		Platform.runLater(() -> 
		{
			try
			{
				if (!ssh.isCancelled())
				{
					searchResults.getItems().addAll(ssh.getResults());
					long time = System.currentTimeMillis() - ssh.getSearchStartTime();
					float inSeconds = (float)time / 1000f;
					inSeconds = ((float)((int)(inSeconds * 100f)) / 100f);
					statusLabel.setText(ssh.getHitCount() + " in " + inSeconds + " seconds");
				}
				else
				{
					statusLabel.setText("Search Cancelled");
				}
			}
			catch (Exception ex)
			{
				String title = "Unexpected Search Error";
				LOG.error(title, ex);
				AppContext.getCommonDialogs().showErrorDialog(title, "There was an unexpected error running the search", ex.toString());
				searchResults.getItems().clear();
				statusLabel.setText("Search Failed");
			}
			finally
			{
				searchRunning.set(false);
			}
		});
	}
	
	private Integer[] getSearchColumns()
	{
		if (searchInColumnsHolder.getChildren().size() > 1)
		{
			ArrayList<Integer> result = new ArrayList<>();
			int deselectedCount = 0;
			for (int i = 1; i < searchInColumnsHolder.getChildren().size(); i++)
			{
				CheckBox cb = ((CheckBox)((StackPane)searchInColumnsHolder.getChildren().get(i)).getChildren().get(0));
				if (cb.isSelected())
				{
					result.add(i - 1);
				}
				else if (!cb.isDisable())
				{
					deselectedCount++;
				}
			}
			//If they didn't uncheck any, its more efficient to query without the column filter.
			return deselectedCount == 0 ? null : result.toArray(new Integer[0]);
		}
		return null;
	}

	private synchronized void search()
	{
		try
		{
			// Sanity check if search already running.
			if (searchRunning.get())
			{
				return;
			}
	
			searchRunning.set(true);
			searchResults.getItems().clear();
			// we get called back when the results are ready.
			
			if (searchIn.getValue() == SearchInOptions.Descriptions)
			{
				ssh = SearchHandler.descriptionSearch(searchText.getText(), searchLimit.getValue(), this, true);
			}
			else
			{
				String searchString = searchText.getText().trim();
				try
				{
					RefexDynamicDataBI data = NumberUtilities.wrapIntoRefexHolder(NumberUtilities.parseNumber(searchString));
					LOG.debug("Doing a refex search with a numeric value");
					ssh = SearchHandler.dynamicRefexSearch((indexer) ->
					{
						try
						{
							return indexer.query(data, currentlyEnteredAssemblageNid, false, getSearchColumns(), searchLimit.getValue(), null);
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					}, this, null, null, null, true);
				}
				catch (NumberFormatException e)
				{
					//Not a number...  is it a valid interval?
					try
					{
						Interval interval = new Interval(searchString);
						LOG.debug("Doing a refex search with an interval value");
						ssh = SearchHandler.dynamicRefexSearch((indexer) ->
						{
							try
							{
								return indexer.queryNumericRange(NumberUtilities.wrapIntoRefexHolder(interval.getLeft()), interval.isLeftInclusive(), 
										NumberUtilities.wrapIntoRefexHolder(interval.getRight()), interval.isRightInclusive(), 
										currentlyEnteredAssemblageNid, getSearchColumns(), searchLimit.getValue(), null);
							}
							catch (Exception e1)
							{
								throw new RuntimeException(e1);
							}
						}, this, null, null, null, true);
					}
					catch (NumberFormatException e1) 
					{
						//run it as a string search
						LOG.debug("Doing a refex search as a string search");
						ssh = SearchHandler.dynamicRefexSearch((indexer) ->
						{
							try
							{
								return indexer.query(new RefexDynamicString(searchText.getText()), currentlyEnteredAssemblageNid, false, 
										getSearchColumns(), searchLimit.getValue(), null);
							}
							catch (Exception e2)
							{
								throw new RuntimeException(e2);
							}
						}, this, null, null, null, true);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Search imploded unexpectedly...", e);
			ssh = null;  //force a null ptr in taskComplete, so an error is displayed.
			taskComplete(0, null);
		}
	}
	
	//TODO a listener to trigger this after a user makes a new one...
	private void populateDynamicRefexList()
	{
		Task<Void> t = new Task<Void>()
		{
			HashSet<SimpleDisplayConcept> dynamicRefexAssemblages = new HashSet<>();

			@Override
			protected Void call() throws Exception
			{
				dynamicRefexAssemblages = new HashSet<>();
				
				LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
				List<SearchResult> refexes = indexer.queryAssemblageUsage(RefexDynamic.REFEX_DYNAMIC_DEFINITION_DESCRIPTION.getNid(), 1000, null);
				for (SearchResult sr : refexes)
				{
					RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
					//These are nested refex references - it returns a description component - concept we want is the parent of that.
					dynamicRefexAssemblages.add(new SimpleDisplayConcept(
							ExtendedAppContext.getDataStore().getComponent(rc.getReferencedComponentNid()).getEnclosingConcept(),
							null));
					
				}
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				dynamicRefexList_.clear();
				dynamicRefexList_.addAll(dynamicRefexAssemblages);
				dynamicRefexList_.sort(new Comparator<SimpleDisplayConcept>()
				{
					@Override
					public int compare(SimpleDisplayConcept o1, SimpleDisplayConcept o2)
					{
						return o1.getDescription().compareToIgnoreCase(o2.getDescription());
					}
				});
			}
		};
		
		Utility.execute(t);
	}
}
