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
 * QueryNodeTreeViewTreeCell
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.gui.querybuilder.node.AssertionNode;
import gov.va.isaac.gui.querybuilder.node.LogicalNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;

import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.beans.property.BooleanProperty;


/**
 * QueryNodeTreeViewTreeCell
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class QueryNodeTreeViewTreeCell extends DragAndDropTreeCell<NodeDraggable> {
	private final BooleanProperty queryNodeTreeViewIsValidProperty;
	
	public QueryNodeTreeViewTreeCell(
			TreeView<NodeDraggable> parentTree,
			Map<String, NodeDraggable> cache,
			BooleanProperty queryNodeTreeViewIsValidProperty) {
		super(parentTree, cache);
		this.queryNodeTreeViewIsValidProperty = queryNodeTreeViewIsValidProperty;
	}

	protected void updateTextFillColor(NodeDraggable node) {
		Color color = Color.BLACK;
		if (node != null && node.getIsValid()) {
			color = Color.BLACK;
			queryNodeTreeViewIsValidProperty.set(QueryBuilderHelper.isQueryNodeTreeViewValid(parentTree));
		} else {
			color = Color.RED;
			queryNodeTreeViewIsValidProperty.set(false);
		}
		logger.debug("Setting initial text fill color of cell containing \"{}\" to {}", (node != null ? node.getDescription() : null), color);
		setTextFill(color);
	}

	public void setCellContextMenus() {
	}
	
	@Override
	protected void updateItem(NodeDraggable item, boolean empty) {
		super.updateItem(item, empty);
		this.item = item;
		String text = (item == null) ? null : item.getDescription();
		this.textProperty().unbind();
		setText(text);

		setCellContextMenus();
		
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

			parentTree.getSelectionModel().select(getTreeItem());
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

			parentTree.getSelectionModel().select(getTreeItem());
		}
	}
}