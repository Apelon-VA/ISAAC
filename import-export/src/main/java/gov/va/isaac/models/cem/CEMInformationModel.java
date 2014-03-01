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
package gov.va.isaac.models.cem;

import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;

import com.google.common.base.Objects;

/**
 * A concrete {@link InformationModel} for displaying CEM models.
 *
 * @author ocarlsen
 */
public class CEMInformationModel implements InformationModel {

    private final String name;
    private final String focusConceptName;
    private final UUID focusConceptUUID;
    private final String importerName;
    private final long time;
    private final Path path;
    private final String moduleName;

    public static CEMInformationModel newInstance(StringMember typeAnnotation,
            ConceptChronicleBI focusConcept, ViewCoordinate vc, BdbTerminologyStore dataStore)
            throws IOException, ContradictionException {

        String modelName = typeAnnotation.getString1();

        ConceptVersionBI focusConceptVersion = focusConcept.getVersion(vc);
        String focusConceptName = focusConceptVersion.getFullySpecifiedDescription().getText();
        UUID focusConceptUUID = focusConceptVersion.getPrimordialUuid();

        // Get metadata from stamp.
        int stampNid = typeAnnotation.getStamp();

        String importerName = "Hard-coded placeholder";

        long time = dataStore.getTimeForStamp(stampNid);

        int pathNid = dataStore.getPathNidForStamp(stampNid);
        Path path = dataStore.getPath(pathNid);

        int moduleNid = dataStore.getModuleNidForStamp(stampNid);
        ConceptChronicleBI module = dataStore.getConcept(moduleNid);
        ConceptVersionBI version = module.getVersion(vc);
        String moduleName = version.getFullySpecifiedDescription().getText();

        return new CEMInformationModel(modelName, focusConceptName,
                focusConceptUUID, importerName, time, path, moduleName);
    }

    public CEMInformationModel(String name, String focusConceptName, UUID focusConceptUUID,
            String importerName, long time, Path path, String moduleName) {
        this.name = name;
        this.focusConceptName = focusConceptName;
        this.focusConceptUUID = focusConceptUUID;
        this.importerName = importerName;
        this.time = time;
        this.path = path;
        this.moduleName = moduleName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InformationModelType getType() {
        return InformationModelType.CEM;
    }

    @Override
    public String getImporterName() {
        return importerName;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    /**
     * @return The name of the concept to which this information model is attached.
     * Eventually this approach be revisited.
     */
    public String getFocusConceptName() {
        return focusConceptName;
    }

    /**
     * @return The UUID of the concept to which this information model is attached.
     * Eventually this approach be revisited.
     */
    public UUID getFocusConceptUUID() {
        return focusConceptUUID;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("type", getType())
                .toString();
    }
}
