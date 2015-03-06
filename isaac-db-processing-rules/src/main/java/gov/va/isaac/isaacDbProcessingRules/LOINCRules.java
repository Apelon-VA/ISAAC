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

import gov.va.isaac.mojos.dbTransforms.TransformConceptIterateI;
import gov.va.isaac.util.OTFUtility;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link LOINCRules}
 * 
 * A Transformer that implements various rules for LOINC transformations.
 * 
 * See docs/initial LOINC Rules.xlsx for the details on these that have been implemented so far.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Named(value = "LOINC representation rules")
public class LOINCRules implements TransformConceptIterateI
{
	private AtomicInteger generatedRels = new AtomicInteger();
	private AtomicInteger mergedConcepts = new AtomicInteger();
	private AtomicInteger examinedConcepts = new AtomicInteger();
	
	private HashMap<Integer, AtomicInteger> ruleHits = new HashMap<>();
	
	//private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final UUID LOINC_PATH = UUID.fromString("b2b1cc96-9ca6-5513-aad9-aa21e61ddc29");
	private int loincPathNid;
	private ViewCoordinate vc;
	
	
	//SNOMED integer id	108252007	Laboratory procedure
	private final UUID sctLabProcedure = UUID.fromString("42bd9b4b-f6da-39d9-b6a7-bb0d4690d6f9");
	
	//SNOMED integer id	489004	Ferritin measurement
	private final UUID sctFerritinMeasurement = UUID.fromString("0fcef5eb-8229-3edf-87ef-d71927ae4d20");
	
	//SNOMED integer id	121278003	Drug measurement
	private final UUID sctDrugMeasurement = UUID.fromString("4ddf00b1-dc17-38b1-a445-2eecbc53913e");
	
	//SNOMED integer id	388469008	Ampicillin RAST
	private final UUID sctAmpicillin = UUID.fromString("7f798b0f-ceab-3c3f-b55a-17538aaa82ef");
	
	//Ampicillin IgE RAST Ql - 15533-3
	private final UUID loincAmpicillin = UUID.fromString("e284b1f7-61bf-5ca0-b2a2-f0fd838e064b");
	
	//CLASSTYPE
	private final UUID classtype = UUID.fromString("537869e6-a36e-5bd5-8e5b-dad90e9f4015");
	
	//ORDER_OBS
	private final UUID orderObs = UUID.fromString("a77932ee-55ea-56c6-9c7d-e93ccf6620a7");
	
	//has_COMPONENT
	private final  UUID hasComponent = UUID.fromString("481bb791-103a-5216-946c-b630aa95d322");
			
	
	int loincFerritinMeasurementNid, loincDrugAllergensNid, classTypeNid, orderObsNid, hasComponentNid;

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getName()
	 */
	@Override
	public String getName()
	{
		return "LOINC representation rules";
	}
	
	/**
	 * @throws IOException 
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#configure(java.io.File, org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI)
	 */
	@Override
	public void configure(File configFile, TerminologyStoreDI ts) throws IOException
	{
		loincPathNid = ts.getConcept(LOINC_PATH).getNid();
		classTypeNid = ts.getConcept(classtype).getNid();
		orderObsNid = ts.getConcept(orderObs).getNid();
		hasComponentNid = ts.getConcept(hasComponent).getNid();
		
		//Some LOINC constants
		//Ferritin | Bld-Ser-Plas
		loincFerritinMeasurementNid = ts.getNidForUuids(UUID.fromString("e1064652-d641-5736-9281-f68a9fef42c0"));
		
		//Drug allergens
		loincDrugAllergensNid = ts.getNidForUuids(UUID.fromString("a20d28ab-15c3-5a0e-ae53-147c0e33ea29"));
		
		vc = StandardViewCoordinates.getSnomedStatedLatest();
		
		// Start with standard view coordinate and override the path setting to use the LOINC path
		Position position = ts.newPosition(ts.getPath(loincPathNid), Long.MAX_VALUE);
		vc.setViewPosition(position);
		
		for (int i = 1000; i < 1004; i++)
		{
			ruleHits.put(i, new AtomicInteger());
		}
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
		if (latest.getPathNid() == loincPathNid)
		{
			//Rule for all other rules:
			if (classTypeIs("1", cc) && (orderIs("Order", cc) || orderIs("Both", cc)))
			{
				//Rule 1000 - everything that matches this is a lab procedure
				addRel(cc, sctLabProcedure, ts);
				ruleHits.get(1000).getAndIncrement();
				
				//Rule 1001
				if (ts.isKindOf(cc.getConceptNid(), loincFerritinMeasurementNid, vc))
				{
					addRel(cc, sctFerritinMeasurement, ts);
					ruleHits.get(1001).getAndIncrement();
				}
				
				//Rule 1002
				if (ts.isKindOf(cc.getConceptNid(), loincDrugAllergensNid, vc) && (!componentContains("Ab.", cc, ts) && !componentContains("lgE", cc, ts)))
				{
					addRel(cc, sctDrugMeasurement, ts);
					ruleHits.get(1002).getAndIncrement();
				}
				
				//Rule 1003
				if (cc.getPrimordialUuid().equals(loincAmpicillin))
				{
					mergeConcepts(cc, sctAmpicillin, ts);
					ruleHits.get(1003).getAndIncrement();
				}
				return true;
			}
		}
		
		return false;
	}
	
	private boolean classTypeIs(String classTypeValue, ConceptChronicleBI cc) throws IOException
	{
		for (RefexVersionBI<?> rv : cc.getAnnotationsActive(vc))
		{
			if (rv.getAssemblageNid() == classTypeNid)
			{
				if (((RefexStringVersionBI<?>)rv).getString1().equals(classTypeValue))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean orderIs(String orderValue, ConceptChronicleBI cc) throws IOException
	{
		for (RefexVersionBI<?> rv : cc.getAnnotationsActive(vc))
		{
			if (rv.getAssemblageNid() == orderObsNid)
			{
				if (((RefexStringVersionBI<?>)rv).getString1().equals(orderValue))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean componentContains(String matchString, ConceptChronicleBI cc, TerminologyStoreDI ts) throws IOException, ContradictionException
	{
		
		for (RelationshipVersionBI<?> rel :  cc.getVersion(vc).getRelationshipsOutgoingActive())
		{
			if (rel.getTypeNid() == hasComponentNid)
			{
				ConceptVersionBI target = ts.getConceptVersion(vc, rel.getDestinationNid());
				for (DescriptionVersionBI<?> d : target.getDescriptionsActive())
				{
					if (d.getText().contains(matchString))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void addRel(ConceptChronicleBI source, UUID target, TerminologyStoreDI ts) throws ValidationException, IOException, InvalidCAB, ContradictionException
	{
		RelationshipCAB rCab = new RelationshipCAB(source.getPrimordialUuid(), Snomed.IS_A.getUuids()[0], target, 0, RelationshipType.STATED_ROLE, 
				IdDirective.GENERATE_HASH);
		
		ts.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.UNSPECIFIED_MODULE.getLenient().getNid(), 
				loincPathNid), StandardViewCoordinates.getWbAuxiliary()).construct(rCab);
		ts.addUncommitted(source);
		
		generatedRels.getAndIncrement();
	}
	
	private void mergeConcepts(ConceptChronicleBI source, UUID target, TerminologyStoreDI ts) throws IOException, InvalidCAB, ContradictionException
	{
		ConceptAttributeVersionBI<?> sourceAttrib = source.getVersion(vc).getConceptAttributesActive();
		ConceptAttributeAB cab = new ConceptAttributeAB(source.getConceptNid(), sourceAttrib.isDefined(), RefexDirective.EXCLUDE);
		cab.setComponentUuidNoRecompute(target);
		
		ts.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.UNSPECIFIED_MODULE.getLenient().getNid(), 
				loincPathNid), StandardViewCoordinates.getWbAuxiliary()).construct(cab);
		ts.addUncommitted(source);
		
		mergedConcepts.incrementAndGet();
	}

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "Implementation of rules 1000 - 1003 from the LOINC rules spreadsheet";
	}

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getWorkResultSummary()
	 */
	@Override
	public String getWorkResultSummary()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Examined " + examinedConcepts.get() + " concepts and added hierarchy linkages to " + generatedRels.get() + " concepts.  "
				+ "Merged " + mergedConcepts.get() + " concepts");
		
		for (Entry<Integer, AtomicInteger> x : ruleHits.entrySet())
		{
			sb.append("  Rule " + x.getKey() + " hit on " + x.getValue() + " concepts");
		}
		
		return sb.toString();
	}
}
