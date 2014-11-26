package gov.va.isaac.gui.enhancedsearchview.model.type.refspec;

import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.model.type.SearchTypeSpecificView;
import gov.va.isaac.gui.querybuilder.QueryNodeType;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.QueryNodeTypeI;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefsetSpecSearchTypeView implements SearchTypeSpecificView {
	private BorderPane refsetSpecParentPane = new BorderPane();

	private Font boldFont = new Font("System Bold", 13.0);

	final static Logger logger = LoggerFactory.getLogger(RefsetSpecSearchTypeView.class);

	@Override
	public Pane setContents(SearchTypeModel typeModel) {
		RefsetSpecSearchTypeModel refsetSpecModel = (RefsetSpecSearchTypeModel)typeModel;

		Pane topPane = setupTopBorder(refsetSpecModel);
		
		refsetSpecParentPane.setTop(topPane);
		refsetSpecParentPane.setCenter(refsetSpecModel .getQueryNodeTreeView());
		refsetSpecParentPane.setRight(refsetSpecModel.getNodeEditorGridPane());
		
		refsetSpecParentPane.setPrefHeight(200);
		refsetSpecParentPane.setMinHeight(200);
		refsetSpecParentPane.setMaxHeight(200);
		BorderPane.setMargin(refsetSpecModel.getNodeEditorGridPane(), new Insets(10));
		BorderPane.setAlignment(refsetSpecModel.getNodeEditorGridPane(), Pos.CENTER_LEFT);
		
		loadMenus(refsetSpecModel);
		
		return refsetSpecParentPane;
	}

	private Pane setupTopBorder(RefsetSpecSearchTypeModel refsetSpecModel) {
		Label rootExp = new Label("Root Expression");
		rootExp.setFont(boldFont);
		
		GridPane topLevelGridPane = new GridPane();

		topLevelGridPane.setHgap(15);
		topLevelGridPane.setPadding(new Insets(15));

		GridPane.setConstraints(rootExp,  0,  0,  1,  1,  HPos.LEFT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		GridPane.setConstraints(refsetSpecModel.getRootNodeTypeComboBox(),  1,  0,  1,  1,  HPos.RIGHT,  VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
		topLevelGridPane.addRow(0, rootExp, refsetSpecModel.getRootNodeTypeComboBox());

		return topLevelGridPane;
	}

	/*
	public void setUnsupportedQueryNodeTypes(QueryNodeTypeI...nodeTypes){
		unsupportedQueryNodeTypes.clear();
		unsupportedQueryNodeTypes.addAll(Arrays.asList(nodeTypes));
	}
*/
	private List<QueryNodeType> getSupportedQueryNodeTypes(List<QueryNodeTypeI> unsupportedNodes, QueryNodeType...nodeTypes) {
		ArrayList<QueryNodeType> supportedNodeTypes = new ArrayList<>();
		
		for (QueryNodeType nodeType : nodeTypes) {
			if (! unsupportedNodes.contains(nodeType)) {
				supportedNodeTypes.add(nodeType);
			}
		}
		
		return supportedNodeTypes;
	}

	
	protected void loadMenus(RefsetSpecSearchTypeModel refsetSpecModel) {
		List<QueryNodeType> groupingNodeTypes = getSupportedQueryNodeTypes(
				refsetSpecModel.getUnsupportedQueryNodeTypes(),
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR);
		
		List<QueryNodeType> conceptAssertionNodeTypes = getSupportedQueryNodeTypes(
				refsetSpecModel.getUnsupportedQueryNodeTypes(),
				QueryNodeType.CONCEPT_IS,
				QueryNodeType.CONCEPT_IS_CHILD_OF,
				QueryNodeType.CONCEPT_IS_DESCENDANT_OF,
				QueryNodeType.CONCEPT_IS_KIND_OF);
		
		List<QueryNodeType> descriptionAssertionNodeTypes = getSupportedQueryNodeTypes(
				refsetSpecModel.getUnsupportedQueryNodeTypes(),
				QueryNodeType.DESCRIPTION_CONTAINS
				);
		
		List<QueryNodeType> relationshipAssertionNodeTypes = getSupportedQueryNodeTypes(
				refsetSpecModel.getUnsupportedQueryNodeTypes(),
				QueryNodeType.REL_RESTRICTION,
				QueryNodeType.REL_TYPE
				);
		
		// Add to dropdown
		boolean separatorNeeded = false;
		if (groupingNodeTypes.size() > 0) {
			refsetSpecModel.getRootNodeTypeComboBox().getItems().addAll(groupingNodeTypes);
			separatorNeeded = true;
		}
		if (conceptAssertionNodeTypes.size() > 0) {
			if (separatorNeeded) {
				refsetSpecModel.getRootNodeTypeComboBox().getItems().add(new Separator());
			}
			refsetSpecModel.getRootNodeTypeComboBox().getItems().addAll(conceptAssertionNodeTypes);
		}
		if (descriptionAssertionNodeTypes.size() > 0) {
			if (separatorNeeded) {
				refsetSpecModel.getRootNodeTypeComboBox().getItems().add(new Separator());
			}
			refsetSpecModel.getRootNodeTypeComboBox().getItems().addAll(descriptionAssertionNodeTypes);
		}
		if (relationshipAssertionNodeTypes.size() > 0) {
			if (separatorNeeded) {
				refsetSpecModel.getRootNodeTypeComboBox().getItems().add(new Separator());
			}
			refsetSpecModel.getRootNodeTypeComboBox().getItems().addAll(relationshipAssertionNodeTypes);
		}
		
		if (refsetSpecModel.getQueryNodeTreeView().getContextMenu() != null) {
			refsetSpecModel.getQueryNodeTreeView().getContextMenu().getItems().clear();
		} else {
			refsetSpecModel.getQueryNodeTreeView().setContextMenu(new ContextMenu());
		}
		refsetSpecModel.addContextMenus(refsetSpecModel.getQueryNodeTreeView().getContextMenu(), refsetSpecModel.getQueryNodeTreeView());
	}

}
