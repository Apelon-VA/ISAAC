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
package gov.va.isaac.gui.listview.operations;

import java.util.List;
import gov.va.isaac.gui.util.Images;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * {@link ParentReplace}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ParentReplace implements Operation
{
	private GridPane root_;
	private ComboBox<String> replaceOptions_;
	private TextField withConcept_;
	private ImageView invalidReplaceOptions_;
	private BooleanProperty replaceOptionsPopulated_ = new SimpleBooleanProperty(false);
	
	public ParentReplace()
	{
		root_ = new GridPane();
		root_.setMaxWidth(Double.MAX_VALUE);
		root_.setHgap(5.0);
		root_.setVgap(3.0);
		
		root_.add(new Label("Replace: "), 0, 0);
	
		replaceOptions_ = new ComboBox<>();
		replaceOptions_.setMaxWidth(Double.MAX_VALUE);

		invalidReplaceOptions_ = Images.EXCLAMATION.createImageView();
		invalidReplaceOptions_.visibleProperty().bind(replaceOptionsPopulated_.not());
		Tooltip t = new Tooltip("A concept must be selected from this drop down");
		Tooltip.install(invalidReplaceOptions_, t);
		
		StackPane sp = new StackPane();
		sp.setMaxWidth(Double.MAX_VALUE);
		sp.getChildren().add(replaceOptions_);
		sp.getChildren().add(invalidReplaceOptions_);
		StackPane.setAlignment(replaceOptions_, Pos.CENTER_LEFT);
		StackPane.setAlignment(invalidReplaceOptions_, Pos.CENTER_RIGHT);
		StackPane.setMargin(invalidReplaceOptions_, new Insets(0.0, 20.0, 0.0, 0.0));
		
		root_.add(sp, 1, 0);
		
		root_.add(new Label("With Parent: "), 0, 1);
		
		withConcept_ = new TextField();
		withConcept_.setPromptText("Concept Required");
		GridPane.setHgrow(withConcept_, Priority.ALWAYS);
		root_.add(withConcept_, 1, 1);
		
		
		replaceOptions_.getItems().addListener(new ListChangeListener<String>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
			{
				//TODO why isn't this working right?
				if (replaceOptions_.getItems().size() > 0)
				{
					replaceOptionsPopulated_.set(true);
					if (replaceOptions_.getSelectionModel().getSelectedItem() == null)
					{
						replaceOptions_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					replaceOptionsPopulated_.set(false);
					replaceOptions_.getSelectionModel().clearSelection();
					replaceOptions_.setPromptText("Populate the Concepts List");
				}
			}
		});
	}
	public Node getNode()
	{
		return root_;
	}
	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Parent, Replace";
	}
	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged(java.util.List)
	 */
	@Override
	public void conceptListChanged(List<String> concepts)
	{
		// TODO Auto-generated method stub
		replaceOptions_.getItems().clear();
		if (concepts.size() > 0)
		{
			replaceOptions_.getItems().addAll(concepts);
		}
	}
}

