/**
 * Copyright 2013
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
package gov.va.legoEdit.model.bdbModel;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.WriteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * 
 * LegoListBDB
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
@Entity
public class LegoListBDB
{

	@SecondaryKey(relate = Relationship.ONE_TO_ONE) protected String groupName;
	@PrimaryKey protected String legoListUUID;
	protected String groupDescription;
	protected String comment;
	protected List<String> legoUniqueIds;
	@SecondaryKey(relate = Relationship.ONE_TO_MANY) protected Set<String> legoUUIDs;
	protected HashMap<String, Integer> legoUUIDsUsage;

	private transient List<LegoBDB> legoBDBRefs;

	@SuppressWarnings("unused")
	private LegoListBDB()
	{
		// required by BDB
	}

	public LegoListBDB(String uuid, String groupName, String groupDescription, String comment)
	{
		this.groupName = groupName;
		this.groupDescription = groupDescription;
		this.comment = comment;
		this.legoListUUID = uuid;
		this.legoUniqueIds = new ArrayList<>();
		this.legoUUIDs = new HashSet<>();
		this.legoUUIDsUsage = new HashMap<>();
	}

	public LegoListBDB(LegoList ll) throws WriteException
	{
		groupDescription = ll.getGroupDescription();
		groupName = ll.getGroupName();
		legoListUUID = ll.getLegoListUUID();
		comment = ll.getComment();
		legoUniqueIds = new ArrayList<>();
		legoBDBRefs = new ArrayList<>();
		this.legoUUIDs = new HashSet<>();
		this.legoUUIDsUsage = new HashMap<>();
		for (Lego l : ll.getLego())
		{
			LegoBDB lBDB = new LegoBDB(l);

			verifyLegoUUID(l.getLegoUUID());
			legoUniqueIds.add(lBDB.getUniqueId());
			legoBDBRefs.add(lBDB);
			legoUUIDs.add(l.getLegoUUID());
			Integer temp = legoUUIDsUsage.get(l.getLegoUUID());
			if (temp == null)
			{
				temp = new Integer(1);
			}
			else
			{
				temp = new Integer(1 + temp.intValue());
			}
			legoUUIDsUsage.put(l.getLegoUUID(), temp);
		}
	}

	public List<LegoBDB> getLegoBDBs()
	{
		return legoBDBRefs;
	}

	public void addLego(LegoBDB lego) throws WriteException
	{
		verifyLegoUUID(lego.getLegoUUID());
		legoUniqueIds.add(lego.getUniqueId());
		legoUUIDs.add(lego.getLegoUUID());
		Integer temp = legoUUIDsUsage.get(lego.getLegoUUID());
		if (temp == null)
		{
			temp = new Integer(1);
		}
		else
		{
			temp = new Integer(1 + temp.intValue());
		}
		legoUUIDsUsage.put(lego.getLegoUUID(), temp);
	}

	public void removeLego(String legoUUID, String legoUniqueId)
	{
		legoUniqueIds.remove(legoUniqueId);
		Integer temp = legoUUIDsUsage.get(legoUUID);
		if (temp != null)
		{
			temp = new Integer(temp.intValue() - 1);
			legoUUIDsUsage.put(legoUUID, temp);
		}
		if (temp == null || temp.intValue() == 0)
		{
			legoUUIDs.remove(legoUUID);
			legoUUIDsUsage.remove(legoUUID);
		}
	}

	private void verifyLegoUUID(String legoUUID) throws WriteException
	{
		// legoUUID should only be used by this legoList (no other)
		List<String> legoListUUIDs = BDBDataStoreImpl.getInstance().getLegoListByLego(legoUUID);
		for (String s : legoListUUIDs)
		{
			if (!this.legoListUUID.equals(s))
			{
				throw new WriteException("The LEGO UUID '" + legoUUID + "' is already in use by the legoList '" + s + "'.  Lego UUIDs should not cross legoLists.");
			}
		}
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public String getGroupDescription()
	{
		return groupDescription;
	}

	public void setGroupDescription(String description)
	{
		this.groupDescription = description;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getComment()
	{
		return comment;
	}

	public String getLegoListUUID()
	{
		return legoListUUID;
	}

	public List<String> getUniqueLegoIds()
	{
		ArrayList<String> result = new ArrayList<>();
		if (legoUniqueIds != null)
		{
			for (String s : this.legoUniqueIds)
			{
				result.add(s);
			}
		}
		return result;
	}

	public LegoList toSchemaLegoList()
	{
		LegoList ll = new LegoList();
		ll.setGroupDescription(groupDescription);
		ll.setGroupName(groupName);
		ll.setLegoListUUID(legoListUUID);
		ll.setComment(comment);
		List<Lego> legos = ll.getLego();

		for (String lui : legoUniqueIds)
		{
			Lego l = ((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getLegoByUniqueId(lui);
			if (l == null)
			{
				throw new DataStoreException("This shouldn't have been null!");
			}
			legos.add(l);
		}

		return ll;
	}
}
