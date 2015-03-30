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

import gov.va.isaac.mojos.dbTransforms.TransformArbitraryI;
import gov.va.isaac.util.OTFUtility;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RxNormHierarchyAdditions}
 * 
 * A Transformer that executes various rules to create more taxonomy in RxNorm
 * 
 * So far:
 * 
 * Let’s further integrate RxNorm and SNOMED CT. For every RxNorm IN concept with the string “penicillin” in its name, make it a child (is-a) 
 * of SNOMED CT fdca98cf-8720-3dbe-bb72-3377d658a85c Penicillin -class of antibiotic- (product).
 * 
 *   a.  What should happen here is that the relationship between the RxNorm penicillin ingredient concepts and the dummy “RxNorm Ingredients” 
 *       is made redundant by the classifier… it would still be there in stated view, but not in inferred view.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Named(value = "RxNorm Hierarchy Additions")
public class RxNormHierarchyAdditions implements TransformArbitraryI
{
	private AtomicInteger addedRels = new AtomicInteger();
	private AtomicInteger examinedConcepts = new AtomicInteger();
	
	private final UUID penicillinProduct = UUID.fromString("fdca98cf-8720-3dbe-bb72-3377d658a85c");//Penicillin -class of antibiotic- (product)
	private final UUID termTypeIN = UUID.fromString("17114d54-ed48-5f0a-a865-4ecec3e31cdc"); //Name for an Ingredient     // IN
	private final UUID rxNormHPath = UUID.fromString("763c21ad-55e3-5bb3-af1e-3e4fb475de44"); //RxNormH Path
	private final UUID rxNormDescType = UUID.fromString("3599879d-78c6-5b1e-b442-9ef08eaedd3c");// "RxNorm Description Type" - refex that carries ingredient linkage

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getName()
	 */
	@Override
	public String getName()
	{
		return "RxNorm Hierarchy Additions";
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
	 * @throws Exception
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#transform(org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI)
	 */
	@Override
	public void transform(TerminologyStoreDI ts) throws Exception
	{
		int rxNormPathNid = ts.getNidForUuids(rxNormHPath);
		int rxNormDescTypeAssemblageNid = ts.getNidForUuids(rxNormDescType);
		ts.iterateConceptDataInParallel(new ProcessUnfetchedConceptDataBI()
		{
			@Override
			public boolean continueWork()
			{
				return true;
			}

			@Override
			public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception
			{
				ConceptChronicleBI cc = fetcher.fetch();
				examinedConcepts.getAndIncrement();
				
				boolean isRxNormPath = false;
				for (int stampNid : cc.getConceptAttributes().getAllStamps())
				{
					if (ts.getPathNidForStamp(stampNid) == rxNormPathNid)
					{
						isRxNormPath = true;
						break;
					}
				}
				
				if (!isRxNormPath)
				{
					return;
				}
				
				boolean isRxNormIN = false;
				boolean hasPenicillin = false;
				for (DescriptionChronicleBI desc : cc.getDescriptions())
				{
					if (hasPenicillin && isRxNormIN)
					{
						break;
					}
					DescriptionVersionBI<?> currentDescription = OTFUtility.getLatestDescVersion(desc.getVersions());
					
					if (currentDescription == null)
					{
						continue;
					}
					
					for (RefexDynamicChronicleBI<?> refex : currentDescription.getRefexesDynamic())
					{
						RefexDynamicVersionBI<?> currentRefex = OTFUtility.getLatestDynamicRefexVersion(refex.getVersions());
						if (currentRefex.getAssemblageNid() == rxNormDescTypeAssemblageNid)
						{
							if (((RefexDynamicUUID)currentRefex.getData()[0]).getDataUUID().equals(termTypeIN))
							{
								isRxNormIN = true;
								break;
							}
						}
					}
					
					if (currentDescription.getText().toLowerCase().contains("penicillin"))
					{
						hasPenicillin = true;
					}
				}
				
				if (hasPenicillin && isRxNormIN)
				{
					RelationshipCAB rCab = new RelationshipCAB(cc.getPrimordialUuid(), Snomed.IS_A.getUuids()[0], penicillinProduct,
							0, RelationshipType.STATED_ROLE, IdDirective.GENERATE_HASH);
					
					ts.getTerminologyBuilder(new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), TermAux.UNSPECIFIED_MODULE.getLenient().getNid(), 
							rxNormPathNid), StandardViewCoordinates.getWbAuxiliary()).construct(rCab);
					ts.addUncommitted(cc);
					
					int last = addedRels.getAndIncrement();
					if (last % 2000 == 0)
					{
						ts.commit();
					}
				}
			}

			@Override
			public String getTitle()
			{
				return "RxNorm Hierachy Creator";
			}

			@Override
			public NativeIdSetBI getNidSet() throws IOException
			{
				return null;
			}

			@Override
			public boolean allowCancel()
			{
				return false;
			}
		});
		ts.commit();
	}

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "Add various relationships between RxNorm and Snomed CT";
	}

	/**
	 * @see gov.va.isaac.mojos.dbTransforms.TransformI#getWorkResultSummary()
	 */
	@Override
	public String getWorkResultSummary()
	{
		return "Examined " + examinedConcepts.get() + " concepts and generated " + addedRels.get() + " new relationships";
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
