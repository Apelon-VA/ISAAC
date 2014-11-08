package gov.va.isaac.gui.enhancedsearchview.model.type;

import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;
import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.component.ComponentContentSearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.component.ComponentContentSearchTypeView;
import gov.va.isaac.gui.enhancedsearchview.model.type.refspec.RefsetSpecSearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.refspec.RefsetSpecSearchTypeView;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class SearchTypeSelector {
	private static ComboBox<SearchType> searchTypeSelector = new ComboBox<SearchType>();
	private static Map<SearchType, SearchTypeSpecificView> typeSpecificViewMap = new HashMap<SearchType, SearchTypeSpecificView>();
	private static Map<SearchType, SearchTypeModel> typeSpecificModelMap = new HashMap<SearchType, SearchTypeModel>();

	private static StackPane criteriaPane = new StackPane();
	private static SearchType currentType;
	
	static {
		typeSpecificViewMap.put(SearchType.COMPONENT_CONTENT,  new ComponentContentSearchTypeView());
//		typeSpecificViewMap.put(SearchType.REFSET_CONTENT,  new RefsetContentSearchTypeView());
		typeSpecificViewMap.put(SearchType.REFSET_SPEC,  new RefsetSpecSearchTypeView());
		
		typeSpecificModelMap.put(SearchType.COMPONENT_CONTENT, new ComponentContentSearchTypeModel());
//		typeSpecificModelMap.put(SearchType.REFSET_CONTENT, new RefsetContentSearchTypeModel());
		typeSpecificModelMap.put(SearchType.REFSET_SPEC, new RefsetSpecSearchTypeModel());

		searchTypeSelector.setOnAction((e) -> changeSearchType());
	}
	public ComboBox<SearchType> getSearchTypeComboBox() {
		return searchTypeSelector;
	}
	
	private static void changeSearchType() {
		SearchType selection = searchTypeSelector.getSelectionModel().getSelectedItem();
		
		currentType = selection;
		searchTypeSelector.getSelectionModel().select(selection);
		setCriteriaPane(typeSpecificViewMap.get(selection).setContents(typeSpecificModelMap.get(selection)));
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
}
