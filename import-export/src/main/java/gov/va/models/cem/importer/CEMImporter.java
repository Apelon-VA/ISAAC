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

import gov.va.isaac.gui.ExtendedAppContext;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author alo
 */
public class CEMImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CEMImporter.class);

    @Inject
    private BdbTerminologyStore ds;
    private TerminologyBuilderBI builder;

    public CEMImporter()
    {
        Hk2Looker.get().inject(this);
    }
    
    public void ImportCEMModel(File file) throws Exception {
        LOG.info("Preparing to import CEM model from: " + file.getName());
        getBuilder();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);

        Node rootNode = document.getFirstChild();
        LOG.info("rootNode: " + rootNode.getNodeName());
        if (!rootNode.getNodeName().toLowerCase().equals("ceml")) {
            LOG.warn("No CEML root node in XML file! " + file.getName());
        } else {
            LOG.info("File OK: " + file.getName());
            Node cetype = null;
            int nodeCount = 0;
            while (rootNode.getChildNodes().item(nodeCount) != null) {
                //LOG.info("childNode : " + nodeCount + " - " + rootNode.getChildNodes().item(nodeCount).getNodeName());
                if (rootNode.getChildNodes().item(nodeCount).getNodeName().equals("cetype")) {
                    cetype = rootNode.getChildNodes().item(nodeCount);
                }
                nodeCount++;
            }
            LOG.info("cetype: " + cetype.getNodeName());
            //"215fd598-e21d-3e27-a0a2-8e23b1b36dfc"
            String focusConceptUuid = cetype.getAttributes().getNamedItem("conceptid").getTextContent();
            ConceptChronicleBI focusConcept = ds.getConcept(UUID.fromString(focusConceptUuid));
            LOG.info("Focus concept:" + focusConcept.toString());
            String type = cetype.getAttributes().getNamedItem("name").getTextContent();
            addStringAnnotation(focusConcept, CEMMetadataBinding.CEM_TYPE_REFSET, type);
            LOG.info("type: " + type);
            String kind = cetype.getAttributes().getNamedItem("kind").getTextContent();
            LOG.info("kind: " + kind);
            nodeCount = 0;
            while (cetype.getChildNodes().item(nodeCount) != null) {
                Node loopNode = cetype.getChildNodes().item(nodeCount);
                //LOG.info("childNode : " + nodeCount + " - " + loopNode.getNodeName());
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
                             case "CWE":
                                addMemberInDataRefset(focusConcept, CEMMetadataBinding.CEM_CD);
                                break;
                            //TODO: add the rest of data types
                        }
                        break;
                    case "qual":
                        String qualName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                        String qualType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                        String qualCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                        RefexChronicleBI qual = addComponent(focusConcept, CEMMetadataBinding.CEM_QUAL, qualType);
                        addConstraint(qual, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, qual.getNid(), qualCard);
                        LOG.info("qual: " + qualName + qualType + qualCard);
                        break;
                    case "mod":
                        String modName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                        String modType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                        String modCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                        RefexChronicleBI mod = addComponent(focusConcept, CEMMetadataBinding.CEM_QUAL, modType);
                        addConstraint(mod, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, mod.getNid(), modCard);
                        LOG.info("mod: " + modName + modType + modCard);
                        break;
                    case "att":
                        String attName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                        String attType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                        String attCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                        RefexChronicleBI att = addComponent(focusConcept, CEMMetadataBinding.CEM_QUAL, attType);
                        addConstraint(att, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, att.getNid(), attCard);
                        LOG.info("att: " + attName + attType + attCard);
                        break;
                    case "item":
                        String itemName = loopNode.getAttributes().getNamedItem("name").getTextContent();
                        String itemType = loopNode.getAttributes().getNamedItem("type").getTextContent();
                        String itemCard = loopNode.getAttributes().getNamedItem("card").getTextContent();
                        RefexChronicleBI item = addComponent(focusConcept, CEMMetadataBinding.CEM_QUAL, itemType);
                        addConstraint(item, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, item.getNid(), itemCard);
                        LOG.info("item: " + itemName + itemType + itemCard);
                        break;
                    case "constraint":
                        String path = loopNode.getAttributes().getNamedItem("path").getTextContent();
                        String value = loopNode.getAttributes().getNamedItem("value").getTextContent();
                        if (path.endsWith("domain")) {
                            addConstraint(focusConcept, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT, focusConcept.getNid(), value);
                        }
                        if (path.endsWith("card")) {
                            addConstraint(focusConcept, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, focusConcept.getNid(), value);
                        }
                        if (path.endsWith("normal")) {
                            addConstraint(focusConcept, CEMMetadataBinding.CEM_NORMAL_CONSTRAINT, focusConcept.getNid(), value);
                        }
                        LOG.info("constraint: " + path + value);
                        break;
                }
                nodeCount++;
            }
            ds.addUncommitted(focusConcept);
            ds.commit();

            LOG.info("Long form after commit:" + focusConcept.toLongString());
        }

    }

    public static void main(String[] args) throws Exception {
        File file = new File("DiastolicBloodPressureMeas-ceml.xml");

    }

    public void importDemoCEMData() throws Exception {

        ConceptChronicleBI bloodPressureTaking = ds.getConcept(UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc"));
        LOG.info("BloodPressureTaking concept:" + bloodPressureTaking.toString());

        RefexChronicleBI modelDataMember = addMemberInDataRefset(bloodPressureTaking, CEMMetadataBinding.CEM_PQ);
        addStringAnnotation(bloodPressureTaking, CEMMetadataBinding.CEM_TYPE_REFSET, "DiastolicBloodPressureMeas");
        addStringAnnotation(bloodPressureTaking, CEMMetadataBinding.CEM_KEY_REFSET, "DiastolicBloodPressure_KEY_ECID");
        addStringAnnotation(bloodPressureTaking, CEMMetadataBinding.CEM_IFO_REFSET, "DiastolicBloodPressureMeas model is a measurement model. A measurement model holds a \"question\" or a \"test\" in the key and holds a numeric value (PQ) answer in data. For example, in the HeartRateMeas model, the \"question\" or the \"test\" is \"What is the heart rate measurement?\", and the \"answer\" in data can be \"100 bpm\".");
        RefexChronicleBI methodDevice = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "MethodDevice");
        addConstraint(methodDevice, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, methodDevice.getNid(), "0-1");

        RefexChronicleBI bodyLocationPrecoord = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "BodyLocationPrecoord");
        addConstraint(bodyLocationPrecoord, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, bodyLocationPrecoord.getNid(), "0-1");

        RefexChronicleBI bodyPosition = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "BodyPosition");
        addConstraint(bodyPosition, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, bodyPosition.getNid(), "0-1");

        RefexChronicleBI abnormalFlag = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "AbnormalFlag");
        addConstraint(abnormalFlag, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, abnormalFlag.getNid(), "0-1");

        RefexChronicleBI deltaFlag = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "DeltaFlag");
        addConstraint(deltaFlag, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, deltaFlag.getNid(), "0-1");

        RefexChronicleBI referenceRangeNar = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "ReferenceRangeNar");
        addConstraint(referenceRangeNar, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, referenceRangeNar.getNid(), "0-1");

        RefexChronicleBI relativeTemporalContext = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_QUAL, "RelativeTemporalContext");
        addConstraint(relativeTemporalContext, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, relativeTemporalContext.getNid(), "0-M");

        RefexChronicleBI subject = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_MOD, "Subject");
        addConstraint(subject, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, subject.getNid(), "0-1");

        RefexChronicleBI observed = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_ATTR, "Observed");
        addConstraint(observed, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, observed.getNid(), "0-1");

        RefexChronicleBI reportedReceived = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_ATTR, "ReportedReceived");
        addConstraint(reportedReceived, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, reportedReceived.getNid(), "0-1");

        RefexChronicleBI verified = addComponent(bloodPressureTaking, CEMMetadataBinding.CEM_ATTR, "Verified");
        addConstraint(verified, CEMMetadataBinding.CEM_CARDINALITY_CONSTRAINT, verified.getNid(), "0-1");

        addConstraint(abnormalFlag, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_CODE_FIELD.getNid(), "AbnormalFlagNumericNom_DOMAIN_ECID");
        addConstraint(deltaFlag, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_CODE_FIELD.getNid(), "DeltaFlagNumericNom_DOMAIN_ECID");
        addConstraint(modelDataMember, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_UNIT_FIELD.getNid(), "PressureUnits_DOMAIN_ECID");
        addConstraint(modelDataMember, CEMMetadataBinding.CEM_NORMAL_CONSTRAINT,
                CEMMetadataBinding.CEM_UNIT_FIELD.getNid(), "MilliMetersOfMercury_ECID");
        addConstraint(methodDevice, CEMMetadataBinding.CEM_DOMAIN_CONSTRAINT,
                CEMMetadataBinding.CEM_CODE_FIELD.getNid(), "BloodPressureMeasurementDevice_DOMAIN_ECID");

        // TODO: Links
        ds.addUncommitted(bloodPressureTaking);
        ds.commit();

        LOG.info("Long form after commit:" + bloodPressureTaking.toLongString());

    }

    public RefexChronicleBI addMemberInDataRefset(ConceptChronicleBI concept, ConceptSpec data) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID,
                concept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_DATA_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, data.getNid());

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addStringAnnotation(ConceptChronicleBI concept, ConceptSpec refset, String value) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.STR,
                concept.getPrimordialUuid(),
                ds.getUuidPrimordialForNid(refset.getNid()),
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, value);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex string UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addComponent(ConceptChronicleBI concept, ConceptSpec type, String componentKey) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_STR,
                concept.getPrimordialUuid(),
                CEMMetadataBinding.CEM_COMPOSITION_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, type.getNid());
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, componentKey);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex component UUID:" + newRefex.getPrimordialUuid());

        concept.addAnnotation(newRefex);

        return newRefex;
    }

    public RefexChronicleBI addConstraint(ComponentChronicleBI cemMember, ConceptSpec constraint, int focus, String value) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB newRefexCab = new RefexCAB(RefexType.CID_CID_STR,
                cemMember.getPrimordialUuid(),
                CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.getUuids()[0],
                IdDirective.GENERATE_RANDOM,
                RefexDirective.EXCLUDE);

        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, constraint.getNid());
        newRefexCab.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, focus);
        newRefexCab.put(ComponentProperty.STRING_EXTENSION_1, value);

        RefexChronicleBI<?> newRefex = builder.constructIfNotCurrent(newRefexCab);

        LOG.info("newRefex contraint UUID:" + newRefex.getPrimordialUuid());

        cemMember.addAnnotation(newRefex);

        return newRefex;
    }

    private EditCoordinate getEC() throws ValidationException, IOException {
        int authorNid = TermAux.USER.getLenient().getConceptNid();
        int module = Snomed.CORE_MODULE.getLenient().getNid();
        int editPathNid = ds.getNidForUuids(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")); // SNOMED CORE path

        return new EditCoordinate(authorNid, module, editPathNid);
    }

    private ViewCoordinate getVC() throws IOException {
        return StandardViewCoordinates.getSnomedStatedLatest();
    }

    private TerminologyBuilderBI getBuilder() throws IOException {
        if (builder == null) {
            builder = new BdbTermBuilder(getEC(), getVC());
        }
        return builder;
    }

}
