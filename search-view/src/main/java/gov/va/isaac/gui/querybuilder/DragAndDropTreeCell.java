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
 * DragAndDropTreeCell
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.gui.querybuilder.node.NodeDraggable;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable.DragMode;
import gov.va.isaac.gui.querybuilder.node.ParentNodeDraggable;
import gov.va.isaac.util.TemporaryUniqueIdCache;

import java.util.Map;

import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DragAndDropTreeCell
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DragAndDropTreeCell<E extends NodeDraggable> extends TreeCell<E> {
	protected final static Logger logger = LoggerFactory.getLogger(DragAndDropTreeCell.class);

	protected final Map<String, NodeDraggable> cache;
	protected final TreeView<NodeDraggable> parentTree;

	protected NodeDraggable item;
	
	protected EventHandler<DragEvent> onDragOverHandler = new EventHandler<DragEvent>() {
		@Override
		public void handle(DragEvent dragEvent) {
			if (dragEvent.getDragboard().hasString() && TemporaryUniqueIdCache.getObjectByUniqueId(dragEvent.getDragboard().getString()) != null) {
				NodeDraggable valueToMove = cache.get(dragEvent.getDragboard().getString());
				logger.debug("Dragging {} ({}) over {}", valueToMove, dragEvent.getDragboard().getString(), item);
				if (valueToMove == null) {
					logger.warn("No item found for {} in cache of size {}. Dumping cache: {}", dragEvent.getDragboard().getString(), cache.size(), cache);
				}
				if (valueToMove == item) {
					logger.debug("Not dropping onto self: " + valueToMove);
				}
				else if (! (item instanceof ParentNodeDraggable)) {
					// Only PARENT nodeType can accept new child nodes

					logger.debug("Cannot drop " + valueToMove + " onto non-parent node: " + item);
				}
				else if (isFirstAncestorOfOrSameAsSecond(valueToMove, item)) {
					// Only PARENT nodeType can accept new child nodes

					logger.debug("Not dropping: " + valueToMove + " is ancestor of or same as " + item);
				}
				else if (! ((ParentNodeDraggable)item).canAddChild(valueToMove)) {
					// Parent may reject child
					logger.debug("Potential parent: " + item + " rejected child: " + valueToMove);
				}
				else {
					// We accept the transfer!!!!!
					dragEvent.acceptTransferModes(TransferMode.MOVE, TransferMode.COPY);
				}
			} else if (dragEvent.getDragboard().hasString() && TemporaryUniqueIdCache.getObjectByUniqueId(dragEvent.getDragboard().getString()) == null) {
				logger.debug("Drag clipboard contains unexpected key value {}.  Ignoring.", dragEvent.getDragboard().getString());
			}
			else
			{
				logger.debug("Dragging empty value over " + item);
			}
			dragEvent.consume();
		}
	};

	protected EventHandler<DragEvent> onDragDroppedHandler = new EventHandler<DragEvent>() {
		@Override
		public void handle(DragEvent dragEvent) {
			NodeDraggable valueToMove = cache.get(dragEvent.getDragboard().getString());
			logger.debug("Drag ({}) dropped {} ({}) onto {}", dragEvent.getTransferMode(), valueToMove, dragEvent.getDragboard().getString(), item);

			TreeItem<NodeDraggable> itemToMove = search(parentTree.getRoot(), valueToMove);
			if (itemToMove == null) {
				itemToMove = new TreeItem<NodeDraggable>(valueToMove);
			}
			TreeItem<NodeDraggable> newParent = search(parentTree.getRoot(), item);

			if (dragEvent.getTransferMode() == TransferMode.MOVE && itemToMove.getParent() != null) {
				// Remove from former parent.
				itemToMove.getParent().getChildren().remove(itemToMove);
			}
			// Add to new parent.
			newParent.getChildren().add(itemToMove);

			newParent.setExpanded(true);
			dragEvent.consume();
		}
	};

	protected EventHandler<MouseEvent> onDragDetectedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			logger.debug("Drag detected on " + item);
			NodeDraggable itemToDrop = null;
			TransferMode transferMode = null;

			if (item == null) {
				return;
			} else if (item.getDragMode() == DragMode.NONE) {
				logger.debug("Not dragging " + item + " with getDragMode() == DragMode.NONE");
				return;
			} else {
				switch(item.getDragMode()) {
				case COPY:
					transferMode = TransferMode.COPY;
					break;
				case MOVE:
					transferMode = TransferMode.MOVE;
					break;
				default:
					throw new RuntimeException("Unsupported " + item.getDragMode().getClass().getName() + "\"" + item.getDragMode() + "\"");
				}
				itemToDrop = item.getItemToDrop();
				String temporaryIdOfItemToDrop = itemToDrop.getTemporaryUniqueId();
				cache.put(temporaryIdOfItemToDrop, itemToDrop);

				logger.debug("Added item {} ({}) to cache (size={}) to {}", itemToDrop, itemToDrop.getTemporaryUniqueId(), cache.size(), transferMode);
			}

			Dragboard dragBoard = startDragAndDrop(transferMode);
			ClipboardContent content = new ClipboardContent();

			content.put(DataFormat.PLAIN_TEXT, itemToDrop.getTemporaryUniqueId());
			dragBoard.setContent(content);
			event.consume();
		}
	};

	protected EventHandler<DragEvent> onDragDoneHandler = new EventHandler<DragEvent>() {
		@Override
		public void handle(DragEvent dragEvent) {
			logger.debug("Drag done on " + item);
			if (item != null) {
				// TODO: Find way to remove itemToDrop, if it differs from item
				cache.remove(item.getTemporaryUniqueId());
				logger.debug("Removed " + item + " from cache (size=" + cache.size() + ")");
			}
			dragEvent.consume();
		}
	};

	public DragAndDropTreeCell(final TreeView<NodeDraggable> parentTree, Map<String, NodeDraggable> cache) {
		this.parentTree = parentTree;
		this.cache = cache;
		
		// ON SOURCE NODE.
		setOnDragDetected(onDragDetectedHandler);
		setOnDragDone(onDragDoneHandler);
		// ON TARGET NODE.
//		setOnDragEntered(new EventHandler<DragEvent>() {
//			@Override
//			public void handle(DragEvent dragEvent) {
//				logger.debug("Drag entered on " + item);
//				if (dragEvent.getGestureSource() != dragEvent.getTarget() &&
//						dragEvent.getDragboard().hasString()) {
//					dragEvent.getTarget().setFill(Color.GREEN);
//				}
//				dragEvent.consume();
//			}
//		});
		setOnDragOver(onDragOverHandler);
		//            setOnDragExited(new EventHandler<DragEvent>() {
		//                @Override
		//                public void handle(DragEvent dragEvent) {
		//                    logger.debug("Drag exited on " + item);
		//                    dragEvent.consume();
		//                }
		//            });        
		setOnDragDropped(onDragDroppedHandler);
	}

	protected TreeItem<NodeDraggable> search(final NodeDraggable valueToSearch) {
		return search(parentTree.getRoot(), valueToSearch);
	}
	protected TreeItem<NodeDraggable> search(final TreeItem<NodeDraggable> currentNode, final NodeDraggable valueToSearch) {
		TreeItem<NodeDraggable> result = null;
		if (currentNode.getValue() == valueToSearch) {
			result = currentNode;
		} else if (!currentNode.isLeaf()) {
			for (TreeItem<NodeDraggable> child : currentNode.getChildren()) {
				result = search(child, valueToSearch);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected void updateItem(E item, boolean empty) {
		super.updateItem(item, empty);
		this.item = item;
		String text = (item == null) ? null : item.toString();
		this.textProperty().unbind();
		setText(text);
	}

	protected boolean isFirstAncestorOfOrSameAsSecond(TreeItem<NodeDraggable> first, TreeItem<NodeDraggable> second) {
		return search(first, second.getValue()) != null;
	}
	protected boolean isFirstAncestorOfOrSameAsSecond(NodeDraggable first, NodeDraggable second) {
		return isFirstAncestorOfOrSameAsSecond(search(parentTree.getRoot(), first), search(parentTree.getRoot(), second));
	}
}
