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
package gov.va.isaac.gui.listview;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * {@link ListBatchViewController}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ListBatchViewController
{
	@FXML private Button loadListButton;

	@FXML private VBox operationsList;

	@FXML private Button clearOperationsButton;

	@FXML private Tab batchResultsTab;

	@FXML private TableView<?> conceptTable;

	@FXML private Button addUncommittedListButton;

	@FXML private Button addOperationButton;

	@FXML private Button clearListButton;

	@FXML private Button executeOperationsButton;

	@FXML private Button saveListButton;

	@FXML private AnchorPane rootPane;

	protected static ListBatchViewController init() throws IOException
	{
		// Load from FXML.
		URL resource = ListBatchViewController.class.getResource("ListBatchView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	public void initialize()
	{
		operationsList.getChildren().add(new OperationNode().getNode());
		
		conceptTable.setPlaceholder(new Label("Drop Concepts Here"));
		conceptTable.getColumns().get(0).prefWidthProperty().bind(conceptTable.widthProperty());
		
//		ImageView ivPlus = new ImageView(Images.PLUS.getImage());
//		Button addOperation = new Button(null, ivPlus);
//		addOperation.setTooltip(new Tooltip("Add operation"));
//		operationsList.getChildren().add(addOperation);
//		VBox.setMargin(addOperation, new Insets(0, 0, 0, 7));
	}

	public AnchorPane getRoot()
	{
		return rootPane;
	}
}
