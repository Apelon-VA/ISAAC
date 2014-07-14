package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.gui.enhancedsearchview.model.PerConceptLuceneSearchStrategy;
import gov.va.isaac.gui.enhancedsearchview.model.PerMatchLuceneSearchStrategy;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultFactoryI;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultFilterI;
import gov.va.isaac.gui.enhancedsearchview.model.SearchStrategyI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.UnmodifiableArrayList;


/**
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class EnhancedSearchViewController {
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewController.class);

	enum AggregationType {
		PER_CONCEPT,
		PER_MATCH
	}
	enum SearchStrategyType {
		LUCENE,
		//REGEX,
		//REFSET
	}
	
	// Cached AggregationType in list form for refreshing aggregationType ComboBox
	private final static ObservableList<AggregationType> aggregationTypes = FXCollections.observableArrayList(new UnmodifiableArrayList<>(AggregationType.values(), AggregationType.values().length));

	// Cached SearchStrategyType in list form for refreshing searchStrategy ComboBox
	private final static ObservableList<SearchStrategyType> searchStrategyTypes = FXCollections.observableArrayList(new UnmodifiableArrayList<>(SearchStrategyType.values(), SearchStrategyType.values().length));
	//private final static ObservableList<SearchStrategyType> searchStrategyTypes = FXCollections.observableArrayList(new UnmodifiableArrayList<>(new SearchStrategyType[] { SearchStrategyType.LUCENE }, 1));

	@FXML private Button searchButton;
	@FXML private TextField searchText;
	@FXML private VBox searchAndFilterVBox;
	@FXML private Pane pane;
	@FXML private ComboBox<AggregationType> aggregationTypeComboBox;
	@FXML private ComboBox<SearchStrategyType> searchStrategyComboBox;
	@FXML private TableView<CompositeSearchResult> searchResultsTable;
	@FXML private Button exportSearchResultsAsTabDelimitedValuesButton;

	//@FXML private VBox dynamicFilterVBox; // only if using dynamic filters
	//@FXML private Button addFilterButton;
	//@FXML private Button clearFiltersButton;
	
	private Window windowForTableViewExportDialog;
	
	private SearchStrategyI<CompositeSearchResult> searchStrategy;
	private final ObservableList<CompositeSearchResult> searchResults = FXCollections.observableArrayList();
	private final List<SearchResultFilterI> filters = new ArrayList<>();

	public static EnhancedSearchViewController init() throws IOException {
		// Load FXML
		URL resource = EnhancedSearchViewController.class.getResource("EnhancedSearchView.fxml");
		LOG.debug("FXML for " + EnhancedSearchViewController.class + ": " + resource);
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	private boolean isSearchValid() {
		if (searchStrategy == null) {
			return false;
		} else {
			return searchStrategy.isValid();
		}
	}

	protected void windowForTableViewExportDialog(Window window) {
		this.windowForTableViewExportDialog = window;
	}
	@FXML
	public void initialize() {
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchStrategyComboBox != null : "fx:id=\"searchStrategyComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchResultsTable != null : "fx:id=\"searchResultsTable\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert aggregationTypeComboBox != null : "fx:id=\"aggregationTypeComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Search results table
		initializeSearchResultsTable();
		initializeAggregationTypeComboBox();
		initializeSearchStrategyComboBox();

		exportSearchResultsAsTabDelimitedValuesButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)  {
				try {
					exportSearchResultsAsTabDelimitedValues();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			} 
		});

		// Default search button to disabled
		searchButton.setDisable(true);
		searchButton.setOnAction((action) -> {
			searchButton.setDisable(true);
			final BusyPopover searchButtonPopover = BusyPopover.createBusyPopover("Running search...", searchButton);

			Utility.execute(() -> {
				try
				{
					executeSearch();

					Platform.runLater(() -> 
					{
						searchButtonPopover.hide();
						if (isSearchValid()) {
							searchButton.setDisable(false);
						}
						refresh();
					});
				}
				catch (Exception e)
				{
					searchButtonPopover.hide();
					LOG.error("Error executing search: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
				}
			});
		});

		// This code only for searchText
		searchText.setPromptText("Enter search text");
		searchText.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				load();
			}
		});
	}
	
	private void initializeSearchResultsTable() {
		searchResultsTable.setTableMenuButtonVisible(true);
		searchResultsTable.setEditable(false);

		TableColumn<CompositeSearchResult, Number> scoreCol = new TableColumn<>("Score");
		TableColumn<CompositeSearchResult, String> statusCol = new TableColumn<>("Status");

		// Only meaningful for AggregationType PER_CONCEPT
		// When AggregationTyppe is PER_MATCH should always be 1
		TableColumn<CompositeSearchResult, Number> numMatchesCol = new TableColumn<>("Matches");
		
		// Only meaningful for AggregationType PER_MATCH
		// When AggregationTyppe is PER_CONCEPT should always be type of first match
		TableColumn<CompositeSearchResult, String> matchingDescTypeCol = new TableColumn<>("Type");
		
		TableColumn<CompositeSearchResult, String> preferredTermCol = new TableColumn<>("Term");
		preferredTermCol.setMaxWidth(Double.MAX_VALUE);
		TableColumn<CompositeSearchResult, String> fsnCol = new TableColumn<>("FSN");
		fsnCol.setMaxWidth(Double.MAX_VALUE);

		TableColumn<CompositeSearchResult, String> matchingTextCol = new TableColumn<>("Text");
		matchingTextCol.setMaxWidth(Double.MAX_VALUE);

		TableColumn<CompositeSearchResult, Number> nidCol = new TableColumn<>("NID");
		nidCol.setVisible(false);

		TableColumn<CompositeSearchResult, String> uuIdCol = new TableColumn<>("UUID");
		uuIdCol.setMaxWidth(Double.MAX_VALUE);
		uuIdCol.setVisible(false);
		
		TableColumn<CompositeSearchResult, String> sctIdCol = new TableColumn<>("SCTID");
		
		searchResultsTable.getColumns().clear();
		searchResultsTable.getColumns().add(scoreCol);
		searchResultsTable.getColumns().add(statusCol);
		searchResultsTable.getColumns().add(preferredTermCol);
		searchResultsTable.getColumns().add(matchingTextCol);
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() == AggregationType.PER_MATCH) {
			searchResultsTable.getColumns().add(matchingDescTypeCol);
		}
		searchResultsTable.getColumns().add(fsnCol);
		searchResultsTable.getColumns().add(numMatchesCol);
		searchResultsTable.getColumns().add(sctIdCol);
		searchResultsTable.getColumns().add(uuIdCol);
		searchResultsTable.getColumns().add(nidCol);

		matchingDescTypeCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
				return new SimpleStringProperty(WBUtility.getConPrefTerm(param.getValue().getMatchingDescriptionComponents().iterator().next().getTypeNid()));
			}
		});
		
		// Do not display numMatchesCol if AggregationType is PER_MATCH
		if (aggregationTypeComboBox.getSelectionModel().getSelectedItem() == AggregationType.PER_MATCH) {
			numMatchesCol.setVisible(false);
		}
		numMatchesCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(CellDataFeatures<CompositeSearchResult, Number> param) {
				return new SimpleIntegerProperty(param.getValue().getMatchingDescriptionComponents().size());
			}
		});

		nidCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(CellDataFeatures<CompositeSearchResult, Number> param) {
					return new SimpleIntegerProperty(param.getValue().getConcept().getNid());
			}
		});
		uuIdCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
					return new SimpleStringProperty(param.getValue().getConcept().getPrimordialUuid().toString().trim());
			}
		});
		sctIdCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
				return new SimpleStringProperty(ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(param.getValue().getConcept())).trim());
			}
		});
		fsnCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
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
			}
		});
		preferredTermCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
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
			}
		});
		scoreCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(CellDataFeatures<CompositeSearchResult, Number> param) {
				return new SimpleDoubleProperty(param.getValue().getBestScore());
			}
		});
		statusCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
				return new SimpleStringProperty(param.getValue().getConcept().getStatus().toString().trim());
			}
		});
		matchingTextCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
				return new SimpleStringProperty(param.getValue().getMatchStrings().iterator().next().trim());
			}
		});

		searchResultsTable.setItems(searchResults);
	}
	
	private void initializeAggregationTypeComboBox() {
		// Force single selection
		aggregationTypeComboBox.getSelectionModel().selectFirst();
		aggregationTypeComboBox.setCellFactory(new Callback<ListView<AggregationType>,ListCell<AggregationType>>(){
			@Override
			public ListCell<AggregationType> call(ListView<AggregationType> p) {

				final ListCell<AggregationType> cell = new ListCell<AggregationType>(){

					@Override
					protected void updateItem(AggregationType a, boolean bln) {
						super.updateItem(a, bln);

						if(a != null){
							setText(a.toString());
						}else{
							setText(null);
						}
					}

				};

				return cell;
			}
		});
		aggregationTypeComboBox.setButtonCell(new ListCell<AggregationType>() {
			@Override
			protected void updateItem(AggregationType t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.toString());
				}
			}
		});
		aggregationTypeComboBox.setOnAction((event) -> {
			LOG.trace("aggregationTypeComboBox event (selected: " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			searchResults.clear();
			
			initializeSearchResultsTable();
			
			load();
			refresh();
			
			// TODO: enable auto search after AggregationType change. Currently messes up formats.
//			if (this.isSearchValid()) {
//				this.executeSearch();
//			}
		});
		
		aggregationTypeComboBox.getSelectionModel().select(AggregationType.PER_CONCEPT);
	}
	
	private void initializeSearchStrategyComboBox() {
		// TODO: Make visible in order to use search strategy ComboBox
		searchAndFilterVBox.setVisible(false);
		
		// Force single selection
		searchStrategyComboBox.getSelectionModel().selectFirst();
		searchStrategyComboBox.setCellFactory(new Callback<ListView<SearchStrategyType>,ListCell<SearchStrategyType>>(){
			@Override
			public ListCell<SearchStrategyType> call(ListView<SearchStrategyType> p) {

				final ListCell<SearchStrategyType> cell = new ListCell<SearchStrategyType>(){

					@Override
					protected void updateItem(SearchStrategyType a, boolean bln) {
						super.updateItem(a, bln);

						if(a != null){
							setText(a.toString());
						}else{
							setText(null);
						}
					}

				};

				return cell;
			}
		});
		searchStrategyComboBox.setButtonCell(new ListCell<SearchStrategyType>() {
			@Override
			protected void updateItem(SearchStrategyType t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.toString());
				}
			}
		});
		searchStrategyComboBox.setOnAction((event) -> {
			SearchStrategyType type = searchStrategyComboBox.getSelectionModel().getSelectedItem();
			LOG.trace("searchStrategyComboBox event (selected: " + type + ")");

			load();
			refresh();
		});
	}

	public void executeSearch() {
		LOG.trace("Running executeSearch()...");

		if (searchStrategy != null) {
			searchStrategy.setSearchTextParameter(searchText.getText());
			if (isSearchValid()) {
				searchStrategy.search();

				for (CompositeSearchResult result : searchResults) {
					LOG.trace(result.toString());
				}
			}
		} else {
			String title = "Search Strategy Not Set";
			String msg = "Search Strategy must be set";
			LOG.error(title);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, "Search Strategy must be set");
		}
	}

	public Pane getRoot() {
		return pane;
	}

	public void loadSearchStrategy() {
		LOG.trace("Running loadSearchStrategy()...");
		searchStrategyComboBox.getSelectionModel().select(SearchStrategyType.LUCENE);
		SearchStrategyType searchStrategyType = searchStrategyComboBox.getValue();
		if (searchStrategyType != null) {
			final SearchResultFactoryI<CompositeSearchResult> srf = new SearchResultFactoryI<CompositeSearchResult> () {
				@Override
				public CompositeSearchResult transform(CompositeSearchResult result) {
					return result;
				}
			};
			
			switch (searchStrategyType) {
			case LUCENE: {
				switch (aggregationTypeComboBox.getSelectionModel().getSelectedItem()) {
				case  PER_CONCEPT:
					searchStrategy = new PerConceptLuceneSearchStrategy<CompositeSearchResult>(srf, searchResults);
					searchStrategy.setComparator(new CompositeSearchResultComparator());
					break;
				case  PER_MATCH:
					searchStrategy = new PerMatchLuceneSearchStrategy<CompositeSearchResult>(srf, searchResults);
					searchStrategy.setComparator(new PerMatchLuceneSearchStrategy.PerMatchCompositeSearchResultComparator());
					break;

				default:
					searchStrategy = null;
					String title = "Unsupported Aggregation Type";
					String msg = "Unsupported AggregationType \"" + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + "\"";
					LOG.error(title);
					AppContext.getCommonDialogs().showErrorDialog(title, msg, "Only AggregationType(s) " + AggregationType.values() + " currently supported");
					break;
				}

				if (searchStrategy != null) {
					searchStrategy.setSearchTextParameter(searchText.getText());
					searchStrategy.setSearchResultFilters(filters);
				}
			}
			break;

			default:
				searchStrategy = null;
				String title = "Unsupported Search Strategy";
				String msg = "Unsupported SearchStrategyType \"" + searchStrategyType + "\"";
				LOG.error(title);
				AppContext.getCommonDialogs().showErrorDialog(title, msg, "Only SearchStrategyType \"LUCENE\" currently supported");
			}
		}
	}

	private void exportSearchResultsAsTabDelimitedValues() {
		FileChooser fileChooser = new FileChooser();
		  
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
        	ObservableList<TableColumn<CompositeSearchResult, ?>> columns = searchResultsTable.getColumns();

        	char delimiter = '\t';

        	LOG.debug("Writing TableView data to file \"" + file.getAbsolutePath() + "\"...");

        	Writer writer = null;
        	try {
        		writer = new BufferedWriter(new FileWriter(file));

        		StringBuilder row = new StringBuilder();
        		for (int colIndex = 0; colIndex < columns.size(); ++colIndex) {
        			TableColumn<CompositeSearchResult, ?> col = columns.get(colIndex);
        			if (! col.isVisible()) {
        				continue;
        			}
        			row.append(col.getText());
        			if (colIndex < (columns.size() - 1)) {
        				row.append(delimiter);
        			} else if (colIndex == (columns.size() - 1)) {
        				row.append("\n");
        			}
        		}
        		LOG.trace(row.toString());
        		writer.write(row.toString());

        		for (int rowIndex = 0; rowIndex < searchResults.size(); ++rowIndex) {
        			row = new StringBuilder();
        			for (int colIndex = 0; colIndex < columns.size(); ++colIndex) {
        				TableColumn<CompositeSearchResult, ?> col = columns.get(colIndex);
        				if (! col.isVisible()) {
        					continue;
        				}
        				row.append(col.getCellObservableValue(rowIndex).getValue().toString());
        				if (colIndex < (columns.size() - 1)) {
        					row.append(delimiter);
        				} else if (colIndex == (columns.size() - 1)) {
        					row.append("\n");
        				}
        			}

        			LOG.trace(row.toString());
        			writer.write(row.toString());
        		}

        		LOG.debug("Wrote " + searchResults.size() + " rows of TableView data to file \"" + file.getAbsolutePath() + "\".");
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

	public void load() {
		// Load searchStrategy
		loadSearchStrategy();

		refresh();
	}

	public void refresh() {
		LOG.trace("Running refresh()...");

		// refresh searchStrategiesComboBox
		aggregationTypeComboBox.setItems(aggregationTypes);

		// refresh searchStrategiesComboBox
		searchStrategyComboBox.setItems(searchStrategyTypes);

		if (isSearchValid()) {
			LOG.debug("Search parameters valid.  Enabling search controls...");

			searchButton.setDisable(false);
		} else {
			LOG.debug("Search parameters invalid.  Disabling search controls...");

			searchButton.setDisable(true);
		}
	}
}