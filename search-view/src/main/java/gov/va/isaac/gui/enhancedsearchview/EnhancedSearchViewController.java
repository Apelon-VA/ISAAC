package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.gui.enhancedsearchview.model.LuceneSearchStrategy;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultsFilterI;
import gov.va.isaac.gui.enhancedsearchview.model.SearchStrategyI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.CompositeSearchResultComparator;
import gov.va.isaac.util.Utility;

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

	enum SearchStrategyType {
		LUCENE,
		//REGEX,
		//REFSET
	}

	// Cached Actions in list form for refreshing actionComboBox
	private final static ObservableList<SearchStrategyType> searchStrategyTypes = FXCollections.observableArrayList(new UnmodifiableArrayList<>(SearchStrategyType.values(), SearchStrategyType.values().length));
	//private final static ObservableList<SearchStrategyType> searchStrategyTypes = FXCollections.observableArrayList(new UnmodifiableArrayList<>(new SearchStrategyType[] { SearchStrategyType.LUCENE }, 1));

	@FXML private Button searchButton;
	@FXML private TextField searchText;
	@FXML private VBox searchAndFilterVBox;
	@FXML private Pane pane;
	@FXML private ComboBox<SearchStrategyType> searchStrategyComboBox;
	@FXML private TableView<CompositeSearchResult> searchResultsTable;
	@FXML private Button exportSearchResultsAsTabDelimitedValuesButton;

	//@FXML private VBox dynamicFilterVBox; // only if using dynamic filters
	//@FXML private Button addFilterButton;
	//@FXML private Button clearFiltersButton;

	private Window windowForTableViewExportDialog;
	
	private SearchStrategyI<CompositeSearchResult> searchStrategy;
	private final ObservableList<CompositeSearchResult> searchResults = FXCollections.observableArrayList();
	private final List<SearchResultsFilterI> filters = new ArrayList<>();

	public static EnhancedSearchViewController init() throws IOException {
		// Load FXML
		URL resource = EnhancedSearchViewController.class.getResource("EnhancedSearchView.fxml");
		System.out.println("FXML for " + EnhancedSearchViewController.class + ": " + resource);
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
	
	private void exportSearchResultsAsTabDelimitedValues() {
		FileChooser fileChooser = new FileChooser();
		  
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(windowForTableViewExportDialog);

		//String tempDir = System.getenv("TEMP");
		//File file = new File(tempDir + File.separator + "EnhanceSearchViewControllerTableViewData.csv");

		ObservableList<TableColumn<CompositeSearchResult, ?>> columns = searchResultsTable.getColumns();

		char delimiter = '\t';
		
		System.out.println("Writing TableView data to file \"" + file.getAbsolutePath() + "\"...");
		
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
			System.out.print(row.toString());
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
				
				System.out.print(row.toString());
				writer.write(row.toString());
			}

			System.out.println("Wrote " + searchResults.size() + " rows of TableView data to file \"" + file.getAbsolutePath() + "\".");
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

	@FXML
	public void initialize() {
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchStrategyComboBox != null : "fx:id=\"searchStrategyComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchResultsTable != null : "fx:id=\"searchResultsTable\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		// Search results table
		initializeSearchResultsTable();

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
		TableColumn<CompositeSearchResult, Number> numMatchesCol = new TableColumn<>("Matches");

		TableColumn<CompositeSearchResult, String> preferredTermCol = new TableColumn<>("Term");
		preferredTermCol.setMaxWidth(Double.MAX_VALUE);
		TableColumn<CompositeSearchResult, String> fsnCol = new TableColumn<>("FSN");
		fsnCol.setMaxWidth(Double.MAX_VALUE);

		TableColumn<CompositeSearchResult, String> matchingTextCol = new TableColumn<>("Text");
		matchingTextCol.setMaxWidth(Double.MAX_VALUE);

		//TableColumn<SearchResultModel, Integer> nidCol = new TableColumn<>("NID");

		TableColumn<CompositeSearchResult, String> uuIdCol = new TableColumn<>("UUID");
		uuIdCol.setMaxWidth(Double.MAX_VALUE);
		uuIdCol.setVisible(false);

		searchResultsTable.getColumns().addAll(
				scoreCol, 
				statusCol, 
				numMatchesCol,
				preferredTermCol,
				matchingTextCol, 
				//nidCol, 
				uuIdCol,
				fsnCol
				);

		uuIdCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<CompositeSearchResult, String> param) {
					return new SimpleStringProperty(param.getValue().getConcept().getPrimordialUuid().toString().trim());
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
		numMatchesCol.setCellValueFactory(new Callback<CellDataFeatures<CompositeSearchResult,Number>, ObservableValue<Number>>() {
			@Override
			public ObservableValue<Number> call(CellDataFeatures<CompositeSearchResult, Number> param) {
				return new SimpleIntegerProperty(param.getValue().getMatchingDescriptionComponents().size());
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

	private void initializeSearchStrategyComboBox() {
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
			System.out.println("searchStrategyComboBox Action (selected: " + type + ")");

			load();
			refresh();
		});
	}

	public void executeSearch() {
		System.out.println("Running executeSearch()...");

		if (searchStrategy != null) {
			searchStrategy.setSearchTextParameter(searchText.getText());
			if (isSearchValid()) {
				searchStrategy.search();

				for (CompositeSearchResult result : searchResults) {
					System.out.println(result);
				}
				//    			for (CompositeSearchResult result : searchResults) {
				//    				final ConceptVersionBI wbConcept = result.getConcept();
				//    				String preferredText = null;
				//					try {
				//						preferredText = wbConcept.getPreferredDescription().getText();
				//					} catch (IOException | ContradictionException e) {
				//						e.printStackTrace();
				//					}
				//    				System.out.println(wbConcept.toUserString() + " (" + result.getConceptNid() + "):");
				//
				//    				for (String matchString : result.getMatchStrings()) {
				//    					if (! matchString.equals(preferredText)) {
				//    						System.out.println("\t" + matchString);
				//    					}
				//    				}
				//    			}
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
		System.out.println("Running loadSearchStrategy()...");
		searchStrategyComboBox.getSelectionModel().select(SearchStrategyType.LUCENE);
		SearchStrategyType searchStrategyType = searchStrategyComboBox.getValue();
		if (searchStrategyType != null) {
			switch (searchStrategyType) {
			case LUCENE:
				searchStrategy = new LuceneSearchStrategy(searchResults);
				searchStrategy.setSearchTextParameter(searchText.getText());
				searchStrategy.setComparator(new CompositeSearchResultComparator());
				searchStrategy.setSearchResultsFilters(filters);
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
	public void load() {
		// Load searchStrategy
		loadSearchStrategy();

		refresh();
	}

	public void refresh() {
		System.out.println("Running refresh()...");

		// refresh searchStrategiesComboBox
		searchStrategyComboBox.setItems(searchStrategyTypes);

		if (isSearchValid()) {
			System.out.println("Search parameters valid.  Enabling search controls...");

			searchButton.setDisable(false);
		} else {
			System.out.println("Search parameters invalid.  Disabling search controls...");

			searchButton.setDisable(true);
		}
	}
}