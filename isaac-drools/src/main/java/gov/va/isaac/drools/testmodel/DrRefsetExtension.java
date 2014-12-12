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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * 
 * {@link DrRefsetExtension}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DrRefsetExtension extends DrComponent
{
	private String primordialUuid;

	private String refsetUuid;
	private String componentUuid;

	private String c1Uuid;
	private String c2Uuid;
	private String c3Uuid;
	private String strValue;
	private boolean booleanValue;
	private float floatValue;
	private int intValue;
	private long longValue;
	private RefsetType refsetType;

	private List<DrIdentifier> identifiers;

	//Inferred properties
	// none yet

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("");
		try
		{
			sb.append(" String Value: " + strValue + "(" + primordialUuid + "),");
			sb.append(" Boolean Value: " + booleanValue + ",");
			sb.append(" Float Value: " + floatValue + ",");
			sb.append(" Int Value: " + intValue + ",");
			sb.append(" Long Value: " + longValue + ",");
			sb.append(" Refset Type: " + refsetType + ",");

			try
			{
				ConceptChronicleBI refset = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(refsetUuid));
				sb.append(" Refset: " + refset + " (" + refsetUuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI c1 = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(c1Uuid));
				sb.append(" C1: " + c1 + " (" + c1Uuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI c2 = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(c2Uuid));
				sb.append(" C2: " + c2 + " (" + c2Uuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI c3 = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(c3Uuid));
				sb.append(" C3: " + c3 + " (" + c3Uuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}

			try
			{
				ConceptChronicleBI c1 = AppContext.getService(TerminologyStoreDI.class).getConcept(UUID.fromString(c1Uuid));
				sb.append(" C1: " + c1 + " (" + c1Uuid + "),");
			}
			catch (IllegalArgumentException ex)
			{
			}
			sb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");
			sb.append("\nIdentifiers: [");
			if (identifiers != null)
			{
				for (DrIdentifier identifier : identifiers)
				{
					int i = 0;
					sb.append(identifier.toString() + (i == identifiers.size() - 1 ? "" : ","));
					i++;
				}
			}
			sb.append("]");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public DrRefsetExtension()
	{
		identifiers = new ArrayList<DrIdentifier>();
	}

	public String getPrimordialUuid()
	{
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid)
	{
		this.primordialUuid = primordialUuid;
	}

	public String getC1Uuid()
	{
		return c1Uuid;
	}

	public void setC1Uuid(String uuid)
	{
		c1Uuid = uuid;
	}

	public String getC2Uuid()
	{
		return c2Uuid;
	}

	public void setC2Uuid(String uuid)
	{
		c2Uuid = uuid;
	}

	public String getC3Uuid()
	{
		return c3Uuid;
	}

	public void setC3Uuid(String uuid)
	{
		c3Uuid = uuid;
	}

	public String getStrValue()
	{
		return strValue;
	}

	public void setStrValue(String strValue)
	{
		this.strValue = strValue;
	}

	public boolean isBooleanValue()
	{
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue)
	{
		this.booleanValue = booleanValue;
	}

	public float getFloatValue()
	{
		return floatValue;
	}

	public void setFloatValue(float floatValue)
	{
		this.floatValue = floatValue;
	}

	public int getIntValue()
	{
		return intValue;
	}

	public void setIntValue(int intValue)
	{
		this.intValue = intValue;
	}

	public long getLongValue()
	{
		return longValue;
	}

	public void setLongValue(long longValue)
	{
		this.longValue = longValue;
	}

	public List<DrIdentifier> getIdentifiers()
	{
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers)
	{
		this.identifiers = identifiers;
	}

	public RefsetType getRefsetType()
	{
		return refsetType;
	}

	public void setRefsetType(RefsetType refsetType)
	{
		this.refsetType = refsetType;
	}

	public String getRefsetUuid()
	{
		return refsetUuid;
	}

	public void setRefsetUuid(String refsetUuid)
	{
		this.refsetUuid = refsetUuid;
	}

	public String getComponentUuid()
	{
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid)
	{
		this.componentUuid = componentUuid;
	}

}
