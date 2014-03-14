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
package gov.va.isaac.models.fhim.fetcher;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.models.InformationModel.Metadata;
import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for fetching FHIM models.
 *
 * @author ocarlsen
 */
public class FHIMFetcher extends ExporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMFetcher.class);

    public FHIMFetcher() throws ValidationException, IOException {
        super();
    }

    public FHIMInformationModel fetchFHIMModel(UUID focusConceptUUID) throws Exception {
        LOG.info("Starting fetch of FHIM model type");
        LOG.debug("focusConceptUUID="+focusConceptUUID);

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get chronicle for concept.
        ConceptChronicleBI focusConcept = getDataStore().getConcept(focusConceptUUID);
        LOG.debug("focusConcept="+focusConcept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations =
                getLatestAnnotations(focusConcept);

        // FHIM Models refset (1).
        StringMember modelAnnotation = getSingleAnnotation(focusConceptAnnotations,
                FHIMMetadataBinding.FHIM_MODELS_REFSET, StringMember.class);

        String modelName = modelAnnotation.getString1();
        FHIMInformationModel informationModel = new FHIMInformationModel(modelName);

        Metadata metadata = Metadata.newInstance(modelAnnotation.getStamp(),
                getDataStore(), getVC());
        informationModel.setMetadata(metadata);

        ConceptVersionBI focusConceptVersion = focusConcept.getVersion(getVC());
        String focusConceptName = focusConceptVersion.getFullySpecifiedDescription().getText();
        informationModel.setFocusConceptName(focusConceptName);
        informationModel.setFocusConceptUUID(focusConceptUUID);

        LOG.debug("informationModel="+informationModel);
        LOG.info("Ending fetch of FHIM model type");

        return informationModel;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
