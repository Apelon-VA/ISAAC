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
package gov.va.isaac.models.util;

import gov.va.isaac.util.WBUtility;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class containing common methods for importing information models.
 *
 * @author ocarlsen
 */
public class ImporterBase extends CommonBase {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterBase.class);

    protected ImporterBase() throws ValidationException, IOException {
        super();
    }

    protected final TerminologyBuilderBI getBuilder() {
        return WBUtility.getBuilder();
    }

    protected RefexChronicleBI<?> addRefexInMemberRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.MEMBER,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex constraints UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }


    protected RefexChronicleBI<?> addRefexInStrExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex string UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addRefexInCidExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, ConceptSpec conceptExtension)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInCidExtensionRefset(focusComponent, refsetConcept, conceptExtension.getNid());
    }

    protected RefexChronicleBI<?> addRefexInCidExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, int conceptExtensionNid)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, conceptExtensionNid);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addRefexInCidStrExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, ConceptSpec componentExtension, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInCidStrExtensionRefset(focusComponent,
                refsetConcept, componentExtension.getNid(), stringExtension);
    }

    protected RefexChronicleBI<?> addRefexInCidStrExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, int componentExtensionNid, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtensionNid);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex composition UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addRefexInIntExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, int intExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.INT,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.INTEGER_EXTENSION_1, intExtensionValue);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex composition UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addRefexInCidIntExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, ConceptSpec componentExtension, int intExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInCidIntExtensionRefset(focusComponent,
                refsetConcept, componentExtension.getNid(), intExtensionValue);
    }

    protected RefexChronicleBI<?> addRefexInCidIntExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, int componentExtensionNid, int intExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_INT,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtensionNid);
        newRefexCab.put(ComponentProperty.INTEGER_EXTENSION_1, intExtensionValue);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex composition UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addRefexInCidCidExtensionRefset(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetConcept, int componentExtension1Nid, int componentExtension2Nid)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_CID,
                focusComponent.getPrimordialUuid(),
                refsetConcept.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtension1Nid);
        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, componentExtension2Nid);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex composition UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }
}
