package gov.va.isaac.gui.enhancedsearchview.model.type.refspec;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ResultsType;
import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.gui.enhancedsearchview.model.SearchTypeModel;
import gov.va.isaac.gui.enhancedsearchview.resulthandler.ResultsToTaxonomy;
import gov.va.isaac.gui.querybuilder.QueryBuilderHelper;
import gov.va.isaac.gui.querybuilder.QueryNodeTreeViewTreeCell;
import gov.va.isaac.gui.querybuilder.QueryNodeType;
import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.QueryNodeTypeI;
import gov.va.isaac.interfaces.utility.DialogResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import org.apache.mahout.math.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefsetSpecSearchTypeModel  extends SearchTypeModel{
	private ComboBox<Object> rootNodeTypeComboBox = new ComboBox<Object>();
	private TreeView<NodeDraggable> queryNodeTreeView = new TreeView<NodeDraggable>();
	private List<QueryNodeTypeI> unsupportedQueryNodeTypes = new Vector<>();
	private BooleanProperty queryNodeTreeViewIsValidProperty = new SimpleBooleanProperty(false);
	private GridPane nodeEditorGridPane = new GridPane();
	private Map<String, NodeDraggable> nodeDragCache = new HashMap<>();

	final static Logger logger = LoggerFactory.getLogger(RefsetSpecSearchTypeModel.class);

	public RefsetSpecSearchTypeModel() {
		initializeQueryNodeTreeView();
		initializeRootNodeTypeComboBox();
	}

	private void initializeQueryNodeTreeView() {
		QueryBuilderHelper.initializeQueryNodeTreeView(queryNodeTreeView, nodeEditorGridPane, queryNodeTreeViewIsValidProperty);
		
		if (queryNodeTreeView.getContextMenu() == null) {
			queryNodeTreeView.setContextMenu(new ContextMenu());
		}
		queryNodeTreeView.getContextMenu().getItems().clear();

		final Tooltip emptyTreeTooltip = new Tooltip("Right-click on TreeView or left-click ComboBox to select root expression");
		//final Tooltip emptyTreeTooltip = new Tooltip("Left-click ComboBox to select root expression");

		queryNodeTreeView.setTooltip(emptyTreeTooltip);
		
		queryNodeTreeView.rootProperty().addListener(new ChangeListener<TreeItem<NodeDraggable>>() {
			@Override
			public void changed(
					ObservableValue<? extends TreeItem<NodeDraggable>> observable,
					TreeItem<NodeDraggable> oldValue,
					TreeItem<NodeDraggable> newValue) {
				if (newValue == null) {
					rootNodeTypeComboBox.getButtonCell().setText("");
					queryNodeTreeView.getContextMenu().getItems().clear();
					addContextMenus(queryNodeTreeView.getContextMenu(), queryNodeTreeView);
					queryNodeTreeView.setTooltip(emptyTreeTooltip);
					queryNodeTreeViewIsValidProperty.set(false);
				} else {
					rootNodeTypeComboBox.getButtonCell().setText(QueryNodeType.valueOf(observable.getValue().getValue()).displayName());
					queryNodeTreeView.getContextMenu().getItems().clear();
					queryNodeTreeView.setTooltip(null);
					queryNodeTreeViewIsValidProperty.set(QueryBuilderHelper.isQueryNodeTreeViewValid(queryNodeTreeView));
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
	

	private void initializeRootNodeTypeComboBox() {
		rootNodeTypeComboBox.setEditable(false);
		rootNodeTypeComboBox.setPromptText("Click to set root expression");
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

	}

	protected void addContextMenus(ContextMenu menu, Node ownerNode) {
		TreeItem<NodeDraggable> currentTreeItem = null;
		NodeDraggable currentNode = null;
		if (ownerNode instanceof TreeCell
				&& (currentTreeItem = ((TreeCell<NodeDraggable>)ownerNode).getTreeItem()) != null
				&& (currentNode = currentTreeItem.getValue()) != null
				&& currentNode instanceof ParentNodeDraggable) {
			logger.debug("Configuring context menu for {}: {}", ownerNode.getClass().getName());

		} else if (ownerNode instanceof TreeView) {
			logger.debug("Configuring context menu for {}", currentNode);
		} else {
			// bad
			String error = 	"Unexpected/innappropriate ContextMenu owner of type " + ownerNode.getClass().getName() + ": " + ownerNode + " with currentTreeItem=" + currentTreeItem + " and currentNode=" + currentNode;

			logger.error(error);

			throw new IllegalArgumentException(error);
		}
		
		addNewNodeSubMenu(menu, ownerNode, "New Grouping",
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR);
		
		addNewNodeSubMenu(menu, ownerNode, "New Concept Assertion",
				QueryNodeType.CONCEPT_IS,
				QueryNodeType.CONCEPT_IS_CHILD_OF,
				QueryNodeType.CONCEPT_IS_DESCENDANT_OF,
				QueryNodeType.CONCEPT_IS_KIND_OF);
		
		addNewNodeSubMenu(menu, ownerNode, "New String Assertion",
				QueryNodeType.DESCRIPTION_CONTAINS);
				//QueryNodeType.DESCRIPTION_LUCENE_MATCH
				//,QueryNodeType.DESCRIPTION_REGEX_MATCH
		
		addNewNodeSubMenu(menu, ownerNode, "New Relationship Assertion",
				QueryNodeType.REL_RESTRICTION,
				QueryNodeType.REL_TYPE);
	}
	private void addNewNodeSubMenu(ContextMenu menu, Node ownerNode, String subMenuName, QueryNodeType...nodeTypes) {
		Menu subMenu = new Menu(subMenuName);
		
		for (QueryNodeType type : nodeTypes) {
			if (unsupportedQueryNodeTypes.contains(type)) {
				continue;
			}

			MenuItem menuItem = new MenuItem(type.displayName());

			if (ownerNode instanceof TreeView) {
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						((TreeView<NodeDraggable>)ownerNode).setRoot(new TreeItem<>(type.constructNode()));
					}
				});
			} else if (ownerNode instanceof TreeCell) {
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
			} else {
				String error = 	"Unexpected/innappropriate ContextMenu owner of type " + ownerNode.getClass().getName() + ": " + ownerNode;

				logger.error(error);

				throw new IllegalArgumentException(error);
			}

			subMenu.getItems().add(menuItem);
		}

		if (subMenu.getItems().size() > 0) {
			menu.getItems().add(subMenu);
		}
	}

	private void addNewParentContextMenu(ContextMenu menu, TreeCell<NodeDraggable> ownerNode) {
		Menu newParentMenu = new Menu("New Parent");
		QueryNodeType[] supportedNewParentNodes = new QueryNodeType[] {
				QueryNodeType.AND,
				QueryNodeType.OR,
				QueryNodeType.XOR
		};
		for (QueryNodeType type : supportedNewParentNodes) {
			if (unsupportedQueryNodeTypes.contains(type)) {
				continue;
			}
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
		
		if (newParentMenu.getItems().size() > 0) {
			menu.getItems().add(newParentMenu);
		}
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

	public ComboBox<Object> getRootNodeTypeComboBox() {
		return rootNodeTypeComboBox;
	}

	public TreeView<NodeDraggable> getQueryNodeTreeView() {
		return queryNodeTreeView;
	}

	public List<QueryNodeTypeI> getUnsupportedQueryNodeTypes() {
		return unsupportedQueryNodeTypes;
	}

	public BooleanProperty getQueryNodeTreeViewIsValidProperty() {
		return queryNodeTreeViewIsValidProperty;
	}

	public GridPane getNodeEditorGridPane() {
		return nodeEditorGridPane;
	}

	public Map<String, NodeDraggable> getNodeDragCache() {
		return nodeDragCache;
	}

	@Override
	public void typeSpecificCopy(SearchTypeModel other) {
		rootNodeTypeComboBox.getItems().clear();
		rootNodeTypeComboBox.getItems().addAll(((RefsetSpecSearchTypeModel)other).getRootNodeTypeComboBox().getItems());
		
		unsupportedQueryNodeTypes.clear();
		unsupportedQueryNodeTypes.addAll(((RefsetSpecSearchTypeModel)other).getUnsupportedQueryNodeTypes());

		queryNodeTreeViewIsValidProperty.set(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeViewIsValidProperty().get());
		
		nodeDragCache.clear();
		nodeDragCache.putAll(((RefsetSpecSearchTypeModel)other).getNodeDragCache());

		nodeEditorGridPane.getChildren().clear();
		nodeEditorGridPane .getChildren().addAll(((RefsetSpecSearchTypeModel)other).getNodeEditorGridPane().getChildren());

		queryNodeTreeView.setRoot(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeView().getRoot());
		queryNodeTreeView.setSelectionModel(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeView().getSelectionModel());
		queryNodeTreeView.rootProperty().set(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeView().rootProperty().get());
		queryNodeTreeView.setContextMenu(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeView().getContextMenu());
		queryNodeTreeView.setTooltip(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeView().getTooltip());
		queryNodeTreeView.setCellFactory(((RefsetSpecSearchTypeModel)other).getQueryNodeTreeView().getCellFactory());
	}

	@Override
	public String getModelDisplayString() {
		return ", RootNode=" + rootNodeTypeComboBox.getSelectionModel().getSelectedItem() 
				+ ", unsupportedQueryNodeTypes=" + Arrays.toString(unsupportedQueryNodeTypes.toArray())
				+ ", queryNodeTreeViewIsValidProperty=" + queryNodeTreeViewIsValidProperty.get()
				+ ", nodeDragCache=" + nodeDragCache.toString()
				+ ", nodeEditorGridPane=" + nodeEditorGridPane.toString()
				+ ", queryNodeTreeView=" + queryNodeTreeView.toString();

	}

	@Override
	public void executeSearch(ResultsType resultsType, String modelMaxResults) {
		try {
			QueryBuilderHelper.executeQuery(QueryBuilderHelper.generateQuery(queryNodeTreeView), queryNodeTreeView);

			SearchModel.getSearchResultsTable().getResults().setItems(FXCollections.observableArrayList(QueryBuilderHelper.getResults()));
			
			if (splitPane.getItems().contains(taxonomyPane)) {
				ResultsToTaxonomy.resultsToSearchTaxonomy();
			}
		} catch (Exception e) {
			logger.error("Failed executing query.  Caught {} {}.", e.getClass().getName(), e.getLocalizedMessage());

			e.printStackTrace();
		
			String title = "Query Execution Failed";
			String msg = "Failed executing Query";
			String details = "Caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\".";
			AppContext.getCommonDialogs().showErrorDialog(title, msg, details, AppContext.getMainApplicationWindow().getPrimaryStage());
		} finally {
			getSearchRunning().set(false);
			bottomPane.refreshBottomPanel();
			bottomPane.refreshTotalResultsSelectedLabel();
		}
	}

	@Override
	public  boolean isCriteriaPanelValid() {
		if (viewCoordinateProperty.get() == null) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean isValidSearch(String errorDialogTitle) {
		if (queryNodeTreeView.getRoot() == null) {
			String details = "No Root Node specified: " + this;
			LOG.warn("Invalid search model (name=" + getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		} else if (!queryNodeTreeViewIsValidProperty.get()) {
			String details = "Invalide Query specified.  May be missing required children?  See Red Nodes: " + this;
			LOG.warn("Invalid search model (name=" + getName() + "). " + details);

			if (errorDialogTitle != null) {
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogTitle, details, AppContext.getMainApplicationWindow().getPrimaryStage());
			}

			return false;
		}
		
		return true;
	}
}
