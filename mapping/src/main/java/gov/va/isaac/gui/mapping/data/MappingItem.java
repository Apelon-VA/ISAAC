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
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
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
	
	public MappingItem(UUID sourceConcept, UUID mappingSetID, UUID targetConcept, UUID qualifier, UUID editorStatus) throws IOException
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
			
			ExtendedAppContext.getDataStore().addUncommitted(cv);
			ExtendedAppContext.getDataStore().commit(cv);
			
			refex_ = (RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().getComponent(mappingItemUUID);
		}
		catch (InvalidCAB | ContradictionException | PropertyVetoException | NoSuchAlgorithmException e)
		{
			throw new RuntimeException("Invalid mapping. Check Source, Target, and Qualifier.", e);
		}
	}

	/**
	 * @return the identifier of this mapping set
	 */
	public UUID getID()	{
		return refex_.getPrimordialUuid();
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
		if (data != null && data.length > 1 && data[1] != null)
		{
			return ((RefexDynamicUUID) data[1]).getDataUUID();
		}
		return null;
	}
	
	public UUID getEditorStatus()
	{
		RefexDynamicDataBI[] data = refex_.getData();
		if (data != null && data.length > 2 && data[2] != null)
		{
			return ((RefexDynamicUUID) data[2]).getDataUUID();
		}
		return null;
	}
	
	public String getSummary() {
		//TODO determine summary
		return "<mapping item summary>";
	}
	
	/**
	 * @return Any comments attached to this mapping set.
	 */
	public List<MappingItemComment> getComments()
	{
		//TODO  DAN implement
		return new ArrayList<MappingItemComment>();
	}
	
	
	public MappingItemComment addComment(String commentText) throws IOException {
		MappingItemComment comment;
		
		try {
			//TODO do we want to utilize the other comment field (don't have to)
			comment = new MappingItemComment(this.getID(), commentText, null); 
		} catch (IOException e) {
			throw new RuntimeException("Unexpected error", e);
		}
		 
		
		return comment;
	}
	
	public void update() {
		//TODO  DAN implement
	}
	
	public void retire() {
		//TODO  DAN implement
	}
	
	public void unRetire() {
		//TODO  DAN implement
	}
	
	/*
	 * Static methods 
	 */
	
	/**
	 * 
	 * @param mappingSetID
	 * @return List<MappingItem>
	 * @throws IOException
	 */
	public static List<MappingItem> getMappingItems(UUID mappingSetID) throws IOException {
		List<MappingItem> mappingItems;
		mappingItems = MappingItemDAO.getMappingItems(mappingSetID);
		return mappingItems;
	}

	public static void generateRandomMappingItems(UUID mappingSetUUID) throws IOException {
		try
		{
			LuceneDescriptionIndexer ldi = AppContext.getService(LuceneDescriptionIndexer.class);
			List<SearchResult> result = ldi.query("acetaminophen", ComponentProperty.DESCRIPTION_TEXT, 100);

			for (int i = 0; i < 10; i++)
			{
				UUID source;
				UUID target = null;
				
				int index =  (int)(Math.random() * 100);
				source = ExtendedAppContext.getDataStore().getConceptForNid(result.get(index).getNid()).getPrimordialUuid();
				
				while (target == null || target.equals(source))
				{
					index =  (int)(Math.random() * 100);
					target = ExtendedAppContext.getDataStore().getConceptForNid(result.get(index).getNid()).getPrimordialUuid();
				}
				
				MappingItem mi = new MappingItem(source, mappingSetUUID, target, UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6"), UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"));
				
			}
		}
		catch (Exception e)
		{
			LOG.error("oops", e);
		}
	}

}
