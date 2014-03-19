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
package gov.va.isaac.models.cem.fetcher;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.models.InformationModel.Metadata;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;

import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for fetching CEM models.
 *
 * @author ocarlsen
 */
public class CEMFetcher extends ExporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(CEMFetcher.class);

    public CEMFetcher() {
        super();
    }

    public CEMInformationModel fetchCEMModel(UUID focusConceptUUID) throws Exception {
        LOG.info("Starting fetch of CEM model type");
        LOG.debug("focusConceptUUID="+focusConceptUUID);

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get chronicle for concept.
        ConceptChronicleBI focusConcept = getDataStore().getConcept(focusConceptUUID);
        LOG.debug("focusConcept="+focusConcept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations =
                getLatestAnnotations(focusConcept);

        // Type attribute (1).
        StringMember typeAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_TYPE_REFSET, StringMember.class);

        // Abort if not found.
        if (typeAnnotation == null) {
            return  null;
        }

        String modelName = typeAnnotation.getString1();
        CEMInformationModel informationModel = new CEMInformationModel(modelName);

        Metadata metadata = Metadata.newInstance(typeAnnotation.getStamp(),
                getDataStore(), getVC());
        informationModel.setMetadata(metadata);

        ConceptVersionBI focusConceptVersion = focusConcept.getVersion(getVC());
        String focusConceptName = focusConceptVersion.getFullySpecifiedDescription().getText();
        informationModel.setFocusConceptName(focusConceptName);
        informationModel.setFocusConceptUUID(focusConceptUUID);

        LOG.debug("informationModel="+informationModel);
        LOG.info("Ending fetch of CEM model type");

        return informationModel;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
