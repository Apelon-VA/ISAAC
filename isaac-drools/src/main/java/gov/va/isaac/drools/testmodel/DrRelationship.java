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
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * 
 * {@link DrRelationship}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DrRelationship extends DrComponent
{
	private String primordialUuid;

	private String sourceUuid;
	private String typeUuid;
	private String targetUuid;
	private String characteristicUuid;
	private String modifierUuid;
	private int relGroup;

	private List<DrIdentifier> identifiers;

	// Inferred properties
	// none yet

	@Override
	public String toString()
	{
		StringBuffer relSb = new StringBuffer("");
		try
		{
			relSb.append("primordialUuid: " + primordialUuid + ",");

			try
			{
				ConceptChronicleBI source = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(sourceUuid));
				relSb.append(" Source Rel: " + source + " (" + sourceUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI type = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(typeUuid));
				relSb.append(" Type: " + type + " (" + typeUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI target = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(targetUuid));
				relSb.append(" Target Rel: " + target + " (" + targetUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI characteristic = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(characteristicUuid));
				relSb.append(" Characteristic: " + characteristic + " (" + characteristicUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI modifier = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(modifierUuid));
				relSb.append(" Modifier: " + modifier + " (" + modifierUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			relSb.append(" Relationship group: " + relGroup + ",");
			relSb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");
			relSb.append("\nIdentifiers: [");
			if (identifiers != null)
			{
				for (DrIdentifier identifier : identifiers)
				{
					int i = 0;
					relSb.append(identifier.toString() + (i == identifiers.size() - 1 ? "" : ","));
					i++;
				}
			}
			relSb.append("]");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return relSb.toString();
	}

	public DrRelationship()
	{
	}

	public String getPrimordialUuid()
	{
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid)
	{
		this.primordialUuid = primordialUuid;
	}

	public String getCharacteristicUuid()
	{
		return characteristicUuid;
	}

	public void setCharacteristicUuid(String characteristicUuid)
	{
		this.characteristicUuid = characteristicUuid;
	}

	public int getRelGroup()
	{
		return relGroup;
	}

	public void setRelGroup(int relGroup)
	{
		this.relGroup = relGroup;
	}

	public String getTypeUuid()
	{
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid)
	{
		this.typeUuid = typeUuid;
	}

	public List<DrIdentifier> getIdentifiers()
	{
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers)
	{
		this.identifiers = identifiers;
	}

	public String getModifierUuid()
	{
		return modifierUuid;
	}

	public void setModifierUuid(String modifierUuid)
	{
		this.modifierUuid = modifierUuid;
	}

	public String getSourceUuid()
	{
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid)
	{
		this.sourceUuid = sourceUuid;
	}

	public String getTargetUuid()
	{
		return targetUuid;
	}

	public void setTargetUuid(String targetUuid)
	{
		this.targetUuid = targetUuid;
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
		result = prime * result + ((characteristicUuid == null) ? 0 : characteristicUuid.hashCode());
		result = prime * result + ((sourceUuid == null) ? 0 : sourceUuid.hashCode());
		result = prime * result + ((targetUuid == null) ? 0 : targetUuid.hashCode());
		result = prime * result + ((typeUuid == null) ? 0 : typeUuid.hashCode());
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
		DrRelationship other = (DrRelationship) obj;
		if (characteristicUuid == null)
		{
			if (other.characteristicUuid != null)
				return false;
		}
		else if (!characteristicUuid.equals(other.characteristicUuid))
			return false;
		if (sourceUuid == null)
		{
			if (other.sourceUuid != null)
				return false;
		}
		else if (!sourceUuid.equals(other.sourceUuid))
			return false;
		if (targetUuid == null)
		{
			if (other.targetUuid != null)
				return false;
		}
		else if (!targetUuid.equals(other.targetUuid))
			return false;
		if (typeUuid == null)
		{
			if (other.typeUuid != null)
				return false;
		}
		else if (!typeUuid.equals(other.typeUuid))
			return false;
		return true;
	}

}
