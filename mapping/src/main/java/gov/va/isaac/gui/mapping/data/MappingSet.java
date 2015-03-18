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
package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MappingSet}
 *
 * A Convenience class to hide unnecessary OTF bits from the Mapping APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MappingSet
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingSet.class);
	
	private String name, inverseName, description, purpose;
	private UUID primordialUUID, editorStatus;
	private boolean active;
	
	/**
	 * 
	 *  Read an existing mapping set from the database
	 * 
	 * @param refex RefexDynamicChronicleBI<?>
	 * @throws IOException 
	 */
	public MappingSet(RefexDynamicVersionBI<?> refex) throws IOException
	{
		this.readFromRefex(refex); //Sets Name, inverseName and Description, etc
	}
	
	
	/**
	 * 
	 * Construct a NEW Mapping Set by creating a new concept and passing in the set name, inverse name, purpose, description
	 *  and the UUID of the editor 
	 * 
	 * @param mappingName Mapping Set Name
	 * @param inverseName Inverse Mapping Set Name
	 * @param purpose Mapping Set Purpose
	 * @param description Mapping Set Description
	 * @param editorStatus UUID editor
	 * @throws IOException
	 */
	public MappingSet (String mappingNameInput, 
							String purposeInput, 
							String descriptionInput, 
							UUID editorStatusInput) throws IOException
	{
		this.setName(mappingNameInput);
		//this.setInverseName(inverseNameInput);
		this.setPurpose(purposeInput);
		this.setDescription(descriptionInput);
		this.setEditorStatus(editorStatusInput);
		RefexDynamicVersionBI<?> refex = MappingSetDAO.createMappingSetRefex(this);
		
		// Read UUID and Active flag from refex
		this.readFromRefex(refex);

		// TODO Remove for primetime
		// Generate random mapping items
		MappingItem.generateRandomMappingItems(this.getPrimordialUUID());
		
	}
	
	public void save() {
		MappingSetDAO.updateMappingSet(this);
	}
	
	public List<MappingItem> getMappingItems() throws IOException {
		return MappingItem.getMappingItems(this.getPrimordialUUID());
	}
	
	//TODO: vk - all of these javadocs
	
	public void setActive(boolean activeInput)
	{
		active = activeInput;
	}
	
	/**
	 * Is this mapping concept active or retired?
	 */
	public boolean isActive()
	{
		return active;
	}
	
	public void setEditorStatus(UUID thisEditorStatus)
	{
		editorStatus = thisEditorStatus;
	}
	
	/**
	 * @return The UUID of the concept that represents the editor selected status of the mapping set.  May return null.
	 */
	public UUID getEditorStatus()
	{
		return editorStatus;
	}
	
	/**
	 * @return The 'purpose' of the mapping set.  May return null.
	 */
	public void setPurpose(String purposeInput) {
		purpose = purposeInput;
	}
	
	public String getPurpose() 
	{
		return purpose;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getInverseName()
	{
		return inverseName;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setName(String nameInput) 
	{
		name = nameInput;
	}
	
	public void setInverseName(String invNameInput) 
	{
		inverseName = invNameInput;
	}
	
	public void setDescription(String descInput) 
	{
		description = descInput;
	}
	
	/**
	 * @return the identifier of this mapping set
	 */
	public UUID getPrimordialUUID()
	{
		return primordialUUID;
	}
	
	/**
	 * @return Any comments attached to this mapping set.
	 */
	public List<Object> getComments()
	{
		//TODO: vk - implement 
		return new ArrayList<>();
	}
	
	/**
	 * Updates the name, inverse name and description variables
	 */
	private void readFromRefex(RefexDynamicVersionBI<?> refex) 
	{
		try
		{
			ConceptVersionBI mappingConcept = OTFUtility.getConceptVersion(refex.getReferencedComponentNid());
			if(mappingConcept != null) 
			{
				primordialUUID = mappingConcept.getPrimordialUuid();
				active = mappingConcept.isActive();
				if (refex.getData().length > 0  && refex.getData()[0] != null)
				{
					editorStatus = ((RefexDynamicUUID)refex.getData()[0]).getDataUUID();
				}
				if (refex.getData().length > 1  && refex.getData()[1] != null)
				{
					purpose = ((RefexDynamicString)refex.getData()[1]).getDataString();
				}
				
				for (DescriptionVersionBI<?> desc : mappingConcept.getDescriptionsActive())
				{
					if (desc.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid())
					{
						if (OTFUtility.isPreferred(desc.getAnnotations()))
						{
							name = desc.getText();
						}
						else //see if it is the inverse name
						{
							for (RefexDynamicChronicleBI<?> annotation : desc.getRefexDynamicAnnotations())
							{
								if (annotation.getAssemblageNid() == ISAAC.ASSOCIATION_INVERSE_NAME.getNid())
								{
									inverseName= desc.getText();
									break;
								}
							}
						}
					}
					else if (desc.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid())
					{
						if (OTFUtility.isPreferred(desc.getAnnotations()))
						{
							description = desc.getText();
						}
					}
					
					
					if (name != null && description != null && inverseName != null)
					{
						//found everything we are looking for
						break;
					}
				}
			}
			else 
			{
				String error = "cannot read mapping concept!";
				LOG.error(error);
				throw new RuntimeException(error);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error reading descriptions", e);
		}
	}
	
	public void retire() {
		MappingSetDAO.retireMappingSet(this);
	}
	
	public void unRetire() {
		MappingSetDAO.unRetireMappingSet(this);
	}
	
	/*
	 * Static Methods
	 */
	public static List<MappingSet> getMappingSets() throws IOException, ContradictionException {
		return MappingSet.getMappingSets(false);
	}
	
	public static List<MappingSet> getMappingSets(boolean activeOnly) throws IOException, ContradictionException {
		return MappingSetDAO.getMappingSets(activeOnly);
	}
}
