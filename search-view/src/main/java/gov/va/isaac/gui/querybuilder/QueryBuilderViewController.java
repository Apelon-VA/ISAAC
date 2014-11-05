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
package gov.va.isaac.gui.querybuilder;


import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.LogicalNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * QueryBuilder
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a> 
 */
public class QueryBuilderViewController
{	
	final static Logger logger = LoggerFactory.getLogger(QueryBuilderViewController.class);
	
	private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
	
	@FXML private BorderPane borderPane;
	@FXML private Button closeButton;
	@FXML private ComboBox<Object> rootNodeTypeComboBox;
	//@FXML private TreeView<NodeDraggable> queryNodeTypeTreeView;
	@FXML private TreeView<NodeDraggable> queryNodeTreeView;
	@FXML private GridPane nodeEditorGridPane;
	@FXML private Button buildQueryButton;
	@FXML private Button executeQueryButton;
	
	//private BusyPopover searchRunningPopover;

	private BooleanProperty queryNodeTreeViewIsValidProperty = new SimpleBooleanProperty(false);
	
	private QueryBuilderView stage;
	private Map<String, NodeDraggable> nodeDragCache = new HashMap<>();
	
	// Initialize GUI (invoked by FXML)
	@FXML
	void initialize()
	{
		assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert closeButton != null : "fx:id=\"closeButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		//assert queryNodeTypeTreeView != null : "fx:id=\"queryNodeTypeTreeView\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert queryNodeTreeView != null : "fx:id=\"queryNodeTreeView\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert rootNodeTypeComboBox != null : "fx:id=\"rootNodeTypeComboBox\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert nodeEditorGridPane != null : "fx:id=\"nodeEditorGridPane\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert buildQueryButton != null : "fx:id=\"buildQueryButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";
		assert executeQueryButton != null : "fx:id=\"executeQueryButton\" was not injected: check your FXML file 'EnhancedSearchView.fxml'.";

		AppContext.getServiceLocator().inject(this);

		initializeQueryNodeTreeView();
//		initializeQueryNodeTypeTreeView();
		initializeRootNodeTypeComboBox();

		closeButton.setOnAction((action) -> {
			stage.close();
		});
		
		buildQueryButton.disableProperty().bind(queryNodeTreeViewIsValidProperty.not());
		buildQueryButton.setOnAction((action) -> {
			QueryBuilderHelper.generateQuery(queryNodeTreeView);
		});
		
		executeQueryButton.disableProperty().bind(buildQueryButton.disableProperty());
		executeQueryButton.setOnAction((action) -> {
			try {
				QueryBuilderHelper.executeQuery(QueryBuilderHelper.generateQuery(queryNodeTreeView), queryNodeTreeView);
			} catch (Exception e) {
				logger.error("Failed executing query.  Caught {} {}.", e.getClass().getName(), e.getLocalizedMessage());

				e.printStackTrace();
			
				String title = "Query Execution Failed";
				String msg = "Failed executing Query";
				String details = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\".";
				AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}
		});
	}
	
	private void initializeRootNodeTypeComboBox() {
		rootNodeTypeComboBox.setEditable(false);
		rootNodeTypeComboBox.setButtonCell(new ListCell<Object>() {
			@Override
			protected void updateItem(Object t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln || t == null || ! (t instanceof QueryNodeType)) {
					if (queryNodeTreeView.getRoot() == null) {
						setText("");
					}
					else {
						setText(QueryNodeType.valueOf(queryNodeTreeView.getRoot().getValue()).displayName());
					}
				} else {
					setText(QueryNodeType.valueOf(queryNodeTreeView.getRoot().getValue()).displayName());
				}
			}
		});

		rootNodeTypeComboBox.setOnAction((event) -> {
			if (rootNodeTypeComboBox.getSelectionModel().getSelectedItem() != null
					&& ! (rootNodeTypeComboBox.getSelectionModel().getSelectedItem() instanceof Separator)) {
				if (queryNodeTreeView.getRoot() != null && rootNodeTypeComboBox.getSelectionModel().getSelectedItem() instanceof QueryNodeType) {
					QueryNodeType selectedType = (QueryNodeType)rootNodeTypeComboBox.getSelectionModel().getSelectedItem();
					
					if (selectedType.getNodeClass() == queryNodeTreeView.getRoot().getClass()) {
						// Don't reset root node of same type
						
						event.consume();
						return;
					}
				}
				
				boolean resetRoot = true;
				if (queryNodeTreeView.getRoot() != null
						&& queryNodeTreeView.getRoot().getChildren().size() > 0
						|| (queryNodeTreeView.getRoot() != null && queryNodeTreeView.getRoot().getValue() != null && (queryNodeTreeView.getRoot().getValue() instanceof AssertionNode) && queryNodeTreeView.getRoot().getValue().getIsValid())) {
					int numDescendants = QueryBuilderHelper.getDescendants(queryNodeTreeView.getRoot()).size();
					DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Root Expression Change Confirmation", "Are you sure you want to reset root expression from " + QueryNodeType.valueOf(queryNodeTreeView.getRoot().getValue()) + " to " + rootNodeTypeComboBox.getSelectionModel().getSelectedItem() + "?" + (numDescendants > 0 ? ("\n\n" + numDescendants + " descendent expression(s) will be deleted") : ""));
					if (response == DialogResponse.YES) {
						resetRoot = true;
					} else {
						resetRoot = false;
					}
				}

				if (resetRoot) {
					queryNodeTreeView.setRoot(new TreeItem<>(((QueryNodeType)rootNodeTypeComboBox.getSelectionModel().getSelectedItem()).constructNode()));

					queryNodeTreeView.getSelectionModel().select(queryNodeTreeView.getRoot());
				} else {
					if (queryNodeTreeView.getRoot() == null) {
						rootNodeTypeComboBox.getButtonCell().setText("");
					}
					else {
						rootNodeTypeComboBox.getButtonCell().setText(QueryNodeType.valueOf(queryNodeTreeView.getRoot().getValue()).displayName());
					}
					event.consume();
				}
			}
		});

		rootNodeTypeComboBox.getItems().addAll(
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR,
				new Separator(),
				QueryNodeType.CONCEPT_IS,
				QueryNodeType.CONCEPT_IS_CHILD_OF,
				QueryNodeType.CONCEPT_IS_DESCENDANT_OF,
				QueryNodeType.CONCEPT_IS_KIND_OF,
				new Separator(),
				QueryNodeType.DESCRIPTION_CONTAINS,
				//QueryNodeType.DESCRIPTION_LUCENE_MATCH,
				//QueryNodeType.DESCRIPTION_REGEX_MATCH,
				new Separator(),
				QueryNodeType.REL_TYPE
				);
	}

	private void initializeQueryNodeTreeView() {
		QueryBuilderHelper.initializeQueryNodeTreeView(queryNodeTreeView, nodeEditorGridPane, queryNodeTreeViewIsValidProperty);
		
		if (queryNodeTreeView.getContextMenu() == null) {
			queryNodeTreeView.setContextMenu(new ContextMenu());
		}
		queryNodeTreeView.getContextMenu().getItems().clear();

		queryNodeTreeView.rootProperty().addListener(new ChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void changed(
					ObservableValue<? extends TreeItem<NodeDraggable>> observable,
					TreeItem<NodeDraggable> oldValue,
					TreeItem<NodeDraggable> newValue) {
				if (newValue == null) {
					rootNodeTypeComboBox.getButtonCell().setText("");
				} else {
					rootNodeTypeComboBox.getButtonCell().setText(QueryNodeType.valueOf(observable.getValue().getValue()).displayName());
				}
			}
		});
		queryNodeTreeView.setCellFactory(new Callback<TreeView<NodeDraggable>, TreeCell<NodeDraggable>>() {
			@Override
			public TreeCell<NodeDraggable> call(TreeView<NodeDraggable> param) {
				final QueryNodeTreeViewTreeCell newCell = new QueryNodeTreeViewTreeCell(param, nodeDragCache, queryNodeTreeViewIsValidProperty) {
					@Override
					public void setCellContextMenus() {
						if (getItem() != null) {
							if (getContextMenu() == null) {
								setContextMenu(new ContextMenu());
							}
							getContextMenu().getItems().clear();
							
							addNewParentContextMenu(getContextMenu(), this);
							getContextMenu().getItems().add(new SeparatorMenuItem());
							
							if (getItem() instanceof ParentNodeDraggable) {
								addContextMenus(getContextMenu(), this);
								getContextMenu().getItems().add(new SeparatorMenuItem());
							}
							addDeleteMenuItem(getContextMenu(), this);
						} else {
							if (getContextMenu() != null) {
								getContextMenu().getItems().clear();
							}
							setContextMenu(null);
						}

					}
				};
				
				return newCell;
			}
		});
	}
	
	private void addDeleteMenuItem(ContextMenu menu, TreeCell<NodeDraggable> currentTreeCell) {
		TreeItem<NodeDraggable> currentTreeItem = currentTreeCell.getTreeItem();
		MenuItem deleteMenuItem = new MenuItem("Delete");
		deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (QueryBuilderHelper.getUserConfirmToDeleteNodeIfNecessary(currentTreeItem)) {
					if (currentTreeItem.getParent() == null) {
						queryNodeTreeView.setRoot(null);
						rootNodeTypeComboBox.getSelectionModel().clearSelection();

						nodeEditorGridPane.getChildren().clear();
					} else {
						currentTreeItem.getParent().getChildren().remove(currentTreeItem);

						nodeEditorGridPane.getChildren().clear();
					}	
				}
			}
		});
		menu.getItems().add(deleteMenuItem);
	}
	private void addNewParentContextMenu(ContextMenu menu, TreeCell<NodeDraggable> ownerNode) {
		{
			Menu newParentMenu = new Menu("New Parent");
			QueryNodeType[] supportedNewParentNodes = new QueryNodeType[] {
					QueryNodeType.AND,
					QueryNodeType.OR,
					QueryNodeType.XOR
			};
			for (QueryNodeType type : supportedNewParentNodes) {
				MenuItem menuItem = new MenuItem(type.displayName());
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
						TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
						
						TreeItem<NodeDraggable> newParent = new TreeItem<>(type.constructNode());

						if (currentTreeItem.getParent() == null) {
							queryNodeTreeView.setRoot(newParent);
							newParent.getChildren().add(currentTreeItem);
						} else {
							TreeItem<NodeDraggable> oldParent = currentTreeItem.getParent();
							oldParent.getChildren().remove(currentTreeItem);
							newParent.getChildren().add(currentTreeItem);
							oldParent.getChildren().add(newParent);
							oldParent.setExpanded(true);
						}

						queryNodeTreeView.getSelectionModel().select(newParent);
						newParent.setExpanded(true);
					}
				});
				newParentMenu.getItems().add(menuItem);
			}
			menu.getItems().add(newParentMenu);
		}
	}
	private void addContextMenus(ContextMenu menu, TreeCell<NodeDraggable> ownerNode) {
		TreeItem<NodeDraggable> currentTreeItem = null;
		NodeDraggable currentNode = null;
		if ((currentTreeItem = ((TreeCell<NodeDraggable>)ownerNode).getTreeItem()) != null
				&& (currentNode = currentTreeItem.getValue()) != null
				&& currentNode instanceof ParentNodeDraggable) {
			// ok
		} else {
			// bad
			String error = 	"Unexpected/innappropriate ContextMenu owner of type " + ownerNode.getClass().getName() + ": " + ownerNode + " with currentTreeItem=" + currentTreeItem + " and currentNode=" + currentNode;

			logger.error(error);

			throw new IllegalArgumentException(error);
		}

		logger.debug("Configuring parent node context menu for {}", ownerNode.getClass().getName());
		
		
		{
			Menu groupingMenu = new Menu("Grouping");
			QueryNodeType[] supportedGroupingNodes = new QueryNodeType[] {
					QueryNodeType.AND,
					QueryNodeType.OR,
					QueryNodeType.XOR
			};
			for (QueryNodeType type : supportedGroupingNodes) {
				MenuItem menuItem = new MenuItem(type.displayName());
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
						TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
						currentTreeItem.getChildren().add(new TreeItem<>(type.constructNode()));
						currentTreeItem.setExpanded(true);
					}
				});
				groupingMenu.getItems().add(menuItem);
			}
			menu.getItems().add(groupingMenu);
		}

		{
			Menu conceptAssertionMenu = new Menu("Concept Assertion");
			QueryNodeType[] supportedConceptAssertionNodes = new QueryNodeType[] {
					QueryNodeType.CONCEPT_IS,
					QueryNodeType.CONCEPT_IS_CHILD_OF,
					QueryNodeType.CONCEPT_IS_DESCENDANT_OF,
					QueryNodeType.CONCEPT_IS_KIND_OF
			};
			for (QueryNodeType type : supportedConceptAssertionNodes) {
				MenuItem menuItem = new MenuItem(type.displayName());
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
						TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
						currentTreeItem.getChildren().add(new TreeItem<>(type.constructNode()));
						currentTreeItem.setExpanded(true);
					}
				});
				conceptAssertionMenu.getItems().add(menuItem);
			}
			menu.getItems().add(conceptAssertionMenu);
		}
		{
			Menu stringAssertionMenu = new Menu("String Assertion");
			QueryNodeType[] supportedStringAssertionNodes = new QueryNodeType[] {
					QueryNodeType.DESCRIPTION_CONTAINS
					//QueryNodeType.DESCRIPTION_LUCENE_MATCH
					//,QueryNodeType.DESCRIPTION_REGEX_MATCH
			};
			for (QueryNodeType type : supportedStringAssertionNodes) {
				MenuItem menuItem = new MenuItem(type.displayName());
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
						TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
						currentTreeItem.getChildren().add(new TreeItem<>(type.constructNode()));
						currentTreeItem.setExpanded(true);
					}
				});
				stringAssertionMenu.getItems().add(menuItem);
			}
			menu.getItems().add(stringAssertionMenu);
		}

		{
			Menu relAssertionMenu = new Menu("Relationship Assertion");
			QueryNodeType[] supportedRelAssertionNodes = new QueryNodeType[] {
					QueryNodeType.REL_TYPE
			};
			for (QueryNodeType type : supportedRelAssertionNodes) {
				MenuItem menuItem = new MenuItem(type.displayName());
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
						TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
						currentTreeItem.getChildren().add(new TreeItem<>(type.constructNode()));
						currentTreeItem.setExpanded(true);
					}
				});
				relAssertionMenu.getItems().add(menuItem);
			}
			menu.getItems().add(relAssertionMenu);
		}
	}

	private void loadContent()
	{
	}
	
	private static class Node<T> {
        private final T data;
        private final Node<T> parent;
        private final List<Node<T>> children = new ArrayList<>();
        
        public Node(T data) {
        	this(null, data);
        }
        public Node(Node<T> parent, T data) {
        	this.parent = parent;
        	this.data = data;
        }
        public Node(Node<T> parent, T data, List<Node<T>> children) {
        	this.parent = parent;
        	this.data = data;
        	this.children.clear();
        	this.children.addAll(children);
        }
    }
//	private NodeDraggable createTreeNodeFromClause(Clause clause) {
//		
//	}
	private void displayQuery(Query query) {
		
	}
	

	

//	private static class QueryNodeTypeTreeCell extends DragAndDropTreeCell<NodeDraggable> {
//		public QueryNodeTypeTreeCell(TreeView<NodeDraggable> parentTree,
//				Map<String, NodeDraggable> cache) {
//			super(parentTree, cache);
//		}
//
//		@Override
//		protected void updateItem(NodeDraggable item, boolean empty) {
//			super.updateItem(item, empty);
//			this.item = item;
//			String text = (item == null) ? null : item.toString();
//			setText(text);
//		}
//	}
	
//	private void initializeQueryNodeTypeTreeView() {
//		//queryNodeTypeTreeView.setEditable(false);
//		//queryNodeTypeTreeView.setCellFactory(cellFactory );
//		queryNodeTypeTreeView.setShowRoot(false);
//		
//		queryNodeTypeTreeView.setCellFactory(new Callback<TreeView<NodeDraggable>, TreeCell<NodeDraggable>>() {
//			@Override
//			public TreeCell<NodeDraggable> call(TreeView<NodeDraggable> param) {
//				return new QueryNodeTypeTreeCell(param, nodeDragCache);
//			}
//		});
//		
//		TreeItem<NodeDraggable> rootQueryNodeTypeItem = new TreeItem<NodeDraggable>(new QueryNodeTypeTreeParentItemData("ROOT"));
//		queryNodeTypeTreeView.setRoot(rootQueryNodeTypeItem);
//		
//		TreeItem<NodeDraggable> groupingRootItem = new TreeItem<NodeDraggable>(new QueryNodeTypeTreeParentItemData("Grouping"));
//		groupingRootItem.getChildren().add(new TreeItem<NodeDraggable>(new QueryNodeTypeTreeLeafItemData(QueryNodeType.AND)));
//		groupingRootItem.getChildren().add(new TreeItem<NodeDraggable>(new QueryNodeTypeTreeLeafItemData(QueryNodeType.OR)));
//		groupingRootItem.getChildren().add(new TreeItem<NodeDraggable>(new QueryNodeTypeTreeLeafItemData(QueryNodeType.XOR)));
//		rootQueryNodeTypeItem.getChildren().add(groupingRootItem);
//		
//		TreeItem<NodeDraggable> conceptAssertionRootItem = new TreeItem<NodeDraggable>(new QueryNodeTypeTreeParentItemData("Concept Assertion"));
//		conceptAssertionRootItem.getChildren().add(new TreeItem<NodeDraggable>(new QueryNodeTypeTreeLeafItemData(QueryNodeType.CONCEPT_IS)));
//		conceptAssertionRootItem.getChildren().add(new TreeItem<NodeDraggable>(new QueryNodeTypeTreeLeafItemData(QueryNodeType.CONCEPT_IS_CHILD_OF)));
//		conceptAssertionRootItem.getChildren().add(new TreeItem<NodeDraggable>(new QueryNodeTypeTreeLeafItemData(QueryNodeType.CONCEPT_IS_DESCENDANT_OF)));
//		rootQueryNodeTypeItem.getChildren().add(conceptAssertionRootItem);
//	}
//
//	public static class QueryNodeTypeTreeParentItemData implements ParentNodeDraggable {
//		private final String name;
//
//		public QueryNodeTypeTreeParentItemData(String name) {
//			super();
//			this.name = name;
//		}
//		
//		public String toString() { return name; }
//
//		/* (non-Javadoc)
//		 * @see gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable#canAddChild(gov.va.isaac.gui.querybuilder.node.NodeDraggable)
//		 */
//		@Override
//		public boolean canAddChild(NodeDraggable child) {
//			return false;
//		}
//		
//		public DragMode getDragMode() { return DragMode.NONE; }
//	}
//
//	public static class QueryNodeTypeTreeLeafItemData implements NodeDraggable {
//		private final QueryNodeType nodeType;
//
//		public QueryNodeTypeTreeLeafItemData(QueryNodeType nodeType) {
//			super();
//			this.nodeType = nodeType;
//		}
//
//		public QueryNodeType getNodeType() {
//			return nodeType;
//		}
//		
//		public String toString() { return nodeType.displayName(); }
//		
//		public DragMode getDragMode() { return DragMode.COPY; }
//		public NodeDraggable getItemToDrop() { return nodeType.construct(); }
//	}

	void setStage(QueryBuilderView stage) {
		this.stage = stage;
	}

	public Region getRootNode() {
		return borderPane;
	}
}
