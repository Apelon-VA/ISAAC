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
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RoleReplaceValue}
 * 
 * An example operation that does nothing during execution, but provides an example for 
 * implementing other operations.  Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class RoleReplaceValue extends Operation
{
	private ComboBox<SimpleDisplayConcept> existingRole_ = new ComboBox<>();
	private StringProperty replaceOptionsInvalidString_ = new SimpleStringProperty("A concept must be selected from this drop down");
	private ConceptNode newRoleValue_ = new ConceptNode(null, true);
	private BooleanBinding allValid_;
	
	private RoleReplaceValue()
	{
		//For HK2 to init
	}
	
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("Existing Role"), 0, 0);
		root_.add(ErrorMarkerUtils.setupErrorMarker(existingRole_, replaceOptionsInvalidString_), 1, 0);
		existingRole_.setPromptText("-Populate the Concepts List-");
		existingRole_.setMaxWidth(Double.MAX_VALUE);
		ComboBoxSetupTool.setupComboBox(existingRole_);
		
		root_.add(new Label("New Role Value"), 0, 1);
		root_.add(newRoleValue_.getNode(), 1, 1);
		
		FxUtils.preventColCollapse(root_, 0);
		GridPane.setHgrow(newRoleValue_.getNode(), Priority.ALWAYS);
		
		//TODO are there restrictions on concepts that can be roles?
		allValid_ = new BooleanBinding()
		{
			{
				super.bind(existingRole_.getItems(), newRoleValue_.isValid());
			}
			@Override
			protected boolean computeValue()
			{
				//do a little unrelated work
				if (existingRole_.getItems().size() > 0)
				{
					replaceOptionsInvalidString_.set(null);
					if (existingRole_.getSelectionModel().getSelectedItem() == null)
					{
						existingRole_.getSelectionModel().selectFirst();
					}
				}
				else
				{
					replaceOptionsInvalidString_.set("A concept must be selected from this drop down");
					existingRole_.getSelectionModel().clearSelection();
					existingRole_.setValue(null);
					existingRole_.setPromptText("-Populate the Concepts List-");
				}
				return existingRole_.getItems().size() > 0 && newRoleValue_.isValid().get();
			}
		};
		
		//TODO populate existingRoleDropDown
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Role, Replace Value";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		//TODO this isn't even close to right - 
		existingRole_.getItems().clear();
		existingRole_.getItems().addAll(conceptList_);
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
		//TODO describe
		return "TODO";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(RoleReplaceValue.this)
		{
			@Override
			protected String call() throws Exception
			{
//				double i = 0;
//				for (SimpleDisplayConcept c : conceptList_)
//				{
//					if (cancelRequested_)
//					{
//						return ParentAddNew.this.getTitle() + " was cancelled";
//					}
//					updateProgress(i, conceptList_.size());
//					updateMessage("processing " + c.getDescription());
//					Thread.sleep(3000);
//					if (cancelRequested_)
//					{
//						return ParentAddNew.this.getTitle() + " was cancelled";
//					}
//					updateProgress((i + 0.5), conceptList_.size());
//					updateMessage("still working on " + c.getDescription());
//					Thread.sleep(3000);
//					if (cancelRequested_)
//					{
//						return ParentAddNew.this.getTitle() + " was cancelled";
//					}
//					updateProgress(++i, conceptList_.size());
//				}
//				return ParentAddNew.this.getTitle() + " completed - modified 0 concepts";
				//TODO implement RoleReplaceValue
				return "Not yet implemented";
			}
		};
	}
}
