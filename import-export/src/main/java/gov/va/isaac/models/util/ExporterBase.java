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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.models.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Base class containing common methods for exporting information models.
 *
 * @author ocarlsen
 */
public abstract class ExporterBase extends CommonBase {

    protected ExporterBase() throws ValidationException, IOException {
        super();
    }

    protected abstract Logger getLogger();

    /**
     * Helper method to filter the specified {@link Collection} of annotations
     * by calling {@link #filterAnnotations(Collection, ConceptSpec, Class)},
     * and then perform a sanity check that there is at most one.
     *
     * @return The sole annotation, or {@code null} if none found.
     * @throws An {@link IllegalStateException} if there is more than one annotation found.
     */
    protected <T> T getSingleAnnotation(
            Collection<? extends RefexChronicleBI<?>> annotations,
            ConceptSpec refsetSpec, Class<T> type)
            throws ValidationException, IOException {

        // Filter members of the specified refset.
        List<T> filtered = filterAnnotations(annotations, refsetSpec, type);

        // Should be 0-1.
        int filteredCount = filtered.size();
        // This check is prohibiting export following the Stan Huff demo.
        // It means something is different between the way the refset-view
        // is using refsets and the way import-export is.
        // Log it for now, resolve later.  (See artf229216.)
//        Preconditions.checkState(filteredCount <= 1,
        getLogger().warn(
                "Expected 0-1 annotations for refset nid " + refsetSpec.getNid() +
                ", found " + filteredCount);


        // Return annotation, or null if none.
        if (filteredCount == 0) {
            return null;
        } else {
            return filtered.get(0);
        }
    }

    /**
     * Helper method to iterate through the specified {@link Collection} of
     * annotations, keeping those belonging to the specified {@link ConceptSpec}.
     * Also performs a sanity check that annotations are instances of the
     * specified {@code type}.
     *
     * @return A new {@link List} of filtered annotations. May be empty.
     * @throws An {@link IllegalStateException} if annotations are not
     *         instances of the specified {@code type}.
     */
    protected <T> List<T> filterAnnotations(Collection<? extends RefexChronicleBI<?>> annotations, ConceptSpec refsetSpec,
            Class<T> type) throws ValidationException, IOException {
        List<T> filtered = Lists.newArrayList();

        for (RefexChronicleBI<?> annotation : annotations) {

            // Filter on specified refset.
            if (annotation.getAssemblageNid() != refsetSpec.getNid()) {
                continue;
            }

            // Expect member type.
            Preconditions.checkState(type.isAssignableFrom(annotation.getClass()),
                    "Expected " + type + "!  Actual type is " + annotation.getClass());
            @SuppressWarnings("unchecked")
            T member = (T) annotation;

            // What we want.
            filtered.add(member);
        }

        return filtered;
    }

    protected Collection<? extends RefexChronicleBI<?>> getLatestAnnotations(ComponentChronicleBI<?> conceptChronicle)
            throws IOException, ContradictionException {

        // Get latest version.
        ComponentVersionBI latestVersion = conceptChronicle.getVersion(getVC());

        // Print out annotations.
        return latestVersion.getAnnotations();
    }
}
