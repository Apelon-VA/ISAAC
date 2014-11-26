package gov.va.isaac.gui.enhancedsearchview.model.type;

import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.refspec.RefsetSpecSearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.refspec.RefsetSpecSearchTypeView;
import gov.va.isaac.gui.enhancedsearchview.model.type.sememe.SememeSearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.sememe.SememeSearchTypeView;
import gov.va.isaac.gui.enhancedsearchview.model.type.text.TextSearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.text.TextSearchTypeView;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class SearchTypeSelector {
	private static ComboBox<SearchType> searchTypeSelector = new ComboBox<SearchType>();
	private static Map<SearchType, SearchTypeSpecificView> typeSpecificViewMap = new HashMap<SearchType, SearchTypeSpecificView>();
	private static Map<SearchType, SearchTypeModel> typeSpecificModelMap = new HashMap<SearchType, SearchTypeModel>();

	private static StackPane criteriaPane = new StackPane();
	private static SearchType currentType;

	private static ComboBox<ResultsType> resultsTypeField;
	private static HBox maxResultsField;
	
	static {
		typeSpecificViewMap.put(SearchType.TEXT,  new TextSearchTypeView());
		typeSpecificViewMap.put(SearchType.SEMEME,  new SememeSearchTypeView());
		typeSpecificViewMap.put(SearchType.REFSET_SPEC,  new RefsetSpecSearchTypeView());
		
		typeSpecificModelMap.put(SearchType.TEXT, new TextSearchTypeModel());
		typeSpecificModelMap.put(SearchType.SEMEME, new SememeSearchTypeModel());
		typeSpecificModelMap.put(SearchType.REFSET_SPEC, new RefsetSpecSearchTypeModel());

		searchTypeSelector.setOnAction((e) -> changeSearchType());
	}

	//private static SearchResultsTable searchResultsTable;
	
	public ComboBox<SearchType> getSearchTypeComboBox() {
		return searchTypeSelector;
	}
	
	private static void changeSearchType() {
		SearchType selection = searchTypeSelector.getSelectionModel().getSelectedItem();
		
		SearchModel.getSearchResultsTable().getResults().getItems().clear();

		// searchType may be temporarily set to null in order to all resetting to the "current" (non-null) type,
		// triggering selector button cell factories and change handlers
		if (selection != null) {
			currentType = selection;
			searchTypeSelector.getSelectionModel().select(selection);

			if (resultsTypeField != null) {
				SearchModel.getSearchResultsTable().initializeSearchResultsTable(currentType, resultsTypeField.getSelectionModel().getSelectedItem());

				if (selection == SearchType.TEXT) {
					resultsTypeField.setVisible(true);
				} else {
					resultsTypeField.setVisible(false);
					resultsTypeField.getSelectionModel().select(ResultsType.CONCEPT);
				}
			}

			if (maxResultsField != null) {
				if (selection == SearchType.REFSET_SPEC) {
					maxResultsField.setVisible(false);
				} else {
					maxResultsField.setVisible(true);
				}
			}

			setCriteriaPane(typeSpecificViewMap.get(selection).setContents(typeSpecificModelMap.get(selection)));
		}
		
		SearchModel.isSearchRunnableProperty().set(SearchModel.isSearchRunnable());
	}

	public void setSearchTypePane(SearchType type) {
		currentType = type;
		searchTypeSelector.getSelectionModel().select(type);
	}
	
	public static void setCriteriaPane(Pane pane) {
		if (!criteriaPane.getChildren().isEmpty()) {
			criteriaPane.getChildren().remove(0);
		}

		criteriaPane.getChildren().add(0, pane);
	}

	public SearchType getCurrentType() {
		return currentType;
	}

	public SearchTypeModel getTypeSpecificModel() {
		return typeSpecificModelMap.get(currentType);
	}

	public BooleanProperty getIsSearchRunnableProperty() { 
		return typeSpecificModelMap.get(currentType).getIsSearchRunnableProperty(); 
	}
	
	public Pane getResultsPane() {
		return criteriaPane;
	}
	
	public void setResultTypeField(ComboBox<ResultsType> comboBox) {
		resultsTypeField = comboBox;
	}

	public void setMaxResultsField(HBox hBox) {
		maxResultsField = hBox;
	}
}
