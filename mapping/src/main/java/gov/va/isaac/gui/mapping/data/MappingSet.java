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
	private String name, inverseName, description;
	
	protected MappingSet(RefexDynamicChronicleBI<?> refex)
	{
		try
		{
			mappingConcept_ = OTFUtility.getConceptVersion(refex.getReferencedComponentNid());
			mappingRefexData_ = refex.getVersion(OTFUtility.getViewCoordinate());
		}
		catch (ContradictionException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected MappingSet (String mappingName, String inverseName, String purpose, String description, UUID editorStatus) throws IOException
	{
		try
		{
			//We need to create a new concept - which itself is defining a dynamic refex - so set that up here.
			RefexDynamicUsageDescription rdud = RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(mappingName, mappingName, description, 
					new RefexDynamicColumnInfo[] {
						new RefexDynamicColumnInfo(0, ISAAC.REFEX_COLUMN_TARGET_CONCEPT.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, null, null),
						new RefexDynamicColumnInfo(1, MappingConstants.MAPPING_QUALIFIERS.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, 
								RefexDynamicValidatorType.IS_KIND_OF, new RefexDynamicUUID(MappingConstants.MAPPING_QUALIFIERS.getPrimodialUuid())),
						new RefexDynamicColumnInfo(2, MappingConstants.MAPPING_STATUS.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, 
								RefexDynamicValidatorType.IS_KIND_OF, new RefexDynamicUUID(MappingConstants.MAPPING_STATUS.getPrimodialUuid()))}, 
					null, true, ComponentType.CONCEPT);
			
			LuceneDynamicRefexIndexerConfiguration.configureColumnsToIndex(rdud.getRefexUsageDescriptorNid(), new Integer[] {0, 1, 2});
			
			//Then, annotate the concept created above as a member of the MappingSet dynamic refex, and add the inverse name, if present.
			ConceptVersionBI createdConcept = OTFUtility.getConceptVersion(rdud.getRefexUsageDescriptorNid());
			if (!StringUtils.isBlank(inverseName))
			{
				DescriptionCAB dCab = new DescriptionCAB(createdConcept.getNid(), Snomed.SYNONYM_DESCRIPTION_TYPE.getNid(), LanguageCode.EN, inverseName,
						false, IdDirective.GENERATE_HASH);
				dCab.addAnnotationBlueprint(new RefexDynamicCAB(dCab.getComponentUuid(), ISAAC.ASSOCIATION_INVERSE_NAME.getPrimodialUuid()));
				OTFUtility.getBuilder().construct(dCab);
			}
			
			RefexDynamicCAB mappingAnnotation = new RefexDynamicCAB(rdud.getRefexUsageDescriptorNid(), MappingConstants.MAPPING_SEMEME_TYPE.getNid());
			mappingAnnotation.setData(new RefexDynamicDataBI[] {
					(editorStatus == null ? null : new RefexDynamicUUID(editorStatus)),
					(StringUtils.isBlank(purpose) ? null : new RefexDynamicString(purpose))}, null);
			OTFUtility.getBuilder().construct(mappingAnnotation);
			
			RefexDynamicCAB associationAnnotation = new RefexDynamicCAB(rdud.getRefexUsageDescriptorNid(), ISAAC.ASSOCIATION_REFEX.getNid());
			associationAnnotation.setData(new RefexDynamicDataBI[] {}, null);
			OTFUtility.getBuilder().construct(associationAnnotation);
			
			ExtendedAppContext.getDataStore().addUncommitted(createdConcept);
			ExtendedAppContext.getDataStore().commit(createdConcept);
			
			//reread
			mappingConcept_ = OTFUtility.getConceptVersion(rdud.getRefexUsageDescriptorNid());
			
			//Find the constructed dynamic refset
			mappingRefexData_ = (RefexDynamicVersionBI<?>)ExtendedAppContext.getDataStore().getComponent(mappingAnnotation.getMemberUUID()).getVersion(OTFUtility.getViewCoordinate());
		}
		catch (ContradictionException | InvalidCAB | PropertyVetoException e)
		{
			throw new RuntimeException("Unexpected error creating mapping", e);
		}
	}
	
	/**
	 * Access to the underlying concept
	 */
	public ConceptVersionBI getMappingConcept()
	{
		return mappingConcept_;
	}
	
	/**
	 * Is this mapping active or retired?
	 */
	public boolean isActive()
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
	
	/**
	 * @return The UUID of the concept that represents the editor selected status of the mapping set.  May return null.
	 */
	public UUID getStatus()
	{
		RefexDynamicDataBI[] data = mappingRefexData_.getData();
		if (data.length > 0)
		{
			return ((RefexDynamicUUID)data[0]).getDataUUID();
		}
		return null;
	}
	
	/**
	 * @return The 'purpose' of the mapping set.  May return null.
	 */
	public String getPurpose()
	{
		RefexDynamicDataBI[] data = mappingRefexData_.getData();
		if (data.length > 1)
		{
			return ((RefexDynamicString)data[1]).getDataString();
		}
		return null;
	}
	
	public String getName()
	{
		readDescriptions();
		return name;
	}
	
	public String getInverseName()
	{
		readDescriptions();
		return inverseName;
	}
	
	public String getDescription()
	{
		readDescriptions();
		return description;
	}
	
	/**
	 * @return the identifier of this mapping set
	 */
	public UUID getID()
	{
		return mappingConcept_.getPrimordialUuid();
	}
	
	private void readDescriptions()
	{
		try
		{
			if (name == null)
			{
				for (DescriptionVersionBI<?> desc : mappingConcept_.getDescriptionsActive())
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
									inverseName = desc.getText();
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
					
					if (name != null && inverseName != null && description != null)
					{
						//Found everything we are looking for.
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error reading descriptions", e);
		}
	}
	
	/**
	 * @return Any comments attached to this mapping set.
	 */
	public List<Object> getComments()
	{
		//TODO implement
		return new ArrayList();
	}
}
