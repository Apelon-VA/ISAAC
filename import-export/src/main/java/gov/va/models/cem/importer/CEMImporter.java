/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.models.cem.importer;

import gov.va.isaac.gui.AppContext;
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

    public CEMImporter(AppContext appContext) throws ValidationException, IOException {
        super(appContext);
    }

    public ConceptChronicleBI importModel(final File file) throws Exception {
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
        String focusConceptUuid = cetypeNode.getAttributes().getNamedItem("conceptid").getTextContent();  // "215fd598-e21d-3e27-a0a2-8e23b1b36dfc"
        ConceptChronicleBI focusConcept = getDataStore().getConcept(UUID.fromString(focusConceptUuid));
        LOG.info("focusConcept: " + focusConcept.toString());
        String type = cetypeNode.getAttributes().getNamedItem("name").getTextContent();
        addStringAnnotation(focusConcept, CEMMetadataBinding.CEM_TYPE_REFSET, type);
        LOG.info("type: " + type);
        String kind = cetypeNode.getAttributes().getNamedItem("kind").getTextContent();
        LOG.info("kind: " + kind);

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
                addMemberInConstraintRefset(qual, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, qual.getNid(), qualCard);
                LOG.info("qual: " + qualName + qualType + qualCard);
                break;
            case "mod":
                String modName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String modType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String modCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI mod = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_MOD, modType);
                addMemberInConstraintRefset(mod, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, mod.getNid(), modCard);
                LOG.info("mod: " + modName + modType + modCard);
                break;
            case "att":
                String attName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String attType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String attCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI att = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_ATTR, attType);
                addMemberInConstraintRefset(att, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, att.getNid(), attCard);
                LOG.info("att: " + attName + attType + attCard);
                break;
            case "item":
                String itemName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                String itemType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                String itemCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                RefexChronicleBI item = addMemberInCompositionRefset(focusConcept, CEMMetadataBinding.CEM_ITEM, itemType);
                addMemberInConstraintRefset(item, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, item.getNid(), itemCard);
                LOG.info("item: " + itemName + itemType + itemCard);
                break;
            case "constraint":
                String path = loopNode.getAttributes().getNamedItem("path").getTextContent();
                String value = loopNode.getAttributes().getNamedItem("value").getTextContent();
                if (path.endsWith("domain")) {
                    addMemberInConstraintRefset(focusConcept, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                            focusConcept.getNid(), value);
                }
                if (path.endsWith("card")) {
                    addMemberInConstraintRefset(focusConcept, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT,
                            focusConcept.getNid(), value);
                }
                if (path.endsWith("normal")) {
                    addMemberInConstraintRefset(focusConcept, CEMMetadataBinding.CEM_NORMAL_CONSTRAINT,
                            focusConcept.getNid(), value);
                }
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

    /**
     * Method to import hard-coded demo data.
     * Used for Sprint 2 development.
     */
    @Deprecated
    public void importDemoCEMData() throws Exception {

        ConceptChronicleBI bloodPressureTaking = getDataStore().getConcept(UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc"));
        LOG.info("BloodPressureTaking concept:" + bloodPressureTaking.toString());

        RefexChronicleBI modelDataMember = addMemberInDataRefset(bloodPressureTaking, CEMMetadataBinding.CEM_PQ);
        addStringAnnotation(bloodPressureTaking, CEMMetadataBinding.CEM_TYPE_REFSET, "DiastolicBloodPressureMeas");
        addStringAnnotation(bloodPressureTaking, CEMMetadataBinding.CEM_KEY_REFSET, "DiastolicBloodPressure_KEY_ECID");
        addStringAnnotation(bloodPressureTaking, CEMMetadataBinding.CEM_INFO_REFSET, "DiastolicBloodPressureMeas model is a measurement model."
                + " A measurement model holds a \"question\" or a \"test\" in the key and holds a numeric value (PQ) answer in data."
                + " For example, in the HeartRateMeas model, the \"question\" or the \"test\" is \"What is the heart rate measurement?\","
                + " and the \"answer\" in data can be \"100 bpm\".");
        RefexChronicleBI methodDevice = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "MethodDevice");
        addMemberInConstraintRefset(methodDevice, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, methodDevice.getNid(), "0-1");

        RefexChronicleBI bodyLocationPrecoord = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "BodyLocationPrecoord");
        addMemberInConstraintRefset(bodyLocationPrecoord, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, bodyLocationPrecoord.getNid(), "0-1");

        RefexChronicleBI bodyPosition = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "BodyPosition");
        addMemberInConstraintRefset(bodyPosition, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, bodyPosition.getNid(), "0-1");

        RefexChronicleBI abnormalFlag = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "AbnormalFlag");
        addMemberInConstraintRefset(abnormalFlag, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, abnormalFlag.getNid(), "0-1");

        RefexChronicleBI deltaFlag = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "DeltaFlag");
        addMemberInConstraintRefset(deltaFlag, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, deltaFlag.getNid(), "0-1");

        RefexChronicleBI referenceRangeNar = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "ReferenceRangeNar");
        addMemberInConstraintRefset(referenceRangeNar, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, referenceRangeNar.getNid(), "0-1");

        RefexChronicleBI relativeTemporalContext = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "RelativeTemporalContext");
        addMemberInConstraintRefset(relativeTemporalContext, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, relativeTemporalContext.getNid(), "0-M");

        RefexChronicleBI subject = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_MOD, "Subject");
        addMemberInConstraintRefset(subject, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, subject.getNid(), "0-1");

        RefexChronicleBI observed = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_ATTR, "Observed");
        addMemberInConstraintRefset(observed, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, observed.getNid(), "0-1");

        RefexChronicleBI reportedReceived = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_ATTR, "ReportedReceived");
        addMemberInConstraintRefset(reportedReceived, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, reportedReceived.getNid(), "0-1");

        RefexChronicleBI verified = addMemberInCompositionRefset(bloodPressureTaking, CEMMetadataBinding.CEM_ATTR, "Verified");
        addMemberInConstraintRefset(verified, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, verified.getNid(), "0-1");

        addMemberInConstraintRefset(abnormalFlag, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_CODE_FIELD.getNid(), "AbnormalFlagNumericNom_DOMAIN_ECID");
        addMemberInConstraintRefset(deltaFlag, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_CODE_FIELD.getNid(), "DeltaFlagNumericNom_DOMAIN_ECID");
        addMemberInConstraintRefset(methodDevice, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_CODE_FIELD.getNid(), "BloodPressureMeasurementDevice_DOMAIN_ECID");
        addMemberInConstraintRefset(modelDataMember, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_UNIT_FIELD.getNid(), "PressureUnits_DOMAIN_ECID");
        addMemberInConstraintRefset(modelDataMember, CEMMetadataBinding.CEM_NORMAL_CONSTRAINT,
                CEMMetadataBinding.CEM_UNIT_FIELD.getNid(), "MilliMetersOfMercury_ECID");

        // TODO: Links
        getDataStore().addUncommitted(bloodPressureTaking);
        getDataStore().commit();

        LOG.info("Long form after commit:" + bloodPressureTaking.toLongString());
    }

    public RefexChronicleBI addMemberInDataRefset(ConceptChronicleBI concept, ConceptSpec data)
            throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                concept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_DATA_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
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
                IdDirective.GENERATE_RANDOM,
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
                IdDirective.GENERATE_RANDOM,
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
                IdDirective.GENERATE_RANDOM,
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
