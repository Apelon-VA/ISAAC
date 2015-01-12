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
package gov.va.isaac.drools.testmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * {@link DrDefiningRolesSet}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DrDefiningRolesSet
{
	private String rolesSetType;
	private List<DrRelationship> relationships;

	//Inferred properties
	private int numberOfIsas = 0;

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("");
		try
		{
			sb.append("RolesSetType: " + rolesSetType + ",");
			sb.append("NumberOfIsas: " + numberOfIsas + ",");
			sb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");

			sb.append("\nRelationships: " + relationships);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public DrDefiningRolesSet()
	{
		super();
		this.relationships = new ArrayList<DrRelationship>();
	}

	public String getRolesSetType()
	{
		return rolesSetType;
	}

	public void setRolesSetType(String rolesSetType)
	{
		this.rolesSetType = rolesSetType;
	}

	public List<DrRelationship> getRelationships()
	{
		return relationships;
	}

	public void setRelationships(List<DrRelationship> relationships)
	{
		this.relationships = relationships;
	}

	public int getNumberOfIsas()
	{
		numberOfIsas = 0;
		for (DrRelationship loopRel : relationships)
		{
			if (loopRel.getCharacteristicUuid().equals("3b0dbd3b-2e53-3a30-8576-6c7fa7773060") && loopRel.getTypeUuid().equals("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")
					&& loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f"))
			{
				numberOfIsas++;
			}
		}
		return numberOfIsas;
	}

	public void setNumberOfIsas(int numberOfIsas)
	{
		this.numberOfIsas = numberOfIsas;
	}

	// GroupCondition options = 'any','ungrouped','same group'
	public boolean checkCardinality(String typeUuid, int min, int max, String groupCondition)
	{
		boolean result = false;
		if (groupCondition.equals("ungrouped"))
		{
			int counter = 0;
			for (DrRelationship loopRel : relationships)
			{
				if (loopRel.getTypeUuid().equals(typeUuid) && loopRel.getRelGroup() == 0 && loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f"))
				{
					counter++;
				}
			}
			if (counter >= min && counter <= max)
			{
				result = true;
			}
		}
		if (groupCondition.equals("same group"))
		{
			result = true;
			Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
			for (DrRelationship loopRel : relationships)
			{
				if (loopRel.getTypeUuid().equals(typeUuid) && loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f"))
				{
					int group = loopRel.getRelGroup();
					if (countMap.containsKey(group))
					{
						countMap.put(group, countMap.get(group) + 1);
					}
					else
					{
						countMap.put(group, 1);
					}
				}
			}
			for (int group : countMap.keySet())
			{
				if (countMap.get(group) < min || countMap.get(group) > max)
				{
					result = false;
				}
			}
		}
		else
		{ // "any" or unknown value
			int counter = 0;
			for (DrRelationship loopRel : relationships)
			{
				if (loopRel.getTypeUuid().equals(typeUuid) && loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f"))
				{
					counter++;
				}
			}
			if (counter >= min && counter <= max)
			{
				result = true;
			}
		}

		return result;
	}

	public boolean hasDuplicatedRoleGroups()
	{
		boolean result = false;

		Map<Integer, List<DrRelationship>> groups = new HashMap<Integer, List<DrRelationship>>();
		Map<Integer, List<DrRelationship>> groups2 = new HashMap<Integer, List<DrRelationship>>();

		for (DrRelationship loopRel : relationships)
		{
			if (loopRel.getRelGroup() != 0 && !groups.containsKey(loopRel.getRelGroup()))
			{
				List<DrRelationship> rels = new ArrayList<DrRelationship>();
				rels.add(loopRel);
				groups.put(loopRel.getRelGroup(), rels);
				groups2.put(loopRel.getRelGroup(), rels);
			}
			else if (loopRel.getRelGroup() != 0 && groups.containsKey(loopRel.getRelGroup()))
			{
				List<DrRelationship> rels = groups.get(loopRel.getRelGroup());
				rels.add(loopRel);
				groups.put(loopRel.getRelGroup(), rels);
				groups2.put(loopRel.getRelGroup(), rels);
			}
		}

		for (int groupId : groups.keySet())
		{
			List<DrRelationship> group = groups.get(groupId);
			for (int group2Id : groups2.keySet())
			{
				List<DrRelationship> group2 = groups2.get(group2Id);
				if (groupId != group2Id && group.containsAll(group2) && group2.containsAll(group))
				{
					result = true;
				}
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relationships == null) ? 0 : relationships.hashCode());
		result = prime * result + ((rolesSetType == null) ? 0 : rolesSetType.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DrDefiningRolesSet other = (DrDefiningRolesSet) obj;
		if (relationships == null)
		{
			if (other.relationships != null)
				return false;
		}
		else if (!relationships.equals(other.relationships))
			return false;
		if (rolesSetType == null)
		{
			if (other.rolesSetType != null)
				return false;
		}
		else if (!rolesSetType.equals(other.rolesSetType))
			return false;
		return true;
	}

}
