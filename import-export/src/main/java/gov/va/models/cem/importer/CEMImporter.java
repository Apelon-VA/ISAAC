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
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
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

        // Parse CETYPE node attributes.
        String focusConceptUuid = "215fd598-e21d-3e27-a0a2-8e23b1b36dfc";
        ConceptChronicleBI focusConcept = getDataStore().getConcept(UUID.fromString(focusConceptUuid));
        LOG.info("focusConcept: " + focusConcept.toString());
        String type = cetypeNode.getAttributes().getNamedItem("name").getTextContent();
        addStringAnnotation(focusConcept, CEMMetadataBinding.CEM_TYPE_REFSET, type);
        LOG.info("type: " + type);

        // Iterate through CETYPE node children and process.
        NodeList cetypeChildren = cetypeNode.getChildNodes();
        for (int nodeCount = 0; nodeCount < cetypeChildren.getLength(); nodeCount++) {
            Node loopNode = cetypeChildren.item(nodeCount);
            LOG.debug("loopNode : " + nodeCount + " - " + loopNode.getNodeName());

            switch (loopNode.getNodeName()) {
            case "key":
                String key = loopNode.getAttributes().getNamedItem("code").getTextContent();
                addStringAnnotation(focusConcept, CEMMetadataBinding.CEM_KEY_REFSET, key);
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

                // TODO: Implement as in Jay's spreadsheet.

                LOG.info("qual: " + qualName + qualType + qualCard);
                break;
            case "mod":
                String modName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String modType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String modCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI mod = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_MOD, modType);

                // TODO: Implement as in Jay's spreadsheet.

                LOG.info("mod: " + modName + modType + modCard);
                break;
            case "att":
                String attName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String attType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String attCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI att = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_ATTR, attType);

                // TODO: Implement as in Jay's spreadsheet.

                LOG.info("att: " + attName + attType + attCard);
                break;
            case "constraint":
                String path = loopNode.getAttributes().getNamedItem("path").getTextContent();
                String value = loopNode.getAttributes().getNamedItem("value").getTextContent();

                // TODO: Implement as in Jay's spreadsheet.

                LOG.info("constraint: " + path + value);
                break;
            }
        }

        getDataStore().addUncommitted(focusConcept);
        getDataStore().commit();

        LOG.debug("Long form after commit:" + focusConcept.toLongString());
        LOG.info("Ending import of CEM model from: " + file.getName());

        return focusConcept;
    }

    public RefexChronicleBI addMemberInDataRefset(ConceptChronicleBI concept, ConceptSpec data)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                concept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_DATA_REFSET.getUuids()[0],
                IdDirective.GENERATE_HASH,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, data.getNid());

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addStringAnnotation(ConceptChronicleBI concept, ConceptSpec refset, String value)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                concept.getPrimordialUuid(),
                getDataStore().getUuidPrimordialForNid(refset.getNid()),
                IdDirective.GENERATE_HASH,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, value);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex string UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addMemberInCompositionRefset(ConceptChronicleBI concept, ConceptSpec type, String componentKey)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                concept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_COMPOSITION_REFSET.getUuids()[0],
                IdDirective.GENERATE_HASH,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, type.getNid());
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, componentKey);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex composition UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addMemberInConstraintRefset(ComponentChronicleBI cemMember, ConceptSpec constraint, int focusNid, String value)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_CID_STR,
                cemMember.getPrimordialUuid(),
                CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.getUuids()[0],
                IdDirective.GENERATE_HASH,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, constraint.getNid());
        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, focusNid);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, value);

        RefexChronicleBI<?> newRefex = getBuilder().constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex constraint UUID:" + newRefex.getPrimordialUuid());

        cemMember.addAnnotation(newRefex);

        return newRefex;
    }
}
