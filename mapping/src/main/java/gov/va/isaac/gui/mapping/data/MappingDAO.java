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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MappingDAO}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class MappingDAO
{
	protected static final Logger LOG = LoggerFactory.getLogger(MappingDAO.class);
	
	protected static RefexDynamicVersionBI<?> readCurrentRefex(UUID refexUUID) throws IOException, ContradictionException
	{
		return (RefexDynamicVersionBI<?>) ExtendedAppContext.getDataStore().getComponentVersion(OTFUtility.getViewCoordinateAllowInactive(), refexUUID);
	}
	
	protected static void setConceptStatus(UUID conceptUUID, Status status) throws IOException
	{
		try
		{
			ConceptVersionBI concept = ExtendedAppContext.getDataStore().getConceptVersion(OTFUtility.getViewCoordinateAllowInactive(), conceptUUID);
			ConceptAttributeVersionBI<?> conAttrib = concept.getConceptAttributes().getVersion(OTFUtility.getViewCoordinateAllowInactive());
			if (conAttrib.getStatus() == status)
			{
				LOG.warn("Tried set the status to the value it already has.  Doing nothing");
			}
			else
			{
				
				ConceptAttributeAB conceptAttribCab = conAttrib.makeBlueprint(OTFUtility.getViewCoordinateAllowInactive(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
				conceptAttribCab.setStatus(status);
				OTFUtility.getBuilder().construct(conceptAttribCab);

				AppContext.getRuntimeGlobals().disableAllCommitListeners();
				ExtendedAppContext.getDataStore().addUncommitted(concept);
				ExtendedAppContext.getDataStore().commit(concept);
			}
		}
		catch (InvalidCAB | ContradictionException e)
		{
			LOG.error("Unexpected!", e);
			throw new IOException("Internal error");
		}
		finally
		{
			AppContext.getRuntimeGlobals().enableAllCommitListeners();
		}
	}
	
	protected static void setRefexStatus(UUID refexUUID, Status status) throws IOException
	{
		try
		{
			RefexDynamicVersionBI<?> rdv = readCurrentRefex(refexUUID);
			if (rdv.getStatus() == status)
			{
				LOG.warn("Tried set the status to the value it already has.  Doing nothing");
			}
			else
			{
				RefexDynamicCAB mappingCab = rdv.makeBlueprint(OTFUtility.getViewCoordinateAllowInactive(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
				mappingCab.setStatus(status);
				RefexDynamicChronicleBI<?> rdc = OTFUtility.getBuilder().construct(mappingCab);

				ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(rdc.getConceptNid());
				ExtendedAppContext.getDataStore().addUncommitted(cc);
				ExtendedAppContext.getDataStore().commit(cc);
			}
		}
		catch (InvalidCAB | ContradictionException e)
		{
			LOG.error("Unexpected!", e);
			throw new IOException("Internal error");
		}
	}
	
	protected static List<SearchResult> search(UUID assemblageUUID) throws IOException
	{
		try
		{
			LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
			if (indexer == null)
			{
				LOG.error("Required index on dynamic refexes is missing!");
				throw new IOException("Required index is not available");
			}
			return indexer.queryAssemblageUsage(ExtendedAppContext.getDataStore().getNidForUuids(assemblageUUID), Integer.MAX_VALUE, Long.MAX_VALUE);
		}
		catch (NumberFormatException | ParseException e)
		{
			LOG.error("Unexpected", e);
			throw new IOException("Unexpected error searching for refexes");
		}
	}
}
