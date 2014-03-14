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
import java.io.IOException;
import java.net.URL;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FindAndReplace}
 * 
 * A Find / Replace operation, mostly ported from the SearchReplaceDialog in TK2
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class FindAndReplace extends Operation
{
	private FindAndReplaceController frc_;
	private Logger logger_ = LoggerFactory.getLogger(this.getClass());
	
	private FindAndReplace()
	{
		//For HK2 to init
	}
	@Override
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		super.init(conceptList);
		try
		{
			URL resource = FindAndReplace.class.getResource("FindAndReplaceController.fxml");
			FXMLLoader loader = new FXMLLoader(resource);
			loader.load();
			frc_ = loader.getController();
			super.root_ = frc_.getRoot();
		}
		catch (IOException e)
		{
			logger_.error("Unexpected error building panel", e);
			throw new RuntimeException("Error building panel");
		}
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Find and Replace";
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
		return "Perform a Find and Replace operation on content in the database";
	}

	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#createTask()
	 */
	@Override
	public CustomTask<String> createTask()
	{
		return new CustomTask<String>(FindAndReplace.this)
		{
			@Override
			protected String call() throws Exception
			{
				double i = 0;
				for (SimpleDisplayConcept c : conceptList_)
				{
					if (cancelRequested_)
					{
						return FindAndReplace.this.getTitle() + " was cancelled";
					}
					updateProgress(i, conceptList_.size());
					updateMessage("Processing " + c.getDescription());
					
					//TODO details...
					
					updateProgress(++i, conceptList_.size());
				}
				//TODO figure out how to return / display formatted output
				return FindAndReplace.this.getTitle() + " completed.";
			}
		};
	}
}
