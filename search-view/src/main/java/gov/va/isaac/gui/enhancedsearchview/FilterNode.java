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
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.enhancedsearchview.filter.FilterView;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.UpdateableBooleanBinding;

import java.util.TreeMap;

import javafx.beans.binding.BooleanExpression;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

import org.glassfish.hk2.api.IterableProvider;

/**
 * {@link FilterNode}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class FilterNode extends VBox
{
	private StackPane subOptionsPane_;
	private ComboBox<String> operation_;
	Button removeOperation_;
	private FilterView currentOperation_;
	
	private TreeMap<String, FilterView> operationsMap_ = new TreeMap<>();
	private EnhancedSearchViewController lbvc_;
	
	private UpdateableBooleanBinding isOperationReady_;
	
	@Inject
	private IterableProvider<FilterView> allOperations_;
	
	protected FilterNode(EnhancedSearchViewController lbvc)
	{
		AppContext.getServiceLocator().inject(this);
		EnhancedSearchViewController lbvc_ = lbvc;
		
		for (FilterView o : allOperations_)
		{
			operationsMap_.put(o.getTitle(), o);
		}
		
		setMaxWidth(Double.MAX_VALUE);
		setStyle("-fx-border-color: lightgrey; -fx-border-width: 2px");
		
		HBox operationSelectionHBox = new HBox();
		operationSelectionHBox.setMinWidth(400);
		operationSelectionHBox.setPadding(new Insets(5, 5, 5, 5));
		getChildren().add(operationSelectionHBox);
		
		subOptionsPane_ = new StackPane();
		subOptionsPane_.setMaxWidth(Double.MAX_VALUE);
		VBox.setMargin(subOptionsPane_, new Insets(5, 5, 5, 5));
		getChildren().add(subOptionsPane_);
		
		ImageView ivMinus = new ImageView(Images.MINUS.getImage());
		removeOperation_ = new Button(null, ivMinus);
		HBox.setMargin(removeOperation_, new Insets(0, 5, 0, 0));
		removeOperation_.setTooltip(new Tooltip("Remove operation"));
		operationSelectionHBox.getChildren().add(removeOperation_);
		
		operation_ = new ComboBox<String>();
		for (String s : operationsMap_.keySet())
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
		isOperationReady_ = new UpdateableBooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				return currentOperation_.isValid().get();
			}
		};
		
		operation_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				subOptionsPane_.getChildren().clear();
				//stop watching old property
				if (currentOperation_ != null)
				{
					isOperationReady_.removeBinding(currentOperation_.isValid());
				}
				currentOperation_ = operationsMap_.get(operation_.getSelectionModel().getSelectedItem());
				//delay init
//				currentOperation_.initIfNot(lbvc_.getConceptList());
				operation_.setTooltip(new Tooltip(currentOperation_.getFilterDescription()));
				//start watching new one
				isOperationReady_.addBinding(currentOperation_.isValid());
				subOptionsPane_.getChildren().add(currentOperation_.getNode());
				isOperationReady_.invalidate();
			}
		});
		
		removeOperation_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				lbvc_.remove(FilterNode.this);
			}
		});
	}
	
	protected BooleanExpression isReadyForExecution()
	{
		return isOperationReady_; 
	}
	
	protected FilterView getFilter()
	{
		return currentOperation_;
	}
}
