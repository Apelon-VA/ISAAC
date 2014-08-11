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
package gov.va.isaac.models.va.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.models.util.MetadataCreatorBase;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create VA metadata concepts.
 *
 * @author jefron
 */
public class VAMetadataCreator  {
    protected static final String TERMINOLOGY_AUXILIARY_CONCEPT = "f4d2fabc-7e96-3b3a-a348-ae867ba74029";

    private static final Logger LOG = LoggerFactory.getLogger(VAMetadataCreator.class);
	private BdbTerminologyStore dataStore;
	private TerminologyBuilderBI dataBuilder;

    public VAMetadataCreator() {
    }

    public VAMetadataCreator(BdbTerminologyStore dataStore,
			TerminologyBuilderBI dataBuilder) {
		this.dataStore = dataStore;
		this.dataBuilder = dataBuilder;
	}

    @SuppressWarnings("unused")
    public boolean createMetadata() throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

System.out.println("11111111111");        
        // Check if metadata already created.
        ConceptChronicleBI VAMetadataCon =dataStore.getConcept(VAMetadataBinding.VA_METADATA.getUuids()[0]);
        System.out.println("222222222222");
        if (VAMetadataCon == null) {
            System.out.println("333333333333333");
            LOG.info("FHIM metadata already created.");
            LOG.info("Preparing to create FHIM metadata.");

            ConceptChronicleBI parentCon = dataStore.getConcept(UUID.fromString(TERMINOLOGY_AUXILIARY_CONCEPT));
            System.out.println("44444444444");
            LOG.debug("Metadata root:" + parentCon.toString());
            System.out.println("44444444444-AAAAAAAAAA with parentCon: " + parentCon.toString());
            System.out.println("44444444444-BBBBBBBBB with parentCon: " + parentCon.getVersion(StandardViewCoordinates.getSnomedStatedLatest()));
             

            VAMetadataCon = createNewConcept(parentCon, "VA Metadata (foundation metadata concept)", "VA Metadata");
            System.out.println("555555555555");

            ConceptChronicleBI VAAllergenTypes = createNewConcept(VAMetadataCon, "Allergen Type (foundation metadata concept)", "Allergen Type");
            ConceptChronicleBI FoodAllergen = createNewConcept(VAAllergenTypes, "Food Allergen (foundation metadata concept)", "Food Allergen");
            ConceptChronicleBI EnvironAllergen = createNewConcept(VAAllergenTypes, "Environmental Allergen (foundation metadata concept)", "Environmental Allergen");
            ConceptChronicleBI MedAllergen = createNewConcept(VAAllergenTypes, "Medication Allergen (foundation metadata concept)", "Medication Allergen");
            System.out.println("66666666666");
            return true;
        } else {
            System.out.println("777777777777");
            ConceptChronicleBI WorkflowConcepts = createNewConcept(VAMetadataCon, "Workflow Concepts (foundation metadata concept)", "Workflow Concepts");
            ConceptChronicleBI jefron_author = createNewConcept(WorkflowConcepts, "Jesse Efron (foundation metadata concept)", "Jesse Efron");
            ConceptChronicleBI assignedState = createNewConcept(WorkflowConcepts, "Assigned Workflow State (foundation metadata concept)", "Assigned Workflow State");
            ConceptChronicleBI demoProject = createNewConcept(WorkflowConcepts, "Demo Project (foundation metadata concept)", "Demo Project");
            System.out.println("888888888888888");
        }


        for (ConceptChronicleBI loopUc : dataStore.getUncommittedConcepts()) {
            LOG.debug("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        System.out.println("999999999999");
        dataStore.commit();
        System.out.println("10101010101010");

        LOG.info("VA metadata creation finished.");
        return true;
    }
	
    protected ConceptChronicleBI createNewConcept(ConceptChronicleBI parent, String fsn,
            String prefTerm) throws IOException, InvalidCAB, ContradictionException {
    	System.out.println("---------------AAA");
        ConceptCB newConCB = createNewConceptBlueprint(parent, fsn, prefTerm);
System.out.println("---------------BBB");
        ConceptChronicleBI newCon = dataBuilder.construct(newConCB);
        System.out.println("---------------CCC");

        dataStore.addUncommitted(newCon);

        return newCon;
    }

    protected ConceptCB createNewConceptBlueprint(ConceptChronicleBI parent, String fsn, String prefTerm)
            throws ValidationException, IOException, InvalidCAB,
            ContradictionException {
        LanguageCode lc = LanguageCode.EN_US;
        UUID isA = Snomed.IS_A.getUuids()[0];
        IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID parentUUIDs[] = new UUID[1];
        parentUUIDs[0] = parent.getPrimordialUuid();
        System.out.println("---------------1111");
        return new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parentUUIDs);
    }

}
