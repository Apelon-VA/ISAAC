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
import gov.va.isaac.gui.ConceptNode;
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
 * {@link RefsetMoveMember}
 * 
 * An example operation that does nothing during execution, but provides an example for
 * implementing other operations. Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class RefsetMoveMember extends Operation
{
	private ComboBox<SimpleDisplayConcept> moveFrom_ = new ComboBox<>();
	private StringProperty moveFromInvalidString_ = new SimpleStringProperty("A concept must be selected from this drop down");
	private ConceptNode moveTo_ = new ConceptNode(null, true);
	private RefsetValueUtils rvs_ = new RefsetValueUtils(true);
	private CheckBox onlyMoveIfMatches_ = new CheckBox("Only move if matches value");

	private BooleanBinding allValid_;

	private RefsetMoveMember()
	{
		// For HK2 to init
	}

	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("Move from"), 0, 0);
		root_.add(ErrorMarkerUtils.setupErrorMarker(moveFrom_, moveFromInvalidString_), 1, 0);
		moveFrom_.setPromptText("-Populate the Concepts List-");
		moveFrom_.setMaxWidth(Double.MAX_VALUE);
		ComboBoxSetupTool.setupComboBox(moveFrom_);

		// TODO populate moveFrom
		moveFrom_.getItems().addListener(new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SimpleDisplayConcept> c)
			{
				if (moveFrom_.getItems().size() > 0)
				{
					moveFromInvalidString_.set(null);
					if (moveFrom_.getSelectionModel().getSelectedItem() == null)
					{
						moveFrom_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					moveFromInvalidString_.set("A concept must be selected from this drop down");
					moveFrom_.getSelectionModel().clearSelection();
					moveFrom_.setValue(null);
					moveFrom_.setPromptText("-Populate the Concepts List-");
				}
				allValid_.invalidate();
			}
		});

		root_.add(new Label("Move to"), 0, 1);
		root_.add(moveTo_.getNode(), 1, 1);

		onlyMoveIfMatches_.setSelected(true);
		Separator s = new Separator(Orientation.HORIZONTAL);
		s.setMaxWidth(Double.MAX_VALUE);
		root_.add(s, 0, 2, 2, 1);

		root_.add(new Label("Options"), 0, 3);
		root_.add(onlyMoveIfMatches_, 1, 3);

		rvs_.setup(root_, 4);

		onlyMoveIfMatches_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (onlyMoveIfMatches_.isSelected())
				{
					rvs_.setup(root_, 4);
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
				bind(moveTo_.isValid(), rvs_.isValid(), onlyMoveIfMatches_.selectedProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (moveFrom_.getItems().size() == 0 || !moveTo_.isValid().get())
				{
					return false;
				}
				if (onlyMoveIfMatches_.isSelected())
				{
					return rvs_.isValid().get();
				}
				return true;
			}
		};

		GridPane.setHgrow(moveTo_.getNode(), Priority.ALWAYS);

		// remove the optional stuff after calculating col 0 width
		FxUtils.preventColCollapse(root_, 0);
		onlyMoveIfMatches_.setSelected(false);
		rvs_.remove(root_);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Refset, Move Member";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		// TODO not correct
		SimpleDisplayConcept sdc = moveFrom_.getSelectionModel().getSelectedItem();
		moveFrom_.getItems().clear();
		moveFrom_.getItems().addAll(conceptList_);
		if (sdc != null && conceptList_.size() > 0)
		{
			moveFrom_.getSelectionModel().select(sdc);
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
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(RefsetMoveMember.this)
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
				// TODO implement RefsetMoveMember
				return "Not yet implemented";
			}
		};
	}
}
