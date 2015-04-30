package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.util.OTFUtility;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;

public class MappingItemDAO extends MappingDAO
{
	/**
	 * Construct (and save to the DB) a new MappingItem.  
	 * @param sourceConcept - the primary ID of the source concept
	 * @param mappingSetID - the primary ID of the mapping type
	 * @param targetConcept - the primary ID of the target concept
	 * @param qualifierID - (optional) the primary ID of the qualifier concept
	 * @param editorStatusID - (optional) the primary ID of the status concept
	 * @throws IOException
	 */
	public static MappingItem createMappingItem(ConceptVersionBI sourceConcept, UUID mappingSetID, ConceptVersionBI targetConcept, UUID qualifierID, UUID editorStatusID) throws IOException
	{
		try
		{
			RefexDynamicCAB mappingAnnotation = new RefexDynamicCAB(sourceConcept.getPrimordialUuid(), mappingSetID);
			mappingAnnotation.setData(new RefexDynamicDataBI[] {
					(targetConcept == null ? null : new RefexDynamicUUID(targetConcept.getPrimordialUuid())),
					(qualifierID == null ? null : new RefexDynamicUUID(qualifierID)),
					(editorStatusID == null ? null : new RefexDynamicUUID(editorStatusID))}, OTFUtility.getViewCoordinateAllowInactive());
			
			
			UUID mappingItemUUID = UuidT5Generator.get(MappingConstants.MAPPING_NAMESPACE.getPrimodialUuid(), 
					sourceConcept.getPrimordialUuid().toString() + "|" 
					+ mappingSetID.toString() + "|"
					+ ((targetConcept == null)? "" : targetConcept.getPrimordialUuid().toString()) + "|" 
					+ ((qualifierID == null)?   "" : qualifierID.toString()));
			
			if (ExtendedAppContext.getDataStore().hasUuid(mappingItemUUID))
			{
				throw new IOException("A mapping with the specified source, target and qualifier already exists in this set.  Please edit that mapping.");
			}
			
			mappingAnnotation.setComponentUuidNoRecompute(mappingItemUUID);
			OTFUtility.getBuilder().construct(mappingAnnotation);

			AppContext.getRuntimeGlobals().disableAllCommitListeners();
			ExtendedAppContext.getDataStore().addUncommitted(sourceConcept);
			ExtendedAppContext.getDataStore().commit(sourceConcept);
			
			return new MappingItem((RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().getComponent(mappingItemUUID));
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException | NoSuchAlgorithmException e)
		{
			LOG.error("Unexpected", e);
			throw new IOException("Invalid mapping. Check Source, Target, and Qualifier.", e);
		}
		finally
		{
			AppContext.getRuntimeGlobals().enableAllCommitListeners();
		}
	}
	
	/**
	 * Read all of the mappings items which are defined as part of the specified mapping set.
	 * 
	 * @param mappingSetID - the mapping set that contains the mapping items
	 * @return
	 * @throws IOException
	 */
	public static List<MappingItem> getMappingItems(UUID mappingSetID, boolean activeOnly) throws IOException
	{
		try
		{
			ArrayList<MappingItem> result = new ArrayList<>();
			boolean hadError = false;
			for (SearchResult sr : search(mappingSetID))
			{
				RefexDynamicVersionBI<?> rc = (RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore()
						.getComponentVersion(OTFUtility.getViewCoordinateAllowInactive(), sr.getNid());
				try
				{
					if (rc != null)
					{
						if (rc.isActive() || !activeOnly)
						{
							result.add(new MappingItem(rc));
						}
					}
				}
				catch (Exception e)
				{
					LOG.error("Unexpected error reading mapping " + rc, e);
					hadError = true;
				}
			}
			if (hadError)
			{
				AppContext.getCommonDialogs().showErrorDialog("Internal Error", "Internal Error", "There was an internal error reading all of the mappings.  See logs.");
			}
			
			return result;
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected error reading comments", e);
			throw new IOException("internal error reading comments");
		}
	}

	/**
	 * Just test / demo code
	 * @param mappingSetUUID
	 * @throws IOException
	 */
	/*
	public static void generateRandomMappingItems(UUID mappingSetUUID)
	{
		try
		{
			LuceneDescriptionIndexer ldi = AppContext.getService(LuceneDescriptionIndexer.class);
			List<SearchResult> result = ldi.query("acetaminophen", ComponentProperty.DESCRIPTION_TEXT, 100);

			for (int i = 0; i < 10; i++)
			{
				UUID source;
				UUID target = null;

				int index = (int) (Math.random() * 100);
				source = ExtendedAppContext.getDataStore().getConceptForNid(result.get(index).getNid()).getPrimordialUuid();

				while (target == null || target.equals(source))
				{
					index = (int) (Math.random() * 100);
					target = ExtendedAppContext.getDataStore().getConceptForNid(result.get(index).getNid()).getPrimordialUuid();
				}

				createMappingItem(source, mappingSetUUID, target, UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6"),
						UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"));
			}
		}
		catch (Exception e)
		{
			LOG.error("oops", e);
		}
	}
	*/
	
	/**
	 * Store the values passed in as a new revision of a mappingItem (the old revision remains in the DB)
	 * @param mappingItem - The MappingItem with revisions (contains fields where the setters have been called)
	 * @throws IOException
	 */
	public static void updateMappingItem(MappingItem mappingItem) throws IOException
	{
		try
		{
			RefexDynamicVersionBI<?> rdv = readCurrentRefex(mappingItem.getPrimordialUUID());
			RefexDynamicCAB mappingItemCab = rdv.makeBlueprint(OTFUtility.getViewCoordinateAllowInactive(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			mappingItemCab.getData()[2] = (mappingItem.getEditorStatusConcept() != null ? new RefexDynamicUUID(mappingItem.getEditorStatusConcept()) : null);
			mappingItemCab.validate(OTFUtility.getViewCoordinateAllowInactive());
			RefexDynamicChronicleBI<?> rdc = OTFUtility.getBuilder().construct(mappingItemCab);

			ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(rdc.getConceptNid());
			AppContext.getRuntimeGlobals().disableAllCommitListeners();
			ExtendedAppContext.getDataStore().addUncommitted(cc);
			ExtendedAppContext.getDataStore().commit(cc);
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException e)
		{
			LOG.error("Unexpected!", e);
			throw new IOException("Internal error");
		}
		finally
		{
			AppContext.getRuntimeGlobals().enableAllCommitListeners();
		}
	}

	/**
	 * @param mappingItemPrimordial - The identifier of the mapping item to be retired
	 * @throws IOException
	 */
	public static void retireMappingItem(UUID mappingItemPrimordial) throws IOException
	{
		setRefexStatus(mappingItemPrimordial, Status.INACTIVE);
	}

	/**
	 * @param mappingItemPrimordial - The identifier of the mapping item to be re-activated
	 * @throws IOException
	 */
	public static void unRetireMappingItem(UUID mappingItemPrimordial) throws IOException
	{
		setRefexStatus(mappingItemPrimordial, Status.ACTIVE);
	}
}
