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
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.gui.enhancedsearchview.model.EnhancedSavedSearch;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToDrools;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToRefset;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToReport;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToWorkflow;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ListBatchViewI;
import gov.va.isaac.search.CompositeSearchResult;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class EnhancedSearchViewController {
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewController.class);

	@FXML private AnchorPane mainPane;
	@FXML private MenuBar enhancedSearchMenuBar;
	@FXML private Menu handleResultsMenu;
	
	@FXML private BorderPane searchBorderPane;
	@FXML private SplitPane searchAndTaxonomySplitPane;

	//@FXML private ListView<DisplayableFilter> searchFilterListView;
	private EnhancedSearchViewTopPane topPane;
	private EnhancedSearchViewBottomPane bottomPane;
	private SearchModel searchModel = new SearchModel();
	
	private Menu     handleSearchMenu 			= new Menu("Handle Results");
	private Menu     saveSearchMenu 			= new Menu("Saved Searches");

	private MenuItem resultsToReportMenuItem    = new MenuItem("Report");
	private MenuItem resultsToListMenuItem      = new MenuItem("List");
	private MenuItem resultsToWorkflowMenuItem  = new MenuItem("Workflow");
	private MenuItem resultsToTaxonomyMenuItem  = new MenuItem("Taxonomy");
	private MenuItem resultsToSememeMenuItem    = new MenuItem("Sememe");
	private MenuItem resultsToDroolsMenuItem    = new MenuItem("Drools");

	private MenuItem saveSearchMenuItem			= new MenuItem("Save Search");
	private Menu     loadSearchMenu 			= new Menu("Load Saved Search");
	


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
		//assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		//assert addIsDescendantOfFilterButton != null : "fx:id=\"addIsDescendantOfFilterButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert searchAndTaxonomySplitPane != null : "fx:id=\"searchResultsAndTaxonomySplitPane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert enhancedSearchMenuBar != null : "fx:id=\"enhancedSearchMenuBar\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		
		Stage stage = AppContext.getMainApplicationWindow().getPrimaryStage();
		
		initializeSearchMenus(stage);
		
		String styleSheet = EnhancedSearchViewController.class.getResource("/isaac-shared-styles.css").toString();
		if (! searchAndTaxonomySplitPane.getStylesheets().contains(styleSheet)) {
			searchAndTaxonomySplitPane.getStylesheets().add(styleSheet);
		}

		topPane = new EnhancedSearchViewTopPane();
		bottomPane = new EnhancedSearchViewBottomPane(stage);
		
		SearchModel.getSearchRunning().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (newValue) {
					searchModel.setSearchRunningPopover(BusyPopover.createBusyPopover("Searching...", topPane.getSearchButton()));
				} else {
					if (searchModel.getSearchRunningPopover() != null) {
						searchModel.getSearchRunningPopover().hide();
					}
				}
			}
		});
		
		//TODO (artf231846) - things that hit the DB (BDB or the Workflow SQL DB) should NOT Be done in the JavaFX foreground thread.  This causes large delays in displaying your GUI.
		//this sort of stuff need to be a in  background thread, with an appropriate progress indicator

		topPane.getSearchButton().setOnAction((action) -> {
			if (SearchModel.getSearchRunning().get() && SearchModel.getSsh() != null) {
				SearchModel.getSsh().cancel();
			} else {
				SearchModel.getSearchTypeSelector().getTypeSpecificModel().search( 
						searchModel.getResultsTypeComboBox().getSelectionModel().getSelectedItem(),
						searchModel.getMaxResultsCustomTextField());
			}
		});
		SearchModel.getSearchRunning().addListener((observable, oldValue, newValue) -> {
			if (SearchModel.getSearchRunning().get()) {
				topPane.getSearchButton().setText("Cancel");
			} else {
				topPane.getSearchButton().setText("Search");
			}
		});
		
		ResultsToTaxonomy.setSearchAndTaxonomySplitPane(searchAndTaxonomySplitPane);

		searchBorderPane.setTop(topPane.getTopPaneVBox());
		searchBorderPane.setBottom(bottomPane.getBottomPaneHBox());
		searchBorderPane.setCenter(SearchModel.getSearchResultsTable().getResults());
		searchModel.setPanes(bottomPane, searchAndTaxonomySplitPane, ResultsToTaxonomy.getTaxonomyPanelBorderPane());
		
		SearchModel.getSearchResultsTable().getResults().getItems().addListener(new ListChangeListener<CompositeSearchResult>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends CompositeSearchResult> c) {
				bottomPane.refreshBottomPanel();
				setSearchMenusDisabled();
			}
		});
		
	}

	public Pane getRoot() {
		//return searchAndTaxonomySplitPane;
		return mainPane;
	}

	interface ColumnValueExtractor {
		String extract(TableColumn<CompositeSearchResult, ?> col);
	}
	
	private void initializeSearchMenus(Stage stage) {
		ResultsToTaxonomy.initializeTaxonomyPanel();

		handleSearchMenu.getItems().clear();
		saveSearchMenu.getItems().clear();
		
		handleSearchMenu.getItems().addAll(resultsToReportMenuItem   ,
										   resultsToListMenuItem     ,
										   resultsToWorkflowMenuItem ,
										   resultsToTaxonomyMenuItem ,
										   resultsToSememeMenuItem   ,
										   resultsToDroolsMenuItem   );
		
		saveSearchMenu.getItems().addAll(saveSearchMenuItem, loadSearchMenu);
		
		enhancedSearchMenuBar.getMenus().addAll(handleSearchMenu, saveSearchMenu);
		
		resultsToListMenuItem.setOnAction((e) -> resultsToList());
		resultsToReportMenuItem.setOnAction((e) -> ResultsToReport.resultsToReport());
		resultsToWorkflowMenuItem.setOnAction((e) -> ResultsToWorkflow.multipleResultsToWorkflow());
		resultsToTaxonomyMenuItem.setOnAction((e) -> ResultsToTaxonomy.resultsToSearchTaxonomy());
		resultsToSememeMenuItem.setOnAction((e) -> createSememe(stage));
		resultsToDroolsMenuItem.setOnAction((e) -> ResultsToDrools.createDroolsOnClipboard(searchModel));
		
		saveSearchMenuItem.setOnAction((e) -> {
			topPane.getSearchSaver().saveSearch();
			EnhancedSavedSearch.refreshSavedSearchMenu(loadSearchMenu);
		});
		
		EnhancedSavedSearch.refreshSavedSearchMenu(loadSearchMenu);
		
		//setSearchMenusDisabled(true);
	}
	
	public void setSearchMenusDisabled() {
		int resultSize = SearchModel.getSearchResultsTable().getResults().getItems().size();

		setSearchMenusDisabled(resultSize == 0);

		resultsToWorkflowMenuItem.setDisable(resultSize > 0 && resultSize <= 5);
	}
	
	public void setSearchMenusDisabled(boolean isDisabled) {
		handleSearchMenu.setDisable(isDisabled);
		saveSearchMenuItem.setDisable(isDisabled);
	}
	
	private void createSememe(Stage stage) {
		try {
			String refexName = ResultsToRefset.resultsToRefset(stage, SearchModel.getSearchResultsTable().getResults());
			
			if (refexName != null) {
				AppContext.getCommonDialogs().showInformationDialog("Sememe Successfully Created", "Created and populated new Sememe (" + refexName + ") with all values in results table");
			}
		} catch (Exception e) {
			AppContext.getCommonDialogs().showErrorDialog("Sememe Creation Failure", "Sememe Creation Failure", "Failed to create and populate Sememe with values in results table"); 
		}
	}

	private void resultsToList() {
		ListBatchViewI lv = AppContext.getService(ListBatchViewI.class, SharedServiceNames.DOCKED);
		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble((DockedViewI) lv);

		List<Integer> nids = new ArrayList<>();
		for (CompositeSearchResult result : SearchModel.getSearchResultsTable().getResults().getItems()) {
			if (! nids.contains(result.getContainingConcept().getNid())) {
				nids.add(result.getContainingConcept().getNid());
			}
		}

		lv.addConcepts(nids);
	}



}
