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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.refexDynamic;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexerConfiguration;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefexDynamicUtil}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexDynamicUtil
{
	private static Logger logger_ = LoggerFactory.getLogger(RefexDynamicUtil.class);
	
	@SuppressWarnings("unchecked")
	public static Collection<RefexDynamicChronicleBI<?>> readMembers(int dynamicRefexConceptNid, boolean allowUnindexedScan, ProgressIndicator progress) 
			throws Exception
	{
		Collection<RefexDynamicChronicleBI<?>> refexMembers;
		
		ConceptVersionBI assemblageConceptFull = OTFUtility.getConceptVersion(dynamicRefexConceptNid);
		if (assemblageConceptFull.isAnnotationStyleRefex())
		{
			refexMembers = new ArrayList<>();
			
			if (LuceneDynamicRefexIndexerConfiguration.isAssemblageIndexed(assemblageConceptFull.getNid()))
			{
				logger_.debug("Using index to read annotation style refex members");
				if (progress != null)
				{
					Platform.runLater(() ->
					{
						progress.setProgress(-1);
					});
				}
				
				LuceneDynamicRefexIndexer ldri = AppContext.getService(LuceneDynamicRefexIndexer.class);
				
				List<SearchResult> results = ldri.queryAssemblageUsage(assemblageConceptFull.getNid(), Integer.MAX_VALUE, Long.MIN_VALUE);
				for (SearchResult sr : results)
				{
					refexMembers.add((RefexDynamicChronicleBI<?>)ExtendedAppContext.getDataStore().getComponent(sr.getNid()));
				}
			}
			else
			{
				if (allowUnindexedScan)
				{
					logger_.debug("Using full database scan to read annotation style refex members");
					RefexAnnotationSearcher processor = new RefexAnnotationSearcher((refex) -> 
					{
						if (refex.getAssemblageNid() == assemblageConceptFull.getConceptNid())
						{
							return true;
						}
						return false;
					}, progress);

					ExtendedAppContext.getDataStore().iterateConceptDataInParallel(processor);
					refexMembers.addAll(processor.getResults());
				}
				else
				{
					throw new RuntimeException("No index available and full scan not allowed!");
				}
			}
		}
		else
		{
			logger_.debug("Reading member style refex members");
			refexMembers = (Collection<RefexDynamicChronicleBI<?>>)assemblageConceptFull.getRefsetDynamicMembers();
		}
		return refexMembers;
	}

	public static List<SimpleDisplayConcept> getAllRefexDefinitions() throws IOException {
		List<SimpleDisplayConcept> allRefexDefinitions = new ArrayList<>();

		try {
			LuceneDynamicRefexIndexer indexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
			List<SearchResult> refexes = indexer.queryAssemblageUsage(RefexDynamic.REFEX_DYNAMIC_DEFINITION_DESCRIPTION.getNid(), 1000, Long.MAX_VALUE);
			for (SearchResult sr : refexes) {
				RefexDynamicChronicleBI<?> rc = (RefexDynamicChronicleBI<?>) ExtendedAppContext.getDataStore().getComponent(sr.getNid());
				if (rc == null) {
					logger_.info("Out of date index?  Search result for refexes contained a NID that can't be resolved: {}" + sr.getNid());
					continue;
				}
				//These are nested refex references - it returns a description component - concept we want is the parent of that.
				allRefexDefinitions.add(new SimpleDisplayConcept(
						ExtendedAppContext.getDataStore().getComponent(rc.getReferencedComponentNid()).getEnclosingConcept(), null));
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		Collections.sort(allRefexDefinitions);
		return allRefexDefinitions;
	}

}
