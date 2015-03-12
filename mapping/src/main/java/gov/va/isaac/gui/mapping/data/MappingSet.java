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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.util.OTFUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;

/**
 * {@link MappingSet}
 *
 * A Convenience class to hide unnecessary OTF bits from the Mapping APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MappingSet
{
	private ConceptVersionBI mappingConcept_;
	private RefexDynamicVersionBI<?> mappingRefexData_;
	
	//cached values
	private String name, inverseName, description, purpose;
	private UUID editorStatus;
	private boolean active;
//	
//	/**
//	 * 
//	 *  Construct a MappingSset by passing in a RefexDyanmicChronicle
//	 * 
//	 * @param refex RefexDynamicChronicleBI<?>
//	 */
//	protected MappingSet(RefexDynamicChronicleBI<?> refex)
//	{
//		try
//		{
//			mappingConcept_ = OTFUtility.getConceptVersion(refex.getReferencedComponentNid());
//			mappingRefexData_ = refex.getVersion(OTFUtility.getViewCoordinate());
//		}
//		catch (ContradictionException e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//	
	
	protected MappingSet() {
		
	}
	
	/**
	 * 
	 * Alternatively construct a Mapping Set by creating a new concept and passing in the set name, inverse name, purpose, description
	 *  and the UUID of the editor 
	 * 
	 * @param mappingName Mapping Set Name
	 * @param inverseName Inverse Mapping Set Name
	 * @param purpose Mapping Set Purpose
	 * @param description Mapping Set Description
	 * @param editorStatus UUID editor
	 * @throws IOException
	 */
	protected MappingSet (String mappingNameInput, 
							String inverseNameInput,
							String purposeInput, 
							String descriptionInput, 
							UUID editorStatusInput) throws IOException
	{
		this.setName(mappingNameInput);
		this.setInverseName(inverseNameInput);
		this.setPurpose(purposeInput);
		this.setDescription(descriptionInput);
		this.setEditorStatus(editorStatusInput);
	}
	
	/**
	 * Access to the underlying concept
	 */
	public ConceptVersionBI getMappingConcept()
	{
		return mappingConcept_;
	}
	
	public void setActive(boolean activeInput)
	{
		active = activeInput;
	}
	
	public boolean getActive(){
		return active;
	}
	
	/**
	 * Is this mapping active or retired?
	 */
	public boolean isActive2()
	{
		try
		{
			return mappingConcept_.isActive();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void setEditorStatus(UUID statusInput)
	{
		editorStatus = statusInput;
	}
	
	public void setStatus2(UUID statusInput)
	{
		if(statusInput == null)
		{
			RefexDynamicDataBI[] data = mappingRefexData_.getData();
			if (data.length > 0)
			{
				editorStatus = ((RefexDynamicUUID)data[0]).getDataUUID();
			}
		} 
		else
		{
			editorStatus = statusInput;
		}
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
	public String getPurpose2()
	{
		RefexDynamicDataBI[] data = mappingRefexData_.getData();
		if (data.length > 1)
		{
			return data[1] == null ? null : ((RefexDynamicString)data[1]).getDataString();
		}
		return null;
	}
	
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
	public UUID getID()
	{
		return mappingConcept_.getPrimordialUuid();
	}
	
	
	/**
	 * @return Any comments attached to this mapping set.
	 */
	public List<Object> getComments()
	{
		//TODO implement
		return new ArrayList<>();
	}
	
	/**
	 * Updates the name, inverse name and description variables
	 */
	private void updateMappingSetVariables() 
	{
		updateMappingSetVariables("all");
	}
	
	/**
	 * Pass in either eiter name, inverseName or description to update that variable. Or pass in
	 *  all to update all of them
	 * @param updateVariable all, name, inverseName, or description to update desired variable
	 */
	private void updateMappingSetVariables(String updateVariable)
	{
		String nameFound, inverseNameFound, descFound;
		nameFound = inverseNameFound = descFound = null;
		
		try
		{
			for (DescriptionVersionBI<?> desc : mappingConcept_.getDescriptionsActive())
			{
				if (desc.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid())
				{
					if (OTFUtility.isPreferred(desc.getAnnotations()))
					{
						nameFound = desc.getText();
					}
					else //see if it is the inverse name
					{
						for (RefexDynamicChronicleBI<?> annotation : desc.getRefexDynamicAnnotations())
						{
							if (annotation.getAssemblageNid() == ISAAC.ASSOCIATION_INVERSE_NAME.getNid())
							{
								inverseNameFound = desc.getText();
								break;
							}
						}
					}
				}
				else if (desc.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid())
				{
					if (OTFUtility.isPreferred(desc.getAnnotations()))
					{
						descFound = desc.getText();
					}
				}
				
				
				//Did we find what we were looking for?
				if(updateVariable.toLowerCase().equals("all") && nameFound != null && inverseNameFound != null && descFound != null)
				{
					name = nameFound;
					inverseName = inverseNameFound;
					description = descFound;
					break;
				}
				else if(nameFound != null && updateVariable.toLowerCase().equals("name")) 
				{
					name = nameFound;
					break;
				} 
				else if(inverseNameFound != null && updateVariable.toLowerCase().equals("inversename"))
				{
					inverseName = inverseNameFound;
					break;
				} else if(descFound != null && updateVariable.toLowerCase().equals("description")) {
					description = descFound;
					break;
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error reading descriptions", e);
		}
	}
	
	
	
	//TODO implement retire and edit?  methods
}
