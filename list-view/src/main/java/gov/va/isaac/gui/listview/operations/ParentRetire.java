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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dragAndDrop.ConceptIdProvider;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link ParentRetire}
 * 
 * An example operation that does nothing during execution, but provides an example for
 * implementing other operations. Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class ParentRetire extends Operation
{
	private ComboBox<SimpleDisplayConcept> retireAsParent_ = new ComboBox<>();
	private StringProperty replaceOptionsInvalidString_ = new SimpleStringProperty("A concept must be selected from this drop down");
	
	private BooleanBinding allValid_;

	private ParentRetire()
	{
		// For HK2 to init
	}

	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("Retire as parent"), 0, 0);
		retireAsParent_.setPromptText("-Populate the Concepts List-");
		Node n = ErrorMarkerUtils.setupErrorMarker(retireAsParent_, replaceOptionsInvalidString_);
		root_.add(n, 1, 0);

		AppContext.getService(DragRegistry.class).setupDragAndDrop(retireAsParent_, new ConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				return retireAsParent_.getValue().getNid() + "";
			}
		}, false);
		// TODO populate retireAsParentCombo

		retireAsParent_.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(n, Priority.ALWAYS);
		FxUtils.preventColCollapse(root_, 0);

		retireAsParent_.getItems().addListener(new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SimpleDisplayConcept> c)
			{
				if (retireAsParent_.getItems().size() > 0)
				{
					replaceOptionsInvalidString_.set(null);
					if (retireAsParent_.getSelectionModel().getSelectedItem() == null)
					{
						retireAsParent_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					replaceOptionsInvalidString_.set("A concept must be selected from this drop down");
					retireAsParent_.getSelectionModel().clearSelection();
					retireAsParent_.setValue(null);
					retireAsParent_.setPromptText("-Populate the Concepts List-");
				}
			}
		});
		
		allValid_ = new BooleanBinding()
		{
			{
				super.bind(replaceOptionsInvalidString_);
			}
			
			@Override
			protected boolean computeValue()
			{
				return StringUtils.isBlank(replaceOptionsInvalidString_.get());
			}
		};
		
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Parent, Retire";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		// noop
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
		//TODO write description
		return "TODO";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(ParentRetire.this)
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
				//TODO implement ParentRetire
				return "Not yet implemented";
			}
		};
	}
}
