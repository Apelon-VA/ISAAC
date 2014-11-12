package gov.va.isaac.gui.querybuilder;

import gov.va.isaac.gui.querybuilder.node.DraggableNode;
import gov.va.isaac.gui.querybuilder.node.DraggableParentNode;
import gov.va.isaac.gui.querybuilder.node.NodeDraggable;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DragAndDropTreeCellExample extends Application {
	Map<String, NodeDraggable> cache = new HashMap<>();
	
	public static class TestParentNode extends DraggableParentNode {
		private Integer data;
		
		/**
		 * @param data
		 */
		public TestParentNode(Integer data) {
			this.data = data;
		}

		public String toString() {
			return "Parent: " + data.toString() + " (" + getTemporaryUniqueId() + ")";
		}
	}
	public static class TestLeafNode extends DraggableNode {
		private Integer data;

		/**
		 * @param data
		 */
		public TestLeafNode(Integer data) {
			this.data = data;
		}
		
		public String toString() {
			return "Leaf: " + data.toString() + " (" + getTemporaryUniqueId() + ")";
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		TreeItem<NodeDraggable> treeRoot = new TreeItem<>(new TestParentNode(0));
		treeRoot.getChildren().add(new TreeItem<>(new TestParentNode(1)));
		treeRoot.getChildren().add(new TreeItem<>(new TestParentNode(2)));
		treeRoot.getChildren().add(new TreeItem<>(new TestParentNode(3)));
		treeRoot.getChildren().add(new TreeItem<>(new TestLeafNode(4)));
		treeRoot.getChildren().add(new TreeItem<>(new TestLeafNode(5)));
		treeRoot.getChildren().add(new TreeItem<>(new TestLeafNode(6)));
		TreeView<NodeDraggable> treeView = new TreeView<>(treeRoot);
		treeView.setCellFactory(new Callback<TreeView<NodeDraggable>, TreeCell<NodeDraggable>>() {
			@Override
			public TreeCell<NodeDraggable> call(TreeView<NodeDraggable> param) {
				return new DragAndDropTreeCell<NodeDraggable>(param, cache);
			}
		});
		AnchorPane.setTopAnchor(treeView, 0d);
		AnchorPane.setRightAnchor(treeView, 0d);
		AnchorPane.setBottomAnchor(treeView, 0d);
		AnchorPane.setLeftAnchor(treeView, 0d);
		//
		AnchorPane root = new AnchorPane();
		root.getChildren().add(treeView);
		Scene scene = new Scene(root, 300, 250);
		//
		primaryStage.setTitle("Hello World!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX 
	 * application. main() serves only as fallback in case the 
	 * application can not be launched through deployment artifacts,
	 * e.g., in IDEs with limited FX support. NetBeans ignores main().
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}