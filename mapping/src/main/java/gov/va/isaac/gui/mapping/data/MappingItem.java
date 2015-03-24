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
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MappingItem}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MappingItem
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingItem.class);

	private UUID editorStatusConcept, primordialUUID, mappingSetIDConcept, qualifierConcept, sourceConcept, targetConcept;
	boolean isActive;
	long creationTime;

	protected MappingItem(RefexDynamicChronicleBI<?> refex) throws IOException
	{
		try
		{
			read(refex.getVersion(OTFUtility.getViewCoordinate()));
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected error", e);
		}
	}
	
	private void read(RefexDynamicVersionBI<?> refex) throws IOException
	{
		primordialUUID = refex.getPrimordialUuid();
		sourceConcept = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(refex.getReferencedComponentNid());
		mappingSetIDConcept = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(refex.getAssemblageNid());
		isActive = refex.isActive();
		creationTime = refex.getTime();
		
		RefexDynamicDataBI[] data = refex.getData();
		targetConcept = ((data != null && data.length > 0) ? ((RefexDynamicUUID) data[0]).getDataUUID() : null);
		qualifierConcept = ((data != null && data.length > 1 && data[1] != null) ? ((RefexDynamicUUID) data[1]).getDataUUID() : null); 
		editorStatusConcept = ((data != null && data.length > 2 && data[2] != null) ? ((RefexDynamicUUID) data[2]).getDataUUID() : null);
	}

	
	public String getSummary() {
		return  (isActive ? "Active " : "Retired ") + "Mapping: " + OTFUtility.getDescription(sourceConcept) + "-" + OTFUtility.getDescription(mappingSetIDConcept)
				+ "-" + OTFUtility.getDescription(targetConcept) + "-" + (qualifierConcept == null ? "no qualifier" : OTFUtility.getDescription(qualifierConcept)) 
				+ "-" + (editorStatusConcept == null ? "no status" : OTFUtility.getDescription(editorStatusConcept)) + "-" + primordialUUID.toString();
	}
	
	/**
	 * @return Any comments attached to this mapping set.
	 * @throws IOException 
	 */
	public List<MappingItemComment> getComments() throws IOException
	{
		return MappingItemCommentDAO.getComments(getPrimordialUUID(), false);
	}
	
	/**
	 * Add a comment to this mapping set
	 * @param commentText - the text of the comment
	 * @return - the added comment
	 * @throws IOException
	 */
	public MappingItemComment addComment(String commentText) throws IOException
	{
		//TODO do we want to utilize the other comment field (don't have to)
		return MappingItemCommentDAO.createMappingItemComment(this.getPrimordialUUID(), commentText, null);
	}

	/**
	 * @return the editorStatusConcept
	 */
	public UUID getEditorStatusConcept()
	{
		return editorStatusConcept;
	}

	//TODO not sure if we should allow changes to the qualifier concept.  I would say yes... but the qualifier concept ID 
	//is part of the value that is hashed to create the UUID of the map item... it may be best to retire and create a new one in this case.
	
	
	/**
	 * @param editorStatusConcept the editorStatusConcept to set
	 */
	public void setEditorStatusConcept(UUID editorStatusConcept)
	{
		this.editorStatusConcept = editorStatusConcept;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive()
	{
		return isActive;
	}

	/**
	 * @return the primordialUUID of this Mapping Item.  Note that this doesn't uniquely identify a mapping item within the system
	 * as changes to the mapping item will retain the same ID - there will now be multiple versions.  They will differ by date.
	 */
	public UUID getPrimordialUUID()
	{
		return primordialUUID;
	}

	/**
	 * @return the mappingSetIDConcept
	 */
	public UUID getMappingSetIDConcept()
	{
		return mappingSetIDConcept;
	}

	/**
	 * @return the qualifierConcept
	 */
	public UUID getQualifierConcept()
	{
		return qualifierConcept;
	}

	/**
	 * @return the sourceConcept
	 */
	public UUID getSourceConcept()
	{
		return sourceConcept;
	}

	/**
	 * @return the targetConcept
	 */
	public UUID getTargetConcept()
	{
		return targetConcept;
	}
	
	/**
	 * @return the creationTime
	 */
	public long getCreationTime()
	{
		return creationTime;
	}
}
