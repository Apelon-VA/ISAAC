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
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.interfaces.gui.views.ListBatchViewI;
import gov.va.isaac.interfaces.workflow.ConceptWorkflowServiceI;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.search.DescriptionAnalogBITypeComparator;
import gov.va.isaac.search.SearchBuilder;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class EnhancedSearchViewController implements TaskCompleteCallback {
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewController.class);

	enum Tasks {
		SEARCH,
		WORKFLOW_EXPORT
	}

	enum AggregationType {
		CONCEPT("Concept"),
		DESCRIPTION("Description");
		
		private final String display;
		
		private AggregationType(String display) {
			this.display = display;
		}
		
		public String toString() {
			return display;
		}
	}

	@FXML private Button searchButton;
	@FXML private TextField searchText;
	@FXML private Label totalResultsDisplayedLabel;
	@FXML private Pane pane;
	@FXML private ComboBox<AggregationType> aggregationTypeComboBox;
	@FXML private TableView<CompositeSearchResult> searchResultsTable;
	@FXML private Button exportSearchResultsAsTabDelimitedValuesButton;
	@FXML private Button exportSearchResultsToListBatchViewButton;
	@FXML private Button exportSearchResultsToWorkflowButton;
    @FXML private ProgressIndicator searchProgress;
    @FXML private Label totalResultsSelectedLabel;
    
    private final BooleanProperty searchRunning = new SimpleBooleanProperty(false);
    private SearchHandle ssh = null;

	private Window windowForTableViewExportDialog;

	ConceptWorkflowServiceI conceptWorkflowService;
	
	public static EnhancedSearchViewController init() throws IOException {
		// Load FXML
		URL resource = EnhancedSearchViewController.class.getResource("EnhancedSearchView.fxml");
		LOG.debug("FXML for " + EnhancedSearchViewController.class + ": " + resource);
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}
	
	@FXML
	public void initialize() {
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert exportSearchResultsToListBatchViewButton != null : "fx:id=\"exportSearchResultsToListBatchViewButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert exportSearchResultsToWorkflowButton != null : "fx:id=\"exportSearchResultsToWorkflowButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert totalResultsSelectedLabel != null : "fx:id=\"totalResultsSelectedLabel\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		String styleSheet = EnhancedSearchViewController.class.getResource("/isaac-shared-styles.css").toString();
		if (! pane.getStylesheets().contains(styleSheet)) {
			pane.getStylesheets().add(styleSheet);
		}

		initializeWorkflowServices();
				 
        final BooleanProperty searchTextValid = new SimpleBooleanProperty(false);
        searchProgress.visibleProperty().bind(searchRunning);
        searchButton.disableProperty().bind(searchTextValid.not());

		// Search results table
		initializeSearchResultsTable();
		initializeAggregationTypeComboBox();

		exportSearchResultsAsTabDelimitedValuesButton.setOnAction((e) -> exportSearchResultsAsTabDelimitedValues());
		exportSearchResultsToListBatchViewButton.setOnAction((e) -> exportSearchResultsToListBatchView());
		exportSearchResultsToWorkflowButton.setOnAction((e) -> exportSearchResultsToWorkflow());

		searchButton.setOnAction((action) -> {
			 if (searchRunning.get() && ssh != null) {
                 ssh.cancel();
             } else {
                 search();
             }
		});
		searchRunning.addListener((observable, oldValue, newValue) -> {
			if (searchRunning.get()) {
				searchButton.setText("Cancel");
			} else {
				searchButton.setText("Search");
			}

		});

		// This code only for searchText
		searchText.setPromptText("Enter search text");
		searchText.setOnAction((e) -> {
			if (searchTextValid.getValue() && ! searchRunning.get()) {
				search();
			}
		});
		
		// Search text must be greater than one character.
		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.length() > 1) {
				searchTextValid.set(true);
			} else {
				searchTextValid.set(false);
			}
		});
	}
	
	private void refreshTotalResultsSelectedLabel() {
		int numSelected = searchResultsTable.getSelectionModel().getSelectedIndices().size();
		if (searchResultsTable.getItems().size() == 0) {
			totalResultsSelectedLabel.setVisible(false);
		}

		switch (numSelected) {
		case 1:
			totalResultsSelectedLabel.setText(numSelected + " result selected");
			break;

		case 0:
		default:
			totalResultsSelectedLabel.setText(numSelected + " results selected");
			break;
		}

		if (searchResultsTable.getItems().size() > 0) {
			totalResultsSelectedLabel.setVisible(true);
		}
	}
	
	// TODO: This doesn't make sense here.  Should be exported to listView, then Workflow
	private void exportSearchResultsToWorkflow() {
		initializeWorkflowServices();

		// Use HashSet to ensure that only one workflow is created for each concept
		if (searchResultsTable.getItems().size() > 0) {
			conceptWorkflowService.synchronizeWithRemote();
		}
		
		Set<Integer> concepts = new HashSet<>();
		for (CompositeSearchResult result : searchResultsTable.getItems()) {
			if (! concepts.contains(result.getConceptNid())) {
				concepts.add(result.getConceptNid());
				
				exportSearchResultToWorkflow(result.getConcept());
			}
		}

		if (searchResultsTable.getItems().size() > 0) {
			conceptWorkflowService.synchronizeWithRemote();
		}
	}

	// TODO: this should be invoked by context menu
	private void exportSearchResultToWorkflow(ConceptVersionBI conceptVersion) {
		initializeWorkflowServices();
		
		// TODO: eliminate hard-coding of processName "terminology-authoring.test1"
		final String processName = "terminology-authoring.test1";
		// TODO: eliminate hard-coding of userName
		final String userName = "alejandro";
		String preferredDescription = null;
		try {
			preferredDescription = conceptVersion.getPreferredDescription().getText();
		} catch (IOException | ContradictionException e1) {
			String title = "Failed creating new concept workflow";
			String msg = "Unexpected error calling getPreferredDescription() of conceptVersion (nid=" + conceptVersion.getConceptNid() + ", uuid=" + conceptVersion.getPrimordialUuid().toString() + "): caught " + e1.getClass().getName();
			LOG.error(title, e1);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e1.getMessage());
			e1.printStackTrace();
		}
		
		LOG.debug("Invoking createNewConceptWorkflowRequest(preferredDescription=\"" + preferredDescription + "\", conceptUuid=\"" + conceptVersion.getPrimordialUuid().toString() + "\", user=\"" + userName + "\", processName=\"" + processName + "\")");
		ProcessInstanceCreationRequestI createdRequest = conceptWorkflowService.createNewConceptWorkflowRequest(preferredDescription, conceptVersion.getPrimordialUuid(), userName, processName);
		LOG.debug("Created ProcessInstanceCreationRequestI: " + createdRequest);
	}
	
	private void exportSearchResultsToListBatchView() {
		ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);
		
		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);
		
		List<Integer> nids = new ArrayList<>();
		for (CompositeSearchResult result : searchResultsTable.getItems()) {
			nids.add(result.getConceptNid());
		}
		
		lv.addConcepts(nids);
	}

	protected void windowForTableViewExportDialog(Window window) {
		this.windowForTableViewExportDialog = window;
	}
	
	private void refreshTotalResultsDisplayedLabel() {
		if (searchResultsTable.getItems().size() == 1) {
			totalResultsDisplayedLabel.setText(searchResultsTable.getItems().size() + " entry displayed");
		} else {
			totalResultsDisplayedLabel.setText(searchResultsTable.getItems().size() + " entries displayed");
		}
		
		refreshTotalResultsSelectedLabel();
	}
	
	@Override
	public void taskComplete(long taskStartTime, Integer taskId) {
		if (taskId == Tasks.SEARCH.ordinal()) {
			// Run on JavaFX thread.
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						if (! ssh.isCancelled()) {
							searchResultsTable.getItems().addAll(ssh.getResults());

							refreshTotalResultsDisplayedLabel();
						}
					} catch (Exception ex) {
						String title = "Unexpected Search Error";
						LOG.error(title, ex);
						AppContext.getCommonDialogs().showErrorDialog(title,
								"There was an unexpected error running the search",
								ex.toString());
						searchResultsTable.getItems().clear();
						refreshTotalResultsDisplayedLabel();
					} finally {
						searchRunning.set(false);
					}
				}
			});
		}
    }

	// MyTableCellCallback adds hooks for double-click and/or other mouse actions to String cells
	private class MyTableCellCallback<T> implements Callback<TableColumn<CompositeSearchResult, T>, TableCell<CompositeSearchResult, T>> {
		/* (non-Javadoc)
		 * @see javafx.util.Callback#call(java.lang.Object)
		 */
		public TableCell<CompositeSearchResult, T> createNewCell() {
			TableCell<CompositeSearchResult, T> cell = new TableCell<CompositeSearchResult, T>() {
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

			return cell;
		}
		
		// This method can be overridden to customize cells
		public TableCell<CompositeSearchResult, T> modifyCell(TableCell<CompositeSearchResult, T> cell) {
//			// This is an example of an EventFilter			
//			cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
//				@Override
//				public void handle(MouseEvent event) {
//					TableCell<?, ?> c = (TableCell<?,?>) event.getSource();
//					
//					if (event.getClickCount() == 1) {
//						LOG.debug(event.getButton() + " single clicked. Cell text: " + c.getText());
//					} else if (event.getClickCount() > 1) {
//						LOG.debug(event.getButton() + " double clicked. Cell text: " + c.getText());
//					}
//				}
//			});

			return cell;
		}
		
		@Override
		public TableCell<CompositeSearchResult, T> call(
				TableColumn<CompositeSearchResult, T> param) {
			TableCell<CompositeSearchResult, T> newCell = createNewCell();
			newCell.setUserData(param.getCellData(newCell.getIndex()));
			
			// This event filter adds a concept-specific context menu to all cells based on underlying concept
			// It is in this method because it should be common to all cells, even those overriding modifyCell()
			newCell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					refreshTotalResultsSelectedLabel();
					
					if (event.getButton() == MouseButton.SECONDARY) {
						TableCell<CompositeSearchResult, T> c = (TableCell<CompositeSearchResult, T>) event.getSource();
						if (c != null && c.getIndex() < searchResultsTable.getItems().size()) {
							CommonMenus.DataProvider dp = new CommonMenus.DataProvider() {
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
							CommonMenus.NIdProvider nidProvider = new CommonMenus.NIdProvider() {
								@Override
								public Set<Integer> getNIds() {
									Set<Integer> nids = new HashSet<>();
									
									for (CompositeSearchResult r : (ObservableList<CompositeSearchResult>)c.getTableView().getSelectionModel().getSelectedItems()) {
										nids.add(r.getConceptNid());
									}
									
									// TODO: determine why we are getting here multiple (2 or 3) times for each selection
									//System.out.println("Selected nids: " + Arrays.toString(nids.toArray()));

									return nids;
								}
							};

							ContextMenu cm = new ContextMenu();
							CommonMenus.addCommonMenus(cm, dp, nidProvider);

							c.setContextMenu(cm);
						}
					}
				}
			});
				
			return modifyCell(newCell);
		}	
	}

//	private void populateConceptSearchResultsTableFromDescriptionSearchResultsTable() {		
//		Map<Integer, CompositeSearchResult> concepts = new HashMap<>();
//		for (CompositeSearchResult result : descriptionSearchResultsTable.getItems()) {
//			if (concepts.get(result.getConceptNid()) == null) {
//				concepts.put(result.getConceptNid(), new CompositeSearchResult(result));
//			} else if (concepts.get(result.getConceptNid()).getBestScore() < result.getBestScore()) {
//				CompositeSearchResult copyOfResult = new CompositeSearchResult(result);
//				copyOfResult.getMatchStrings().addAll(concepts.get(result.getConceptNid()).getMatchStrings());
//				copyOfResult.getComponents().addAll(concepts.get(result.getConceptNid()).getComponents());
//				concepts.put(result.getConceptNid(), copyOfResult);
//			} else {
//				concepts.get(result.getConceptNid()).getComponents().addAll(result.getComponents());
//			}
//		}
//
//		List<CompositeSearchResult> conceptResults = new ArrayList<>(concepts.values());
//		Collections.sort(conceptResults, new CompositeSearchResultComparator());
//		conceptSearchResultsTable.getItems().setAll(conceptResults);
//	}
	private void initializeSearchResultsTable() {
		assert searchResultsTable != null : "fx:id=\"searchResultsTable\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Enable selection of multiple rows.  Context menu handlers are coded to send collections.
		searchResultsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		// Clear underlying data structure
		searchResultsTable.getItems().clear();
		
		// Enable optional menu to make visible columns invisible and currently invisible columns visible
		searchResultsTable.setTableMenuButtonVisible(true);
		
		// Disable editing of table data
		searchResultsTable.setEditable(false);

		// Match quality between 0 and 1
		TableColumn<CompositeSearchResult, Number> scoreCol = new TableColumn<>("Score");
		scoreCol.setCellValueFactory((param) -> new SimpleDoubleProperty(param.getValue().getBestScore()));
		scoreCol.setCellFactory(new MyTableCellCallback<Number>());
		scoreCol.setCellFactory(new MyTableCellCallback<Number>() {
			public TableCell<CompositeSearchResult, Number> createNewCell() {

				final DecimalFormat fmt = new DecimalFormat("#.####");
				
				TableCell<CompositeSearchResult, Number> cell = new TableCell<CompositeSearchResult, Number>() {
					@Override
					public void updateItem(Number item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty ? null : getString());
						setGraphic(null);
					}

					private String getString() {	
						fmt.setRoundingMode(RoundingMode.HALF_UP);
						return getItem() == null ? "" : fmt.format(getItem().doubleValue());
					}
				};
				
				cell.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();
				
						if (c != null && c.getItem() != null) {
							Tooltip tooltip = new Tooltip(c.getItem().toString());
							Tooltip.install(cell, tooltip);
						}
					}
				});
				
				return cell;
			}
		});
		
		// Active status
		TableColumn<CompositeSearchResult, String> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getConcept().getStatus().toString().trim()));
		statusCol.setCellFactory(new MyTableCellCallback<String>());

		// numMatchesCol only meaningful for AggregationType CONCEPT
		// When AggregationTyppe is DESCRIPTION should always be 1
		TableColumn<CompositeSearchResult, Number> numMatchesCol = new TableColumn<>("Matches");
		numMatchesCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getMatchingDescriptionComponents().size()));
		numMatchesCol.setCellFactory(new MyTableCellCallback<Number>() {
			@Override
			public TableCell<CompositeSearchResult, Number> modifyCell(TableCell<CompositeSearchResult, Number> cell) {
				
				cell.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TableCell<?, ?> c = (TableCell<?,?>) event.getSource();

						if (c != null && c.getIndex() < searchResultsTable.getItems().size()) {
							CompositeSearchResult result = searchResultsTable.getItems().get(c.getIndex());
							StringBuilder buffer = new StringBuilder();
							String fsn = null;
							try {
								fsn = result.getConcept().getFullySpecifiedDescription().getText().trim();
							} catch (IOException | ContradictionException e) {
								String title = "Failed getting FSN";
								String msg = "Failed getting fully specified description";
								LOG.error(title);
								AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage());
								e.printStackTrace();
							}

							List<DescriptionAnalogBI<?>> matchingDescComponents = new ArrayList<DescriptionAnalogBI<?>>(result.getMatchingDescriptionComponents());
							Collections.sort(matchingDescComponents, new DescriptionAnalogBITypeComparator());
							for (DescriptionAnalogBI<?> descComp : matchingDescComponents) {
								String type = WBUtility.getConPrefTerm(descComp.getTypeNid());
								buffer.append(type + ": " + descComp.getText() + "\n");
							}
							Tooltip tooltip = new Tooltip("Matching descriptions for \"" + fsn + "\":\n" + buffer.toString());

							Tooltip.install(cell, tooltip);
						}
					}
				});
				
				return cell;
			}
		});

		// Preferred term
		TableColumn<CompositeSearchResult, String> preferredTermCol = new TableColumn<>("Term");
		preferredTermCol.setCellFactory(new MyTableCellCallback<String>());
		preferredTermCol.setCellValueFactory((param) -> {
			try {
				return new SimpleStringProperty(param.getValue().getConcept().getPreferredDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting preferred description";
				String msg = "Failed getting preferred description";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage());
				e.printStackTrace();
				return null;
			}
		});

		// Fully Specified Name
		TableColumn<CompositeSearchResult, String> fsnCol = new TableColumn<>("FSN");
		fsnCol.setCellFactory(new MyTableCellCallback<String>());
		fsnCol.setCellValueFactory((param) -> {
			try {
				return new SimpleStringProperty(param.getValue().getConcept().getFullySpecifiedDescription().getText().trim());
			} catch (IOException | ContradictionException e) {
				String title = "Failed getting FSN";
				String msg = "Failed getting fully specified description";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Encountered " + e.getClass().getName() + ": " + e.getLocalizedMessage());
				e.printStackTrace();
				return null;
			}
		});

		// Matching description text.
		// If AggregationType is CONCEPT then arbitrarily picks first matching description
		TableColumn<CompositeSearchResult, String> matchingTextCol = new TableColumn<>("Text");
		matchingTextCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getMatchStrings().iterator().next().trim()));
		matchingTextCol.setCellFactory(new MyTableCellCallback<String>());

		// matchingDescTypeCol is string value type of matching description term displayed
		// Only meaningful for AggregationType DESCRIPTION
		// When AggregationTyppe is CONCEPT should always be type of first match
		TableColumn<CompositeSearchResult, String> matchingDescTypeCol = new TableColumn<>("Type");
		matchingDescTypeCol.setCellValueFactory((param) -> new SimpleStringProperty(WBUtility.getConPrefTerm(param.getValue().getMatchingDescriptionComponents().iterator().next().getTypeNid())));
		matchingDescTypeCol.setCellFactory(new MyTableCellCallback<String>());
		// matchingDescTypeCol defaults to invisible for anything but DESCRIPTION
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() != AggregationType.DESCRIPTION) {
			matchingDescTypeCol.setVisible(false);
		}

		// NID set to invisible because largely for debugging purposes only
		TableColumn<CompositeSearchResult, Number> nidCol = new TableColumn<>("NID");
		nidCol.setCellValueFactory((param) -> new SimpleIntegerProperty(param.getValue().getConcept().getNid()));
		nidCol.setCellFactory(new MyTableCellCallback<Number>());
		nidCol.setVisible(false);

		// UUID set to invisible because largely for debugging purposes only
		TableColumn<CompositeSearchResult, String> uuIdCol = new TableColumn<>("UUID");
		uuIdCol.setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getConcept().getPrimordialUuid().toString().trim()));
		uuIdCol.setVisible(false);
		uuIdCol.setCellFactory(new MyTableCellCallback<String>());

		// Optional SCT ID
		TableColumn<CompositeSearchResult, String> sctIdCol = new TableColumn<>("SCTID");
		sctIdCol.setCellValueFactory((param) -> new SimpleStringProperty(ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(param.getValue().getConcept())).trim()));
		sctIdCol.setCellFactory(new MyTableCellCallback<String>());

		searchResultsTable.getColumns().clear();
		
		// Default column ordering. May be changed within session
		searchResultsTable.getColumns().add(scoreCol);
		searchResultsTable.getColumns().add(statusCol);
		searchResultsTable.getColumns().add(fsnCol);
		searchResultsTable.getColumns().add(preferredTermCol);
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() == AggregationType.CONCEPT) {
			searchResultsTable.getColumns().add(numMatchesCol);
		}
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() == AggregationType.DESCRIPTION) {
			searchResultsTable.getColumns().add(matchingTextCol);
			searchResultsTable.getColumns().add(matchingDescTypeCol);
		}
		searchResultsTable.getColumns().add(sctIdCol);
		searchResultsTable.getColumns().add(uuIdCol);
		searchResultsTable.getColumns().add(nidCol);

		AppContext.getService(DragRegistry.class).setupDragOnly(searchResultsTable, new SingleConceptIdProvider() {
            @Override
            public String getConceptId()
            {
                CompositeSearchResult dragItem = searchResultsTable.getSelectionModel().getSelectedItem();
                if (dragItem != null)
                {
                	LOG.debug("Dragging concept id " + dragItem.getConceptNid());
                    return dragItem.getConceptNid() + "";
                }
                return null;
            }
        });
		
		refreshTotalResultsDisplayedLabel();
	}
	
	private void initializeWorkflowServices() {
		if (conceptWorkflowService == null) {
			conceptWorkflowService = AppContext.getService(ConceptWorkflowServiceI.class);
		}
		
		assert conceptWorkflowService != null;
	}
	
	private void initializeAggregationTypeComboBox() {
		assert aggregationTypeComboBox != null : "fx:id=\"aggregationTypeComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Force single selection
		aggregationTypeComboBox.getSelectionModel().selectFirst();
		aggregationTypeComboBox.setCellFactory((p) -> {
			final ListCell<AggregationType> cell = new ListCell<AggregationType>() {
				@Override
				protected void updateItem(AggregationType a, boolean bln) {
					super.updateItem(a, bln);

					if(a != null){
						setText(a.toString() + " search");
					}else{
						setText(null);
					}
				}
			};

			return cell;
		});
		aggregationTypeComboBox.setButtonCell(new ListCell<AggregationType>() {
			@Override
			protected void updateItem(AggregationType t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.toString() + " search");
				}
			}
		});
		aggregationTypeComboBox.setOnAction((event) -> {
			LOG.trace("aggregationTypeComboBox event (selected: " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			initializeSearchResultsTable();
		});

        aggregationTypeComboBox.setItems(FXCollections.observableArrayList(AggregationType.values()));
        aggregationTypeComboBox.getSelectionModel().select(AggregationType.CONCEPT);
	}
	
	private synchronized void search() {
        // Sanity check if search already running.
        if (searchRunning.get()) {
            return;
        }

        searchRunning.set(true);
        searchResultsTable.getItems().clear();

		refreshTotalResultsDisplayedLabel();
		
        // "we get called back when the results are ready."
        switch (aggregationTypeComboBox.getSelectionModel().getSelectedItem()) {
        case  CONCEPT:
        {
        	SearchBuilder builder = SearchBuilder.conceptDescriptionSearchBuilder(searchText.getText());
        	builder.setCallback(this);
        	builder.setTaskId(Tasks.SEARCH.ordinal());
            ssh = SearchHandler.doConceptSearch(builder);
            break;
        }
        case DESCRIPTION:
        {
        	SearchBuilder builder = SearchBuilder.descriptionSearchBuilder(searchText.getText());
        	builder.setCallback(this);
        	builder.setComparator(new CompositeSearchResultComparator());
        	builder.setTaskId(Tasks.SEARCH.ordinal());
            ssh = SearchHandler.doDescriptionSearch(builder);
            break;
        }
            
            default:
    			String title = "Unsupported Aggregation Type";
    			String msg = "Aggregation Type " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + " not supported";
    			LOG.error(title);
    			AppContext.getCommonDialogs().showErrorDialog(title, msg, "Aggregation Type must be one of " + Arrays.toString(aggregationTypeComboBox.getItems().toArray()));

    			break;
        }
    }

	public Pane getRoot() {
		return pane;
	}

	interface ColumnValueExtractor {
		String extract(TableColumn<CompositeSearchResult, ?> col);
	}
	private static String getTableViewRow(TableView<CompositeSearchResult> table, String delimiter, String lineTerminator, ColumnValueExtractor extractor) {
    	ObservableList<TableColumn<CompositeSearchResult, ?>> columns = table.getColumns();
		StringBuilder row = new StringBuilder();

		for (int colIndex = 0; colIndex < columns.size(); ++colIndex) {
			TableColumn<CompositeSearchResult, ?> col = columns.get(colIndex);
			if (! col.isVisible()) {
				// Ensure that newline is written even if column is not
				if (colIndex == (columns.size() - 1) && lineTerminator != null) {
					// Append newline to row
     				row.append(lineTerminator);
				}

				continue;
			}
			// Extract text or data from column and append to row
			row.append(extractor.extract(col));
			if (colIndex < (columns.size() - 1)) {
				if (delimiter != null) {
					// Ensure that delimiter is written only if there are remaining visible columns to be written
					boolean hasMoreVisibleCols = false;
					for (int remainingColsIndex = colIndex + 1; remainingColsIndex < columns.size(); ++remainingColsIndex) {
						if (columns.get(remainingColsIndex).isVisible()) {
							hasMoreVisibleCols = true;
							break;
						}
					}
					if (hasMoreVisibleCols) {
						// Append delimiter to row
						row.append(delimiter);
					}
				}
			} else if (colIndex == (columns.size() - 1) && lineTerminator != null) {
				// Append newline to row
				row.append(lineTerminator);
			}
		}
		
		return row.toString();
	}

	private void exportSearchResultsAsTabDelimitedValues() {
		FileChooser fileChooser = new FileChooser();
		final String delimiter = "\t";
		final String newLine = "\n";
		
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(windowForTableViewExportDialog);

		//String tempDir = System.getenv("TEMP");
		//File file = new File(tempDir + File.separator + "EnhanceSearchViewControllerTableViewData.csv");

        if (file == null) {
        	LOG.warn("FileChooser returned null export file.  Cancel possibly requested.");
        } else { // if (file != null)
        	LOG.debug("Writing TableView data to file \"" + file.getAbsolutePath() + "\"...");

        	Writer writer = null;
        	try {
        		writer = new BufferedWriter(new FileWriter(file));
        		String headerRow = getTableViewRow(searchResultsTable, delimiter, newLine, (col) -> col.getText());

        		LOG.trace(headerRow);
        		writer.write(headerRow);

        		for (int rowIndex = 0; rowIndex < searchResultsTable.getItems().size(); ++rowIndex) {
        			final int finalRowIndex = rowIndex;
        			String dataRow = getTableViewRow(searchResultsTable, delimiter, newLine, (col) -> col.getCellObservableValue(finalRowIndex).getValue().toString());
        			LOG.trace(dataRow);
        			writer.write(dataRow);
        		}

        		LOG.debug("Wrote " + searchResultsTable.getItems().size() + " rows of TableView data to file \"" + file.getAbsolutePath() + "\".");
        	} catch (IOException e) {
        		LOG.error("FAILED writing TableView data to file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
        		e.printStackTrace();
        	}
        	finally {
        		try {
        			writer.flush();
        		} catch (IOException e) {
        			LOG.error("FAILED flushing TableView data file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
        			e.printStackTrace();
        		}
        		try {
        			writer.close();
        		} catch (IOException e) {
        			LOG.error("FAILED closing TableView data file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
        			e.printStackTrace();
        		}
        	}
        }
	}
}