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

import com.sun.xml.internal.ws.util.StringUtils;

/**
 * Class for exporting a CEM model to an XML {@link File}.
 *
 * @author ocarlsen
 */
public class CEMExporter extends ExporterBase implements CEMXmlConstants {

    private static final Logger LOG = LoggerFactory.getLogger(CEMExporter.class);

    private final OutputStream outputStream;

    private Document document;

    public CEMExporter(OutputStream outputStream) throws ValidationException, IOException {
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

        // Build a DOM tree in the style of CEM.
        this.document = buildDom();
        Element root = buildCemTree(focusConceptAnnotations);
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

    private Element buildCemTree(Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations)
            throws ValidationException, IOException, ContradictionException {
        Element root = document.createElement(CEML);

        // CETYPE element.
        Element cetype = buildCetypeElement(focusConceptAnnotations);
        root.appendChild(cetype);

        return root;
    }

    /**
     * The spec for this model is in Jay's spreadsheet (ISAAC/resources/cem.xlsx).
     */
    private Element buildCetypeElement(Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations)
            throws ValidationException, IOException, ContradictionException {
        Element cetype = document.createElement(CETYPE);

        // Name attribute (1).
        StringMember nameAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_TYPE_REFSET, StringMember.class);
        if (nameAnnotation == null) {
            LOG.info("No CEM_TYPE_REFSET member found.");
        } else {
            Attr nameAttr = buildNameAttr(nameAnnotation);
            cetype.setAttributeNode(nameAttr);
        }

        // Key element (0-1).
        StringMember keyAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_KEY_REFSET, StringMember.class);
        if (keyAnnotation == null) {
            LOG.info("No CEM_KEY_REFSET member found.");
        } else {
            Element key = buildKeyElement(keyAnnotation);
            cetype.appendChild(key);
        }

        // Data element (0-1).
        NidMember dataAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_DATA_REFSET, NidMember.class);
        if (dataAnnotation == null) {
            LOG.info("No CEM_DATA_REFSET member found.");
        } else {
            Element data = buildDataElement(dataAnnotation);
            cetype.appendChild(data);
        }

        // Qual elements (0-M).
        List<NidStringMember> qualAnnotations = getCompositionAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_QUAL);
        if (qualAnnotations.isEmpty()) {
            LOG.info("No CEM_QUAL members found.");
        } else {
            for (NidStringMember qualAnnotation : qualAnnotations) {
                Element qual = buildCompositionElement(QUAL, qualAnnotation);
                cetype.appendChild(qual);
            }
        }

        // Mod elements (0-M).
        List<NidStringMember> modAnnotations = getCompositionAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_MOD);
        if (modAnnotations.isEmpty()) {
            LOG.info("No CEM_MOD members found.");
        } else {
            for (NidStringMember modAnnotation : modAnnotations) {
                Element mod = buildCompositionElement(MOD, modAnnotation);
                cetype.appendChild(mod);
            }
        }

        // Att elements (0-M).
        List<NidStringMember> attAnnotations = getCompositionAnnotations(
                focusConceptAnnotations, CEMMetadataBinding.CEM_ATTR);
        if (attAnnotations.isEmpty()) {
            LOG.info("No CEM_ATTR members found.");
        } else {
            for (NidStringMember attAnnotation : attAnnotations) {
                Element att = buildCompositionElement(ATT, attAnnotation);
                cetype.appendChild(att);
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
                Element constraint = buildConstraintElement(constraintAnnotation);
                cetype.appendChild(constraint);
            }
        }

        return cetype;
    }

	private Element buildConstraintElement(MembershipMember constraintAnnotation)
            throws ValidationException, IOException {
        Element e = document.createElement(CONSTRAINT);

        Collection<? extends RefexChronicleBI<?>> annotations = constraintAnnotation.getAnnotations();

        // Path attribute (1).
        StringMember pathAnnotation = getSingleAnnotation(annotations,
                CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET, StringMember.class);
        if (pathAnnotation == null) {
            LOG.info("No CEM_CONSTRAINTS_PATH_REFSET members found.");
        } else {
            Attr pathAttr = document.createAttribute(PATH);
            String path = pathAnnotation.getString1();
            pathAttr.setNodeValue(path);
            e.setAttributeNode(pathAttr);
        }

        // Value attribute (1).
        StringMember valueAnnotation = getSingleAnnotation(annotations,
                CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET, StringMember.class);
        if (valueAnnotation == null) {
            LOG.info("No CEM_CONSTRAINTS_VALUE_REFSET members found.");
        } else {
            Attr valueAttr = document.createAttribute(VALUE);
            String value = valueAnnotation.getString1();
            valueAttr.setNodeValue(value);
            e.setAttributeNode(valueAttr);
        }

        return e;
    }

    private Element buildCompositionElement(String elementName, NidStringMember compositionRefex)
            throws ValidationException, IOException, ContradictionException {
        Element e = document.createElement(elementName);

        // Type attribute.
        Attr typeAttr = document.createAttribute(TYPE);
        String type = compositionRefex.getString1();
        typeAttr.setNodeValue(type);
        e.setAttributeNode(typeAttr);

        // Name attribute.
        Attr nameAttr = document.createAttribute(NAME);
        String name = StringUtils.decapitalize(type);
        nameAttr.setNodeValue(name);
        e.setAttributeNode(nameAttr);

        // Constraint attribute (0-1).
        MembershipMember constraint = getMembershipAnnotation(compositionRefex, CEMMetadataBinding.CEM_CONSTRAINTS_REFSET);
        if (constraint == null) {
            LOG.info("No CEM_CONSTRAINTS_REFSET member found.");
        } else {
            StringMember pathAnnotation = getStringAnnotation(constraint, CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET);
            StringMember valueAnnotation = getStringAnnotation(constraint, CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET);
            String path = pathAnnotation.getString1();
            String value = valueAnnotation.getString1();
            Attr constraintAttr = document.createAttribute(path);
            constraintAttr.setNodeValue(value);
            e.setAttributeNode(constraintAttr);
        }


        // Value element
        StringMember valueAnnotation = getCompStringAnnotation(compositionRefex, CEMMetadataBinding.CEM_VALUE_REFSET);
        if (valueAnnotation != null) {
	        String value = valueAnnotation.getString1();
	        e.setTextContent(value);
        }

        return e;
    }

    private Element buildDataElement(NidMember dataAnnotation)
            throws ValidationException, IOException {
        Element data = document.createElement(DATA);

        // Convert to string.
        int nid = dataAnnotation.getNid1();
        String type = null;
        if (nid == CEMMetadataBinding.CEM_PQ.getNid()) {
            type = "pq";
        } else if (nid == CEMMetadataBinding.CEM_CD.getNid()) {
            type = "cd";
        } else {
            throw new IllegalStateException("Unrecognized CEM_DATA_REFSET member nid: " + nid);
        }

        // Type attribute.
        Attr typeAttr = document.createAttribute(TYPE);
        typeAttr.setNodeValue(type);
        data.setAttributeNode(typeAttr);

        return data;
    }

    private Element buildKeyElement(StringMember keyAnnotation) {
        Element key = document.createElement(KEY);

        // Code attribute.
        Attr codeAttr = document.createAttribute(CODE);
        String code = keyAnnotation.getString1();
        codeAttr.setNodeValue(code);
        key.setAttributeNode(codeAttr);

        return key;
    }

    private Attr buildNameAttr(StringMember nameAttribute) {
        Attr nameAttr = document.createAttribute(NAME);

        String name = nameAttribute.getString1();
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
}
