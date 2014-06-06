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
package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.gui.ComboBoxSetupTool;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RefsetReplaceValue}
 * 
 * An example operation that does nothing during execution, but provides an example for
 * implementing other operations. Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class RefsetReplaceValue extends Operation
{
	private ComboBox<SimpleDisplayConcept> inRefset_ = new ComboBox<>();
	private StringProperty inRefsetInvalidString_ = new SimpleStringProperty("A concept must be selected from this drop down");
	private RefsetValueUtils replaceWithValue_ = new RefsetValueUtils(true);
	private RefsetValueUtils currentRefsetValue_ = new RefsetValueUtils(true);
	private CheckBox onlyReplaceIfMatches_ = new CheckBox("Only replace if the current value is");

	private BooleanBinding allValid_;

	private RefsetReplaceValue()
	{
		// For HK2 to init
	}

	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("In refset"), 0, 0);
		root_.add(ErrorMarkerUtils.setupErrorMarker(inRefset_, inRefsetInvalidString_), 1, 0);
		inRefset_.setPromptText("-Populate the Concepts List-");
		inRefset_.setMaxWidth(Double.MAX_VALUE);
		ComboBoxSetupTool.setupComboBox(inRefset_);

		// TODO populate moveFrom
		inRefset_.getItems().addListener(new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SimpleDisplayConcept> c)
			{
				if (inRefset_.getItems().size() > 0)
				{
					inRefsetInvalidString_.set(null);
					if (inRefset_.getSelectionModel().getSelectedItem() == null)
					{
						inRefset_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					inRefsetInvalidString_.set("A concept must be selected from this drop down");
					inRefset_.getSelectionModel().clearSelection();
					inRefset_.setValue(null);
					inRefset_.setPromptText("-Populate the Concepts List-");
				}
				allValid_.invalidate();
			}
		});

		root_.add(new Label("Replace with"), 0, 1);
		replaceWithValue_.setup(root_, 2);

		onlyReplaceIfMatches_.setSelected(true);
		Separator s = new Separator(Orientation.HORIZONTAL);
		s.setMaxWidth(Double.MAX_VALUE);
		root_.add(s, 0, 4, 2, 1);

		root_.add(new Label("Options"), 0, 5);
		root_.add(onlyReplaceIfMatches_, 1, 5);

		currentRefsetValue_.setup(root_, 6);

		onlyReplaceIfMatches_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (onlyReplaceIfMatches_.isSelected())
				{
					currentRefsetValue_.setup(root_, 6);
				}
				else
				{
					currentRefsetValue_.remove(root_);
				}
			}
		});

		allValid_ = new BooleanBinding()
		{
			{
				bind(currentRefsetValue_.isValid(), replaceWithValue_.isValid(), onlyReplaceIfMatches_.selectedProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (inRefset_.getItems().size() == 0 || !replaceWithValue_.isValid().get())
				{
					return false;
				}
				if (onlyReplaceIfMatches_.isSelected())
				{
					return currentRefsetValue_.isValid().get();
				}
				return true;
			}
		};

		GridPane.setHgrow(onlyReplaceIfMatches_, Priority.ALWAYS);

		// remove the optional stuff after calculating col 0 width
		FxUtils.preventColCollapse(root_, 0);
		onlyReplaceIfMatches_.setSelected(false);
		currentRefsetValue_.remove(root_);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Refset, Replace Value";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		// TODO not correct
		SimpleDisplayConcept sdc = inRefset_.getSelectionModel().getSelectedItem();
		inRefset_.getItems().clear();
		inRefset_.getItems().addAll(conceptList_);
		if (sdc != null && conceptList_.size() > 0)
		{
			inRefset_.getSelectionModel().select(sdc);
		}
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#isValid()
	 */
	@Override
	public BooleanExpression isValid()
	{
		return allValid_;
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getOperationDescription()
	 */
	@Override
	public String getOperationDescription()
	{
		return "Move a refset member to a different refset";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<OperationResult> createTask()
	{
		return new CustomTask<OperationResult>(RefsetReplaceValue.this)
		{
			@Override
			protected OperationResult call() throws Exception
			{
				// double i = 0;
				// for (SimpleDisplayConcept c : conceptList_)
				// {
				// if (cancelRequested_)
				// {
				// return ParentAddNew.this.getTitle() + " was cancelled";
				// }
				// updateProgress(i, conceptList_.size());
				// updateMessage("processing " + c.getDescription());
				// Thread.sleep(3000);
				// if (cancelRequested_)
				// {
				// return ParentAddNew.this.getTitle() + " was cancelled";
				// }
				// updateProgress((i + 0.5), conceptList_.size());
				// updateMessage("still working on " + c.getDescription());
				// Thread.sleep(3000);
				// if (cancelRequested_)
				// {
				// return ParentAddNew.this.getTitle() + " was cancelled";
				// }
				// updateProgress(++i, conceptList_.size());
				// }
				// return ParentAddNew.this.getTitle() + " completed - modified 0 concepts";
				// TODO implement RefsetReplaceValue
				return new OperationResult();
			}
		};
	}
}
