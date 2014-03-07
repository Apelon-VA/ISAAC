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

import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link ParentReplace}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ParentReplace extends Operation
{
	private ComboBox<String> replaceOptions_;
	private StringProperty replaceOptionsInvalidString_ = new SimpleStringProperty("A concept must be selected from this drop down");
	private ConceptNode withConcept_;

	private BooleanBinding operationIsReady_;

	public ParentReplace(ObservableList<String> conceptList)
	{
		super(conceptList);
		root_.add(new Label("Replace: "), 0, 0);

		replaceOptions_ = new ComboBox<>();
		replaceOptions_.setMaxWidth(Double.MAX_VALUE);
		replaceOptions_.setPromptText("Populate the Concepts List");
		replaceOptions_.getItems().addAll(conceptList);
		root_.add(ErrorMarkerUtils.setupErrorMarker(replaceOptions_, replaceOptionsInvalidString_), 1, 0);

		root_.add(new Label("With Parent: "), 0, 1);
		withConcept_ = new ConceptNode(null, true);
		root_.add(withConcept_.getNode(), 1, 1);

		GridPane.setHgrow(withConcept_.getNode(), Priority.ALWAYS);

		initActionListeners();
	}

	private void initActionListeners()
	{
		replaceOptions_.getItems().addListener(new ListChangeListener<String>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
			{
				if (replaceOptions_.getItems().size() > 0)
				{
					replaceOptionsInvalidString_.set(null);
					if (replaceOptions_.getSelectionModel().getSelectedItem() == null)
					{
						replaceOptions_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					replaceOptionsInvalidString_.set("A concept must be selected from this drop down");
					replaceOptions_.getSelectionModel().clearSelection();
					replaceOptions_.setValue(null);
					replaceOptions_.setPromptText("-Populate the Concepts List-");
				}
			}
		});
		
		operationIsReady_ = new BooleanBinding()
		{
			{
				super.bind(replaceOptionsInvalidString_, withConcept_.isValid());
			}

			@Override
			protected boolean computeValue()
			{
				if (StringUtils.isBlank(replaceOptionsInvalidString_.get()) && withConcept_.isValid().get())
				{
					return true;
				}
				return false;
			}
		};
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
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	public void conceptListChanged()
	{
		// TODO Auto-generated method stub
		replaceOptions_.getItems().clear();
		replaceOptions_.getItems().addAll(conceptList_);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#isValid()
	 */
	@Override
	public BooleanExpression isValid()
	{
		return operationIsReady_;
	}
}
