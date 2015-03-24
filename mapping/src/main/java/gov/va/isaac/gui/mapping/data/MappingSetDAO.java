package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;
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
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
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
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MappingSet}
 *
 * A Convenience class to hide unnecessary OTF bits from the Mapping APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MappingSetDAO extends MappingDAO
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingSetDAO.class);
	
	/**
	 * Create and store a new mapping set in the DB.
	 * @param mappingName - The name of the mapping set (used for the FSN and preferred term of the underlying concept)
	 * @param inverseName - (optional) inverse name of the mapping set (if it makes sense for the mapping)
	 * @param purpose - (optional) - user specified purpose of the mapping set
	 * @param description - the intended use of the mapping set
	 * @param editorStatus - (optional) user specified status concept of the mapping set
	 * @return
	 * @throws IOException
	 */
	public static MappingSet createMappingSet(String mappingName, String inverseName, String purpose, String description, UUID editorStatus) 
			throws IOException
	{
		try
		{
			//We need to create a new concept - which itself is defining a dynamic refex - so set that up here.
			RefexDynamicUsageDescription rdud = RefexDynamicUsageDescriptionBuilder
					.createNewRefexDynamicUsageDescriptionConcept(mappingName, mappingName, description, 
					new RefexDynamicColumnInfo[] {
						new RefexDynamicColumnInfo(0, ISAAC.REFEX_COLUMN_TARGET_COMPONENT.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, null, null),
						new RefexDynamicColumnInfo(1, MappingConstants.MAPPING_QUALIFIERS.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, 
								RefexDynamicValidatorType.IS_KIND_OF, new RefexDynamicUUID(MappingConstants.MAPPING_QUALIFIERS.getPrimodialUuid())),
						new RefexDynamicColumnInfo(2, MappingConstants.MAPPING_STATUS.getPrimodialUuid(), RefexDynamicDataType.UUID, null, false, 
								RefexDynamicValidatorType.IS_KIND_OF, new RefexDynamicUUID(MappingConstants.MAPPING_STATUS.getPrimodialUuid()))}, 
					null, true, ComponentType.CONCEPT);
			
			Utility.execute(() ->
			{
				try
				{
					LuceneDynamicRefexIndexerConfiguration.configureColumnsToIndex(rdud.getRefexUsageDescriptorNid(), new Integer[] {0, 1, 2}, true);
				}
				catch (Exception e)
				{
					LOG.error("Unexpected error enabling the index on newly created mapping set!", e);
				}
			});
			
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
					(StringUtils.isBlank(purpose) ? null : new RefexDynamicString(purpose))}, OTFUtility.getViewCoordinate());
			OTFUtility.getBuilder().construct(mappingAnnotation);
			
			RefexDynamicCAB associationAnnotation = new RefexDynamicCAB(rdud.getRefexUsageDescriptorNid(), ISAAC.ASSOCIATION_REFEX.getNid());
			associationAnnotation.setData(new RefexDynamicDataBI[] {}, null);
			OTFUtility.getBuilder().construct(associationAnnotation);
			
			ExtendedAppContext.getDataStore().addUncommitted(createdConcept);
			ExtendedAppContext.getDataStore().commit(createdConcept);
			
			//Find the constructed dynamic refset
			return new MappingSet((RefexDynamicVersionBI<?>)ExtendedAppContext.getDataStore().getComponent(mappingAnnotation.getMemberUUID())
					.getVersion(OTFUtility.getViewCoordinate()));
		
		}
		catch (ContradictionException | InvalidCAB | PropertyVetoException e)
		{
			LOG.error("unexpected", e);
			throw new IOException("Unexpected error creating mapping", e);
		}
	}
	
	/**
	 * Store the changes (done via set methods) on the passed in mapping set.  
	 * @param mappingSet - The mappingSet that carries the changes
	 * @throws IOException
	 */
	public static void updateMappingSet(MappingSet mappingSet) throws IOException {
		try
		{
			ConceptVersionBI mappingConcept = ExtendedAppContext.getDataStore().getConceptVersion(OTFUtility.getViewCoordinate(), mappingSet.getPrimordialUUID());
			
			for (DescriptionVersionBI<?> desc : mappingConcept.getDescriptionsActive())
			{
				if (desc.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid())
				{
					if (OTFUtility.isPreferred(desc.getAnnotations()))
					{
						//Set the name
						DescriptionCAB dCab = desc.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
						dCab.setText(mappingSet.getName());
						OTFUtility.getBuilder().construct(dCab);
					}
					else
					//see if it is the inverse name
					{
						for (RefexDynamicChronicleBI<?> annotation : desc.getRefexDynamicAnnotations())
						{
							if (annotation.getAssemblageNid() == ISAAC.ASSOCIATION_INVERSE_NAME.getNid())
							{
								//set the inverse name
								DescriptionCAB dCab = desc.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
								dCab.setText(mappingSet.getInverseName());
								OTFUtility.getBuilder().construct(dCab);
								break;
							}
						}
					}
				}
				else if (desc.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid())
				{
					if (OTFUtility.isPreferred(desc.getAnnotations()))
					{
						//set the description
						DescriptionCAB dCab = desc.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
						dCab.setText(mappingSet.getDescription());
						OTFUtility.getBuilder().construct(dCab);
					}
				}
			}
			
			RefexDynamicVersionBI<?> mappingRefex = null;
			for (RefexDynamicChronicleBI<?> refex : mappingConcept.getRefexDynamicAnnotations())
			{
				if (refex.getAssemblageNid() == MappingConstants.MAPPING_SEMEME_TYPE.getNid())
				{
					mappingRefex = refex.getVersion(OTFUtility.getViewCoordinate());
					break;
				}
			}
			
			if (mappingRefex == null)
			{
				LOG.error("Couldn't find mapping refex?");
				throw new IOException("internal error");
			}
			
			RefexDynamicCAB mappingRefexCab = mappingRefex.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			mappingRefexCab.setData(new RefexDynamicDataBI[] {
					(mappingSet.getEditorStatus() == null ? null : new RefexDynamicUUID(mappingSet.getEditorStatus())),
					(StringUtils.isBlank(mappingSet.getPurpose()) ? null : new RefexDynamicString(mappingSet.getPurpose()))}, null);
			OTFUtility.getBuilder().construct(mappingRefexCab);

			ExtendedAppContext.getDataStore().addUncommitted(mappingConcept);
			ExtendedAppContext.getDataStore().commit(mappingConcept);
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException e)
		{
			LOG.error("Unexpected!", e);
			throw new IOException("Internal error");
		}
	}
	
	public static List<MappingSet> getMappingSets(boolean activeOnly) throws IOException
	{
		try
		{
			ArrayList<MappingSet> result = new ArrayList<>();
			for (SearchResult sr : search(MappingConstants.MAPPING_SEMEME_TYPE.getPrimodialUuid()))
			{
				RefexDynamicVersionBI<?> rc = (RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().
						getComponentVersion(OTFUtility.getViewCoordinate(), sr.getNid());
				if (rc != null)
				{
					ConceptVersionBI con = ExtendedAppContext.getDataStore().getConceptVersion(OTFUtility.getViewCoordinate(), rc.getReferencedComponentNid());
					if (activeOnly && !con.isActive())
					{
						continue;
					}
					result.add(new MappingSet(rc));
				}
			}
			
			return result;
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected error reading mappings", e);
			throw new IOException("Error reading mappings", e);
		}
	}
	
	public static void retireMappingSet(UUID mappingSetPrimordialUUID) throws IOException
	{
		setConceptStatus(mappingSetPrimordialUUID, Status.INACTIVE);
	}
	
	public static void unRetireMappingSet(UUID mappingSetPrimordialUUID) throws IOException
	{
		setConceptStatus(mappingSetPrimordialUUID, Status.ACTIVE);
	}
}