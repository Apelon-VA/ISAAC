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
package gov.va.isaac.associations;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicNid;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;

/**
 * {@link Association}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Association
{
	private RefexDynamicVersionBI<?> refex_;

	//TODO Write the code that checks the index states on startup

	public Association(RefexDynamicVersionBI<?> data)
	{
		refex_ = data;
	}

	public ComponentChronicleBI<?> getSourceComponent() throws IOException
	{
		return ExtendedAppContext.getDataStore().getComponent(refex_.getReferencedComponentNid());
	}

	public ComponentChronicleBI<?> getTargetComponent() throws IOException, ContradictionException
	{
		int targetColIndex = AssociationUtilities.findTargetColumnIndex(refex_.getAssemblageNid());
		if (targetColIndex >= 0)
		{
		
			RefexDynamicDataBI[] data = refex_.getData();
			if (data != null && data.length > targetColIndex)
			{
				if (data[targetColIndex].getRefexDataType() == RefexDynamicDataType.UUID)
				{
					return ExtendedAppContext.getDataStore().getComponent(((RefexDynamicUUID) data[targetColIndex]).getDataUUID());
				}
				else if (data[targetColIndex].getRefexDataType() == RefexDynamicDataType.NID)
				{
					return ExtendedAppContext.getDataStore().getComponent(((RefexDynamicNid) data[targetColIndex]).getDataNid());
				}
			}
		}
		else
		{
			throw new RuntimeException("unexpected");
		}
		
		return null;
	}

	public ConceptChronicleBI getAssociationTypeConcept() throws IOException
	{
		return ExtendedAppContext.getDataStore().getConcept(refex_.getAssemblageNid());
	}

	public String getAssociationName() throws IOException, ContradictionException
	{
		ConceptVersionBI cc = getAssociationTypeConcept().getVersion(OTFUtility.getViewCoordinate());
		
		String best = null;
		for (DescriptionVersionBI<?> desc : cc.getDescriptionsActive(Snomed.SYNONYM_DESCRIPTION_TYPE.getNid()))
		{
			if (best == null)
			{
				best = desc.getText();
			}
			if (OTFUtility.isPreferred(desc.getAnnotations()))
			{
				return desc.getText();
			}
		}
		return best;
	}

	public String getAssociationInverseName() throws ContradictionException, IOException
	{
		ConceptVersionBI cc = getAssociationTypeConcept().getVersion(OTFUtility.getViewCoordinate());
		
		for (DescriptionVersionBI<?> desc : cc.getDescriptionsActive(Snomed.SYNONYM_DESCRIPTION_TYPE.getNid()))
		{
			for (RefexDynamicVersionBI<?> descNestedType : desc.getRefexesDynamicActive(OTFUtility.getViewCoordinate()))
			{
				if (descNestedType.getAssemblageNid() == ISAAC.ASSOCIATION_INVERSE_NAME.getNid())
				{
					return desc.getText();
				}
			}
		}
		return null;
	}

	public RefexDynamicVersionBI<?> getData()
	{
		return refex_;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		try
		{
			return "Association [Name: " + getAssociationName() + " Inverse Name: " + getAssociationInverseName() + " Source: " + getSourceComponent().getPrimordialUuid() 
					+ " Type: " + getAssociationTypeConcept().getPrimordialUuid() + " Target: " + getTargetComponent().getPrimordialUuid() + "]";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return refex_.toString();
		}
	}
}
