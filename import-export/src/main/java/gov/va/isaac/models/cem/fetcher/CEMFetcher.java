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
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
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

    public CEMFetcher() throws ValidationException, IOException {
        super();
    }

    public String fetchCEMType(UUID conceptUUID) throws Exception {
        LOG.info("Starting fetch of CEM model type");
        LOG.debug("conceptUUID="+conceptUUID);

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get chronicle for concept.
        ComponentChronicleBI<?> focusConcept = getDataStore().getComponent(conceptUUID);
        LOG.debug("focusConcept="+focusConcept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations = getLatestAnnotations(focusConcept);

        // Type attribute (1).
        StringMember typeAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_TYPE_REFSET, StringMember.class);

        String type = null;
        if (typeAnnotation != null) {
            type = typeAnnotation.getString1();
        }

        LOG.debug("type="+type);
        LOG.info("Ending fetch of CEM model type");

        return type;
    }
}
