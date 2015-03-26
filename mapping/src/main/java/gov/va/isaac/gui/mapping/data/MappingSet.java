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

import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.SimpleStringProperty;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
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
public class MappingSet extends MappingObject
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingSet.class);

	private String name, inverseName, description, purpose;
	private UUID primordialUUID, editorStatusConcept;
	private boolean active;
	private long creationDate;

	/**
	 * 
	 * Read an existing mapping set from the database
	 * 
	 * @param refex RefexDynamicChronicleBI<?>
	 * @throws IOException
	 */
	protected MappingSet(RefexDynamicVersionBI<?> refex) throws IOException
	{
		this.readFromRefex(refex); //Sets Name, inverseName and Description, etc
	}

	public List<MappingItem> getMappingItems(boolean activeOnly)
	{
		List<MappingItem> mappingItems = null;
		try
		{
			mappingItems = MappingItemDAO.getMappingItems(this.getPrimordialUUID(), activeOnly);
		}
		catch (Exception e)
		{
			LOG.error("Error retrieving Mapping Items for " + this.getName(), e);
			mappingItems = new ArrayList<MappingItem>();
		}
		return mappingItems;
	}

	/**
	 * Is this mapping concept active or retired?
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * Change the editor status concept - may specify null
	 * @param editorStatusConcept
	 */
	public void setEditorStatusConcept(UUID editorStatusConcept)
	{
		this.editorStatusConcept = editorStatusConcept;
	}

	/**
	 * @return The UUID of the concept that represents the editor selected status of the mapping set. May return null.
	 */
	public UUID getEditorStatusConcept()
	{
		return editorStatusConcept;
	}

	public SimpleStringProperty getEditorStatusConceptProperty()
	{
		return propertyLookup(getEditorStatusConcept());
	}
	

	/**
	 * @param purpose - The 'purpose' of the mapping set. May specify null.
	 */
	public void setPurpose(String purpose)
	{
		this.purpose = purpose;
	}

	/**
	 * @return - the 'purpose' of the mapping set - may be null
	 */
	public String getPurpose()
	{
		return purpose;
	}

	/**
	 * @return the name of the mapping set
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return - The inverse name of the mapping set - may return null
	 */
	public String getInverseName()
	{
		return inverseName;
	}

	/**
	 * @return - The user specified description of the mapping set.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param name - Change the name of the mapping set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param inverseName - Change the inverse name of the mapping set
	 */
	public void setInverseName(String inverseName)
	{
		this.inverseName = inverseName;
	}

	/**
	 * @param description - specify the description of the mapping set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return The summary of the mapping set
	 */
	public String getSummary(boolean activeOnly)
	{
		List<MappingItem> mappingItems;
		mappingItems = this.getMappingItems(activeOnly);
		return Integer.toString(mappingItems.size()) + " Mapping Items";
	}

	/**
	 * @return the identifier of this mapping set
	 */
	public UUID getPrimordialUUID()
	{
		return primordialUUID;
	}
	
	public long getCreationDate()
	{
		return creationDate;
	}

	/**
	 * @return Any comments attached to this mapping set.
	 * @throws IOException
	 */
	public List<MappingItemComment> getComments() throws IOException
	{
		return MappingItemCommentDAO.getComments(getPrimordialUUID(), false);
	}

	private void readFromRefex(RefexDynamicVersionBI<?> refex) throws IOException
	{
		try
		{
			//ConceptVersionBI mappingConcept = OTFUtility.getConceptVersion(refex.getReferencedComponentNid());
			ConceptVersionBI mappingConcept = MappingSetDAO.getMappingConcept(refex); 
			if (mappingConcept != null)
			{
				primordialUUID = mappingConcept.getPrimordialUuid();
				active = mappingConcept.isActive();
				creationDate = mappingConcept.getTime();
				if (refex.getData().length > 0 && refex.getData()[0] != null)
				{
					editorStatusConcept = ((RefexDynamicUUID) refex.getData()[0]).getDataUUID();
				}
				if (refex.getData().length > 1 && refex.getData()[1] != null)
				{
					purpose = ((RefexDynamicString) refex.getData()[1]).getDataString();
				}

				for (DescriptionVersionBI<?> desc : mappingConcept.getDescriptionsActive())
				{
					if (desc.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid())
					{
						if (OTFUtility.isPreferred(desc.getAnnotations()))
						{
							name = desc.getText();
						}
						else
						//see if it is the inverse name
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
				throw new IOException(error);
			}
		}
		catch (ContradictionException | ValidationException e)
		{
			LOG.error("Unexpected error", e);
			throw new IOException("internal error");
		}
	}
}
