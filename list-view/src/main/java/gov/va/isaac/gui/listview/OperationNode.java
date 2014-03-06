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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.listview;

import gov.va.isaac.gui.listview.operations.Operation;
import gov.va.isaac.gui.listview.operations.ParentReplace;
import gov.va.isaac.gui.listview.operations.PlaceHolder;
import gov.va.isaac.gui.util.Images;
import java.util.TreeMap;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * {@link OperationNode}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class OperationNode
{
	private VBox root_;
	private StackPane subOptionsPane_;
	private ComboBox<String> operation_;
	Button removeOperation_;
	private Operation currentOperation_;
	
	private TreeMap<String, Operation> operations_ = new TreeMap<>();
	private ListBatchViewController lbvc_;
	
	protected OperationNode(ListBatchViewController lbvc)
	{
		lbvc_ = lbvc;
		//TODO be smarter about this, build a utility
		Operation operation = new ParentReplace();
		operations_.put(operation.getTitle(), operation);
		operation = new PlaceHolder();
		operations_.put(operation.getTitle(), operation);
		
		root_ = new VBox();
		root_.setMaxWidth(Double.MAX_VALUE);
		root_.setMinWidth(300);
		root_.setStyle("-fx-border-color: lightgrey; -fx-border-width: 2px");
		
		
		HBox operationSelectionHBox = new HBox();
		operationSelectionHBox.setPadding(new Insets(5, 5, 5, 5));
		root_.getChildren().add(operationSelectionHBox);
		
		subOptionsPane_ = new StackPane();
		subOptionsPane_.setMaxWidth(Double.MAX_VALUE);
		VBox.setMargin(subOptionsPane_, new Insets(5, 5, 5, 5));
		root_.getChildren().add(subOptionsPane_);
		
		ImageView ivMinus = new ImageView(Images.MINUS.getImage());
		removeOperation_ = new Button(null, ivMinus);
		HBox.setMargin(removeOperation_, new Insets(0, 5, 0, 0));
		removeOperation_.setTooltip(new Tooltip("Remove operation"));
		operationSelectionHBox.getChildren().add(removeOperation_);
		
		operation_ = new ComboBox<String>();
		for (String s : operations_.keySet())
		{
			operation_.getItems().add(s);
		}
		operation_.setMaxWidth(Double.MAX_VALUE);
		operationSelectionHBox.getChildren().add(operation_);
		HBox.setHgrow(operation_, Priority.ALWAYS);
		
		initActionHandlers();
		operation_.getSelectionModel().select(0);
	}
	
	private void initActionHandlers()
	{
		operation_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				subOptionsPane_.getChildren().clear();
				currentOperation_ = operations_.get(operation_.getSelectionModel().getSelectedItem());
				subOptionsPane_.getChildren().add(currentOperation_.getNode());
			}
		});
		
		removeOperation_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				lbvc_.remove(OperationNode.this);
			}
		});
		
		lbvc_.getConceptList().addListener(new ListChangeListener<String>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
			{
				currentOperation_.conceptListChanged(lbvc_.getConceptList());
			}
		});
	}
	
	protected Node getNode()
	{
		return root_;
	}
}
