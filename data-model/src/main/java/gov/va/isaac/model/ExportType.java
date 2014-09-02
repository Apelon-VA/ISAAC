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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.model;

import javafx.collections.ObservableList;

import com.sun.javafx.collections.ImmutableObservableList;


/**
 * An enumerated type representing various export type.
 *
 * @author tnaing
 */
public enum ExportType {

    OWL("Owl", "xml"),
    RF2("RF2", null),
    ECONCEPT("eConcept", ".jbin");

    private static final ImmutableObservableList<ExportType> VALUES =
            new ImmutableObservableList<>(ExportType.values());

    private final String displayName;
    private final String fileExtension;

    private ExportType(String displayName, String fileExtension) {
        this.displayName = displayName;
        this.fileExtension = fileExtension;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static ObservableList<ExportType> asObservableList() {
        return VALUES;
    }
}
