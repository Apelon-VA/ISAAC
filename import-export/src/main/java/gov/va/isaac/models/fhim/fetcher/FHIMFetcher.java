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
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.InformationModel.Metadata;
import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Class for fetching FHIM models.
 *
 * @author ocarlsen
 */
public class FHIMFetcher extends ExporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMFetcher.class);

    public FHIMFetcher() {
        super();
    }

    public List<InformationModel> fetchFHIMModels() throws Exception {
        LOG.info("Starting fetch of FHIM model types");
        List<InformationModel> models = Lists.newArrayList();

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get the roots of all FHIM models.
        ConceptChronicleBI modelsRefsetConcept = getDataStore().getConcept(FHIMMetadataBinding.FHIM_MODELS_REFSET.getNid());
        List<StringMember> modelRefsetMembers = getRefsetMembers(modelsRefsetConcept, StringMember.class);
        if (modelRefsetMembers.isEmpty()) {
            LOG.info("No FHIM_MODELS_REFSET members found.");
            return models;
        }

        // Iterate through and create information models.
        for (StringMember modelRefsetMember : modelRefsetMembers) {
            String modelName = modelRefsetMember.getString1();
            UUID modelUUID = modelRefsetMember.getUUIDs().get(0);
            FHIMInformationModel informationModel = new FHIMInformationModel(modelName, modelUUID);

            Metadata metadata = Metadata.newInstance(modelRefsetMember.getStamp(),
                    getDataStore(), getVC());
            informationModel.setMetadata(metadata);

            int focusConceptNid = modelRefsetMember.getReferencedComponentNid();
            ComponentChronicleBI<ConceptVersionBI> focusConcept = getDataStore().getConcept(focusConceptNid);
            ConceptVersionBI focusConceptVersion = focusConcept .getVersion(getVC());
            String focusConceptName = focusConceptVersion.getFullySpecifiedDescription().getText();
            informationModel.setFocusConceptName(focusConceptName);

            UUID focusConceptUUID = focusConcept.getPrimordialUuid();
            informationModel.setFocusConceptUUID(focusConceptUUID);

            LOG.debug("informationModel="+informationModel);
            models.add(informationModel);
        }

        LOG.info("Ending fetch of FHIM model type");
        return models;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
