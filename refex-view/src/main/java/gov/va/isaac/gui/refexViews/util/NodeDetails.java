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
package gov.va.isaac.gui.refexViews.util;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * {@link NodeDetails}
 * 
 * A helper class that contains all of the return details for the structured created by a static call to 
 * {@link RefexDataTypeFXNodeBuilder}.
 * 
 * *** WARNING *** boundToAllValid is an Observable List - because its contents will change based on user input if this 
 * was created for a Polymorphic node!  You must add a listener to the list in this case!  A utility method is provided
 * for adding this listener which will automatically update a parent list.  Make sure to call cleanup, to prevent a memory
 * leak if you use this mechanism.
 * 
 * The list will not change for data types other than Polymorphic.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class NodeDetails
{
	protected Object dataField;
	protected Node nodeForDisplay;
	protected ObservableList<ReadOnlyStringProperty> boundToAllValid = new ObservableListWrapper<>(new ArrayList<>());  
	private ListChangeListener<ReadOnlyStringProperty> changeListener;
	
	/**
	 * @return the dataField
	 */
	public Object getDataField()
	{
		return dataField;
	}
	/**
	 * @return the nodeForDisplay
	 */
	public Node getNodeForDisplay()
	{
		return nodeForDisplay;
	}
	/**
	 * @return things that were bound to the allValid parameter.
	 */
	public ObservableList<ReadOnlyStringProperty> getBoundToAllValid()
	{
		return boundToAllValid;
	}
	
	public void addUpdateParentListListener(List<ReadOnlyStringProperty> listToUpdate)
	{
		if (changeListener != null)
		{
			boundToAllValid.removeListener(changeListener);
		}
		changeListener = new ListChangeListener<ReadOnlyStringProperty>()
		{
			
			@Override
			public void onChanged(Change<? extends ReadOnlyStringProperty> listChange)
			{
				while (listChange.next())
				{
					for (ReadOnlyStringProperty sp : listChange.getRemoved())
					{
						listToUpdate.remove(sp);
					}
					listToUpdate.addAll(listChange.getAddedSubList());
				}
			}
		};
		boundToAllValid.addListener(changeListener);
	}
	
	public void cleanupListener()
	{
		if (changeListener != null)
		{
			boundToAllValid.removeListener(changeListener);
			changeListener = null;
		}
	}
}