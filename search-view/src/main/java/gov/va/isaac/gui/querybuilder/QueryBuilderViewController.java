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
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.LogicalNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable;
import gov.va.isaac.gui.querybuilder.node.RelType;
import gov.va.isaac.gui.querybuilder.node.SingleConceptAssertionNode;
import gov.va.isaac.gui.querybuilder.node.SingleStringAssertionNode;
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
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
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
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
			generateQuery();
		});
		
		executeQueryButton.disableProperty().bind(buildQueryButton.disableProperty());
		executeQueryButton.setOnAction((action) -> {
			try {
				executeQuery(generateQuery());
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
		if (treeNode.getValue() instanceof AssertionNode) {
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
				if (bln || t instanceof Separator) {
					setText("");
				} else {
					setText(t.toString());
				}
			}
		});
		rootNodeTypeComboBox.setOnAction((event) -> {
			if (rootNodeTypeComboBox.getSelectionModel().getSelectedItem() != null && ! (rootNodeTypeComboBox.getSelectionModel().getSelectedItem() instanceof Separator)) {
				queryNodeTreeView.setRoot(new TreeItem<>(((QueryNodeType)rootNodeTypeComboBox.getSelectionModel().getSelectedItem()).constructNode()));
			
				queryNodeTreeView.getSelectionModel().select(queryNodeTreeView.getRoot());
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
		queryNodeTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
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

					updateTextFillColor(assertionNode);

					queryNodeTreeView.getSelectionModel().select(getTreeItem());
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

					updateTextFillColor(logicalNode);

					queryNodeTreeView.getSelectionModel().select(getTreeItem());
				}
			}
		}
		queryNodeTreeView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends TreeItem<NodeDraggable>> c) {
				
				if (queryNodeTreeView.getSelectionModel().getSelectedItems().size() == 1) {
					handleItemSelection(queryNodeTreeView.getSelectionModel().getSelectedItems().get(0).getValue());
				}
			}
		});
		queryNodeTreeView.setCellFactory(new Callback<TreeView<NodeDraggable>, TreeCell<NodeDraggable>>() {
			@Override
			public TreeCell<NodeDraggable> call(TreeView<NodeDraggable> param) {
				final QueryNodeTreeViewTreeCell newCell = new QueryNodeTreeViewTreeCell(param, nodeDragCache);

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
	
	private void handleItemSelection(NodeDraggable draggableNode) {
		logger.debug("Cell selected containing item: {}", draggableNode);

		if (draggableNode == null) {
			String error = "Selected TreeCell has null item";
			Log.warn(error);
		} else {
			if (draggableNode instanceof LogicalNode) {
				LogicalNode logicalNode = (LogicalNode)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(logicalNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++);
				
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
				Label nodeEditorLabel = new Label(singleConceptAssertionNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++);
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
			} else if (draggableNode instanceof SingleStringAssertionNode) {
				SingleStringAssertionNode singleStringAssertionNode = (SingleStringAssertionNode)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(singleStringAssertionNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++);
				
				TextField stringNode = ((SingleStringAssertionNode)draggableNode).getStringInputField();
				
				if (singleStringAssertionNode != null) {
					stringNode.setText(singleStringAssertionNode.getString());
				}

				nodeEditorGridPane.addRow(rowIndex++, new Label("Concept"), stringNode);
				CheckBox inversionCheckBox = new CheckBox();
				inversionCheckBox.setText("Invert (NOT)");
				inversionCheckBox.setSelected(singleStringAssertionNode.getInvert());
				
				inversionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(
							ObservableValue<? extends Boolean> observable,
							Boolean oldValue,
							Boolean newValue) {
						singleStringAssertionNode.setInvert(newValue);
					}
				});
				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			} else if (draggableNode instanceof RelType) {
				RelType relTypeNode = (RelType)draggableNode;
				nodeEditorGridPane.getChildren().clear();
				int rowIndex = 0;
				Label nodeEditorLabel = new Label(relTypeNode.getNodeTypeName());
				nodeEditorGridPane.addRow(rowIndex++, nodeEditorLabel);
				nodeEditorGridPane.addRow(rowIndex++);
				
				{
					ConceptVersionBI currentRelTypeConcept = null;
					if (relTypeNode.getRelTypeConceptNid() != null) {
						currentRelTypeConcept = WBUtility.getConceptVersion(relTypeNode.getRelTypeConceptNid());
					}
					ConceptNode relTypeConceptNode = new ConceptNode(currentRelTypeConcept, true);
					relTypeConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relTypeNode.setRelTypeConceptNid(0);
							} else {
								relTypeNode.setRelTypeConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("RelType Concept"), relTypeConceptNode.getNode());
				}
				{
					ConceptVersionBI currentTargetConcept = null;
					if (relTypeNode.getTargetConceptNid() != null) {
						currentTargetConcept = WBUtility.getConceptVersion(relTypeNode.getTargetConceptNid());
					}
					ConceptNode targetConceptNode = new ConceptNode(currentTargetConcept, true);
					targetConceptNode.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
						@Override
						public void changed(
								ObservableValue<? extends ConceptVersionBI> observable,
								ConceptVersionBI oldValue,
								ConceptVersionBI newValue) {
							if (newValue == null) {
								relTypeNode.setTargetConceptNid(0);
							} else {
								relTypeNode.setTargetConceptNid(newValue.getConceptNid());
							}
						}
					});
					nodeEditorGridPane.addRow(rowIndex++, new Label("Target Concept"), targetConceptNode.getNode());
				}
				{
					CheckBox subsumptionCheckBox = new CheckBox();
					subsumptionCheckBox.setText("Use Subsumption");
					subsumptionCheckBox.setSelected(relTypeNode.getUseSubsumption());
					
					//singleConceptAssertionNode.getInvertProperty().bind(inversionCheckBox.selectedProperty());
					subsumptionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(
								ObservableValue<? extends Boolean> observable,
								Boolean oldValue,
								Boolean newValue) {
							relTypeNode.setUseSubsumption(newValue);
						}
					});

					nodeEditorGridPane.addRow(rowIndex++, subsumptionCheckBox);
				}
				
				CheckBox inversionCheckBox = new CheckBox();
				inversionCheckBox.setText("Invert (NOT)");
				inversionCheckBox.setSelected(relTypeNode.getInvert());
				
				//singleConceptAssertionNode.getInvertProperty().bind(inversionCheckBox.selectedProperty());
				inversionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(
							ObservableValue<? extends Boolean> observable,
							Boolean oldValue,
							Boolean newValue) {
						relTypeNode.setInvert(newValue);
					}
				});
				nodeEditorGridPane.addRow(rowIndex++, inversionCheckBox);
			}
		}
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

		{
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
				MenuItem menuItem = new MenuItem(type.name());
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
				MenuItem menuItem = new MenuItem(type.name());
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
				MenuItem menuItem = new MenuItem(type.name());
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
	

	private void executeQuery(Query query) {
		NativeIdSetBI result = null;
		try {
			result = generateQuery().compute();
		} catch (Exception e) {
			logger.error("Failed executing query.  Caught {} {}.", e.getClass().getName(), e.getLocalizedMessage());

			e.printStackTrace();
			
			String title = "Query Execution Failed";
			String msg = "Failed executing Query";
			String details = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\".";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
		
			return;
		}
		
		if (result != null) {
			StringBuilder builder = new StringBuilder();
			
			builder.append("Search yielded " + result.size() + " results:\n");
			if (result.size() >0) {
				for (int nid : result.getSetValues()) {
					String componentDescription = null;
					ComponentVersionBI component = WBUtility.getComponentVersion(nid);
					if (component != null) {
						componentDescription = ComponentDescriptionHelper.getComponentDescription(component);
					}
					if (componentDescription == null) {
						componentDescription = WBUtility.getDescriptionIfConceptExists(nid);
					}
					if (componentDescription == null) {
						try {
							componentDescription = WBUtility.getConPrefTerm(nid);
						} catch (Exception e) {
							//
						}
					}
					if (componentDescription != null) {
						builder.append("nid=" + nid + "\t\"" + componentDescription.replaceAll("\n", " ") + "\"\n");
					} else {
						builder.append("nid=" + nid + "\n");
					}
				}
			}
			AppContext.getCommonDialogs().showInformationDialog("Search Results", builder.toString());
		}
	}

	private Query generateQuery() {
		logger.debug("Generating Query...");

		// This should never happen if Build button is properly disabled
		if (! isQueryNodeTreeViewValid()) {
			String error = "Cannot generate Query from invalid clause tree";
			logger.error(error);
			throw new RuntimeException(error);
		}

		ViewCoordinate viewCoordinate = null;
		try {
			viewCoordinate = StandardViewCoordinates.getSnomedInferredLatest();
		} catch (IOException ex) {
			logger.error("Failed getting default ViewCoordinate. Caught {} \"{}\"", ex.getClass().getName(), ex.getLocalizedMessage());
		}

		Query syntheticQuery = new Query(viewCoordinate) {
			
			@Override
			protected NativeIdSetBI For() throws IOException {
				return dataStore.getAllConceptNids();
			}

			@Override
			public void Let() throws IOException {
			}

			@Override
			public Clause Where() {
				return ClauseFactory.createClause(this, queryNodeTreeView.getRoot());
			}
			
		};
		
		return syntheticQuery;
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
