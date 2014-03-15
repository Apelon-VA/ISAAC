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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Common superclass for metadata bindings.
 *
 * @author ocarlsen
 */
public class MetadataBindingBase {

    public static List<ConceptSpec> getAll(Class<?> clazz) {
        try {
            ArrayList<ConceptSpec> allConceptSpec = new ArrayList<>();

            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers())
                        && field.getType() == ConceptSpec.class) {
                    allConceptSpec.add((ConceptSpec) field.get(null));
                }
            }

            return allConceptSpec;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected!", e);
        }
    }

}
