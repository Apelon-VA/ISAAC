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
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GenerateMissingPreferredTerms}
 * 
 * A Transformer that checks all concepts in the DB, looking to see if each
 * concept has a description type of preferred. If a concept does not have a
 * description type of preferred - it creates a new description from the FSN
 * (stripping out the semantic tag, if present.
 * 
 * Logs an error if no FSN is found on a concept.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Named(value = "Generate Missing Preferred Terms")
public class GenerateMissingPreferredTerms implements TransformConceptIterateI
{
	private AtomicInteger generatedDescriptions = new AtomicInteger();
	private AtomicInteger examinedConcepts = new AtomicInteger();
	private AtomicInteger missingFSNs = new AtomicInteger();
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getName()
	 */
	@Override
	public String getName()
	{
		return "Generate Missing Preferred Terms";
	}
	
	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#configure(java.io.File, org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI)
	 */
	@Override
	public void configure(File configFile, TerminologyStoreDI ts)
	{
		// noop
	}

	/**
	 * 
	 * @see gov.va.isaac.mojos.dbTransforms.TransformConceptIterateI#transform(org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI, 
	 *  org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI)
	 */
	@Override
	public boolean transform(TerminologyStoreDI ts, ConceptChronicleBI cc) throws Exception
	{
		examinedConcepts.getAndIncrement();
		boolean foundPreferred = false;;
		String fsnText = null;
		LanguageCode fsnLC = null;
		int pathNid = -1;
		int moduleNid = -1;
		
		for (DescriptionChronicleBI desc : cc.getDescriptions())
		{
			if (foundPreferred && fsnText != null)
			{
				break;
			}
			
			DescriptionVersionBI<?> currentDescription = OTFUtility.getLatestDescVersion(desc.getVersions());
			
			if (currentDescription == null)
			{
				log.warn("No description version found on concept - {} - description - ", cc.toLongString(), desc.toUserString());
				missingFSNs.incrementAndGet();
				continue;
			}
			
			if (currentDescription.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getNid() 
					|| currentDescription.getTypeNid() == SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getNid())
			{
				fsnText = currentDescription.getText();
				fsnLC = LanguageCode.getLangCode(currentDescription.getLang());
				pathNid = currentDescription.getPathNid();
				moduleNid = currentDescription.getModuleNid();
			}
			else if (currentDescription.getTypeNid() == SnomedMetadataRf2.SYNONYM_RF2.getNid() ||
					currentDescription.getTypeNid() == SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getNid())
			{
				for (RefexChronicleBI<?> refex : currentDescription.getRefexes())
				{
					RefexVersionBI<?> currentRefex = OTFUtility.getLatestRefexVersion(refex.getVersions());
					if (currentRefex instanceof RefexNidVersionBI)
					{
						if (((RefexNidVersionBI<?>)currentRefex).getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getNid() ||
								((RefexNidVersionBI<?>)currentRefex).getNid1() == SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getNid())
						{
							foundPreferred = true;
							break;
						}
					}
				}
			}
		}
		
		if (!foundPreferred)
		{
			if (fsnText == null)
			{
				log.warn("No description FSN version found on concept - {}", cc.toLongString());
				missingFSNs.getAndIncrement();
			}
			else
			{
				String fsnWithoutSemTag = fsnText;
				
				if (fsnWithoutSemTag.endsWith(")") && fsnWithoutSemTag.contains("("))
				{
					fsnWithoutSemTag = fsnWithoutSemTag.substring(0, fsnWithoutSemTag.lastIndexOf("("));
				}
				DescriptionCAB dCab = new DescriptionCAB(cc.getPrimordialUuid(), Snomed.SYNONYM_DESCRIPTION_TYPE.getUuids()[0], fsnLC, fsnWithoutSemTag,
						true, IdDirective.GENERATE_HASH);
				dCab.makePreferredNameDialectRefexes(fsnLC);
				
				ts.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), moduleNid, pathNid), 
						StandardViewCoordinates.getWbAuxiliary()).construct(dCab);
				ts.addUncommitted(cc);
				generatedDescriptions.getAndIncrement();
				return true;
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
		return "Generated preferred term descriptions for concepts based off of their FSN description, if no preferred term description is found.";
	}

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getWorkResultSummary()
	 */
	@Override
	public String getWorkResultSummary()
	{
		return "Examined " + examinedConcepts.get() + " concepts and generated " + generatedDescriptions.get() + " new descriptions"
				+ (missingFSNs.get() > 0 ? ".  ERROR: [" + missingFSNs.get() + " concepts were missing FSNs]" : "");
	}

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getWorkResultDocBookTable()
	 */
	@Override
	public String getWorkResultDocBookTable()
	{
		return "Not yet created";
	}
}
