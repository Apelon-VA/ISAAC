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
package gov.va.isaac.isaacDbProcessingRules;

import gov.va.isaac.isaacDbProcessingRules.spreadsheet.RuleDefinition;
import gov.va.isaac.isaacDbProcessingRules.spreadsheet.SpreadsheetReader;
import gov.va.isaac.mojos.dbTransforms.TransformConceptIterateI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

/**
 * {@link BaseSpreadsheetCode}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class BaseSpreadsheetCode implements TransformConceptIterateI 
{
	protected HashMap<UUID, Integer> uuidToNidMap_ = new HashMap<>();
	protected HashMap<Integer, AtomicInteger> generatedRels = new HashMap<>();
	protected HashMap<Integer, AtomicInteger> mergedConcepts = new HashMap<>();
	protected AtomicInteger examinedConcepts = new AtomicInteger();
	//Value of the hashmap isn't used
	protected TreeMap<Integer, Set<String>> ruleHits = new TreeMap<>();
	protected ConcurrentHashMap<String, Set<Integer>> conceptHitsByRule = new ConcurrentHashMap<>();
	protected List<RuleDefinition> rules;
	protected long startTime;

	protected TerminologyStoreDI ts_;
	protected ViewCoordinate vc_;
	
	private String name_;
	
	public BaseSpreadsheetCode(String name)
	{
		name_ = name;
	}
	
	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getName()
	 */
	@Override
	public String getName()
	{
		return name_;
	}
	
	protected void configure(String spreadsheetFileName, UUID path, TerminologyStoreDI ts) throws IOException
	{
		startTime = System.currentTimeMillis();
		//TODO pass in the spreadsheet?  But then where to store it?
		ts_ = ts;
		vc_ = StandardViewCoordinates.getSnomedStatedLatest();
		
		// Start with standard view coordinate and override the path setting to use the LOINC path
		Position position = ts.newPosition(ts.getPath(getNid(path)), Long.MAX_VALUE);
		vc_.setViewPosition(position);
		
		rules = new SpreadsheetReader().readSpreadSheet(SpreadsheetReader.class.getResourceAsStream(spreadsheetFileName));
		for (RuleDefinition rd : rules)
		{
			ruleHits.put(rd.getId(), Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
			generatedRels.put(rd.getId(), new AtomicInteger());
			mergedConcepts.put(rd.getId(), new AtomicInteger());
		}
	}
	
	protected int getNid(UUID uuid)
	{
		Integer nid = uuidToNidMap_.get(uuid);
		if (nid == null)
		{
			try
			{
				nid = ts_.getConcept(uuid).getNid();
				uuidToNidMap_.put(uuid, nid);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		if (nid == null || nid == 0)
		{
			throw new RuntimeException("Failed to find nid for uuid " + uuid);
		}
		return nid;
	}
	
	protected void addRel(ConceptChronicleBI source, UUID target, UUID path) throws ValidationException, IOException, InvalidCAB, ContradictionException
	{
		RelationshipCAB rCab = new RelationshipCAB(source.getPrimordialUuid(), Snomed.IS_A.getUuids()[0], target, 0, RelationshipType.STATED_ROLE, 
				IdDirective.GENERATE_HASH);
		
		ts_.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.UNSPECIFIED_MODULE.getLenient().getNid(), 
				getNid(path)), StandardViewCoordinates.getWbAuxiliary()).construct(rCab);
		ts_.addUncommitted(source);
	}
	
	protected void mergeConcepts(ConceptChronicleBI source, UUID mergeOnto, UUID path) throws IOException, InvalidCAB, ContradictionException
	{
		//TODO this doesn't work - 

		//Create a TtkConcept of the thing we want to merge - but change the primary UUID to the thing we want to merge onto.
		//Keep our UUID as a secondary.
		//TRY 1 - this still doesn't seem quite right - the other id ends up as an alternate identifier, instead of a primary... which seems wrong.
		TtkConceptChronicle tcc = new TtkConceptChronicle(source);
		
		UUID temp = tcc.getPrimordialUuid();
		tcc.setPrimordialUuid(mergeOnto);
		if (tcc.getConceptAttributes().getAdditionalIdComponents() == null)
		{
			tcc.getConceptAttributes().setAdditionalIdComponents(new ArrayList<TtkIdentifier>());
		}
		
		TtkIdentifier id = new TtkIdentifierUuid(temp);
		id.setStatus(tcc.getConceptAttributes().getStatus());
		id.setTime(tcc.getConceptAttributes().getTime());
		id.setAuthorUuid(tcc.getConceptAttributes().getAuthorUuid());
		id.setModuleUuid(tcc.getConceptAttributes().getModuleUuid());
		id.setPathUuid(tcc.getConceptAttributes().getPathUuid());
		id.setAuthorityUuid(path);

		tcc.getConceptAttributes().getAdditionalIdComponents().add(id);
		
		//THIS didn't work either
//		ConceptVersionBI sourceVersion = source.getVersion(vc);
//		ConceptAttributeAB cab = new ConceptAttributeAB(sourceVersion.getConceptNid(), true, RefexDirective.EXCLUDE);
//		cab.setStatus(Status.INACTIVE);
//		
//		ts.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.UNSPECIFIED_MODULE.getLenient().getNid(), 
//				loincPathNid), StandardViewCoordinates.getWbAuxiliary()).construct(cab);
//		ts.addUncommitted(sourceVersion);
		
		ConceptChronicle.mergeAndWrite(tcc);
		
		
		
		//TRY 2 - this still lost the other UUID - need to ask Keith about this
//		ConceptChronicle mergeOntoCC = ConceptChronicle.get(ts.getNidForUuids(mergeOnto));
//		
//		ConceptChronicle.mergeWithEConcept(tcc, mergeOntoCC);
//		ts.addUncommittedNoChecks(mergeOntoCC);
	}
	
	protected int sum(Collection<AtomicInteger> values)
	{
		int total = 0;
		for (AtomicInteger ai : values)
		{
			total += ai.get();
		}
		return total;
	}
	
	
	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getWorkResultDocBookTable()
	 */
	@Override
	public String getWorkResultDocBookTable()
	{
		StringBuilder sb = new StringBuilder();
		String eol = System.getProperty("line.separator");

		sb.append("<table frame='all'>" + eol);
		sb.append("\t<title>Results</title>" + eol);
		sb.append("\t<tgroup cols='6' align='center' colsep='1' rowsep='1'>" + eol);
		for (int i = 1; i <= 6; i++)
		{
			sb.append("\t\t<colspec colname='c" + i + "' colwidth='1*' />" + eol);
		}
		sb.append("\t\t<thead>" + eol);
		sb.append("\t\t\t<row>" + eol);
		sb.append("\t\t\t\t<entry>Transform</entry>" + eol);
		sb.append("\t\t\t\t<entry>Examined Concepts</entry>" + eol);
		sb.append("\t\t\t\t<entry>Generated Descriptions</entry>" + eol);
		sb.append("\t\t\t\t<entry>Generated Relationships</entry>" + eol);
		sb.append("\t\t\t\t<entry>Merged Concepts</entry>" + eol);
		sb.append("\t\t\t\t<entry>Execution Time</entry>" + eol);
		sb.append("\t\t\t</row>" + eol);
		sb.append("\t\t</thead>" + eol);
		
		sb.append("\t\t<tbody>" + eol);
		
		int i = 0;
		for (Entry<Integer, Set<String>> x : ruleHits.entrySet())
		{
			i++;
			sb.append("\t\t\t<row>" + eol);
			sb.append("\t\t\t\t<entry>" + x.getKey() + "</entry>" + eol);
			if (i == 1)
			{
				sb.append("\t\t\t\t<entry morerows='" + (ruleHits.size() - 1) + "'>" + examinedConcepts.get() + "</entry>" + eol);
				sb.append("\t\t\t\t<entry morerows='" + (ruleHits.size() - 1) + "'>-</entry>" + eol);  //no descriptions generated here
			}
			sb.append("\t\t\t\t<entry>" + generatedRels.get(x.getKey()).get() + "</entry>" + eol);
			sb.append("\t\t\t\t<entry>" + mergedConcepts.get(x.getKey()).get() + "</entry>" + eol);
			if (i == 1)
			{
				long temp = System.currentTimeMillis() - startTime;
				int seconds = (int)(temp / 1000l);
				sb.append("\t\t\t\t<entry morerows='" + (ruleHits.size() - 1) + "'>" + seconds + " seconds</entry>" + eol);
			}
			
			sb.append("\t\t\t</row>" + eol);
		}
	
		sb.append("\t\t</tbody>" + eol);
		sb.append("\t</tgroup>" + eol);
		sb.append("</table>" + eol);
		
		return sb.toString();
	}
	
	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getWorkResultSummary()
	 */
	@Override
	public String getWorkResultSummary()
	{
		String eol = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		
		int totalRelCount = sum(generatedRels.values());
		int totalMergedCount = sum(mergedConcepts.values());
		
		sb.append("Examined " + examinedConcepts.get() + " concepts and added hierarchy linkages to " + totalRelCount + " concepts.  "
				+ "Merged " + totalMergedCount + " concepts" + eol);
		
		sb.append("Rule,Concept Count,Concept UUID,Concept FSN,Concept UUID,Concept FSN" + eol);
		for (Entry<Integer, Set<String>> x : ruleHits.entrySet())
		{
			sb.append(x.getKey() + "," + x.getValue().size());
			if (x.getValue().size() <= 50)
			{
				for (String s : x.getValue())
				{
					sb.append("," + s);
				}
			}
			sb.append(eol);
		}
		
		sb.append(eol);
		sb.append(eol);
		sb.append("Concepts modified by more than one rule:" + eol);
		sb.append("Concept UUID,Concept FSN,Rule ID,Rule ID" + eol);
		for (Entry<String, Set<Integer>> x : conceptHitsByRule.entrySet())
		{
			if (x.getValue().size() > 1)
			{
				sb.append(x.getKey());
				for (Integer ruleId : x.getValue())
				{
					sb.append("," + ruleId);
				}
				sb.append(eol);
			}
		}
		
		return sb.toString();
	}
	
	protected UUID findSCTTarget(RuleDefinition rd) throws Exception
	{
		UUID sctTargetConcept = null;
		if (rd.getSctID() != null)
		{
			sctTargetConcept = UuidFactory.getUuidFromAlternateId(TermAux.SNOMED_IDENTIFIER.getUuids()[0], rd.getSctID().toString());
		}
		if (sctTargetConcept == null || !ts_.hasConcept(sctTargetConcept))
		{
			//try to find by FSN
			sctTargetConcept = null;
			SearchHandle sh = SearchHandler.descriptionSearch("\"" + rd.getSctFSN() + "\"", 5, null, true);
			for (CompositeSearchResult csr : sh.getResults())
			{
				for (String s : csr.getMatchingStrings())
				{
					if (rd.getSctFSN().equals(s))
					{
						//this is the concept we wanted.
						sctTargetConcept = csr.getContainingConcept().getPrimordialUuid();
						break;
					}
				}
				if (sctTargetConcept != null)
				{
					break;
				}
			}
		}
		
		if (sctTargetConcept == null)
		{
			throw new RuntimeException("Couldn't find target concept");
		}
		return sctTargetConcept;
	}

}
