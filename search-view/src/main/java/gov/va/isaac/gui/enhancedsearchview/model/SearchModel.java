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

/**
 * SearchViewModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.model;

import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.gui.enhancedsearchview.EnhancedSearchViewBottomPane;
import gov.va.isaac.gui.enhancedsearchview.IntegerField;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.model.type.SearchTypeSelector;
import gov.va.isaac.search.SearchHandle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchModel {
	private static final Logger LOG = LoggerFactory.getLogger(SearchModel.class);

	private static SearchTypeSelector searchTypeSelector = new SearchTypeSelector();

	private static SearchResultsTable searchResults = new SearchResultsTable();  
	private static ComboBox<ResultsType> resultsTypeComboBox = new ComboBox<ResultsType>();
	private static IntegerField maxResultsCustomTextField = new IntegerField(); 
	private static SearchHandle ssh = null;
	private static BusyPopover searchRunningPopover;
	private final static BooleanProperty searchRunning = new SimpleBooleanProperty(false);
	

	static {
		maxResultsCustomTextField.setMaxWidth(50);

		searchTypeSelector.getSearchTypeComboBox().getSelectionModel().selectFirst();
		searchTypeSelector.getSearchTypeComboBox().setItems(FXCollections.observableArrayList(SearchType.values()));

		searchResults.initializeSearchResultsTable(resultsTypeComboBox.getSelectionModel().getSelectedItem());
		initializeResultsTypeOptions();
		initializeSearchTypeComboBoxController();
	}
	
	@Override
	public String toString() {
		SearchTypeModel model = searchTypeSelector.getTypeSpecificModel();
		return "SearchViewModel [isValid=" + model.getIsSearchRunnableProperty() + ", name=" + model.getName()
				+ ", description=" + model.getDescription()+ ", maxResults=" + model.getMaxResults()
				+ ", droolsExpr=" + model.getDroolsExpr() 
				+ ", viewCoordinate=" + (model.getViewCoordinateProperty().get() != null ? model.getViewCoordinateProperty().get().getViewPosition() : null) + "]"
				+ model.getModelDisplayString();
	}

	public SearchTypeSelector getSearchTypeSelector() {
		return searchTypeSelector;
	}
	
	public IntegerField getMaxResultsCustomTextField() {
		return maxResultsCustomTextField;
	}

	public SearchResultsTable getSearchResultsTable() {
		return searchResults;
	}

	public ComboBox<ResultsType> getResultsTypeComboBox() {
		return resultsTypeComboBox;
	}

	private static void initializeSearchTypeComboBoxController() {
//		searchCriteriaPane = new BorderPane();
	
		// searchTypeController.setOnAction((event) -> {
		/*
		 * 			
		 * LOG.trace("aggregationTypeComboBox event (selected: " + aggregationTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			searchModel.getsearchModel.getSearchResultsTable()().getItems().clear();
			initializesearchModel.getsearchModel.getSearchResultsTable()()();
		});

		*/
	}
	private static void initializeResultsTypeOptions() {
		// Force single selection
		resultsTypeComboBox.getSelectionModel().selectFirst();
		resultsTypeComboBox.setCellFactory((p) -> {
			final ListCell<ResultsType> cell = new ListCell<ResultsType>() {
				@Override
				protected void updateItem(ResultsType a, boolean bln) {
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
		resultsTypeComboBox.setButtonCell(new ListCell<ResultsType>() {
			@Override
			protected void updateItem(ResultsType t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText("Display " + t.toString());
				}
			}
		});
		resultsTypeComboBox.setOnAction((event) -> {
			LOG.trace("aggregationTypeComboBox event (selected: " + resultsTypeComboBox.getSelectionModel().getSelectedItem() + ")");

			searchResults.getResults().getItems().clear();
			searchResults.initializeSearchResultsTable(resultsTypeComboBox.getSelectionModel().getSelectedItem());
		});

		resultsTypeComboBox.setItems(FXCollections.observableArrayList(ResultsType.values()));
		resultsTypeComboBox.getSelectionModel().select(ResultsType.DESCRIPTION);
	}

	public void executeSearch(ResultsType resultsType, String maxResults) {
		searchTypeSelector.getTypeSpecificModel().executeSearch(resultsType, maxResults);		
	}

	public SearchHandle getSsh() {
		return ssh;
	}

	public void setSsh(SearchHandle ssh) {
		this.ssh = ssh;
	}

	public BooleanProperty getSearchRunning() {
		return searchRunning;
	}

	public void setSearchRunningPopover(BusyPopover popover) {
		searchRunningPopover = popover;
	}

	public BusyPopover getSearchRunningPopover() {
		return searchRunningPopover;
	}

	public void setPanes(EnhancedSearchViewBottomPane bottomPane, SplitPane splitPane, BorderPane taxonomyPane) {
		searchResults.setBottomPane(bottomPane);
		SearchTypeModel.setPanes(bottomPane, splitPane, taxonomyPane);
	}

	public void setInitialCriteriaPane() {
		searchTypeSelector.setSearchTypePane(SearchType.REFSET_SPEC);
	}
}