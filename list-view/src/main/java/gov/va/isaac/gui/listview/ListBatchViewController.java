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

import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import java.io.IOException;
import java.net.URL;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@FXML private TableView<String> conceptTable;
	@FXML private Button addUncommittedListButton;
	@FXML private Button addOperationButton;
	@FXML private Button clearListButton;
	@FXML private Button executeOperationsButton;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Button saveListButton;
	@FXML private AnchorPane rootPane;

	private UpdateableBooleanBinding allOperationsReady_;
	StringProperty reasonWhyExecuteDisabled_ = new SimpleStringProperty();
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());

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
		operationsList.getChildren().add(new OperationNode(this));

		conceptTable.setPlaceholder(new Label("Drop Concepts Here"));
		conceptTable.getColumns().get(0).prefWidthProperty().bind(conceptTable.widthProperty());
		
		//remove it, wrap it, readd it.
		executeOperationsToolbar.getItems().remove(executeOperationsButton);
		executeOperationsToolbar.getItems().add(ErrorMarkerUtils.setupDisabledInfoMarker(executeOperationsButton, reasonWhyExecuteDisabled_));

		addOperationButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				OperationNode operationNode = new OperationNode(ListBatchViewController.this);
				operationsList.getChildren().add(operationNode);
				allOperationsReady_.addBinding(operationNode.isReadyForExecution());
				allOperationsReady_.invalidate();
			}
		});

		clearOperationsButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				operationsList.getChildren().clear();
				allOperationsReady_.clearBindings();
				allOperationsReady_.invalidate();
			}
		});

		clearListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				conceptTable.getItems().clear();
			}
		});

		saveListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				//TODO implement
				logger_.error("Not yet implemented");
			}
		});
		
		loadListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				//TODO implement
				logger_.error("Not yet implemented");
			}
		});
		
		addUncommittedListButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				//TODO implement
				logger_.error("Not yet implemented");
				conceptTable.getItems().add("Foobar");
			}
		});
		
		allOperationsReady_ = new UpdateableBooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				for (Node n : operationsList.getChildren())
				{
					if (n instanceof OperationNode)
					{
						OperationNode on = (OperationNode)n;
						if (!on.isReadyForExecution().get())
						{
							reasonWhyExecuteDisabled_.set("All errors must be corrected in the Operations List before execution");
							return false;
						}
					}
					else
					{
						logger_.error("Design error - Node isn't an operationNode!");
					}
				}
				if (operationsList.getChildren().size() == 0)
				{
					reasonWhyExecuteDisabled_.set("At least one operation must be specified");
					return false;
				}
				return true;
			}
		};
		
		for (Node n : operationsList.getChildren())
		{
			if (n instanceof OperationNode)
			{
				allOperationsReady_.addBinding(((OperationNode) n).isReadyForExecution());
			}
			else
			{
				logger_.error("Design error - Node isn't an operationNode!");
			}
		}
		allOperationsReady_.invalidate();
		
		executeOperationsButton.disableProperty().bind(allOperationsReady_.not());
	}

	protected void remove(OperationNode node)
	{
		if (!operationsList.getChildren().remove(node))
		{
			logger_.error("Unexpected error removing operation item");
		}
		allOperationsReady_.removeBinding(node.isReadyForExecution());
		allOperationsReady_.invalidate();
	}
	
	protected ObservableList<String> getConceptList()
	{
		return conceptTable.getItems();
	}

	public AnchorPane getRoot()
	{
		return rootPane;
	}
}
