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
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RefsetRetireMember}
 * 
 * An example operation that does nothing during execution, but provides an example for
 * implementing other operations. Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class RefsetRetireMember extends Operation
{
	private ComboBox<SimpleDisplayConcept> retireFrom_ = new ComboBox<>();
	private StringProperty retireFromInvalidString_ = new SimpleStringProperty("A concept must be selected from this drop down");
	private RefsetValueUtils rvs_ = new RefsetValueUtils(true);
	private CheckBox onlyRetireIfMatches_ = new CheckBox("Only retire if matches value");

	private BooleanBinding allValid_;

	private RefsetRetireMember()
	{
		// For HK2 to init
	}

	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("Retire from"), 0, 0);
		Node wrappedRetireFrom = ErrorMarkerUtils.setupErrorMarker(retireFrom_, retireFromInvalidString_);
		root_.add(wrappedRetireFrom, 1, 0);
		retireFrom_.setMaxWidth(Double.MAX_VALUE);
		retireFrom_.setPromptText("-Populate the Concepts List-");
		ComboBoxSetupTool.setupComboBox(retireFrom_);

		// TODO populate retireFrom
		retireFrom_.getItems().addListener(new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SimpleDisplayConcept> c)
			{
				if (retireFrom_.getItems().size() > 0)
				{
					retireFromInvalidString_.set(null);
					if (retireFrom_.getSelectionModel().getSelectedItem() == null)
					{
						retireFrom_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					retireFromInvalidString_.set("A concept must be selected from this drop down");
					retireFrom_.getSelectionModel().clearSelection();
					retireFrom_.setValue(null);
					retireFrom_.setPromptText("-Populate the Concepts List-");
				}
				allValid_.invalidate();
			}
		});

		onlyRetireIfMatches_.setSelected(true);

		Separator s = new Separator(Orientation.HORIZONTAL);
		s.setMaxWidth(Double.MAX_VALUE);
		root_.add(s, 0, 1, 2, 1);
		
		root_.add(new Label("Options"), 0, 2);
		root_.add(onlyRetireIfMatches_, 1, 2);

		rvs_.setup(root_, 3);

		onlyRetireIfMatches_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (onlyRetireIfMatches_.isSelected())
				{
					rvs_.setup(root_, 3);
				}
				else
				{
					rvs_.remove(root_);
				}
			}
		});

		allValid_ = new BooleanBinding()
		{
			{
				bind(rvs_.isValid(), onlyRetireIfMatches_.selectedProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (retireFrom_.getItems().size() == 0)
				{
					return false;
				}
				if (onlyRetireIfMatches_.isSelected())
				{
					return rvs_.isValid().get();
				}
				return true;
			}
		};

		GridPane.setHgrow(wrappedRetireFrom, Priority.ALWAYS);

		// remove the optional stuff after calculating col 0 width
		FxUtils.preventColCollapse(root_, 0);
		onlyRetireIfMatches_.setSelected(false);
		rvs_.remove(root_);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Refset, Retire Member";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		// TODO not correct
		SimpleDisplayConcept sdc = retireFrom_.getSelectionModel().getSelectedItem();
		retireFrom_.getItems().clear();
		retireFrom_.getItems().addAll(conceptList_);
		if (sdc != null && conceptList_.size() > 0)
		{
			retireFrom_.getSelectionModel().select(sdc);
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
		return "Retire a refset member from a refset";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(RefsetRetireMember.this)
		{
			@Override
			protected String call() throws Exception
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
				// TODO implement RefsetRetireMember
				return "Not yet implemented";
			}
		};
	}
}
