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
package gov.va.isaac.models.util;

import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;

import java.util.UUID;

import com.google.common.base.Objects;

/**
 * An abstract {@link InformationModel} for concrete implementations.
 *
 * @author ocarlsen
 */
public class AbstractInformationModel implements InformationModel {

    private final String name;
    private final UUID uuid;
    private final InformationModelType type;

    private Metadata metadata;
    private String focusConceptName;
    private UUID focusConceptUUID;

    public AbstractInformationModel(String name, UUID uuid,
            InformationModelType type) {
        super();
        this.name = name;
        this.uuid = uuid;
        this.type = type;
    }

    @Override
    public final String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public final InformationModelType getType() {
        return type;
    }

    @Override
    public final Metadata getMetadata() {
        return metadata;
    }

    public final void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public final String getFocusConceptName() {
        return focusConceptName;
    }

    public final void setFocusConceptName(String focusConceptName) {
        this.focusConceptName = focusConceptName;
    }

    @Override
    public final UUID getFocusConceptUUID() {
        return focusConceptUUID;
    }

    public final void setFocusConceptUUID(UUID focusConceptUUID) {
        this.focusConceptUUID = focusConceptUUID;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("type", getType())
                .toString();
    }
}
