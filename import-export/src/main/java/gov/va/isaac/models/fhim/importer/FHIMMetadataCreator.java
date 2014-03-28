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
package gov.va.isaac.models.fhim.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.models.util.MetadataCreatorBase;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create FHIM metadata concepts.
 *
 * @author ocarlsen
 */
public class FHIMMetadataCreator extends MetadataCreatorBase {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMMetadataCreator.class);

    public FHIMMetadataCreator() {
        super();
    }

    @Override
    @SuppressWarnings("unused")
    public boolean createMetadata() throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Check if metadata already created.
        ConceptChronicleBI FHIMRefsets = getConcept(FHIMMetadataBinding.FHIM_REFSET.getUuids()[0]);
        if (FHIMRefsets != null) {
            LOG.info("FHIM metadata already created.");
            return false;
        }

        LOG.info("Preparing to create FHIM metadata.");

        ConceptChronicleBI refsetsRoot = getDataStore().getConcept(UUID.fromString(REFSET_CONCEPT));
        LOG.debug("Refsets root:" + refsetsRoot.toString());

        FHIMRefsets = createNewConcept(refsetsRoot, "FHIM reference sets (foundation metadata concept)", "FHIM reference set");

        ConceptChronicleBI FHIMModelsRefset = createNewConcept(FHIMRefsets, "FHIM Models reference set (foundation metadata concept)", "FHIM Models reference set");
        ConceptChronicleBI FHIMClassesRefset = createNewConcept(FHIMRefsets, "FHIM Classes reference set (foundation metadata concept)", "FHIM Classes reference set");
        ConceptChronicleBI FHIMEnumerationsRefset = createNewConcept(FHIMRefsets, "FHIM Enumerations reference set (foundation metadata concept)", "FHIM Enumerations reference set");
        ConceptChronicleBI FHIMEnumerationValuesRefset = createNewConcept(FHIMRefsets, "FHIM EnumerationValues reference set (foundation metadata concept)", "FHIM EnumerationValues reference set");
        ConceptChronicleBI FHIMAttributesRefset = createNewConcept(FHIMRefsets, "FHIM Attributes reference set (foundation metadata concept)", "FHIM Attributes reference set");
        ConceptChronicleBI FHIMDefaultValuesRefset = createNewConcept(FHIMRefsets, "FHIM DefaultValues reference set (foundation metadata concept)", "FHIM DefaultValues reference set");
        ConceptChronicleBI FHIMGeneralizationsRefset = createNewConcept(FHIMRefsets, "FHIM Generalizations reference set (foundation metadata concept)", "FHIM Generalizations reference set");
        ConceptChronicleBI FHIMDependenciesRefset = createNewConcept(FHIMRefsets, "FHIM Dependencies reference set (foundation metadata concept)", "FHIM Dependencies reference set");
        ConceptChronicleBI FHIMAssociationsRefset = createNewConcept(FHIMRefsets, "FHIM Associations reference set (foundation metadata concept)", "FHIM Associations reference set");
        ConceptChronicleBI FHIMAssociationEndsRefset = createNewConcept(FHIMRefsets, "FHIM Association Ends reference set (foundation metadata concept)", "FHIM Association Ends reference set");
        ConceptChronicleBI FHIMMultiplicityRefset = createNewConcept(FHIMRefsets, "FHIM Multiplicity reference set (foundation metadata concept)", "FHIM Multiplicity reference set");
        ConceptChronicleBI FHIMVisibilityRefset = createNewConcept(FHIMRefsets, "FHIM Visibility reference set (foundation metadata concept)", "FHIM Visibility reference set");

        ConceptChronicleBI attributesRoot = getDataStore().getConcept(UUID.fromString(REFSET_ATTRIBUTE_CONCEPT));
        LOG.debug("Attributes root:" + attributesRoot.toString());

        ConceptChronicleBI FHIMAttributes = createNewConcept(attributesRoot, "FHIM attributes (foundation metadata concept)", "FHIM attributes");

        // Data types.
        ConceptChronicleBI FHIMDataTypes = createNewConcept(FHIMAttributes, "FHIM data types (foundation metadata concept)", "FHIM data types");
        ConceptChronicleBI FHIMCode = createNewConcept(FHIMDataTypes, "FHIM Code (foundation metadata concept)", "FHIM Code");
        ConceptChronicleBI FHIMObservationQualifier = createNewConcept(FHIMDataTypes, "FHIM ObservationQualifier (foundation metadata concept)", "FHIM ObservationQualifier");
        ConceptChronicleBI FHIMObservationStatement = createNewConcept(FHIMDataTypes, "FHIM ObservationStatement (foundation metadata concept)", "FHIM ObservationStatement");
        ConceptChronicleBI FHIMPysicalQuantity = createNewConcept(FHIMDataTypes, "FHIM PysicalQuantity (foundation metadata concept)", "FHIM PysicalQuantity");
        ConceptChronicleBI FHIMPulsePosition = createNewConcept(FHIMDataTypes, "FHIM PulsePosition (foundation metadata concept)", "FHIM PulsePosition");

        // Multiplicity types.
        ConceptChronicleBI FHIMMultiplicityTypes = createNewConcept(FHIMAttributes, "FHIM Multiplicity types (foundation metadata concept)", "FHIM multiplicity types");
        ConceptChronicleBI FHIMUpper = createNewConcept(FHIMMultiplicityTypes, "FHIM Upper (foundation metadata concept)", "FHIM Upper");
        ConceptChronicleBI FHIMLower = createNewConcept(FHIMMultiplicityTypes, "FHIM Lower (foundation metadata concept)", "FHIM Lower");

        for (ConceptChronicleBI loopUc : getDataStore().getUncommittedConcepts()) {
            LOG.debug("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        getDataStore().commit();

        LOG.info("FHIM metadata creation finished.");
        return true;
    }
}
