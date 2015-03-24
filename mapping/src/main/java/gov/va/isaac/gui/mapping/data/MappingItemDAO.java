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
import org.ihtsdo.otf.query.lucene.LuceneDescriptionIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
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
	 * @param qualifier - (optional) the primary ID of the qualifier concept
	 * @param editorStatus - (optional) the primary ID of the status concept
	 * @throws IOException
	 */
	public static MappingItem createMappingItem(UUID sourceConcept, UUID mappingSetID, UUID targetConcept, UUID qualifier, UUID editorStatus) throws IOException
	{
		try
		{
			ConceptVersionBI cv = OTFUtility.getConceptVersion(sourceConcept);
			
			RefexDynamicCAB mappingAnnotation = new RefexDynamicCAB(sourceConcept, mappingSetID);
			mappingAnnotation.setData(new RefexDynamicDataBI[] {
					(targetConcept == null ? null : new RefexDynamicUUID(targetConcept)),
					(qualifier == null ? null : new RefexDynamicUUID(qualifier)),
					(editorStatus == null ? null : new RefexDynamicUUID(editorStatus))}, OTFUtility.getViewCoordinate());
			
			
			UUID mappingItemUUID = UuidT5Generator.get(MappingConstants.MAPPING_NAMESPACE.getPrimodialUuid(), 
					sourceConcept.toString() + "|" 
					+ mappingSetID.toString() + "|"
					+ targetConcept.toString() + "|" 
					+ ((qualifier == null)? "" : qualifier.toString()));
			
			if (ExtendedAppContext.getDataStore().hasUuid(mappingItemUUID))
			{
				throw new IOException("A mapping with the specified source, target and qualifier already exists in this set.  Please edit that mapping.");
			}
			
			mappingAnnotation.setComponentUuidNoRecompute(mappingItemUUID);
			OTFUtility.getBuilder().construct(mappingAnnotation);

			AppContext.getRuntimeGlobals().disableAllCommitListeners();
			ExtendedAppContext.getDataStore().addUncommitted(cv);
			ExtendedAppContext.getDataStore().commit(cv);
			
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
	public static List<MappingItem> getMappingItems(UUID mappingSetID) throws IOException
	{
		ArrayList<MappingItem> result = new ArrayList<>();
		for (SearchResult sr : search(mappingSetID))
		{
			RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
			result.add(new MappingItem(rc));
		}

		return result;
	}

	/**
	 * Just test / demo code
	 * @param mappingSetUUID
	 * @throws IOException
	 */
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
			RefexDynamicCAB mappingItemCab = rdv.makeBlueprint(OTFUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			mappingItemCab.getData()[2] = (mappingItem.getEditorStatusConcept() != null ? new RefexDynamicUUID(mappingItem.getEditorStatusConcept()) : null);
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
