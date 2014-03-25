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
package gov.va.isaac.models.cem.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.models.util.MetadataCreatorBase;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create CEM metadata concepts.
 *
 * @author alo
 * @author ocarlsen
 */
public class CEMMetadataCreator extends MetadataCreatorBase {

    private static final Logger LOG = LoggerFactory.getLogger(CEMMetadataCreator.class);

    public CEMMetadataCreator() {
        super();
    }

    @Override
    @SuppressWarnings("unused")
    public boolean createMetadata() throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Check if metadata already created.
        ConceptChronicleBI CEMRefsets = getConcept(CEMMetadataBinding.CEM_REFSET.getUuids()[0]);
        if (CEMRefsets != null) {
            LOG.info("CEM metadata already created.");
            return false;
        }

        LOG.info("Preparing to create CEM metadata.");

        ConceptChronicleBI refsetsRoot = getDataStore().getConcept(UUID.fromString(REFSET_CONCEPT));
        LOG.debug("Refsets root:" + refsetsRoot.toString());

        CEMRefsets = createNewConcept(refsetsRoot, "CEM reference sets (foundation metadata concept)", "CEM reference sets");

        ConceptChronicleBI CEMDataRefset = createNewConcept(CEMRefsets, "CEM data reference set (foundation metadata concept)", "CEM data reference set");
        ConceptChronicleBI CEMTypeRefset = createNewConcept(CEMRefsets, "CEM type reference set (foundation metadata concept)", "CEM type reference set");
        ConceptChronicleBI CEMKeyRefset = createNewConcept(CEMRefsets, "CEM key reference set (foundation metadata concept)", "CEM key reference set");
        ConceptChronicleBI CEMInfoRefset = createNewConcept(CEMRefsets, "CEM info reference set (foundation metadata concept)", "CEM info reference set");
        ConceptChronicleBI CEMCompositionRefset = createNewConcept(CEMRefsets, "CEM composition reference set (foundation metadata concept)", "CEM composition reference set");
        ConceptChronicleBI CEMConstraintsRefset = createNewConcept(CEMRefsets, "CEM constraints reference set (foundation metadata concept)", "CEM constraints reference set");
        ConceptChronicleBI CEMConstraintPath = createNewConcept(CEMRefsets, "CEM constraints path reference set (foundation metadata concept)", "CEM constraint path");
        ConceptChronicleBI CEMConstraintValue = createNewConcept(CEMRefsets, "CEM constraints value reference set (foundation metadata concept)", "CEM constraint value");
        ConceptChronicleBI CEMValue = createNewConcept(CEMRefsets, "CEM value reference set (foundation metadata concept)", "CEM constraint path");

        ConceptChronicleBI attributesRoot = getDataStore().getConcept(UUID.fromString(REFSET_ATTRIBUTE_CONCEPT));
        LOG.debug("Attributes root:" + attributesRoot.toString());

        ConceptChronicleBI CEMAttributes = createNewConcept(attributesRoot, "CEM attributes (foundation metadata concept)", "CEM attributes");

        ConceptChronicleBI CEMDataTypes = createNewConcept(CEMAttributes, "CEM data types (foundation metadata concept)", "CEM data types");
        ConceptChronicleBI CEMCDType = createNewConcept(CEMDataTypes, "CEM CD data type (foundation metadata concept)", "CEM CD data type");
        ConceptChronicleBI CEMPQType = createNewConcept(CEMDataTypes, "CEM PQ data type (foundation metadata concept)", "CEM PQ data type");

        ConceptChronicleBI CEMComponentTypes = createNewConcept(CEMAttributes, "CEM component types (foundation metadata concept)", "CEM component types");
        ConceptChronicleBI CEMItem = createNewConcept(CEMComponentTypes, "CEM item (foundation metadata concept)", "CEM item");
        ConceptChronicleBI CEMQual = createNewConcept(CEMComponentTypes, "CEM qualifier (foundation metadata concept)", "CEM quaifier");
        ConceptChronicleBI CEMMod = createNewConcept(CEMComponentTypes, "CEM modifier (foundation metadata concept)", "CEM modifier");
        ConceptChronicleBI CEMAttr = createNewConcept(CEMComponentTypes, "CEM attribution (foundation metadata concept)", "CEM attribution");
        ConceptChronicleBI CEMLink = createNewConcept(CEMComponentTypes, "CEM link (foundation metadata concept)", "CEM link");

        for (ConceptChronicleBI loopUc : getDataStore().getUncommittedConcepts()) {
            LOG.debug("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        getDataStore().commit();

        LOG.info("CEM metadata creation finished.");
        return true;
    }
}
