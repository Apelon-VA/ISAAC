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
import gov.va.isaac.models.util.MetadataCreator;

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
public class FHIMMetadataCreator extends MetadataCreator {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMMetadataCreator.class);

    public FHIMMetadataCreator() throws ValidationException, IOException {
        super();
    }

    @Override
    @SuppressWarnings("unused")
    public void createMetadata() throws Exception {
        LOG.info("Preparing to create metadata");

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        ConceptChronicleBI refsetsRoot = getDataStore().getConcept(UUID.fromString(REFSET_CONCEPT));
        LOG.info("Refsets root:" + refsetsRoot.toString());

        ConceptChronicleBI FHIMRefset = createNewConcept(refsetsRoot, "FHIM reference sets (foundation metadata concept)", "FHIM reference sets");

        ConceptChronicleBI attributesRoot = getDataStore().getConcept(UUID.fromString(REFSET_ATTRIBUTE_CONCEPT));
        LOG.info("Attributes root:" + attributesRoot.toString());

        ConceptChronicleBI FHIMAttributes = createNewConcept(attributesRoot, "FHIM attributes (foundation metadata concept)", "FHIM attributes");

        for (ConceptChronicleBI loopUc : getDataStore().getUncommittedConcepts()) {
            LOG.debug("Uncommitted concept:" + loopUc.toString() + " - " + loopUc.getPrimordialUuid());
        }

        getDataStore().commit();

        LOG.info("Metadata creation finished");
    }
}
