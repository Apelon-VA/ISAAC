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
import gov.va.isaac.gui.util.FxUtils;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link ParentAddNew}
 * 
 * An example operation that does nothing during execution, but provides an example for 
 * implementing other operations.  Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class ParentAddNew extends Operation
{
	private ComboBox<SimpleDisplayConcept> relationship_ = new ComboBox<>();
	private ConceptNode newParent_ = new ConceptNode(null, true);
	
	private ParentAddNew()
	{
		//For HK2 to init
	}
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("Relationship to use"), 0, 0);
		root_.add(relationship_, 1, 0);
		root_.add(new Label("New Parent"), 0, 1);
		root_.add(newParent_.getNode(), 1, 1);
		
		ComboBoxSetupTool.setupComboBox(relationship_);
		//TODO populate relationshipCombo
		
		relationship_.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(relationship_, Priority.ALWAYS);
		FxUtils.preventColCollapse(root_, 0);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Parent, Add New";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#conceptListChanged()
	 */
	@Override
	protected void conceptListChanged()
	{
		//noop
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#isValid()
	 */
	@Override
	public BooleanExpression isValid()
	{
		return newParent_.isValid();
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getOperationDescription()
	 */
	@Override
	public String getOperationDescription()
	{
		return "Add a new parent to a concept";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(ParentAddNew.this)
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
				//TODO implement ParentAddNew
				return "Not yet implemented";
			}
		};
	}
}
