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

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.FxUtils;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import org.glassfish.hk2.api.PerLookup;

/**
 * {@link LogicCreateUnionSet}
 * 
 * An example operation that does nothing during execution, but provides an example for 
 * implementing other operations.  Will remove this code, eventually, as other real operations
 * are implemented.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
//@Service  TODO Uncomment this when implemented
@PerLookup
public class LogicCreateUnionSet extends Operation
{
	private LogicCreateUnionSet()
	{
		//For HK2 to init
	}
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		root_.add(new Label("User configuration detail: "), 0, 0);
		root_.add(new Label("user setting"), 1, 0);
		FxUtils.preventColCollapse(root_, 0);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Logic, Create Union Set";
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
		return new SimpleBooleanProperty(true);
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getOperationDescription()
	 */
	@Override
	public String getOperationDescription()
	{
		return "A do-nothing operation for demonstration purposes";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(LogicCreateUnionSet.this)
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
				//TODO implement LogicCreateUnionSet
				return "Not yet implemented";
			}
		};
	}
}
