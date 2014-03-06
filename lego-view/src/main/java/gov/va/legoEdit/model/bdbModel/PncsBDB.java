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
package gov.va.legoEdit.model.bdbModel;

import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * 
 * PncsBDB
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */

@Entity
public class PncsBDB
{

	@PrimaryKey private String uniqueId;

	@SecondaryKey(relate = Relationship.MANY_TO_ONE) protected int id;
	protected String name;
	protected String value;

	@SuppressWarnings("unused")
	private PncsBDB()
	{
		// required by BDB
	}

	public PncsBDB(Pncs pncs) throws WriteException
	{
		id = pncs.getId();
		name = pncs.getName();
		value = pncs.getValue();
		uniqueId = makeUniqueId(id, value);

		// Need to make sure that they didn't pass in an ID with a name that differs from what it previously had...
		Pncs existing = BDBDataStoreImpl.getInstance().getPncs(id, value);
		if (existing != null)
		{
			if (!existing.getName().equals(name))
			{
				throw new WriteException("The PNCS ID '" + id + "' and value '" + value + "' is already associated with the name '" + existing.getName()
						+ "'.  It cannot be reassociated with '" + name + "'.");
			}
		}

	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

	public String getUniqueId()
	{
		return this.uniqueId;
	}

	public static String makeUniqueId(int id, String value)
	{
		return id + ":" + value;
	}

	public Pncs toSchemaPncs()
	{
		Pncs pncs = new Pncs();
		pncs.setId(id);
		pncs.setName(name);
		pncs.setValue(value);
		return pncs;
	}
}
