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
import gov.va.isaac.isaacDbProcessingRules.spreadsheet.Operand;
import gov.va.isaac.isaacDbProcessingRules.spreadsheet.RuleDefinition;
import gov.va.isaac.isaacDbProcessingRules.spreadsheet.SelectionCriteria;
import gov.va.isaac.mojos.dbTransforms.TransformConceptIterateI;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
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
public class LOINCSpreadsheetRules  extends BaseSpreadsheetCode implements TransformConceptIterateI
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
	
	
	private LOINCSpreadsheetRules()
	{
		super("LOINC spreadsheet rules");
	}

	/**
	 * @throws IOException 
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#configure(java.io.File, org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI)
	 */
	@Override
	public void configure(File configFile, TerminologyStoreDI ts) throws IOException
	{
		configure("/SOLOR LOINC Rules.xlsx", LOINC_PATH, ts);
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
				case METHOD:
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
		ruleHits.get(rd.getId()).add(cc.getPrimordialUuid() + "," + OTFUtility.getFullySpecifiedName(cc));
		if (rd.getId() != 1000)  //skip rule 1000, it modifies the entire LOINC set
		{
			Set<Integer> rules = conceptHitsByRule.get(cc.getPrimordialUuid());
			if (rules == null)
			{
				rules = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
				Set<Integer> oldRules = conceptHitsByRule.put(cc.getPrimordialUuid().toString() + "," + OTFUtility.getFullySpecifiedName(cc), rules);
				if (oldRules != null)
				{
					//two different threads tried to do this at the same time.  merge
					rules.addAll(oldRules);
				}
			}
			rules.add(rd.getId());
		}
		
		UUID sctTargetConcept = findSCTTarget(rd);

		switch (rd.getAction())
		{
			case CHILD_OF:
				addRel(cc, sctTargetConcept);
				generatedRels.get(rd.getId()).getAndIncrement();
				break;
			case SAME_AS:
				mergeConcepts(cc, sctTargetConcept, LOINC_PATH);
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
	
	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "Implementation of rules processing from a spreadsheet";
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
