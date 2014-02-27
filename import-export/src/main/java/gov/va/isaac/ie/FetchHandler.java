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
package gov.va.isaac.ie;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.cem.fetcher.CEMFetcher;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Class for fetching imported information models.
 *
 * @author ocarlsen
 */
public class FetchHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FetchHandler.class);

    public FetchHandler() throws ValidationException, IOException {
        super();
    }

    /**
     * Method called by the ISAAC application to fetch the information models.
     * @throws Exception
     */
    public List<String> fetchModels(InformationModelType modelType) throws Exception {
        LOG.debug("modelType=" + (modelType != null ? modelType.name() : "All"));

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get "Blood pressure taking (procedure)" concept.
        UUID conceptUUID = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");

        if (modelType == null) {

            // Fetch all model types.
            List<String> allModelTypes = Lists.newArrayList();
            allModelTypes.addAll(fetchCEM(conceptUUID));
            allModelTypes.addAll(fetchCIMI());
            allModelTypes.addAll(fetchFHIM());
            allModelTypes.addAll(fetchHeD());
            return allModelTypes;

        } else {

            // Fetch individual model type.
            switch (modelType) {
            case CEM:
                return fetchCEM(conceptUUID);
            case CIMI:
                return fetchCIMI();
            case FHIM:
                return fetchFHIM();
            case HeD:
                return fetchHeD();
            default:
                throw new IllegalArgumentException("Unrecognized modelType: " + modelType);
            }
        }
    }

    private List<String> fetchHeD() {

        // Not yet imported.
        return Collections.emptyList();
    }

    private List<String> fetchFHIM() {

        // Not yet imported.
        return Collections.emptyList();
    }

    private List<String> fetchCIMI() {

        // Not yet imported.
        return Collections.emptyList();
    }

    private List<String> fetchCEM(UUID conceptUUID) throws Exception {
        List<String> types = Lists.newArrayList();

        CEMFetcher fetcher = new CEMFetcher();
        String type = fetcher.fetchCEMType(conceptUUID);
        if (type != null) {
            types.add(type);
        }

        return types;
    }
}
