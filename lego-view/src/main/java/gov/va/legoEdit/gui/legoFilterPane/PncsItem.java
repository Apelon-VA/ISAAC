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
package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.legoEdit.model.schemaModel.Pncs;
import javafx.beans.value.ObservableObjectValue;

/**
 * 
 * {@link PncsItem}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PncsItem
{

	String name;
	int id;
	ObservableObjectValue<String> displayType;

	public PncsItem(String name, int id, ObservableObjectValue<String> displayType)
	{
		this.name = name;
		this.id = id;
		this.displayType = displayType;
	}

	public PncsItem(Pncs pncs, ObservableObjectValue<String> displayType)
	{
		this.name = pncs.getName();
		this.id = pncs.getId();
		this.displayType = displayType;
	}

	@Override
	public String toString()
	{
		if (displayType.get().equals("Name") || name.equals(LegoFilterPaneController.ANY))
		{
			return name;
		}
		else
		{
			return id + "";
		}
	}

	public String getName()
	{
		return name;
	}

	public int getId()
	{
		return id;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof PncsItem)
		{
			PncsItem other = (PncsItem) obj;
			if (this.id == other.id && this.name.equals(other.name))
			{
				return true;
			}
		}
		return false;
	}
}
