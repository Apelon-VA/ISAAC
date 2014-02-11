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
package gov.va.models.cem.importer;

import gov.va.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

/**
 * Class for importing a CEM model from a {@link File}.
 *
 * @author alo
 * @author ocarlsen
 */
@SuppressWarnings("rawtypes")
public class CEMImporter extends ImporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(CEMImporter.class);

    public CEMImporter() throws ValidationException, IOException {
        super();
    }

    @SuppressWarnings("unused")
    public ConceptChronicleBI importModel(File file) throws Exception {
        LOG.info("Preparing to import CEM model from: " + file.getName());

        // Parse XML file.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);

        Node rootNode = document.getFirstChild();
        LOG.info("rootNode: " + rootNode.getNodeName());

        // Sanity check.
        Preconditions.checkState(rootNode.getNodeName().toLowerCase().equals("ceml"),
                "No CEML root node in XML file! " + file.getName());

        LOG.info("File OK: " + file.getName());

        // Look for CETYPE child node.
        Node cetypeNode = null;
        NodeList childNodes = rootNode.getChildNodes();
        for (int nodeCount = 0; nodeCount < childNodes.getLength(); nodeCount++) {
            Node childNode = childNodes.item(nodeCount);
            LOG.debug("childNode : " + nodeCount + " - " + childNode.getNodeName());

            if (childNode.getNodeName().equals("cetype")) {
                cetypeNode = childNode;
                break;
            }
        }

        // Sanity check.
        Preconditions.checkNotNull(cetypeNode, "No CETYPE child node in XML file! " + file.getName());

        LOG.info("cetype: " + cetypeNode.getNodeName());

        // Get focus concept.
        String focusConceptUuid = "215fd598-e21d-3e27-a0a2-8e23b1b36dfc";
        ConceptChronicleBI focusConcept = getDataStore().getConcept(UUID.fromString(focusConceptUuid));
        LOG.info("focusConcept: " + focusConcept.toString());

        // Throw exception if import already performed.
        ComponentVersionBI latestVersion = focusConcept.getVersion(getVC());
        Collection<? extends RefexChronicleBI<?>> annotations = latestVersion.getAnnotations();
        for (RefexChronicleBI<?> annotation : annotations) {
            Preconditions.checkState(annotation.getAssemblageNid() != CEMMetadataBinding.CEM_TYPE_REFSET.getNid(),
                    "CEM import has already been performed on " + focusConceptUuid);
        }

        // Parse CETYPE node attributes.
        String type = cetypeNode.getAttributes().getNamedItem("name").getTextContent();
        addMemberInRefset(focusConcept, CEMMetadataBinding.CEM_TYPE_REFSET, type);
        LOG.info("type: " + type);

        // Iterate through CETYPE node children and process.
        NodeList cetypeChildren = cetypeNode.getChildNodes();
        for (int nodeCount = 0; nodeCount < cetypeChildren.getLength(); nodeCount++) {
            Node loopNode = cetypeChildren.item(nodeCount);
            LOG.debug("loopNode : " + nodeCount + " - " + loopNode.getNodeName());

            switch (loopNode.getNodeName()) {
            case "key":
                String key = loopNode.getAttributes().getNamedItem("code").getTextContent();
                addMemberInRefset(focusConcept, CEMMetadataBinding.CEM_KEY_REFSET, key);
                LOG.info("key: " + key);
                break;
            case "data":
                String data = loopNode.getAttributes().getNamedItem("type").getTextContent();
                LOG.info("data: " + data);
                switch (data.toUpperCase()) {
                case "PQ":
                    addMemberInDataRefset(focusConcept, CEMMetadataBinding.CEM_PQ);
                    break;
                case "CD":
                    addMemberInDataRefset(focusConcept, CEMMetadataBinding.CEM_CD);
                    break;
                case "CWE":  // Another way of specifying CEM CD data type.
                    addMemberInDataRefset(focusConcept, CEMMetadataBinding.CEM_CD);
                    break;
                    //TODO: add the rest of data types
                }
                break;
            case "qual":
                String qualName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String qualType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String qualCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI qual = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_QUAL, qualType);

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI qualConstraint = addMemberInConstraintsRefset(qual);
                RefexChronicleBI qualPath = addMemberInRefset(qualConstraint, CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, "card");
                RefexChronicleBI qualValue = addMemberInRefset(qualConstraint, CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, qualCard);

                LOG.info(String.format("qual: %s %s %s", qualName, qualType, qualCard));
                break;
            case "mod":
                String modName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String modType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String modCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI mod = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_MOD, modType);

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI modConstraint = addMemberInConstraintsRefset(mod);
                RefexChronicleBI modPath = addMemberInRefset(modConstraint, CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, "card");
                RefexChronicleBI modValue = addMemberInRefset(modConstraint, CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, modCard);

                LOG.info(String.format("mod: %s %s %s", modName, modType, modCard));
                break;
            case "att":
                String attName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String attType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String attCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI att = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_ATTR, attType);

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI attConstraint = addMemberInConstraintsRefset(att);
                RefexChronicleBI attPath = addMemberInRefset(attConstraint, CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, "card");
                RefexChronicleBI attValue = addMemberInRefset(attConstraint, CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, attCard);

                LOG.info(String.format("att: %s %s %s", attName, attType, attCard));
                break;
            case "constraint":
                String path = loopNode.getAttributes().getNamedItem("path").getTextContent();
                String value = loopNode.getAttributes().getNamedItem("value").getTextContent();

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI constraint = addMemberInConstraintsRefset(focusConcept);
                RefexChronicleBI constraintPath = addMemberInRefset(constraint, CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, path);
                RefexChronicleBI constraintValue = addMemberInRefset(constraint, CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, value);

                LOG.info(String.format("constraint: %s %s", path, value));
                break;
            }
        }

        getDataStore().addUncommitted(focusConcept);
        getDataStore().commit();

        LOG.debug("Long form after commit:" + focusConcept.toLongString());
        LOG.info("Ending import of CEM model from: " + file.getName());

        return focusConcept;
    }

    public RefexChronicleBI addMemberInDataRefset(ConceptChronicleBI focusConcept,
            ConceptSpec conceptExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                focusConcept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_DATA_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, conceptExtension.getNid());

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        focusConcept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addMemberInRefset(ComponentChronicleBI focusComponent,
            ConceptSpec refsetSpec, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                focusComponent.getPrimordialUuid(),
                getDataStore().getUuidPrimordialForNid(refsetSpec.getNid()),
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex string UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addMemberInCompositionRefset(ConceptChronicleBI focusConcept,
            ConceptSpec componentExtension, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                focusConcept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_COMPOSITION_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, componentExtension.getNid());
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, stringExtension);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex composition UUID:" + newRefex.getPrimordialUuid());

        focusConcept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addMemberInConstraintsRefset(ComponentChronicleBI focusComponent)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.MEMBER,
                focusComponent.getPrimordialUuid(),
                CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex constraints UUID:" + newRefex.getPrimordialUuid());

        focusComponent.addAnnotation(newRefex);

        return newRefex;
    }
}
