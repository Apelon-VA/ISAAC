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

import java.util.UUID;

import com.google.common.base.Objects;

/**
 * A concrete {@link InformationModel} for displaying CEM models.
 *
 * @author ocarlsen
 */
public class CEMInformationModel implements InformationModel {

    private final String name;
    private final Metadata metadata;
    private final String focusConceptName;
    private final UUID focusConceptUUID;

    public CEMInformationModel(String name, Metadata metadata,
            String focusConceptName, UUID focusConceptUUID) {
        this.name = name;
        this.focusConceptName = focusConceptName;
        this.focusConceptUUID = focusConceptUUID;
        this.metadata = metadata;
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
    public Metadata getMetadata() {
        return metadata;
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
