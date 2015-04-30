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
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Named;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RxNormSpreadsheetRules}
 * 
 * A Transformer that implements various rules for LOINC transformations.
 * 
 * See docs/initial LOINC Rules.xlsx for the details on these that have been implemented so far.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Named(value = "RxNorm spreadsheet rules")
public class RxNormSpreadsheetRules extends BaseSpreadsheetCode implements TransformConceptIterateI 
{
	private final UUID RXNORM_PATH = UUID.fromString("763c21ad-55e3-5bb3-af1e-3e4fb475de44");

	private final UUID RXCUI = UUID.fromString("da3a2dc0-8d17-57a2-b894-ba3086904aa3");
	private final UUID RxNormDescType = UUID.fromString("3599879d-78c6-5b1e-b442-9ef08eaedd3c");
	private final UUID IN = UUID.fromString("17114d54-ed48-5f0a-a865-4ecec3e31cdc");

	private RxNormSpreadsheetRules()
	{
		super ("RxNorm spreadhsheet rules");
	}
	
	/**
	 * @throws IOException 
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#configure(java.io.File, org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI)
	 */
	@Override
	public void configure(File configFile, TerminologyStoreDI ts) throws IOException
	{
		configure("/SOLOR RxNorm Rules.xlsx", RXNORM_PATH, ts);
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
		if (latest.getPathNid() == getNid(RXNORM_PATH))
		{
			//Rule for all other rules:
			if (ttyIs(IN, cc))
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
				case RXCUI:
					passed = rxCuiIs(sc.getValue(), cc);
					break;
				default :
					throw new RuntimeException("Unhandled type");
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
		
		UUID sctTargetConcept = findSCTTarget(rd);
		
		switch (rd.getAction())
		{
			case CHILD_OF:
				addRel(cc, sctTargetConcept, RXNORM_PATH);
				generatedRels.get(rd.getId()).getAndIncrement();
				break;
			default :
				throw new RuntimeException("Unhandled Action");
			
		}
		return true;
	}
	
	private boolean rxCuiIs(String component, ConceptChronicleBI cc) throws IOException, ContradictionException
	{
		for (RefexDynamicVersionBI<?> rdv : cc.getRefexesDynamicActive(vc_))
		{
			if (rdv.getAssemblageNid() == getNid(RXCUI))
			{
				if (rdv.getData(0).getDataObject().toString().equals(component))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean ttyIs(UUID tty, ConceptChronicleBI cc) throws IOException, ContradictionException
	{
		for (DescriptionChronicleBI d : cc.getDescriptions())
		{
			for (RefexDynamicVersionBI<?> rdv : d.getRefexesDynamicActive(vc_))
			{
				if (rdv.getAssemblageNid() == getNid(RxNormDescType))
				{
					if (((RefexDynamicUUIDBI)rdv.getData(0)).getDataUUID().equals(IN))
					{
						return true;
					}
				}
			}
		}
		return false;
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
		
		RxNormSpreadsheetRules lsr = new RxNormSpreadsheetRules();
		lsr.configure(null, ExtendedAppContext.getDataStore());
		lsr.transform(ExtendedAppContext.getDataStore(), ExtendedAppContext.getDataStore().getConcept(UUID.fromString("b8a86aff-a33d-5ab9-88fe-bb3cfd8dce39")));
		System.out.println(lsr.getWorkResultSummary());
		System.out.println(lsr.getWorkResultDocBookTable());
	}
}
