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
import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable;
import gov.va.isaac.interfaces.utility.DialogResponse;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

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
	
	//private static BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
	
	@FXML private BorderPane borderPane;
	@FXML private Button closeButton;
	@FXML private ComboBox<Object> rootNodeTypeComboBox;
	//@FXML private TreeView<NodeDraggable> queryNodeTypeTreeView;
	@FXML private TreeView<NodeDraggable> queryNodeTreeView;
	@FXML private GridPane nodeEditorGridPane;
	@FXML private Button buildQueryButton;
	@FXML private Button executeQueryButton;
	
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
		addContextMenus(queryNodeTreeView.getContextMenu(), queryNodeTreeView);

		queryNodeTreeView.rootProperty().addListener(new ChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void changed(
					ObservableValue<? extends TreeItem<NodeDraggable>> observable,
					TreeItem<NodeDraggable> oldValue,
					TreeItem<NodeDraggable> newValue) {
				if (newValue == null) {
					rootNodeTypeComboBox.getButtonCell().setText("");
					addContextMenus(queryNodeTreeView.getContextMenu(), queryNodeTreeView);
					queryNodeTreeView.setTooltip(new Tooltip("Right-click on TreeView or left-click ComboBox to select root expression"));
				} else {
					rootNodeTypeComboBox.getButtonCell().setText(QueryNodeType.valueOf(observable.getValue().getValue()).displayName());
					queryNodeTreeView.getContextMenu().getItems().clear();
					queryNodeTreeView.setTooltip(null);
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

	private void addSubMenu(ContextMenu menu, Node ownerNode, String subMenuName, QueryNodeType...nodeTypes) {
		Menu groupingMenu = new Menu(subMenuName);
		
		for (QueryNodeType type : nodeTypes) {
			MenuItem menuItem = new MenuItem(type.displayName());
			menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					if (ownerNode instanceof TreeView) {
						((TreeView<NodeDraggable>)ownerNode).setRoot(new TreeItem<>(type.constructNode()));
					} else if (ownerNode instanceof TreeCell) {
						TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
						TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
						currentTreeItem.getChildren().add(new TreeItem<>(type.constructNode()));
						currentTreeItem.setExpanded(true);
					} else {
						String error = 	"Unexpected/innappropriate ContextMenu owner of type " + ownerNode.getClass().getName() + ": " + ownerNode;

						logger.error(error);

						throw new IllegalArgumentException(error);
					}
				}
			});
			groupingMenu.getItems().add(menuItem);
		}
		menu.getItems().add(groupingMenu);
	}

	private void addContextMenus(ContextMenu menu, Node ownerNode) {
		TreeItem<NodeDraggable> currentTreeItem = null;
		NodeDraggable currentNode = null;
		if (ownerNode instanceof TreeCell
				&& (currentTreeItem = ((TreeCell<NodeDraggable>)ownerNode).getTreeItem()) != null
				&& (currentNode = currentTreeItem.getValue()) != null
				&& currentNode instanceof ParentNodeDraggable) {
			// ok
		} else if (ownerNode instanceof TreeView) {
			// ok
		} else {
			// bad
			String error = 	"Unexpected/innappropriate ContextMenu owner of type " + ownerNode.getClass().getName() + ": " + ownerNode + " with currentTreeItem=" + currentTreeItem + " and currentNode=" + currentNode;

			logger.error(error);

			throw new IllegalArgumentException(error);
		}

		logger.debug("Configuring parent node context menu for {}", ownerNode.getClass().getName());
		
		addSubMenu(menu, ownerNode, "Grouping",
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR);
		
		addSubMenu(menu, ownerNode, "Concept Assertion",
				QueryNodeType.CONCEPT_IS,
				QueryNodeType.CONCEPT_IS_CHILD_OF,
				QueryNodeType.CONCEPT_IS_DESCENDANT_OF,
				QueryNodeType.CONCEPT_IS_KIND_OF);
		
		addSubMenu(menu, ownerNode, "String Assertion",
				QueryNodeType.DESCRIPTION_CONTAINS);
				//QueryNodeType.DESCRIPTION_LUCENE_MATCH
				//,QueryNodeType.DESCRIPTION_REGEX_MATCH
		
		addSubMenu(menu, ownerNode, "Relationship Assertion",
				QueryNodeType.REL_TYPE);
		
		addSubMenu(menu, ownerNode, "Refset Assertion",
				QueryNodeType.REFSET_CONTAINS_CONCEPT,
				QueryNodeType.REFSET_CONTAINS_KIND_OF_CONCEPT,
				QueryNodeType.REFSET_CONTAINS_STRING);
	}
	
//	private static class Node<T> {
//        private final T data;
//        private final Node<T> parent;
//        private final List<Node<T>> children = new ArrayList<>();
//        
//        public Node(T data) {
//        	this(null, data);
//        }
//        public Node(Node<T> parent, T data) {
//        	this.parent = parent;
//        	this.data = data;
//        }
//        public Node(Node<T> parent, T data, List<Node<T>> children) {
//        	this.parent = parent;
//        	this.data = data;
//        	this.children.clear();
//        	this.children.addAll(children);
//        }
//    }
//	private NodeDraggable createTreeNodeFromClause(Clause clause) {
//		
//	}

	void setStage(QueryBuilderView stage) {
		this.stage = stage;
	}
	QueryBuilderView getStage() {
		return stage;
	}

	public Region getRootNode() {
		return borderPane;
	}
}
