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

import gov.va.isaac.AppContext;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * 
 * {@link DrLanguageDesignationSet}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DrLanguageDesignationSet
{

	Set<DrDescription> descriptions;
	String languageRefsetUuid;

	// Inferred properties
	int size = 0;
	int preferredTermOccurrence = 0;
	int preferredFsnOccurrence = 0;
	boolean hasDuplicates = false;

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("");
		try
		{
			try
			{
				ConceptChronicleBI languageRefset = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(languageRefsetUuid));
				sb.append("Language Refset: " + languageRefset + " (" + languageRefsetUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			sb.append(" Size: " + size + ",");
			sb.append(" PreferredTermOccurrence: " + preferredTermOccurrence + ",");
			sb.append(" PreferredFsnOccurrence: " + preferredFsnOccurrence + ",");
			sb.append(" HasDuplicates: " + hasDuplicates);
			sb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public DrLanguageDesignationSet()
	{
		descriptions = new HashSet<DrDescription>();
	}

	public Set<DrDescription> getDescriptions()
	{
		return descriptions;
	}

	public void setDescriptions(Set<DrDescription> descriptions)
	{
		this.descriptions = descriptions;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getPreferredTermOccurrence()
	{
		return preferredTermOccurrence;
	}

	public void setPreferredTermOccurrence(int preferredTermOccurrence)
	{
		this.preferredTermOccurrence = preferredTermOccurrence;
	}

	public int getPreferredFsnOccurrence()
	{
		return preferredFsnOccurrence;
	}

	public void setPreferredFsnOccurrence(int preferredFsnOccurrence)
	{
		this.preferredFsnOccurrence = preferredFsnOccurrence;
	}

	public boolean isHasDuplicates()
	{
		return hasDuplicates;
	}

	public void setHasDuplicates(boolean hasDuplicates)
	{
		this.hasDuplicates = hasDuplicates;
	}

	public String getLanguageRefsetUuid()
	{
		return languageRefsetUuid;
	}

	public void setLanguageRefsetUuid(String languageRefsetUuid)
	{
		this.languageRefsetUuid = languageRefsetUuid;
	}
}
