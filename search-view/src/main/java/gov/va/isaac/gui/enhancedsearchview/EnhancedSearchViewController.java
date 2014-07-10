package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultModel;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultModelComparator;
import gov.va.isaac.gui.enhancedsearchview.model.SearchResultModelLuceneSearchStrategy;
import gov.va.isaac.gui.enhancedsearchview.model.SearchStrategyI;
import gov.va.isaac.util.Utility;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.ihtsdo.otf.tcc.api.coordinate.Status;
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
    	REGEX,
    	REFSET
    }

	// Cached Actions in list form for refreshing actionComboBox
	private final static ObservableList<SearchStrategyType> searchStrategyTypes = FXCollections.observableArrayList(new UnmodifiableArrayList<>(SearchStrategyType.values(), SearchStrategyType.values().length));

    @FXML private Button searchButton;
    @FXML private TextField searchText;
    @FXML private VBox dynamicSearchAndFilterVBox;
    @FXML private Pane pane;
    @FXML private ComboBox<SearchStrategyType> searchStrategyComboBox;
	@FXML private TableView<SearchResultModel> searchResultsTable;
    
    private SearchStrategyI<SearchResultModel> searchStrategy;
    private final ObservableList<SearchResultModel> searchResults = FXCollections.observableArrayList();

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

        TableColumn<SearchResultModel, Double> scoreCol = new TableColumn<>("Score");
        TableColumn<SearchResultModel, Status> statusCol = new TableColumn<>("Status");
        
        TableColumn<SearchResultModel, String> fsnCol = new TableColumn<>("FSN");
        fsnCol.setMaxWidth(Double.MAX_VALUE);
        
        TableColumn<SearchResultModel, String> matchingTextCol = new TableColumn<>("Text");
        matchingTextCol.setMaxWidth(Double.MAX_VALUE);

        TableColumn<SearchResultModel, Integer> nidCol = new TableColumn<>("NID");
        
        TableColumn<SearchResultModel, String> uuIdCol = new TableColumn<>("UUID");
        uuIdCol.setMaxWidth(Double.MAX_VALUE);
        
        
        searchResultsTable.getColumns().addAll(scoreCol, statusCol, matchingTextCol, fsnCol, nidCol, uuIdCol);

        fsnCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, String>("fsn")
        	);
        scoreCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, Double>("score")
        	);
        statusCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, Status>("status")
        	);
        fsnCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, String>("fsn")
        	);
        nidCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, Integer>("id")
        	);
        uuIdCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, String>("uuId")
        	);
        matchingTextCol.setCellValueFactory(
        	    new PropertyValueFactory<SearchResultModel, String>("matchingText")
        	);
        
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
    			
    			for (SearchResultModel model : searchResults) {
    				System.out.println(model);
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
    	SearchStrategyType searchStrategyType = searchStrategyComboBox.getValue();
    	if (searchStrategyType != null) {
    		switch (searchStrategyType) {
    		case LUCENE:
//    			searchStrategy = new AbstractLuceneSearchStrategy<CompositeSearchResult>(searchResults) {
//					@Override
//					public CompositeSearchResult transform(CompositeSearchResult result) {
//						return result;
//					}
//    			};
//    			searchStrategy.setSearchTextParameter(searchText.getText());
//    			searchStrategy.setComparator(new CompositeSearchResultComparator());
    			searchStrategy = new SearchResultModelLuceneSearchStrategy(searchResults);
    			searchStrategy.setSearchTextParameter(searchText.getText());
    			searchStrategy.setComparator(new SearchResultModelComparator());
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