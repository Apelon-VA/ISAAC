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

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create FHIM metadata concepts.
 *
 * @author ocarlsen
 */
public class FHIMMetadataCreator extends MetadataCreatorBase {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMMetadataCreator.class);

    public FHIMMetadataCreator() throws ValidationException, IOException {
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

        ConceptChronicleBI FHIMTypeRefset = createNewConcept(FHIMRefsets, "FHIM type reference set (foundation metadata concept)", "FHIM type reference set");
        ConceptChronicleBI FHIMCodeRefset = createNewConcept(FHIMRefsets, "FHIM code reference set (foundation metadata concept)", "FHIM code reference set");
        ConceptChronicleBI FHIMDataRefset = createNewConcept(FHIMRefsets, "FHIM data reference set (foundation metadata concept)", "FHIM data reference set");
        ConceptChronicleBI FHIMCompositionRefset = createNewConcept(FHIMRefsets, "FHIM composition reference set (foundation metadata concept)", "FHIM composition reference set");
        ConceptChronicleBI FHIMConstraintsRefset = createNewConcept(FHIMRefsets, "FHIM constraints reference set (foundation metadata concept)", "FHIM constraints reference set");
        ConceptChronicleBI FHIMConstraintPath = createNewConcept(FHIMRefsets, "FHIM constraints path reference set (foundation metadata concept)", "FHIM constraint path");
        ConceptChronicleBI FHIMConstraintValue = createNewConcept(FHIMRefsets, "FHIM constraints value reference set (foundation metadata concept)", "FHIM constraint value");

        ConceptChronicleBI attributesRoot = getDataStore().getConcept(UUID.fromString(REFSET_ATTRIBUTE_CONCEPT));
        LOG.debug("Attributes root:" + attributesRoot.toString());

        ConceptChronicleBI FHIMAttributes = createNewConcept(attributesRoot, "FHIM attributes (foundation metadata concept)", "FHIM attributes");

        ConceptChronicleBI FHIMDataTypes = createNewConcept(FHIMAttributes, "FHIM data types (foundation metadata concept)", "FHIM data types");
        ConceptChronicleBI FHIMPysicalQuantityDataType = createNewConcept(FHIMDataTypes, "FHIM PysicalQuantity data type (foundation metadata concept)", "FHIM PysicalQuantity data type");
        // TODO: Other data types as necessary.

        ConceptChronicleBI FHIMComponentTypes = createNewConcept(FHIMAttributes, "FHIM component types (foundation metadata concept)", "FHIM component types");
        ConceptChronicleBI FHIMQual = createNewConcept(FHIMComponentTypes, "FHIM qualifier (foundation metadata concept)", "FHIM quaifier");
        ConceptChronicleBI FHIMMod = createNewConcept(FHIMComponentTypes, "FHIM modifier (foundation metadata concept)", "FHIM modifier");
        ConceptChronicleBI FHIMAttr = createNewConcept(FHIMComponentTypes, "FHIM attribution (foundation metadata concept)", "FHIM attribution");

        for (ConceptChronicleBI loopUc : getDataStore().getUncommittedConcepts()) {
            LOG.debug("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        getDataStore().commit();

        LOG.info("FHIM metadata creation finished.");
        return true;
    }
}
