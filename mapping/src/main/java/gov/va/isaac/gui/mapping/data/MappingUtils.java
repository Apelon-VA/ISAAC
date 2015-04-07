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
import gov.va.isaac.constants.MappingConstants;
import gov.va.isaac.gui.RenameableDisplayConcept;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.refexDynamic.RefexDynamicUtil;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.search.SearchResultsIntersectionFilter;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.TaskCompleteCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.ihtsdo.otf.query.lucene.LuceneDescriptionType;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MappingUtils}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MappingUtils
{
	protected static final Logger LOG = LoggerFactory.getLogger(MappingUtils.class);
	
	public static final UUID SNOMED_UUID = UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
	public static final UUID LOINC_UUID  = UUID.fromString("b2b1cc96-9ca6-5513-aad9-aa21e61ddc29");
	public static final UUID RXNORM_UUID = UUID.fromString("763c21ad-55e3-5bb3-af1e-3e4fb475de44"); 
	
	public static final HashMap<String, ConceptVersionBI> CODE_SYSTEM_CONCEPTS = new HashMap<String, ConceptVersionBI>(); 
	static {
		CODE_SYSTEM_CONCEPTS.put("SNOMED CT", OTFUtility.getConceptVersion(SNOMED_UUID));
		CODE_SYSTEM_CONCEPTS.put("LOINC",     OTFUtility.getConceptVersion(LOINC_UUID));
		CODE_SYSTEM_CONCEPTS.put("RxNorm",    OTFUtility.getConceptVersion(RXNORM_UUID));
	}
	
	public static List<SimpleDisplayConcept> getStatusConcepts() throws IOException
	{
		ArrayList<SimpleDisplayConcept> result = new ArrayList<>();
		try
		{
			for (ConceptVersionBI cv : OTFUtility.getAllChildrenOfConcept(MappingConstants.MAPPING_STATUS.getNid(), true))
			{
				result.add(new SimpleDisplayConcept(cv));
			}
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected", e);
			throw new IOException("Unexpected error");
		}
		
		Collections.sort(result);
		return result;
	}
	
	public static List<SimpleDisplayConcept> getQualifierConcepts() throws IOException
	{
		ArrayList<SimpleDisplayConcept> result = new ArrayList<>();
		try
		{
			for (ConceptVersionBI cv : OTFUtility.getAllChildrenOfConcept(MappingConstants.MAPPING_QUALIFIERS.getNid(), true))
			{
				result.add(new SimpleDisplayConcept(cv));
			}
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected", e);
			throw new IOException("Unexpected error");
		}
		
		Collections.sort(result);
		return result;
	}
	
	/**
	 * Launch a search in a background thread (returns immediately) handing back a handle to the search.
	 * @param searchString - the query string
	 * @param callback - (optional) the class instance that desires a callback when the background threaded search completes
	 * @param descriptionType - (optional) if provided, only searches within the specified description type
	 * @param advancedDescriptionType - (optional) if provided, only searches within the specified advanced description type.  
	 * When this parameter is provided, the descriptionType parameter is ignored.
	 * @param targetCodeSystemPathNid - (optional) Restrict the results to concepts from the specified path. 
	 * @param memberOfRefsetNid - (optional) Restrict the results to concepts that are members of the specified refset.
	 * @param childOfNid - (optional) restrict the results to concepts that are children of the specified concept
	 * @return - A handle to the running search.
	 * @throws IOException
	 */
	public static SearchHandle search(String searchString, TaskCompleteCallback callback, LuceneDescriptionType descriptionType, 
			UUID advancedDescriptionType, Integer targetCodeSystemPathNid, Integer memberOfRefsetNid, Integer childOfNid) throws IOException
	{
		ArrayList<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> filters = new ArrayList<>();
		
		if (targetCodeSystemPathNid != null)
		{
			filters.add(new Function<List<CompositeSearchResult>, List<CompositeSearchResult>>()
			{
				@Override
				public List<CompositeSearchResult> apply(List<CompositeSearchResult> t)
				{
					ArrayList<CompositeSearchResult> keep = new ArrayList<>();
					
					for (CompositeSearchResult csr : t)
					{
						if (csr.getContainingConcept().getPathNid() == targetCodeSystemPathNid)
						{
							keep.add(csr);
						}
					}
					return keep;
				}
			});
		}
		
		if (memberOfRefsetNid != null)
		{
			filters.add(new Function<List<CompositeSearchResult>, List<CompositeSearchResult>>()
			{
				@Override
				public List<CompositeSearchResult> apply(List<CompositeSearchResult> t)
				{
					try
					{
						ArrayList<CompositeSearchResult> keep = new ArrayList<>();
						HashSet<Integer> refsetMembers = new HashSet<>();

						for(RefexDynamicChronicleBI<?> member : RefexDynamicUtil.readMembers(memberOfRefsetNid, true, null))
						{
							refsetMembers.add(member.getReferencedComponentNid());
						}
						
						for (CompositeSearchResult csr : t)
						{
							if (refsetMembers.contains(csr.getContainingConcept().getNid()))
							{
								keep.add(csr);
							}
						}
						return keep;
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}
			});
		}
		
		if (childOfNid != null)
		{
			filters.add(new Function<List<CompositeSearchResult>, List<CompositeSearchResult>>()
			{
				@Override
				public List<CompositeSearchResult> apply(List<CompositeSearchResult> t)
				{
					try
					{
						ArrayList<CompositeSearchResult> keep = new ArrayList<>();
						BdbTerminologyStore ds = ExtendedAppContext.getDataStore();
						ViewCoordinate vc = OTFUtility.getViewCoordinate();
						
						for (CompositeSearchResult csr : t)
						{
							if (ds.isChildOf(csr.getContainingConcept().getNid(), childOfNid, vc))
							{
								keep.add(csr);
							}
						}
						return keep;
					}
					catch (IOException | ContradictionException e)
					{
						throw new RuntimeException(e);
					}
				}
			});
		}
		
		SearchResultsIntersectionFilter filterSet = (filters.size() > 0 ? new SearchResultsIntersectionFilter(filters) : null);
		
		if (descriptionType == null && advancedDescriptionType == null)
		{
			return SearchHandler.descriptionSearch(searchString, 500, false, (UUID)null, callback, null, filterSet, null, true);
		}
		else if (advancedDescriptionType != null)
		{
			return SearchHandler.descriptionSearch(searchString, 500, false, advancedDescriptionType, callback, null, filterSet, null, true);
		}
		else if (descriptionType != null)
		{
			return SearchHandler.descriptionSearch(searchString, 500, false, descriptionType, callback, null, filterSet, null, true);
		}
		else
		{
			throw new RuntimeException("Logic failure!");
		}
	}
	
	/**
	 * Launch a search in a background thread (returns immediately) handing back a handle to the search.
	 * @param sourceConceptNid - the source concept of the map - the descriptions of this concept will be used to create a search
	 * @param callback - (optional) the class instance that desires a callback when the background threaded search completes
	 * @param descriptionType - (optional) if provided, only searches within the specified description type
	 * @param advancedDescriptionType - (optional) if provided, only searches within the specified advanced description type.  
	 * When this parameter is provided, the descriptionType parameter is ignored.
	 * @param targetCodeSystemPathNid - (optional) Restrict the results to concepts from the specified path. 
	 * @param memberOfRefsetNid - (optional) Restrict the results to concepts that are members of the specified refset.
	 * @param childOfNid - (optional) restrict the results to concepts that are children of the specified concept
	 * @return - A handle to the running search.
	 * @throws IOException
	 */
	public static SearchHandle search(int sourceConceptNid, TaskCompleteCallback callback, LuceneDescriptionType descriptionType, 
			UUID advancedDescriptionType, Integer targetCodeSystemPathNid, Integer memberOfRefsetNid, Integer childOfNid) throws IOException
	{
		StringBuilder searchString;
		try
		{
			searchString = new StringBuilder();
			
			ConceptVersionBI cv = OTFUtility.getConceptVersion(sourceConceptNid);
			
			for (DescriptionVersionBI<?> desc : cv.getDescriptionsActive())
			{
				//brackets are somewhat common, and choke the query parser
				searchString.append(desc.getText().replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]"));
				searchString.append(" ");
			}
		}
		catch (ContradictionException e)
		{
			LOG.error("Unexpected error", e);
			throw new IOException(e);
		}
		
		return search(searchString.toString(), callback, descriptionType, advancedDescriptionType, targetCodeSystemPathNid, memberOfRefsetNid, childOfNid);
	}
	
	public static List<SimpleDisplayConcept> getExtendedDescriptionTypes() throws IOException
	{
		Set<ConceptVersionBI> extendedDescriptionTypes;
		ArrayList<SimpleDisplayConcept> temp = new ArrayList<>();
		try
		{
			if (ExtendedAppContext.getDataStore().hasConcept(SnomedMetadataRf2.DESCRIPTION_NAME_IN_SOURCE_TERM_RF2.getPrimodialUuid())) {
				extendedDescriptionTypes = OTFUtility.getAllLeafChildrenOfConcept(SnomedMetadataRf2.DESCRIPTION_NAME_IN_SOURCE_TERM_RF2.getNid());
				for (ConceptVersionBI c : extendedDescriptionTypes)
				{
					temp.add(new SimpleDisplayConcept(c));
				}
			}
			Collections.sort(temp);
			return temp;
		}
		catch (ContradictionException e)
		{
			throw new IOException(e);
		}
	}

	public static List<SimpleDisplayConcept> getCodeSystems() {
		List<SimpleDisplayConcept> codeSystems = new ArrayList<SimpleDisplayConcept>();
		for (String codeSystemName : CODE_SYSTEM_CONCEPTS.keySet()) {
			ConceptVersionBI concept = CODE_SYSTEM_CONCEPTS.get(codeSystemName);
			if (concept != null) {
				RenameableDisplayConcept rdc = new RenameableDisplayConcept(concept);
				rdc.setDescription(codeSystemName);
				codeSystems.add(rdc);
			}
		}
		return codeSystems;
	}
	
}
