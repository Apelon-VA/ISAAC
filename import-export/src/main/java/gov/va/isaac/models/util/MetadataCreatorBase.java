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

import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Abstract superclass for metadata creators.
 *
 * @author ocarlsen
 */
public abstract class MetadataCreatorBase extends CommonBase {

    protected MetadataCreatorBase() throws ValidationException, IOException {
        super();
    }

    protected static final String REFSET_CONCEPT = "7e38cd2d-6f1a-3a81-be0b-21e6090573c2";
    protected static final String REFSET_ATTRIBUTE_CONCEPT = "7e52203e-8a35-3121-b2e7-b783b34d97f2";

    /**
     * Concrete subclasses should implement to create metadata if it does not
     * already exist.  Return value should indicate as appropriate.
     * @return {@code true} if metadata was created, or {@code false} if metadata
     * already existed and so creation was bypassed.
     * @throws Exception if there is a problem creating metadata.
     */
    public abstract boolean createMetadata() throws Exception;

    /**
     * @return The {@link ConceptChronicleBI} for the specified {@link UUID},
     * or {@code null} if it has not yet been created.
     */
    protected ConceptChronicleBI getConcept(UUID uuid) throws IOException {
        ConceptChronicleBI concept = getDataStore().getConcept(uuid);
        if (concept.getDescriptions().size() == 0) {
            return null;
        }
        return concept;
    }

    protected ConceptChronicleBI createNewConcept(ConceptChronicleBI parent, String fsn,
            String prefTerm) throws IOException, InvalidCAB, ContradictionException {
        List<ConceptChronicleBI> oneParent = new ArrayList<ConceptChronicleBI>();
        oneParent.add(parent);
        return createNewConcept(oneParent, fsn, prefTerm);
    }

    protected ConceptChronicleBI createNewConcept(List<ConceptChronicleBI> parents, String fsn,
            String prefTerm) throws IOException, InvalidCAB, ContradictionException {
        LanguageCode lc = LanguageCode.EN_US;
        UUID isA = Snomed.IS_A.getUuids()[0];
        IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID[] parentsUuids = new UUID[parents.size()];
        int count = 0;
        for (ConceptChronicleBI parent : parents) {
            parentsUuids[count] = parent.getPrimordialUuid();
            count++;
        }
        ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parentsUuids);

        ConceptChronicleBI newCon = WBUtility.getBuilder().construct(newConCB);
        getDataStore().addUncommitted(newCon);

        return newCon;
    }
}
