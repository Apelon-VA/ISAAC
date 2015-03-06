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
package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.util.OTFUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;

/**
 * {@link MappingItem}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MappingItem
{
	private RefexDynamicVersionBI<?> refex_;

	protected MappingItem(RefexDynamicChronicleBI<?> refex)
	{
		try
		{
			refex_ = refex.getVersion(OTFUtility.getViewCoordinate());
		}
		catch (ContradictionException e)
		{
			throw new RuntimeException("Unexpected error", e);
		}
	}
	
	protected MappingItem(UUID sourceConcept, UUID mappingSetID, UUID targetConcept, UUID qualifier, UUID editorStatus) throws IOException
	{
		try
		{
			ConceptVersionBI cv = OTFUtility.getConceptVersion(sourceConcept);
			
			RefexDynamicCAB mappingAnnotation = new RefexDynamicCAB(sourceConcept, mappingSetID);
			mappingAnnotation.setData(new RefexDynamicDataBI[] {
					(targetConcept == null ? null : new RefexDynamicUUID(targetConcept)),
					(qualifier == null ? null : new RefexDynamicUUID(qualifier)),
					(editorStatus == null ? null : new RefexDynamicUUID(editorStatus))}, OTFUtility.getViewCoordinate());
			
			
			UUID mappingUUID = UuidT5Generator.get(MappingConstants.MAPPING_NAMESPACE.getPrimodialUuid(), 
					sourceConcept.toString() + "|" 
					+ mappingSetID.toString() + "|"
					+ targetConcept.toString() + "|" 
					+ qualifier.toString());
			
			if (ExtendedAppContext.getDataStore().hasUuid(mappingUUID))
			{
				throw new IOException("A mapping with the specified source, target and qualifier already exists in this set.  Please edit that mapping.");
			}
			
			mappingAnnotation.setComponentUuidNoRecompute(mappingUUID);
			
			OTFUtility.getBuilder().construct(mappingAnnotation);
			
			ExtendedAppContext.getDataStore().addUncommitted(cv);
			ExtendedAppContext.getDataStore().commit(cv);
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException | NoSuchAlgorithmException e)
		{
			throw new RuntimeException("Unexpected error", e);
		}
		
	}

	public UUID getSourceConcept()
	{
		try
		{
			return ExtendedAppContext.getDataStore().getUuidPrimordialForNid(refex_.getReferencedComponentNid());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unexpected error", e);
		}
	}

	public UUID getMappingTypeConcept()
	{
		try
		{
			return ExtendedAppContext.getDataStore().getUuidPrimordialForNid(refex_.getAssemblageNid());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unexpected error", e);
		}
	}

	public UUID getTargetConcept()
	{
		RefexDynamicDataBI[] data = refex_.getData();
		if (data != null && data.length > 0)
		{
			return ((RefexDynamicUUID) data[0]).getDataUUID();
		}
		return null;
	}
	
	public UUID getQualifierConcept()
	{
		RefexDynamicDataBI[] data = refex_.getData();
		if (data != null && data.length > 1)
		{
			return ((RefexDynamicUUID) data[1]).getDataUUID();
		}
		return null;
	}
	
	public UUID getEditorStatusConcept()
	{
		RefexDynamicDataBI[] data = refex_.getData();
		if (data != null && data.length > 2)
		{
			return ((RefexDynamicUUID) data[2]).getDataUUID();
		}
		return null;
	}
	
	/**
	 * @return Any comments attached to this mapping set.
	 */
	public List<Object> getComments()
	{
		//TODO implement
		return new ArrayList<>();
	}
	
	//TODO implement edit / retire methods
}
