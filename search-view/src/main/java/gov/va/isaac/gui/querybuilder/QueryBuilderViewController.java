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
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.Invertable;
import gov.va.isaac.gui.querybuilder.node.LogicalNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable;
import gov.va.isaac.gui.querybuilder.node.SingleConceptAssertionNode;
import gov.va.isaac.util.WBUtility;

import java.util.HashMap;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * QueryBuilder
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a> 
 */
public class QueryBuilderViewController
{	
	private final static Logger logger = LoggerFactory.getLogger(QueryBuilderViewController.class);
	
	@FXML private BorderPane borderPane;
	@FXML private Button closeButton;
	@FXML private ComboBox<Object> rootNodeTypeComboBox;
	//@FXML private TreeView<NodeDraggable> queryNodeTypeTreeView;
	@FXML private TreeView<NodeDraggable> queryNodeTreeView;
	@FXML private GridPane nodeEditorGridPane;
	@FXML private Button buildQueryButton;
	
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

		AppContext.getServiceLocator().inject(this);

		initializeQueryNodeTreeView();
//		initializeQueryNodeTypeTreeView();
		initializeRootNodeTypeComboBox();

		closeButton.setOnAction((action) -> {
			stage.close();
		});
		
		buildQueryButton.disableProperty().bind(queryNodeTreeViewIsValidProperty.not());
		buildQueryButton.setOnAction((action) -> {
			generateQueryFromTree();
		});
	}

	private boolean isQueryNodeTreeViewValid() {
		return isValidExpression(queryNodeTreeView.getRoot());
	}
	private boolean isValidExpression(TreeItem<NodeDraggable> treeNode) {
		if (treeNode == null) {
			return false;
		}
		if (treeNode.getValue() == null) {
			return false;
		}
		if (treeNode.getValue() instanceof Invertable) {
			return ((AssertionNode)treeNode.getValue()).getIsValid();
		} else if (treeNode.getValue() instanceof LogicalNode) {
			LogicalNode logicalNode = (LogicalNode)treeNode.getValue();
			int numChildren = treeNode.getChildren().size();
			if (numChildren > logicalNode.getMaxChildren() || numChildren < logicalNode.getMinimumChildren()) {
				return false;
			}
			Boolean childrenValid = null;
			for (TreeItem<NodeDraggable> childTreeNode : treeNode.getChildren()) {
				if (isValidExpression(childTreeNode)) {
					if (childrenValid == null) {
						childrenValid = true;
					}
				} else {
					childrenValid = false;
				}
			}
			
			return childrenValid != null ? childrenValid : false;
		} else {
			logger.warn("isValidExpression() encountered node {} of unexpected type {}.  Expected AssertionNode or LogicalNode.", treeNode.getValue().getDescription(), treeNode.getValue());
			return false;
		}
	}
	
	private void initializeRootNodeTypeComboBox() {
		rootNodeTypeComboBox.setEditable(false);
		rootNodeTypeComboBox.setButtonCell(new ListCell<Object>() {
			@Override
			protected void updateItem(Object t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.toString());
				}
			}
		});
		rootNodeTypeComboBox.setOnAction((event) -> {
			if (rootNodeTypeComboBox.getSelectionModel().getSelectedItem() != null) {
				queryNodeTreeView.setRoot(new TreeItem<>(((QueryNodeType)rootNodeTypeComboBox.getSelectionModel().getSelectedItem()).construct()));
			}
		});

		rootNodeTypeComboBox.getItems().addAll(
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR,
				new Separator(),
				QueryNodeType.CONCEPT_IS,
				QueryNodeType.CONCEPT_IS_CHILD_OF,
				QueryNodeType.CONCEPT_IS_DESCENDANT_OF);
	}
	private void initializeQueryNodeTreeView() {
		if (queryNodeTreeView.getContextMenu() == null) {
			queryNodeTreeView.setContextMenu(new ContextMenu());
		}
		queryNodeTreeView.getContextMenu().getItems().clear();
		
		class QueryNodeTreeViewTreeCell extends DragAndDropTreeCell<NodeDraggable> {
			public QueryNodeTreeViewTreeCell(
					TreeView<NodeDraggable> parentTree,
					Map<String, NodeDraggable> cache) {
				super(parentTree, cache);
			}

			protected void updateTextFillColor(NodeDraggable node) {
				Color color = Color.BLACK;
				if (node != null && node.getIsValid()) {
					color = Color.BLACK;
					queryNodeTreeViewIsValidProperty.set(isQueryNodeTreeViewValid());
				} else {
					color = Color.RED;
					queryNodeTreeViewIsValidProperty.set(false);
				}
				logger.debug("Setting initial text fill color of cell containing \"{}\" to {}", (node != null ? node.getDescription() : null), color);
				setTextFill(color);
			}

			@Override
			protected void updateItem(NodeDraggable item, boolean empty) {
				super.updateItem(item, empty);
				this.item = item;
				String text = (item == null) ? null : item.getDescription();
				this.textProperty().unbind();
				setText(text);
				if (item != null) {
					//TreeItem<NodeDraggable> currentTreeItem = search(item);
					
					//Label graphic = new Label(item.getDescription());
					if (getContextMenu() == null) {
						setContextMenu(new ContextMenu());
					}
					getContextMenu().getItems().clear();
					if (item instanceof ParentNodeDraggable) {
						addParentMenus(getContextMenu(), this);
						getContextMenu().getItems().add(new SeparatorMenuItem());
					}
					addDeleteMenuItem(getContextMenu(), this);
				} else {
					if (getContextMenu() != null) {
						getContextMenu().getItems().clear();
					}
					setContextMenu(null);
				}
				
				updateTextFillColor();
				
				if (getItem() != null && getItem() instanceof AssertionNode) {
					AssertionNode assertionNode = (AssertionNode)getItem();
					this.textProperty().bind(assertionNode.getDescriptionProperty());
					assertionNode.getIsValidProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue,
								Boolean newValue) {
							updateTextFillColor(assertionNode);
						}
					});
				} else if (getItem() != null && getItem() instanceof LogicalNode) {
					LogicalNode logicalNode = (LogicalNode)getItem();
					this.textProperty().bind(logicalNode.getDescriptionProperty());
					if (getTreeItem().getChildren().size() < logicalNode.getMinimumChildren()
							|| getTreeItem().getChildren().size() > logicalNode.getMaxChildren()) {
						logicalNode.setIsValid(false);
					} else {
						logicalNode.setIsValid(true);
					}
					getTreeItem().getChildren().addListener(new ListChangeListener<TreeItem<NodeDraggable>>() {
						@Override
						public void onChanged(
								javafx.collections.ListChangeListener.Change<? extends TreeItem<NodeDraggable>> c) {
							if (getTreeItem().getChildren().size() < logicalNode.getMinimumChildren()
									|| getTreeItem().getChildren().size() > logicalNode.getMaxChildren()) {
								logicalNode.setIsValid(false);
							} else {
								logicalNode.setIsValid(true);
							}
						}
					});
					logicalNode.getIsValidProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue,
								Boolean newValue) {
							updateTextFillColor(logicalNode);
						}
					});
				}
			}
		}
		queryNodeTreeView.setCellFactory(new Callback<TreeView<NodeDraggable>, TreeCell<NodeDraggable>>() {
			@Override
			public TreeCell<NodeDraggable> call(TreeView<NodeDraggable> param) {
				final QueryNodeTreeViewTreeCell newCell = new QueryNodeTreeViewTreeCell(param, nodeDragCache);

				newCell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						TreeCell<NodeDraggable> c = (TreeCell<NodeDraggable>) event.getSource();
						logger.debug("{} single clicked. Cell text: {}", event.getButton(), c.getText());

						if (event.getClickCount() == 1) {

							NodeDraggable draggableNode = c.getItem();

							logger.debug("{} single clicked. Cell contains item: {}", event.getButton(), draggableNode);

							if (draggableNode == null) {
								String error = "Single-clicked TreeCell has null item";
								Log.warn(error);

								//throw new RuntimeException(error);
							} else {
								if (draggableNode instanceof LogicalNode) {
									LogicalNode logicalNode = (LogicalNode)draggableNode;
									nodeEditorGridPane.getChildren().clear();
									int rowIndex = 0;
									nodeEditorGridPane.addRow(rowIndex++, new Label(logicalNode.getNodeTypeName()));
									
									CheckBox inversionCheckBox = new CheckBox();
									inversionCheckBox.setText("Invert (NOT)");
									inversionCheckBox.setSelected(logicalNode.getInvert());
									
									//singleConceptAssertionNode.getInvertProperty().bind(inversionCheckBox.selectedProperty());
									inversionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
										@Override
										public void changed(
												ObservableValue<? extends Boolean> observable,
												Boolean oldValue,
												Boolean newValue) {
											logicalNode.setInvert(newValue);
										}
									});
									nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
								} else if (draggableNode instanceof SingleConceptAssertionNode) {
									SingleConceptAssertionNode singleConceptAssertionNode = (SingleConceptAssertionNode)draggableNode;
									nodeEditorGridPane.getChildren().clear();
									int rowIndex = 0;
									nodeEditorGridPane.addRow(rowIndex++, new Label(draggableNode.getNodeTypeName()));
									ConceptVersionBI currentConcept = null;
									if (singleConceptAssertionNode.getNid() != null) {
										currentConcept = WBUtility.getConceptVersion(singleConceptAssertionNode.getNid());
									}
									ConceptNode conceptNode = new ConceptNode(currentConcept, true);
									conceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
										@Override
										public void changed(
												ObservableValue<? extends ConceptVersionBI> observable,
												ConceptVersionBI oldValue,
												ConceptVersionBI newValue) {
											if (newValue == null) {
												singleConceptAssertionNode.getNidProperty().set(0);
											} else {
												singleConceptAssertionNode.setNid(newValue.getConceptNid());
											}
										}
									});
									nodeEditorGridPane.addRow(rowIndex++, new Label("Concept"), conceptNode.getNode());
									CheckBox inversionCheckBox = new CheckBox();
									inversionCheckBox.setText("Invert (NOT)");
									inversionCheckBox.setSelected(singleConceptAssertionNode.getInvert());
									
									//singleConceptAssertionNode.getInvertProperty().bind(inversionCheckBox.selectedProperty());
									inversionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
										@Override
										public void changed(
												ObservableValue<? extends Boolean> observable,
												Boolean oldValue,
												Boolean newValue) {
											singleConceptAssertionNode.setInvert(newValue);
										}
									});
									nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
								}
							}
						}
//						else if (event.getClickCount() > 1) {
//							logger.debug(event.getButton() + " double clicked. Cell text: " + c.getText());
//						}
					}
				});
				return newCell;
			}
			
		});
		
		queryNodeTreeView.rootProperty().addListener(new ChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void changed(
					ObservableValue<? extends TreeItem<NodeDraggable>> observable,
					TreeItem<NodeDraggable> oldValue,
					TreeItem<NodeDraggable> newValue) {
				if (newValue != null) {
					queryNodeTreeViewIsValidProperty.set(isQueryNodeTreeViewValid());
				} else {
					queryNodeTreeViewIsValidProperty.set(false);
				}
			}
		});
	}
	
//	private static TreeItem<NodeDraggable> findRootNode(TreeItem<NodeDraggable> node) {
//		if (node.getParent() == null) {
//			return node;
//		} else {
//			return findRootNode(node.getParent());
//		}
//	}
	private void addDeleteMenuItem(ContextMenu menu, TreeCell<NodeDraggable> currentTreeCell) {
		TreeItem<NodeDraggable> currentTreeItem = currentTreeCell.getTreeItem();
		MenuItem deleteMenuItem = new MenuItem("Delete");
		deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (currentTreeItem.getParent() == null) {
					queryNodeTreeView.setRoot(null);
					rootNodeTypeComboBox.getSelectionModel().clearSelection();

					nodeEditorGridPane.getChildren().clear();
				} else {
					currentTreeItem.getParent().getChildren().remove(currentTreeItem);

					nodeEditorGridPane.getChildren().clear();
				}	
				//currentTreeCell.setContextMenu(null);
				//currentTreeCell.setDisable(true);
			}
		});
		menu.getItems().add(deleteMenuItem);
	}
	private void addParentMenus(ContextMenu menu, TreeCell<NodeDraggable> ownerNode) {
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

		Menu groupingMenu = new Menu("Grouping");
		QueryNodeType[] supportedGroupingNodes = new QueryNodeType[] {
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR
		};
		for (QueryNodeType type : supportedGroupingNodes) {
			MenuItem menuItem = new MenuItem(type.name());
			menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
					TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
					currentTreeItem.getChildren().add(new TreeItem<>(type.construct()));
					currentTreeItem.setExpanded(true);
				}
			});
			groupingMenu.getItems().add(menuItem);
		}
		menu.getItems().add(groupingMenu);

		Menu assertionMenu = new Menu("Concept Assertion");
		QueryNodeType[] supportedAssertionNodes = new QueryNodeType[] {
				QueryNodeType.CONCEPT_IS,
				QueryNodeType.CONCEPT_IS_CHILD_OF,
				QueryNodeType.CONCEPT_IS_DESCENDANT_OF
		};
		for (QueryNodeType type : supportedAssertionNodes) {
			MenuItem menuItem = new MenuItem(type.name());
			menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					TreeCell<NodeDraggable> cell = (TreeCell<NodeDraggable>)ownerNode;
					TreeItem<NodeDraggable> currentTreeItem = cell.getTreeItem();
					currentTreeItem.getChildren().add(new TreeItem<>(type.construct()));
					currentTreeItem.setExpanded(true);
				}
			});
			assertionMenu.getItems().add(menuItem);
		}
		menu.getItems().add(assertionMenu);
	}

	private void loadContent()
	{
	}
 
	private Query generateQueryFromTree() {
		logger.debug("Generating Query from tree...");

		// TODO: implement generateQueryFromTree()
		return null;
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
//		public String toString() { return nodeType.name(); }
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
