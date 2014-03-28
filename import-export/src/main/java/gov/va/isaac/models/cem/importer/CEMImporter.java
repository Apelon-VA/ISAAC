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
package gov.va.isaac.models.cem.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.ImportHandler;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.CEMInformationModel.Composition;
import gov.va.isaac.models.cem.CEMInformationModel.Constraint;
import gov.va.isaac.models.cem.CEMXmlConstants;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

/**
 * Class for importing a CEM model from an XML {@link File}.
 *
 * @author alo
 * @author ocarlsen
 */
@SuppressWarnings("rawtypes")
public class CEMImporter extends ImporterBase implements ImportHandler, CEMXmlConstants {

    private static final Logger LOG = LoggerFactory.getLogger(CEMImporter.class);

    public CEMImporter() {
        super();
    }

    @Override
    public ConceptChronicleBI importModel(File file) throws Exception {
        LOG.info("Preparing to import CEM model from: " + file.getName());

        // Make sure in background thread.
        FxUtils.checkBackgroundThread();

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

        // Load DOM tree from file.
        Node domRoot = loadModel(file);

        // Parse into CEM model.
        CEMInformationModel infoModel = createInformationModel(domRoot);

        // Annotate focusConcept with Refset members.
        annotateWithRefsets(focusConcept, infoModel);

        getDataStore().addUncommitted(focusConcept);
        getDataStore().commit();

        LOG.debug("Long form after commit:" + focusConcept.toLongString());
        LOG.info("Ending import of CEM model from: " + file.getName());

        return focusConcept;
    }

    @SuppressWarnings("unused")
    private void annotateWithRefsets(ConceptChronicleBI focusConcept,
            CEMInformationModel infoModel)
            throws IOException, InvalidCAB, ContradictionException {

        // CEM Type refset.
        String type = infoModel.getName();
        addMemberInTypeRefset(focusConcept, type);

        // CEM Key refset.
        String key = infoModel.getKey();
        addMemberInKeyRefset(focusConcept, key);

        // CEM Data refset.
        ConceptSpec dataType = infoModel.getDataType();
        addMemberInDataRefset(focusConcept, dataType);

        // CEM Composition refset : Quals.
        List<Composition> quals = infoModel.getQualComponents();
        for (Composition qual : quals) {
            String component = qual.getComponent();
            RefexChronicleBI qualRefex = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_QUAL, component);

            // Constraint?
            Constraint constraint = qual.getConstraint();
            if (constraint !=  null) {
                String path = constraint.getPath();
                String value = constraint.getValue();

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI constraintRefex = addMemberInConstraintsRefset(qualRefex);
                RefexChronicleBI pathRefex = addRefexInStrExtensionRefset(constraintRefex,
                        CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, path);
                RefexChronicleBI qalueRefex = addRefexInStrExtensionRefset(constraintRefex,
                        CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, value);
            }

            // Value?
            String value = qual.getValue();
            if (value != null) {
                RefexChronicleBI qualCompRefex = addMemberInValueRefset(qualRefex, value);
            }
        }

        // CEM Composition refset : Mods.
        List<Composition> mods = infoModel.getModComponents();
        for (Composition mod : mods) {
            String component = mod.getComponent();
            RefexChronicleBI modRefex = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_MOD, component);

            // Constraint?
            Constraint constraint = mod.getConstraint();
            if (constraint !=  null) {
                String path = constraint.getPath();
                String value = constraint.getValue();

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI constraintRefex = addMemberInConstraintsRefset(modRefex);
                RefexChronicleBI pathRefex = addRefexInStrExtensionRefset(constraintRefex,
                        CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, path);
                RefexChronicleBI qalueRefex = addRefexInStrExtensionRefset(constraintRefex,
                        CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, value);
            }

            // Value?
            String value = mod.getValue();
            if (value != null) {
                RefexChronicleBI modCompRefex = addMemberInValueRefset(modRefex, value);
            }
        }

        // CEM Composition refset : Atts.
        List<Composition> atts = infoModel.getAttComponents();
        for (Composition att : atts) {
            String component = att.getComponent();
            RefexChronicleBI attRefex = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_ATT, component);

            // Constraint?
            Constraint constraint = att.getConstraint();
            if (constraint !=  null) {
                String path = constraint.getPath();
                String value = constraint.getValue();

                // Simulate String-String refset for CEM Constraints.
                RefexChronicleBI constraintRefex = addMemberInConstraintsRefset(attRefex);
                RefexChronicleBI pathRefex = addRefexInStrExtensionRefset(constraintRefex,
                        CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, path);
                RefexChronicleBI qalueRefex = addRefexInStrExtensionRefset(constraintRefex,
                        CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, value);
            }

            // Value?
            String value = att.getValue();
            if (value != null) {
                RefexChronicleBI attCompRefex = addMemberInValueRefset(attRefex, value);
            }
        }

        // CEM Constraints refset.
        List<Constraint> constraints = infoModel.getConstraints();
        for (Constraint constraint : constraints) {
            String path = constraint.getPath();
            String value = constraint.getValue();

            // Simulate String-String refset for CEM Constraints.
            RefexChronicleBI constraintRefex = addMemberInConstraintsRefset(focusConcept);
            RefexChronicleBI constraintPathRefex = addRefexInStrExtensionRefset(constraintRefex,
                    CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, path);
            RefexChronicleBI constraintValueRefex = addRefexInStrExtensionRefset(constraintRefex,
                    CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, value);
        }
    }

    private CEMInformationModel createInformationModel(Node rootNode) {

        // Look for CETYPE child node.
        Node cetypeNode = null;
        NodeList childNodes = rootNode.getChildNodes();
        for (int nodeCount = 0; nodeCount < childNodes.getLength(); nodeCount++) {
            Node childNode = childNodes.item(nodeCount);
            LOG.debug("childNode : " + nodeCount + " - " + childNode.getNodeName());

            if (childNode.getNodeName().equals(CETYPE)) {
                cetypeNode = childNode;
                break;
            }
        }

        // Sanity check.
        Preconditions.checkNotNull(cetypeNode, "No CETYPE child node in XML file!");
        LOG.debug("cetype: " + cetypeNode.getNodeName());

        // Parse CETYPE node attributes.
        String name = cetypeNode.getAttributes().getNamedItem(NAME).getTextContent();
        LOG.info("name: " + name);

        CEMInformationModel infoModel = new CEMInformationModel(name);

        // Iterate through CETYPE node children and process.
        NodeList cetypeChildren = cetypeNode.getChildNodes();
        for (int nodeCount = 0; nodeCount < cetypeChildren.getLength(); nodeCount++) {
            Node loopNode = cetypeChildren.item(nodeCount);
            LOG.debug("loopNode : " + nodeCount + " - " + loopNode.getNodeName());

            switch (loopNode.getNodeName()) {
            case KEY:
                String key = loopNode.getAttributes().getNamedItem(CODE).getTextContent();
                LOG.info("key: " + key);
                infoModel.setKey(key);
                break;
            case DATA:
                String data = loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
                LOG.info("data: " + data);
                switch (data.toUpperCase()) {
                case PQ:
                    infoModel.setDataType(CEMMetadataBinding.CEM_PQ);
                    break;
                case CD:
                    infoModel.setDataType(CEMMetadataBinding.CEM_CD);
                    break;
                case CWE:  // Another way of specifying CEM CD data type.
                    infoModel.setDataType(CEMMetadataBinding.CEM_CD);
                    break;
                    //TODO: add the rest of data types
                }
                break;
            case QUAL:
                String qualName = loopNode.getAttributes().getNamedItem(NAME).getTextContent();
                String qualType = loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
                String qualCard = loopNode.getAttributes().getNamedItem(CARD).getTextContent();
                String qualCompValue = loopNode.getTextContent();
                LOG.info(String.format("mod: %s %s %s %s", qualName, qualType, qualCard, qualCompValue));

                Composition qual = infoModel.addQualComponent(qualType);

                // Cardinality constraint.
                if (qualCard != null && qualCard.length() > 0) {
                    Constraint constraint = new Constraint(CARD, qualCard);
                    qual.setConstraint(constraint);
                }

                // Hack for Stan Huff demo.
                if (qualCompValue != null && qualCompValue.length() > 0) {
                    qual.setValue(qualCompValue);
                }

                break;
            case MOD:
                String modName = loopNode.getAttributes().getNamedItem(NAME).getTextContent();
                String modType = loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
                String modCard = loopNode.getAttributes().getNamedItem(CARD).getTextContent();
                String modCompValue = loopNode.getTextContent();
                LOG.info(String.format("mod: %s %s %s %s", modName, modType, modCard, modCompValue));

                Composition mod = infoModel.addModComponent(modType);

                // Cardinality constraint.
                if (modCard != null && modCard.length() > 0) {
                    Constraint constraint = new Constraint(CARD, modCard);
                    mod.setConstraint(constraint);
                }

                // Hack for Stan Huff demo.
                if (modCompValue != null && modCompValue.length() > 0) {
                   mod.setValue(modCompValue);
                }

                break;
            case ATT:
                String attName = loopNode.getAttributes().getNamedItem(NAME).getTextContent();
                String attType = loopNode.getAttributes().getNamedItem(TYPE).getTextContent();
                String attCard = loopNode.getAttributes().getNamedItem(CARD).getTextContent();
                String attCompValue = loopNode.getTextContent();
                LOG.info(String.format("att: %s %s %s %s", attName, attType, attCard, attCompValue));

                Composition att = infoModel.addAttComponent(attType);

                // Cardinality constraint.
                if (attCard != null && attCard.length() > 0) {
                    Constraint constraint = new Constraint(CARD, attCard);
                    att.setConstraint(constraint);
                }

                // Hack for Stan Huff demo.
               if (attCompValue != null && attCompValue.length() > 0) {
                    att.setValue(attCompValue);
                }

                break;
            case CONSTRAINT:
                String path = loopNode.getAttributes().getNamedItem(PATH).getTextContent();
                String value = loopNode.getAttributes().getNamedItem(VALUE).getTextContent();
                LOG.info(String.format("constraint: %s %s", path, value));

                Constraint constraint = new Constraint(path, value);
                infoModel.addConstraint(constraint);

                break;
            }
        }

        return infoModel;
    }

    private RefexChronicleBI addMemberInValueRefset(RefexChronicleBI focusComponent, String value)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInStrExtensionRefset(focusComponent, CEMMetadataBinding.CEM_VALUE_REFSET,
                value);
    }

    private RefexChronicleBI addMemberInDataRefset(ConceptChronicleBI focusConcept,
            ConceptSpec conceptExtension)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInCidExtensionRefset(focusConcept, CEMMetadataBinding.CEM_DATA_REFSET,
                conceptExtension);
    }

    private RefexChronicleBI addMemberInCompositionRefset(ConceptChronicleBI focusConcept,
            ConceptSpec componentExtension, String stringExtension)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInCidStrExtensionRefset(focusConcept, CEMMetadataBinding.CEM_COMPOSITION_REFSET,
                componentExtension, stringExtension);
    }

    private RefexChronicleBI addMemberInConstraintsRefset(ComponentChronicleBI focusComponent)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInMemberRefset(focusComponent, CEMMetadataBinding.CEM_CONSTRAINTS_REFSET);
    }

    private RefexChronicleBI addMemberInKeyRefset(ConceptChronicleBI focusConcept,
            String key)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInStrExtensionRefset(focusConcept, CEMMetadataBinding.CEM_KEY_REFSET, key);

    }

    private RefexChronicleBI addMemberInTypeRefset(ConceptChronicleBI focusConcept,
            String type)
            throws IOException, InvalidCAB, ContradictionException {
        return addRefexInStrExtensionRefset(focusConcept, CEMMetadataBinding.CEM_TYPE_REFSET, type);

    }

    private Node loadModel(File file) throws ParserConfigurationException, SAXException, IOException {

        // Parse XML file.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);

        Node rootNode = document.getFirstChild();
        LOG.info("rootNode: " + rootNode.getNodeName());

        // Sanity check.
        Preconditions.checkState(rootNode.getNodeName().toLowerCase().equals(CEML),
                "No CEML root node in XML file! " + file.getName());

        LOG.info("File OK: " + file.getName());

        return rootNode;
    }
}
