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
package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * {@link LegoListByReference}
 *
 * A LegoList representation that only keeps the identifiers necessary to fetch the Lego, rather than keeping the entire Lego in memory.
 * Used to trim the memory footprint of the GUI.
 * 
 * Note - it may be useful if the DB could return something like this - but will wait and see if it is necessary for performance or not.
 * At the moment, I don't think it is.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoListByReference
{
	private String groupName;
	private String legoListUUID;
	private String groupDescription;
	private String comments;
	private List<LegoReference> lego;

	public LegoListByReference(LegoList legoList, boolean skipLegoRefs)
	{
		this.groupName = legoList.getGroupName();
		this.groupDescription = legoList.getGroupDescription();
		this.legoListUUID = legoList.getLegoListUUID();
		this.comments = legoList.getComment();
		if (!skipLegoRefs)
		{
			for (Lego l : legoList.getLego())
			{
				getLegoReference().add(new LegoReference(l));
			}
		}
	}

	public String getGroupName()
	{
		return groupName;
	}

	public String getLegoListUUID()
	{
		return legoListUUID;
	}

	public String getGroupDescription()
	{
		return groupDescription;
	}

	public String getComments()
	{
		return comments;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public void setComments(String comments)
	{
		this.comments = comments;
	}

	public void setDescription(String description)
	{
		this.groupDescription = description;
	}

	public List<LegoReference> getLegoReference()
	{
		if (lego == null)
		{
			lego = new ArrayList<LegoReference>();
		}
		return this.lego;
	}

}
