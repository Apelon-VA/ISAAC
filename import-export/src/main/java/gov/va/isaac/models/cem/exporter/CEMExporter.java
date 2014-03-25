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
package gov.va.isaac.models.cem.exporter;

import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.CEMInformationModel.ComponentType;
import gov.va.isaac.models.cem.CEMInformationModel.Composition;
import gov.va.isaac.models.cem.CEMInformationModel.Constraint;
import gov.va.isaac.models.cem.CEMXmlConstants;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refex.type_membership.MembershipMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

/**
 * Class for exporting a CEM model to an XML {@link File}.
 *
 * @author ocarlsen
 */
public class CEMExporter extends ExporterBase implements CEMXmlConstants {

    private static final Logger LOG = LoggerFactory.getLogger(CEMExporter.class);

    private final OutputStream outputStream;

    private Document document;

    public CEMExporter(OutputStream outputStream) {
        super();
        this.outputStream = outputStream;
    }

    public void exportModel(UUID conceptUUID) throws Exception {
        LOG.info("Starting export of CEM model");

        // Get chronicle for concept.
        ComponentChronicleBI<?> focusConcept = getDataStore().getComponent(conceptUUID);
        LOG.debug("focusConcept="+focusConcept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations = getLatestAnnotations(focusConcept);

        // Parse into CEM model.
        CEMInformationModel infoModel = createInformationModel(focusConceptAnnotations);

        // Abort if not available.
        if (infoModel == null) {
            LOG.warn("No CEM model to export on " + conceptUUID);
            return;
        }

        // Build a DOM tree in the style of CEM.
        this.document = buildDom();
        Element root = buildCemTree(infoModel);
        document.appendChild(root);

        // Transform DOM tree into stream.
        Transformer transformer = buildTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);

        LOG.info("Ending export of CEM model");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private CEMInformationModel createInformationModel(
            Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations)
            throws ValidationException, IOException, ContradictionException {

        // Name attribute (1).
        StringMember nameAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_TYPE_REFSET, StringMember.class);
        if (nameAnnotation == null) {
            LOG.info("No CEM_TYPE_REFSET member found.");
            return null;
        }

        String name = nameAnnotation.getString1();
        CEMInformationModel infoModel = new CEMInformationModel(name);

        // Key element (0-1).
        StringMember keyAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_KEY_REFSET, StringMember.class);
        if (keyAnnotation == null) {
            LOG.info("No CEM_KEY_REFSET member found.");
        } else {
            String key = keyAnnotation.getString1();
            infoModel.setKey(key);
        }

        // Data element (0-1).
        NidMember dataTypeAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_DATA_REFSET, NidMember.class);
        if (dataTypeAnnotation == null) {
            LOG.info("No CEM_DATA_REFSET member found.");
        } else {

            // Convert to ConceptSpec.
            int nid = dataTypeAnnotation.getNid1();
            ConceptSpec dataType = null;
            if (nid == CEMMetadataBinding.CEM_PQ.getNid()) {
                dataType = CEMMetadataBinding.CEM_PQ;
            } else if (nid == CEMMetadataBinding.CEM_CD.getNid()) {
                dataType = CEMMetadataBinding.CEM_CD;
            } else {
                throw new IllegalStateException("Unrecognized CEM_DATA_REFSET member nid: " + nid);
            }
            infoModel.setDataType(dataType);
        }

        // Qual elements (0-M).
        List<NidStringMember> qualAnnotations = getCompositionAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_QUAL);
        if (qualAnnotations.isEmpty()) {
            LOG.info("No CEM_QUAL members found.");
        } else {
            for (NidStringMember qualAnnotation : qualAnnotations) {
                ComponentType componentType = ComponentType.QUAL;
                addComposition(componentType, infoModel, qualAnnotation);
            }
        }

        // Mod elements (0-M).
        List<NidStringMember> modAnnotations = getCompositionAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_MOD);
        if (modAnnotations.isEmpty()) {
            LOG.info("No CEM_MOD members found.");
        } else {
            for (NidStringMember modAnnotation : modAnnotations) {
                ComponentType componentType = ComponentType.MOD;
                addComposition(componentType, infoModel, modAnnotation);
            }
        }

        // Att elements (0-M).
        List<NidStringMember> attAnnotations = getCompositionAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_ATT);
        if (attAnnotations.isEmpty()) {
            LOG.info("No CEM_ATTR members found.");
        } else {
            for (NidStringMember attAnnotation : attAnnotations) {
                ComponentType componentType = ComponentType.ATT;
                addComposition(componentType, infoModel, attAnnotation);
            }
        }

        // Constraint elements (0-M).
        List<MembershipMember> constraintAnnotations = filterAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_CONSTRAINTS_REFSET,
                MembershipMember.class);
        if (constraintAnnotations.isEmpty()) {
            LOG.info("No CEM_CONSTRAINTS_REFSET members found.");
        } else {
            for (MembershipMember constraintAnnotation : constraintAnnotations) {
                addConstraint(infoModel, constraintAnnotation);
            }
        }

        return infoModel;
    }

    private void addConstraint(CEMInformationModel infoModel,
            MembershipMember constraintAnnotation)
            throws IOException, ContradictionException {
        Constraint constraint = createConstraint(constraintAnnotation);
        infoModel.addConstraint(constraint);
    }

    private void addComposition(ComponentType componentType,
            CEMInformationModel infoModel, NidStringMember compositionAnnotation)
            throws ValidationException, IOException, ContradictionException {

        // Component composition.
        String component = compositionAnnotation.getString1();
        Composition composition = null;
        switch (componentType) {
        case ATT:
            composition = infoModel.addAttComponent(component);
            break;
        case MOD:
            composition = infoModel.addModComponent(component);
            break;
        case QUAL:
            composition = infoModel.addQualComponent(component);
            break;
        default:
            throw new IllegalArgumentException("Unrecognized componentType: " + componentType);
        }

        // Constraint.
        MembershipMember constraintAnnotation = getMembershipAnnotation(compositionAnnotation,
                CEMMetadataBinding.CEM_CONSTRAINTS_REFSET);
        if (constraintAnnotation == null) {
            LOG.info("No CEM_CONSTRAINTS_REFSET member found.");
        } else {
            Constraint constraint = createConstraint(constraintAnnotation);
            composition.setConstraint(constraint);
        }

        // Value.
        StringMember valueAnnotation = getCompStringAnnotation(compositionAnnotation, CEMMetadataBinding.CEM_VALUE_REFSET);
        if (valueAnnotation == null) {
            LOG.info("No CEM_VALUE_REFSET member found.");
        } else {
            String value = valueAnnotation.getString1();
            composition.setValue(value);
        }
    }

    private Constraint createConstraint(MembershipMember constraintAnnotation) throws IOException,
            ContradictionException {
        String path = null;
        StringMember pathAnnotation = getStringAnnotation(constraintAnnotation, CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET);
        if (pathAnnotation == null) {
            LOG.info("No CEM_CONSTRAINTS_PATH_REFSET members found.");
        } else {
            path = pathAnnotation.getString1();
        }

        String value = null;
        StringMember valueAnnotation = getStringAnnotation(constraintAnnotation, CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET);
        if (valueAnnotation == null) {
            LOG.info("No CEM_CONSTRAINTS_VALUE_REFSET members found.");
        } else {
            value = valueAnnotation.getString1();
        }

        return new Constraint(path, value);
    }

    private Element buildCemTree(CEMInformationModel infoModel)
            throws ValidationException, IOException, ContradictionException {
        Element root = document.createElement(CEML);

        // CETYPE element.
        Element cetype = buildCetypeElement(infoModel);
        root.appendChild(cetype);

        return root;
    }

    /**
     * The spec for this model is in Jay's spreadsheet (ISAAC/resources/cem.xlsx).
     * @throws IOException
     * @throws ValidationException
     * @throws ContradictionException
     */
    private Element buildCetypeElement(CEMInformationModel infoModel)
            throws ValidationException, IOException, ContradictionException {
        Element cetype = document.createElement(CETYPE);

        // Name attribute (1).
        String name = infoModel.getName();
        Attr nameAttr = buildNameAttr(name);
        cetype.setAttributeNode(nameAttr);

        // Key element (0-1).
        String key = infoModel.getKey();
        Element keyElement = buildKeyElement(key);
        cetype.appendChild(keyElement);

        // Data element (0-1).
        ConceptSpec dataType = infoModel.getDataType();
        Element dataElement = buildDataElement(dataType);
        cetype.appendChild(dataElement);

        // Qual elements (0-M).
        List<Composition> quals = infoModel.getQualComponents();
        for (Composition qual : quals) {
            Element qualElement = buildCompositionElement(QUAL, qual);
            cetype.appendChild(qualElement);
        }

        // Mod elements (0-M).
        List<Composition> mods = infoModel.getModComponents();
        for (Composition mod : mods) {
            Element modElement = buildCompositionElement(MOD, mod);
            cetype.appendChild(modElement);
        }

        // Att elements (0-M).
        List<Composition> atts = infoModel.getAttComponents();
        for (Composition att : atts) {
            Element attElement = buildCompositionElement(ATT, att);
            cetype.appendChild(attElement);
        }

        // Constraint elements (0-M).
        List<Constraint> constraints = infoModel.getConstraints();
        for (Constraint constraint : constraints) {
            Element constraintElement = buildConstraintElement(constraint);
            cetype.appendChild(constraintElement);
        }

        return cetype;
    }

	private Element buildConstraintElement(Constraint constraint) {
        Element e = document.createElement(CONSTRAINT);

        // Path attribute (1).
        Attr pathAttr = document.createAttribute(PATH);
        String path = constraint.getPath();
        if (path != null) {
            pathAttr.setNodeValue(path);
        }
        e.setAttributeNode(pathAttr);

        // Value attribute (1).
        Attr valueAttr = document.createAttribute(VALUE);
        String value = constraint.getValue();
        if (value != null) {
            valueAttr.setNodeValue(value);
        }
        e.setAttributeNode(valueAttr);

        return e;
    }

    private Element buildCompositionElement(String elementName, Composition composition) {
        Element e = document.createElement(elementName);

        // Type attribute.
        Attr typeAttr = document.createAttribute(TYPE);
        String type = composition.getComponent();
        typeAttr.setNodeValue(type);
        e.setAttributeNode(typeAttr);

        // Name attribute.
        Attr nameAttr = document.createAttribute(NAME);
        String name = decapitalize(type);
        nameAttr.setNodeValue(name);
        e.setAttributeNode(nameAttr);

        // Constraint attribute (0-1).
        Constraint constraint = composition.getConstraint();
        if (constraint != null) {
            String path = constraint.getPath();
            String value = constraint.getValue();
            Attr constraintAttr = document.createAttribute(path);
            constraintAttr.setNodeValue(value);
            e.setAttributeNode(constraintAttr);
        }

        // Value element
        String value = composition.getValue();
        if (value != null) {
	        e.setTextContent(value);
        }

        return e;
    }

    private Element buildDataElement(ConceptSpec dataType)
            throws ValidationException, IOException {
        Element data = document.createElement(DATA);

        // Convert to string.
        int nid = dataType.getNid();
        String type = null;
        if (nid == CEMMetadataBinding.CEM_PQ.getNid()) {
            type = "pq";
        } else if (nid == CEMMetadataBinding.CEM_CD.getNid()) {
            type = "cd";
        } else {
            throw new IllegalStateException("Unrecognized dataType: " + dataType);
        }

        // Type attribute.
        Attr typeAttr = document.createAttribute(TYPE);
        typeAttr.setNodeValue(type);
        data.setAttributeNode(typeAttr);

        return data;
    }

    private Element buildKeyElement(String code) {
        Element key = document.createElement(KEY);

        // Code attribute.
        Attr codeAttr = document.createAttribute(CODE);
        codeAttr.setNodeValue(code);
        key.setAttributeNode(codeAttr);

        return key;
    }

    private Attr buildNameAttr(String name) {
        Attr nameAttr = document.createAttribute(NAME);

        nameAttr.setNodeValue(name);

        return nameAttr;
    }

    private StringMember getCompStringAnnotation(NidStringMember owner, ConceptSpec refsetSpec)
            throws IOException, ContradictionException {

        // Get annotations of owner.
        Collection<? extends RefexChronicleBI<?>> annotations = getLatestAnnotations(owner);

        return getSingleAnnotation(annotations, refsetSpec, StringMember.class);
    }


    private StringMember getStringAnnotation(MembershipMember owner, ConceptSpec refsetSpec)
            throws IOException, ContradictionException {

        // Get annotations of owner.
        Collection<? extends RefexChronicleBI<?>> annotations = getLatestAnnotations(owner);

        return getSingleAnnotation(annotations, refsetSpec, StringMember.class);
    }

    private MembershipMember getMembershipAnnotation(NidStringMember owner, ConceptSpec conceptSpec)
            throws ValidationException, IOException, ContradictionException {

        // Get annotations of owner.
        Collection<? extends RefexChronicleBI<?>> annotations = getLatestAnnotations(owner);

        return getSingleAnnotation(annotations, conceptSpec,
                MembershipMember.class);
    }

    private List<NidStringMember> getCompositionAnnotations(
            Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations,
            ConceptSpec refsetSpec)
            throws ValidationException, IOException {

        // Filter members of CEMMetadataBinding.CEM_COMPOSITION_REFSET.
        List<NidStringMember> annotations = filterAnnotations(focusConceptAnnotations,
                CEMMetadataBinding.CEM_COMPOSITION_REFSET, NidStringMember.class);

        // Filter again, keep those having the specified refset as their concept extension.
        List<NidStringMember> filtered = Lists.newArrayList();
        for (NidStringMember annotation : annotations) {
            if (refsetSpec.getNid() == annotation.getC1Nid()) {
                filtered.add(annotation);
            }
        }

        return filtered;
    }

    private Document buildDom() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private Transformer buildTransformer() throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer =  factory.newTransformer();

        // Indent output.
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // Skip XML declaration header.
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        return transformer;
    }
    
    /**
     * Utility method to take a string and convert it to normal Java variable
     * name capitalization. This normally means converting the first character
     * from upper case to lower case, but in the (unusual) special case when
     * there is more than one character and both the first and second characters
     * are upper case, we leave it alone.
     * 
     * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays as
     * "URL".
     * 
     * Parameters
     * @param name The string to be decapitalized. 
     * Returns: 
     * @return The decapitalized version of the string.
     * 
     * Note, this was copied from 1.7_40 release of the JDK, as it was removed from com.sun.xml.internal.ws.util.StringUtils in 1.8.
     */

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 
                && Character.isUpperCase(name.charAt(1)) 
                && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
