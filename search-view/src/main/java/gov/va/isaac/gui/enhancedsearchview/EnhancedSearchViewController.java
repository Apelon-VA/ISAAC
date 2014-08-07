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
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.Filter;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.LuceneFilter;
import gov.va.isaac.gui.enhancedsearchview.SearchViewModel.RegExpFilter;
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
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;

import javax.naming.InvalidNameException;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Search;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
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

	@FXML private HBox maxResultsHBox;
	@FXML private Label maxResultsCustomTextFieldLabel;
	private CustomTextField maxResultsCustomTextField;
	
//	@FXML private Button addLuceneFilterButton;
//	@FXML private Button addRegExpFilterButton;
//	@FXML private ListView<SearchViewModel.Filter> searchFiltersListView;
	
	@FXML private Button saveSearchButton;
	@FXML private ComboBox<SimpleDisplayConcept> savedSearchesComboBox;
	@FXML private Button searchButton;
	
    // TODO: temporarily used along with currentViewCoordinate ViewCoordinate as model for single Lucene Search
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
    @FXML private Button resetDefaultsButton;
    
    private final BooleanProperty searchRunning = new SimpleBooleanProperty(false);
    private SearchHandle ssh = null;
    
    // TODO: temporarily used along with searchText TextField as model for single Lucene Search
    private ViewCoordinate currentSearchViewCoordinate = WBUtility.getViewCoordinate();

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
		assert resetDefaultsButton != null : "fx:id=\"resetDefaultsButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert maxResultsHBox != null : "fx:id=\"maxResultsHBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert maxResultsCustomTextFieldLabel != null : "fx:id=\"maxResultsCustomTextFieldLabel\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert saveSearchButton != null : "fx:id=\"saveSearchButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
//		assert searchFiltersListView != null : "fx:id=\"searchFiltersListView\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
//		assert addLuceneFilterButton != null : "fx:id=\"addLuceneFilterButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
//		assert addRegExpFilterButton != null : "fx:id=\"addRegExpFilterButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert savedSearchesComboBox != null : "fx:id=\"savedSearchesComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		String styleSheet = EnhancedSearchViewController.class.getResource("/isaac-shared-styles.css").toString();
		if (! pane.getStylesheets().contains(styleSheet)) {
			pane.getStylesheets().add(styleSheet);
		}

		initializeWorkflowServices();
	
        final BooleanProperty searchTextValid = new SimpleBooleanProperty(false);
        searchButton.disableProperty().bind(searchTextValid.not());
        searchProgress.visibleProperty().bind(searchRunning);

        maxResultsCustomTextFieldLabel.setText("Max Results");
        maxResultsCustomTextField = new CustomTextField();
        maxResultsCustomTextField.setNumericOnly(true);
        maxResultsCustomTextField.setMaxWidth(50);
        ObservableList<Node> hBoxChildren = maxResultsHBox.getChildren();
        hBoxChildren.add(maxResultsCustomTextField);
        
		// Search results table
		initializeSearchResultsTable();
		initializeAggregationTypeComboBox();
		initializeSavedSearchComboBox();
		
		exportSearchResultsAsTabDelimitedValuesButton.setOnAction((e) -> exportSearchResultsAsTabDelimitedValues());
		exportSearchResultsToListBatchViewButton.setOnAction((e) -> exportSearchResultsToListBatchView());
		exportSearchResultsToWorkflowButton.setOnAction((e) -> exportSearchResultsToWorkflow());
		resetDefaultsButton.setOnAction((e) -> resetDefaults());
		
		saveSearchButton.setOnAction((action) -> {
			saveSearch(); 
			});
		
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
	
	private static void displayDynamicRefex(RefexDynamicVersionBI<?> refex) {
		displayDynamicRefex(refex, 0);
	}
	private static void displayDynamicRefex(RefexDynamicVersionBI<?> refex, int depth) {
		String indent = "";
		
		for (int i = 0; i < depth; ++i) {
			indent += "\t";
		}
		
		RefexDynamicUsageDescription dud = null;
		try {
			dud = refex.getRefexDynamicUsageDescription();
		} catch (IOException | ContradictionException e) {
			LOG.error("Failed executing getRefexDynamicUsageDescription().  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
			
			return;
		}
		RefexDynamicColumnInfo[] colInfo = dud.getColumnInfo();
		RefexDynamicDataBI[] data = refex.getData();
		// TODO: change to use LOG
		System.out.println(indent + "dynamic refex nid=" + refex.getNid() + ", uuid=" + refex.getPrimordialUuid());
		System.out.println(indent + "dynamic refex name=\"" + dud.getRefexName() + "\": " + refex.toUserString() + " with " + colInfo.length + " columns:");
		for (int colIndex = 0; colIndex < colInfo.length; ++colIndex) {
			RefexDynamicColumnInfo currentCol = colInfo[colIndex];
			String name = currentCol.getColumnName();
			RefexDynamicDataType type = currentCol.getColumnDataType();
			UUID colUuid = currentCol.getColumnDescriptionConcept();
			RefexDynamicDataBI colData = data[colIndex];

			// TODO: change to use LOG
			System.out.println(indent + "\t" + "dynamic refex: " + refex.toUserString() + " col #" + colIndex + " (uuid=" + colUuid + ", type=" + type.getDisplayName() + "): " + name + "=" + colData.getDataObject());
			System.out.println();
		}
		
		Collection<? extends RefexDynamicVersionBI<?>> embeddedRefexes = null;
		try {
			embeddedRefexes = refex.getRefexesDynamicActive(WBUtility.getViewCoordinate());

			for (RefexDynamicVersionBI<?> embeddedRefex : embeddedRefexes) {
				displayDynamicRefex(embeddedRefex, depth + 1);
			}
		} catch (IOException e) {
			LOG.error("Failed executing getRefexesDynamicActive(WBUtility.getViewCoordinate()).  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private void loadEmbeddedSearchFilterAttributes(RefexDynamicVersionBI<?> refex, Map<Integer, Collection<Filter>> filterOrderMap, Filter newFilter) throws InvalidNameException, IndexOutOfBoundsException, IOException, ContradictionException {
		// Now read SEARCH_FILTER_ATTRIBUTES refex column
		for (RefexDynamicVersionBI<?> embeddedRefex : refex.getRefexesDynamicActive(WBUtility.getViewCoordinate())) {
			displayDynamicRefex(embeddedRefex);
			
			RefexDynamicUsageDescription embeddedRefexDUD = null;
			try {
				embeddedRefexDUD = embeddedRefex.getRefexDynamicUsageDescription();
			} catch (IOException | ContradictionException e) {
				LOG.error("Failed performing getRefexDynamicUsageDescription() on embedded refex: caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
				
				return;
			}
			
			if (embeddedRefexDUD.getRefexName().equals(Search.SEARCH_FILTER_ATTRIBUTES.getDescription() /*"Search Filter Attributes"*/)) {
				RefexDynamicIntegerBI filterOrderCol = (RefexDynamicIntegerBI)embeddedRefex.getData(Search.SEARCH_FILTER_ATTRIBUTES_FILTER_ORDER_COLUMN.getDescription());
				if (filterOrderMap.get(filterOrderCol.getDataInteger()) == null) {
					filterOrderMap.put(filterOrderCol.getDataInteger(), new ArrayList<>());
				}
				filterOrderMap.get(filterOrderCol.getDataInteger()).add(newFilter);
			} else {
				LOG.warn("Encountered unexpected embedded refex \"" + embeddedRefexDUD.getRefexName() + "\". Ignoring...");
			}
		}
	}
	
	private void loadSavedSearch(SimpleDisplayConcept displayConcept) {
		LOG.info("loadSavedSearch(\"" + displayConcept.getDescription() + "\" (nid=" + displayConcept.getNid() + ")");

		SearchViewModel model = null;

		try {
			ConceptVersionBI matchingConcept = WBUtility.getConceptVersion(displayConcept.getNid());

			if (matchingConcept != null) {
				// TODO: change to use LOG
				System.out.println("loadSavedSearch(): savedSearchesComboBox has concept: " + matchingConcept);

				Map<Integer, Collection<Filter>> filterOrderMap = new TreeMap<>();
				
				model = new SearchViewModel();
				
				// TODO: change to use LOG
				System.out.println("loadSavedSearch() concept \"" + displayConcept + "\" all refexes: " +  matchingConcept.getRefexes().size());
				System.out.println("loadSavedSearch() concept \"" + displayConcept + "\" all dynamic refexes: " +  matchingConcept.getRefexesDynamic().size());
				System.out.println("loadSavedSearch() concept \"" + displayConcept + "\" active dynamic refexes (StandardViewCoordinates.getWbAuxiliary()): " +  matchingConcept.getRefexesDynamicActive(StandardViewCoordinates.getWbAuxiliary()).size());
				System.out.println("loadSavedSearch() concept \"" + displayConcept + "\" active dynamic refexes (WBUtility.getViewCoordinate()): " +  matchingConcept.getRefexesDynamicActive(WBUtility.getViewCoordinate()).size());

				for (RefexDynamicVersionBI<?> refex : matchingConcept.getRefexesDynamicActive(WBUtility.getViewCoordinate())) {
					displayDynamicRefex(refex);
					
					RefexDynamicUsageDescription dud = null;
					try {
						dud = refex.getRefexDynamicUsageDescription();
					} catch (IOException | ContradictionException e) {
						LOG.error("Failed performing getRefexDynamicUsageDescription(): caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
						
						return;
					}

					if (dud.getRefexName().equals(Search.SEARCH_GLOBAL_ATTRIBUTES.getDescription() /*"Search Global Attributes"*/)) {
						// handle "Search Global Attributes"
						
						// TODO: change to use LOG
						System.out.println("Loading data into model from Search Global Attributes refex");
						
						RefexDynamicByteArrayBI serializedViewCoordinate = (RefexDynamicByteArrayBI)refex.getData(Search.SEARCH_GLOBAL_ATTRIBUTES_VIEW_COORDINATE_COLUMN.getDescription());
						
						// Serialize passed View Coordinate into byte[]serializedViewCoordinate.getData()
						ByteArrayInputStream input = new ByteArrayInputStream(serializedViewCoordinate.getDataByteArray());
						
						ObjectInputStream oos = new ObjectInputStream(input);
						ViewCoordinate vc = new ViewCoordinate();
						vc.readExternal(oos);
						model.setViewCoordinate(vc);

						// TODO: change to use LOG
						System.out.println("Read View Coordinate from " + dud.getRefexName() + " refex: " + model.getViewCoordinate());
						
					} else if (dud.getRefexName().equals(Search.SEARCH_LUCENE_FILTER.getDescription() /*"Search Lucene Filter"*/)) {
						// handle "Search Lucene Filter"

						// TODO: change to use LOG
						System.out.println("Loading data into model from Search Lucene Filter refex");
						
						LuceneFilter newFilter = new LuceneFilter();
						
						RefexDynamicStringBI searchParamCol = (RefexDynamicStringBI)refex.getData(Search.SEARCH_LUCENE_FILTER_PARAMETER_COLUMN.getDescription());

						newFilter.setSearchParameter(searchParamCol.getDataString());

						// TODO: change to use LOG
						System.out.println("Read String search parameter from " + dud.getRefexName() + " refex: \"" + newFilter.getSearchParameter() + "\"");

						loadEmbeddedSearchFilterAttributes(refex, filterOrderMap, newFilter);
					} else if (dud.getRefexName().equals(Search.SEARCH_REGEXP_FILTER.getDescription() /*"Search RegExp Filter"*/)) {
						// handle "Search RegExp Filter"

						// TODO: change to use LOG
						System.out.println("Loading data into model from Search RegExp Filter refex");
						
						RegExpFilter newFilter = new RegExpFilter();
						
						RefexDynamicStringBI searchParamCol = (RefexDynamicStringBI)refex.getData(Search.SEARCH_REGEXP_FILTER_PARAMETER_COLUMN.getDescription());

						newFilter.setSearchParameter(searchParamCol.getDataString());

						// TODO: change to use LOG
						System.out.println("Read String search parameter from " + dud.getRefexName() + " refex: \"" + newFilter.getSearchParameter() + "\"");

						loadEmbeddedSearchFilterAttributes(refex, filterOrderMap, newFilter);
					} else {
						// handle or ignore
						LOG.warn("Concept \"" + displayConcept + "\" contains unexpected refex \"" + dud.getRefexName() + "\".  Ignoring...");
					}
				}

				for (int order : filterOrderMap.keySet()) {
					model.getFilters().addAll(filterOrderMap.get(order));
				}

				// TODO: change to use LOG
				System.out.println("loadSavedSearch() loaded model: " + model);
				
				if (model.getViewCoordinate() == null) {
					LOG.error("Failed loading saved search \"" + displayConcept.getDescription() + "\" (nid=" + displayConcept.getNid() + ").  View Coordinate is null.");
					
					return;
				} else if (model.getFilters().size() < 1) {
					LOG.error("Failed loading saved search \"" + displayConcept.getDescription() + "\" (nid=" + displayConcept.getNid() + ").  No filters found (must be at least 1).");
					
					return;
				} else if (model.getFilters().size() > 1) {
					// TODO: remove this check when supporting multiple filters
					LOG.error("Failed loading saved search \"" + displayConcept.getDescription() + "\" (nid=" + displayConcept.getNid() + ").  Too many filters (must be exactly 1).");
					
					return;
				} else if (! (model.getFilters().get(0) instanceof LuceneFilter)) {
					// TODO: remove this check when supporting non-Lucene filters
					LOG.error("Failed loading saved search \"" + displayConcept.getDescription() + "\" (nid=" + displayConcept.getNid() + ").  Filters of type " + model.getFilters().get(0).getClass().getName() + " not supported. Currently, only Lucene filters supported.");
					
					return;
				} else {
					// TODO: This is a hack for when we support exactly one Lucene Filter.  Change when multiple/various filters supported.
					searchText.setText(((LuceneFilter)model.getFilters().get(0)).getSearchParameter());
					this.currentSearchViewCoordinate = model.getViewCoordinate();

					// TODO: change to use LOG
					System.out.println("loadSavedSearch() loaded model: " + model);
					
					return;
				}
			} else {
				LOG.error("Failed loading saved search \"" + displayConcept.getDescription() + "\" (nid=" + displayConcept.getNid() + ")");
				return;
			}
		} catch (Exception e) {
			LOG.error("Failed loading saved search. Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
			e.printStackTrace();
			return;
		}
	}
	
	private void saveSearch() {
		// TODO: change to use LOG
		System.out.println("saveSearch() called.  Search specified: " + savedSearchesComboBox.valueProperty().getValue());

		Object valueAsObject = savedSearchesComboBox.valueProperty().getValue();

		if (valueAsObject != null) {
			SimpleDisplayConcept existingSavedSearch = null;
			String specifiedDescription = null;
			
			if (valueAsObject instanceof SimpleDisplayConcept) {
				existingSavedSearch = (SimpleDisplayConcept)valueAsObject;
				specifiedDescription = existingSavedSearch.getDescription();
			} else if (valueAsObject instanceof String) {
				specifiedDescription = (String)valueAsObject;
				for (SimpleDisplayConcept saveSearchInComboBoxList : savedSearchesComboBox.getItems()) {
					if (valueAsObject.equals(saveSearchInComboBoxList.getDescription())) {
						existingSavedSearch = saveSearchInComboBoxList;
						break;
					}
				}
			} else {
				String title = "Failed saving search";
				String msg = "Unsupported valueProperty value type in savedSearchesComboBox: " + valueAsObject.getClass().getName();
				String details = "Must either select or specify search name in order to save search and valueProperty must be either of type String or SimpleDisplayConcept";
				LOG.error(title + ". " + msg + details);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details);
				
				return;
			}
			
			String nameToSave = null;
			if (existingSavedSearch != null) {
				nameToSave = existingSavedSearch.getDescription();

				// TODO: change to use LOG
				System.out.println("saveSearch(): modifying existing saved search: " + existingSavedSearch + " (nid=" + existingSavedSearch.getNid() + ")");
				
				// TODO: remove this when modification/replacement is implemented
				String title = "Failed saving search";
				String msg = "Cannot modify existing saved search \"" + nameToSave + "\"";
				String details = "Modification or replacement of existing saves is not currently supported";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details);
				
				return;
			} else {
				nameToSave = specifiedDescription + " search by " + System.getProperty("user.name") + " at " + LocalDateTime.now().toString();

				// TODO: change to use LOG
				System.out.println("saveSearch(): creating new saved search: nickname=" + specifiedDescription + ", fullname=\"" + nameToSave + "\"");	
			}

			SearchViewModel model = new SearchViewModel();

			LuceneFilter filter = new LuceneFilter();
			filter.setSearchParameter(searchText.getText());
			model.getFilters().add(filter);

			model.setViewCoordinate(currentSearchViewCoordinate = WBUtility.getViewCoordinate());

			SearchConceptBuilder.doSave(nameToSave, nameToSave, model);

			refreshSavedSearchComboBox();
		} else {
			String title = "Failed saving search";
			String msg = "No search name or concept specified or selected";
			String details = "Must either select or specify search name in order to save search";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details);
		}
	}
	
	private void resetDefaults() {
		maxResultsCustomTextField.setText("");
		initializeSearchResultsTable();
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
			if (! nids.contains(result.getConceptNid())) {
				nids.add(result.getConceptNid());
			}
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
						
						if (c != null && c.getIndex() < c.getTableView().getItems().size()) {
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
		
		// Backup existing data in order to restore after reinitializing
		List<CompositeSearchResult> searchResultsTableBackup = new ArrayList<>(searchResultsTable.getItems());
		
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
		
		Collections.sort(searchResultsTableBackup, new CompositeSearchResultComparator());
		searchResultsTable.getItems().addAll(searchResultsTableBackup);
		
		refreshTotalResultsDisplayedLabel();
	}
	
	private void initializeWorkflowServices() {
		if (conceptWorkflowService == null) {
			conceptWorkflowService = AppContext.getService(ConceptWorkflowServiceI.class);
		}
		
		assert conceptWorkflowService != null;
	}
	
	private void initializeSavedSearchComboBox() {
		assert savedSearchesComboBox != null : "fx:id=\"savedSearchesComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Force single selection
		savedSearchesComboBox.getSelectionModel().selectFirst();
		savedSearchesComboBox.setCellFactory((p) -> {
			final ListCell<SimpleDisplayConcept> cell = new ListCell<SimpleDisplayConcept>() {
				@Override
				protected void updateItem(SimpleDisplayConcept c, boolean emptyRow) {
					super.updateItem(c, emptyRow);

					if(c == null) {
						setText(null);
					}else {
						setText(c.getDescription());
					}
				}
			};

			return cell;
		});
		savedSearchesComboBox.setButtonCell(new ListCell<SimpleDisplayConcept>() {
			@Override
			protected void updateItem(SimpleDisplayConcept c, boolean emptyRow) {
				super.updateItem(c, emptyRow); 
				if (emptyRow) {
					setText("");
				} else {
					setText(c.getDescription());
				}
			}
		});
		savedSearchesComboBox.valueProperty().addListener(new ChangeListener<Object>() {
			@Override public void changed(ObservableValue<? extends Object> ov, Object t, Object t1) {

				// TODO: change to use LOG
				System.out.println("savedSearchesComboBox ObservableValue: " + ov);
				
				if (t instanceof SimpleDisplayConcept) {
					SimpleDisplayConcept tSimpleDisplayConcept = (SimpleDisplayConcept)t;

					// TODO: change to use LOG
					System.out.println("savedSearchesComboBox old value: " + tSimpleDisplayConcept != null ? (tSimpleDisplayConcept.getDescription() + " (nid=" + tSimpleDisplayConcept.getNid() + ")") : null);
				} else {
					// TODO: change to use LOG
					System.out.println("savedSearchesComboBox old value: " + t);
				}
				if (t1 instanceof SimpleDisplayConcept) {
					SimpleDisplayConcept t1SimpleDisplayConcept = (SimpleDisplayConcept)t1;

					// TODO: change to use LOG
					System.out.println("savedSearchesComboBox new value: " + t1SimpleDisplayConcept != null ? (t1SimpleDisplayConcept.getDescription() + " (nid=" + t1SimpleDisplayConcept.getNid() + ")") : null);
				
					loadSavedSearch(t1SimpleDisplayConcept);
				} else {
					// TODO: change to use LOG
					System.out.println("savedSearchesComboBox new value: " + t1);
				}
			}    
		});
//		savedSearchesComboBox.setOnAction((event) -> {
//			
//			Object selectedItem = savedSearchesComboBox.getSelectionModel().getSelectedItem();
//			
//			if (selectedItem instanceof String) {
//				System.out.println("savedSearchesComboBox event selected String \"" + selectedItem + "\"");
//				
//				SimpleDisplayConcept buttonCellContent = savedSearchesComboBox.getButtonCell().getItem();
//				System.out.println("savedSearchesComboBox event button cell has \"" + buttonCellContent + "\" (nid=" + (buttonCellContent != null ? ((SimpleDisplayConcept)buttonCellContent).getNid() : null) + ")");
//
//			} else if (selectedItem instanceof SimpleDisplayConcept) {
//				System.out.println("savedSearchesComboBox event selected SimpleDisplayConcept \"" + selectedItem + "\" (nid=" + (selectedItem != null ? ((SimpleDisplayConcept)selectedItem).getNid() : null) + ")");
//
//				loadSavedSearch();
//			}
//		});
		
		savedSearchesComboBox.setEditable(true);

		refreshSavedSearchComboBox();
	}
	
	private void refreshSavedSearchComboBox() {
		Task<List<SimpleDisplayConcept>> loadSavedSearches = new Task<List<SimpleDisplayConcept>>() {
			private ObservableList<SimpleDisplayConcept> searches = FXCollections.observableList(new ArrayList<>());

			@Override
			protected List<SimpleDisplayConcept> call() throws Exception {
				List<ConceptVersionBI> savedSearches = WBUtility.getAllChildrenOfConcept(Search.SEARCH_PERSISTABLE.getNid(), true);
				
				for (ConceptVersionBI concept : savedSearches) {
					searches.add(new SimpleDisplayConcept(concept));
				}
				
				return searches;
			}

			@Override
			protected void succeeded() {
				super.succeeded();

				savedSearchesComboBox.setItems(searches);
			}
		};

		savedSearchesComboBox.getItems().clear();
		Utility.execute(loadSavedSearches);
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
						setText("Display " + a.toString());
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
					setText("Display " + t.toString());
				}
			}
		});
		aggregationTypeComboBox.setOnAction((event) -> {
			LOG.trace("aggregationTypeComboBox event (selected: " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			searchResultsTable.getItems().clear();
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
        	if (maxResultsCustomTextField.getText() != null && maxResultsCustomTextField.getText().length() > 0) {
        		Integer maxResults = Integer.valueOf(maxResultsCustomTextField.getText());
        		if (maxResults != null && maxResults > 0) {
        			builder.setSizeLimit(maxResults);
        		}
        	}
            ssh = SearchHandler.doConceptSearch(builder);
            break;
        }
        case DESCRIPTION:
        {
        	SearchBuilder builder = SearchBuilder.descriptionSearchBuilder(searchText.getText());
        	builder.setCallback(this);
        	builder.setTaskId(Tasks.SEARCH.ordinal());
        	if (maxResultsCustomTextField.getText() != null && maxResultsCustomTextField.getText().length() > 0) {
        		Integer maxResults = Integer.valueOf(maxResultsCustomTextField.getText());
        		if (maxResults != null && maxResults > 0) {
        			builder.setSizeLimit(maxResults);
        		}
        	}
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