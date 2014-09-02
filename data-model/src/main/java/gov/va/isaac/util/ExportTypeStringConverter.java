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
package gov.va.isaac.util;

import gov.va.isaac.model.ExportType;
import javafx.util.StringConverter;

/**
 * A {@link StringConverter} for the {@link ExportType} enum.
 *
 * @author ocarlsen
 */
public class ExportTypeStringConverter extends StringConverter<ExportType> {

    @Override
    public String toString(ExportType modelType) {
        return modelType.getDisplayName();
    }

    @Override
    public ExportType fromString(String modelTypeName) {
        return ExportType.valueOf(modelTypeName);
    }
}