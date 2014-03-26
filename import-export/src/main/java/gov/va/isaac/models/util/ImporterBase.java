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
import java.util.Map;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Base class containing common methods for importing information models.
 *
 * @author ocarlsen
 */
public class ImporterBase extends CommonBase {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterBase.class);

    private final Map<ConceptSpec, ConceptChronicleBI> specConceptMap;

    protected ImporterBase() {
        super();
        this.specConceptMap = Maps.newHashMap();
    }

    protected final TerminologyBuilderBI getBuilder() {
        return WBUtility.getBuilder();
    }

    protected ConceptChronicleBI getConcept(ConceptSpec refsetSpec)
            throws IOException, ValidationException {
        ConceptChronicleBI refsetConcept = specConceptMap.get(refsetSpec);
        if (refsetConcept == null) {
            refsetConcept = getDataStore().getConcept(refsetSpec.getNid());
            specConceptMap.put(refsetSpec, refsetConcept);
        }
        return refsetConcept;
    }

    protected RefexChronicleBI<?> addMemberAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.MEMBER,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addStrExtensionMember(ConceptChronicleBI focusConcept,
            ConceptSpec refsetSpec, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                focusConcept.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        ConceptChronicleBI refsetConcept = getConcept(refsetSpec);
        if (! refsetConcept.isAnnotationStyleRefex()) {
            getDataStore().addUncommitted(refsetConcept);
        } else {
            getDataStore().addUncommitted(focusConcept);
        }

        return newRefex;
    }

    protected RefexChronicleBI<?> addStrExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addCidExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, ConceptSpec conceptExtension)
            throws IOException, InvalidCAB, ContradictionException {
        return addCidExtensionAnnotation(focusComponent, refsetSpec, conceptExtension.getNid());
    }

    protected RefexChronicleBI<?> addCidExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int conceptExtensionNid)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, conceptExtensionNid);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addCidStrExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, ConceptSpec componentExtension, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        return addCidStrExtensionAnnotation(focusComponent,
                refsetSpec, componentExtension.getNid(), stringExtension);
    }

    protected RefexChronicleBI<?> addCidStrExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int componentExtensionNid, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtensionNid);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addIntExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int intExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.INT,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.INTEGER_EXTENSION_1, intExtensionValue);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addCidIntExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, ConceptSpec componentExtension, int intExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        return addCidIntExtensionAnnotation(focusComponent,
                refsetSpec, componentExtension.getNid(), intExtensionValue);
    }

    protected RefexChronicleBI<?> addCidBooleanExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int componentExtensionNid, boolean booleanExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_BOOLEAN,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtensionNid);
        newRefexCab.put(ComponentProperty.BOOLEAN_EXTENSION_1, booleanExtensionValue);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addCidIntExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int componentExtensionNid, int intExtensionValue)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_INT,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtensionNid);
        newRefexCab.put(ComponentProperty.INTEGER_EXTENSION_1, intExtensionValue);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addCidCidExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int componentExtension1Nid, int componentExtension2Nid)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_CID,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtension1Nid);
        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, componentExtension2Nid);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    protected RefexChronicleBI<?> addCidCidStrExtensionAnnotation(ComponentChronicleBI<?> focusComponent,
            ConceptSpec refsetSpec, int componentExtension1Nid, int componentExtension2Nid, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_CID_STR,
                focusComponent.getPrimordialUuid(),
                refsetSpec.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.INCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtension1Nid);
        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, componentExtension2Nid);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);
        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }
}
