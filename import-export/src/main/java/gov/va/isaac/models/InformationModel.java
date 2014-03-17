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
package gov.va.isaac.models;

import gov.va.isaac.model.InformationModelType;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Defines API for displaying information models.
 *
 * @author ocarlsen
 */
public interface InformationModel {

    public static final class Metadata {

        private final String importerName;
        private final long time;
        private final Path path;
        private final String moduleName;

        public static Metadata newInstance(int stampNid, BdbTerminologyStore dataStore,
                ViewCoordinate vc) throws IOException, ContradictionException {

            String importerName = "Hard-coded placeholder";

            long time = dataStore.getTimeForStamp(stampNid);

            int pathNid = dataStore.getPathNidForStamp(stampNid);
            Path path = dataStore.getPath(pathNid);

            int moduleNid = dataStore.getModuleNidForStamp(stampNid);
            ConceptChronicleBI module = dataStore.getConcept(moduleNid);
            ConceptVersionBI version = module.getVersion(vc);
            String moduleName = version.getFullySpecifiedDescription().getText();

            return new Metadata(importerName, time, path, moduleName);
        }

        public Metadata(String importerName, long time, Path path, String moduleName) {
            this.importerName = importerName;
            this.time = time;
            this.path = path;
            this.moduleName = moduleName;
        }

        public String getImporterName() {
            return importerName;
        }

        public long getTime() {
            return time;
        }

        public Path getPath() {
            return path;
        }

        public String getModuleName() {
            return moduleName;
        }
    }

    public String getName();

    public InformationModelType getType();

    public Metadata getMetadata();

    public String getFocusConceptName();

    public UUID getFocusConceptUUID();

}
