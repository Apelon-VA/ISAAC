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
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.search.CompositeSearchResult;

import java.io.IOException;
import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EnhancedSearchViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class EnhancedSearchViewController {
	private static final Logger LOG = LoggerFactory.getLogger(EnhancedSearchViewController.class);

	@FXML private BorderPane searchBorderPane;
	@FXML private SplitPane searchAndTaxonomySplitPane;

	//@FXML private ListView<DisplayableFilter> searchFilterListView;
	private EnhancedSearchViewTopPane topPane;
	private EnhancedSearchViewBottomPane bottomPane;
	private SearchModel searchModel = new SearchModel();
	
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

		String styleSheet = EnhancedSearchViewController.class.getResource("/isaac-shared-styles.css").toString();
		if (! searchAndTaxonomySplitPane.getStylesheets().contains(styleSheet)) {
			searchAndTaxonomySplitPane.getStylesheets().add(styleSheet);
		}

		topPane = new EnhancedSearchViewTopPane();
		bottomPane = new EnhancedSearchViewBottomPane(AppContext.getMainApplicationWindow().getPrimaryStage());
		
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
			}
		});
	}

	public SplitPane getRoot() {
		return searchAndTaxonomySplitPane;
	}

	interface ColumnValueExtractor {
		String extract(TableColumn<CompositeSearchResult, ?> col);
	}
}
