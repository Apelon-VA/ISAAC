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
package gov.va.isaac.isaacDbProcessingRules;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.init.SystemInit;
import gov.va.isaac.isaacDbProcessingRules.loinc.Operand;
import gov.va.isaac.isaacDbProcessingRules.loinc.RuleDefinition;
import gov.va.isaac.isaacDbProcessingRules.loinc.SelectionCriteria;
import gov.va.isaac.isaacDbProcessingRules.loinc.SpreadsheetReader;
import gov.va.isaac.mojos.dbTransforms.TransformConceptIterateI;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;
import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.lucene.LuceneDynamicRefexIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link LOINCSpreadsheetRules}
 * 
 * A Transformer that implements various rules for LOINC transformations.
 * 
 * See docs/initial LOINC Rules.xlsx for the details on these that have been implemented so far.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Named(value = "LOINC spreadsheet rules")
public class LOINCSpreadsheetRules implements TransformConceptIterateI
{
	private final UUID LOINC_PATH = UUID.fromString("b2b1cc96-9ca6-5513-aad9-aa21e61ddc29");
	
	private final UUID LOINC_NUM = UUID.fromString("ee19b536-ca30-52ce-91a0-f1089e710f9c");

	//CLASSTYPE
	private final UUID CLASSTYPE = UUID.fromString("537869e6-a36e-5bd5-8e5b-dad90e9f4015");
	
	//ORDER_OBS
	private final UUID ORDER_OBS = UUID.fromString("a77932ee-55ea-56c6-9c7d-e93ccf6620a7");
	
	//has_COMPONENT
	private final UUID HAS_COMPONENT = UUID.fromString("481bb791-103a-5216-946c-b630aa95d322");
	
	//has_SYSTEM
	private final UUID HAS_SYSTEM = UUID.fromString("c901d71a-381a-560e-967c-c2b2dfebdabc");
	
	//has_METHOD_TYP
	private final UUID HAS_METHOD_TYPE = UUID.fromString("a0e9ca70-0c0e-5cc8-ad2f-442fff44be6f");
	
	private HashMap<UUID, Integer> uuidToNidMap_ = new HashMap<>();
	private HashMap<Integer, AtomicInteger> generatedRels = new HashMap<>();
	private HashMap<Integer, AtomicInteger> mergedConcepts = new HashMap<>();
	private AtomicInteger examinedConcepts = new AtomicInteger();
	private TreeMap<Integer, AtomicInteger> ruleHits = new TreeMap<>();
	private List<RuleDefinition> rules;
	private long startTime;

	private TerminologyStoreDI ts_;
	private ViewCoordinate vc_;

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getName()
	 */
	@Override
	public String getName()
	{
		return "LOINC spreadsheet rules";
	}
	
	/**
	 * @throws IOException 
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#configure(java.io.File, org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI)
	 */
	@Override
	public void configure(File configFile, TerminologyStoreDI ts) throws IOException
	{
		startTime = System.currentTimeMillis();
		//TODO pass in the spreadsheet?  But then where to store it?
		ts_ = ts;
		vc_ = StandardViewCoordinates.getSnomedStatedLatest();
		
		// Start with standard view coordinate and override the path setting to use the LOINC path
		Position position = ts.newPosition(ts.getPath(getNid(LOINC_PATH)), Long.MAX_VALUE);
		vc_.setViewPosition(position);
		
		rules = new SpreadsheetReader().readSpreadSheet(SpreadsheetReader.class.getResourceAsStream("/rules.xlsx"));
		for (RuleDefinition rd : rules)
		{
			ruleHits.put(rd.getId(), new AtomicInteger());
			generatedRels.put(rd.getId(), new AtomicInteger());
			mergedConcepts.put(rd.getId(), new AtomicInteger());
		}
	}
	
	private int getNid(UUID uuid)
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

	/**
	 * 
	 * @see gov.va.isaac.mojos.dbTransforms.TransformConceptIterateI#transform(org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI, 
	 *  org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI)
	 */
	@Override
	public boolean transform(TerminologyStoreDI ts, ConceptChronicleBI cc) throws Exception
	{
		examinedConcepts.incrementAndGet();
		ConceptAttributeVersionBI<?> latest = OTFUtility.getLatestAttributes(cc.getConceptAttributes().getVersions());
		if (latest.getPathNid() == getNid(LOINC_PATH))
		{
			//Rule for all other rules:
			if (classTypeIs("1", cc) && (orderIs("Order", cc) || orderIs("Both", cc)))
			{
				boolean commitRequired = false;
				
				for (RuleDefinition rd : rules)
				{
					try
					{
						boolean ruleNeedsCommit = processRule(rd, cc);
						if (ruleNeedsCommit)
						{
							commitRequired = true;
						}
					}
					catch (Exception e)
					{
						throw new RuntimeException("Failure processing rule " + rd.getId(), e);
					}
				}
				return commitRequired;
			}
		}
		
		return false;
	}
	
	private boolean processRule(RuleDefinition rd, ConceptChronicleBI cc) throws Exception
	{
		for (SelectionCriteria sc : rd.getCriteria())
		{
			boolean invert = false;
			if (sc.getOperand() != null && sc.getOperand() == Operand.NOT)
			{
				invert = true;
			}

			boolean passed;
			switch (sc.getType())
			{
				case COMPONENT:
					passed = componentIs(sc.getValue(), cc);
					break;
				case CONCEPT:
					passed = cc.getConceptNid() == getLoincConceptNid(sc);
					break;
				case DESCENDENT_OF:
					passed = ts_.isKindOf(cc.getConceptNid(), getLoincConceptNid(sc), vc_);
					break;
				case METHOD_TYPE:
					passed = methodTypeIs(sc.getValue(), cc);
					break;
				case SYSTEM:
					passed = systemIs(sc.getValue(), cc);
					break;
				default :
					throw new RuntimeException("Unhandeled type");
			}
			if (invert)
			{
				passed = (passed ? false : true);
			}
			if (!passed)
			{
				return passed;
			}
		}
		
		//passed all criteria
		ruleHits.get(rd.getId()).incrementAndGet();
		
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
		
		switch (rd.getAction())
		{
			case CHILD_OF:
				addRel(cc, sctTargetConcept);
				generatedRels.get(rd.getId()).getAndIncrement();
				break;
			case SAME_AS:
				mergeConcepts(cc, sctTargetConcept);
				mergedConcepts.get(rd.getId()).incrementAndGet();
				break;
			default :
				throw new RuntimeException("Unhandled Action");
			
		}
		return true;
	}
	
	private int getLoincConceptNid(SelectionCriteria sc) throws IOException, ParseException, PropertyVetoException
	{
		if (sc.getValueId() != null && sc.getValueId().length() > 0)
		{
			ConceptChronicleBI cc;
			if (Utility.isUUID(sc.getValueId()))
			{
				cc = ts_.getConcept(UUID.fromString(sc.getValueId()));
			}
			else
			{
				LuceneDynamicRefexIndexer refexIndexer = AppContext.getService(LuceneDynamicRefexIndexer.class);
				
				if (refexIndexer == null)
				{
					throw new RuntimeException("No sememe indexer found, aborting.");
				}
				List<SearchResult> searchResults = refexIndexer.query(new RefexDynamicString("\"" + sc.getValueId() + "\""), getNid(LOINC_NUM), false, 
						new Integer[] {0}, 5, null);
				if (searchResults.size() != 1)
				{
					throw new RuntimeException("Unexpected - multiple hits on ID " + sc.getValueId());
				}
				else
				{
					cc = ts_.getComponent(searchResults.get(0).getNid()).getEnclosingConcept();
				}
			}
			for (DescriptionChronicleBI dc : cc.getDescriptions())
			{
				for (DescriptionVersionBI<?> dv : dc.getVersions())
				{
					if (dv.getText().equals(sc.getValue()))
					{
						return cc.getNid();
					}
				}
			}
			//TODO maybe fail?
			System.err.println("ERROR -------------------");
			System.err.println("ERROR - The concept '" + cc + "' did not have a description that matched the expected value of '" + sc.getValue() + "' from the spreadsheet");
			System.err.println("ERROR -------------------");
			return cc.getNid();
		}
		else
		{
			throw new RuntimeException("Not yet handeled");
		}
	}
	
	private boolean classTypeIs(String classTypeValue, ConceptChronicleBI cc) throws IOException
	{
		return attributeIs(CLASSTYPE, classTypeValue, cc);
	}
	
	private boolean orderIs(String orderValue, ConceptChronicleBI cc) throws IOException
	{
		return attributeIs(ORDER_OBS, orderValue, cc);
	}
	
	private boolean attributeIs(UUID attributeType, String orderValue, ConceptChronicleBI cc) throws IOException
	{
		for (RefexDynamicVersionBI<?> rv : cc.getRefexesDynamicActive(vc_))
		{
			if (rv.getAssemblageNid() == getNid(attributeType))
			{
				if (rv.getData()[0].getDataObject().toString().equals(orderValue))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean systemIs(String system, ConceptChronicleBI cc) throws IOException, ContradictionException
	{
		return associationTargetValueIs(HAS_SYSTEM, system, cc);
	}
	
	private boolean componentIs(String component, ConceptChronicleBI cc) throws IOException, ContradictionException
	{
		return associationTargetValueIs(HAS_COMPONENT, component, cc);
	}
	
	private boolean methodTypeIs(String component, ConceptChronicleBI cc) throws IOException, ContradictionException
	{
		return associationTargetValueIs(HAS_METHOD_TYPE, component, cc);
	}
	
	private boolean associationTargetValueIs(UUID type, String matchText, ConceptChronicleBI cc) throws IOException, ContradictionException
	{
		for (RelationshipVersionBI<?> rel :  cc.getVersion(vc_).getRelationshipsOutgoingActive())
		{
			if (rel.getTypeNid() == getNid(type))
			{
				ConceptVersionBI target = ts_.getConceptVersion(vc_, rel.getDestinationNid());
				for (DescriptionVersionBI<?> d : target.getDescriptionsActive())
				{
					if ((matchText.startsWith("*") && d.getText().endsWith(matchText)) || 
							(matchText.endsWith("*") && d.getText().startsWith(matchText)) ||
							d.getText().equals(matchText))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void addRel(ConceptChronicleBI source, UUID target) throws ValidationException, IOException, InvalidCAB, ContradictionException
	{
		RelationshipCAB rCab = new RelationshipCAB(source.getPrimordialUuid(), Snomed.IS_A.getUuids()[0], target, 0, RelationshipType.STATED_ROLE, 
				IdDirective.GENERATE_HASH);
		
		ts_.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.UNSPECIFIED_MODULE.getLenient().getNid(), 
				getNid(LOINC_PATH)), StandardViewCoordinates.getWbAuxiliary()).construct(rCab);
		ts_.addUncommitted(source);
	}
	
	private void mergeConcepts(ConceptChronicleBI source, UUID mergeOnto) throws IOException, InvalidCAB, ContradictionException
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
		id.setAuthorityUuid(LOINC_PATH);

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

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "Implementation of rules processing from a spreadsheet";
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
		int totalMergedCount = sum(generatedRels.values());
		
		sb.append("Examined " + examinedConcepts.get() + " concepts and added hierarchy linkages to " + totalRelCount + " concepts.  "
				+ "Merged " + totalMergedCount + " concepts" + eol);
		
		for (Entry<Integer, AtomicInteger> x : ruleHits.entrySet())
		{
			sb.append("  Rule " + x.getKey() + " hit on " + x.getValue() + " concepts" + eol);
		}
		
		return sb.toString();
	}
	
	private int sum(Collection<AtomicInteger> values)
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
		for (Entry<Integer, AtomicInteger> x : ruleHits.entrySet())
		{
			i++;
			sb.append("\t\t\t<row>" + eol);
			sb.append("\t\t\t\t<entry>LOINC " + x.getKey() + "</entry>" + eol);
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
	
	public static void main(String[] args) throws Exception
	{
		IOException dataStoreLocationInitException = SystemInit.doBasicSystemInit(new File("../../../ISAAC-DB/isaac-db-solor/target/"));
		if (dataStoreLocationInitException != null)
		{
			System.err.println("Configuration of datastore path failed.  DB will not be able to start properly!  " + dataStoreLocationInitException);
			System.exit(-1);
		}
		AppContext.getService(UserProfileManager.class).configureAutomationMode(new File("profiles"));
		
		LOINCSpreadsheetRules lsr = new LOINCSpreadsheetRules();
		lsr.configure(null, ExtendedAppContext.getDataStore());
		lsr.transform(ExtendedAppContext.getDataStore(), ExtendedAppContext.getDataStore().getConcept(UUID.fromString("b8a86aff-a33d-5ab9-88fe-bb3cfd8dce39")));
		System.out.println(lsr.getWorkResultSummary());
		System.out.println(lsr.getWorkResultDocBookTable());
	}
}
