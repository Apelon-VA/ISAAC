/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.models.cem.exporter;

import gov.va.models.cem.importer.CEMMetadataBinding;
import gov.va.models.util.ExporterBase;

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
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refex.type_membership.MembershipMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string.NidNidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.sun.xml.internal.ws.util.StringUtils;

/**
 * Class for exporting a CEM model to a {@link File}.
 *
 * @author ocarlsen
 */
public class CEMExporter extends ExporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(CEMExporter.class);

    private Document document;

    public CEMExporter() throws ValidationException, IOException {
        super();
    }

    public void exportModel(UUID conceptUUID, File file) throws Exception {
        LOG.info("Preparing to export CEM model to: " + file.getName());

        // Get chronicle for concept.
        ComponentChronicleBI<?> focusConcept = getDataStore().getComponent(conceptUUID);
        LOG.debug("focusConcept="+focusConcept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations = getLatestAnnotations(focusConcept);

        // Build a DOM tree in the style of CEM.
        this.document = buildDom();
        Element root = buildTree(focusConceptAnnotations);
        document.appendChild(root);

        // Transform DOM tree into stream.
        OutputStream outputStream = System.out;  // TODO: Output to file when done.
        Transformer transformer = buildTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);

        LOG.info("Ending export of CEM model to: " + file.getName());
    }

    private Element buildTree(Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations)
            throws ValidationException, IOException, ContradictionException {
        Element root = buildRootElement();

        Element cetype = buildCetypeElement(focusConceptAnnotations);
        root.appendChild(cetype);

        return root;
    }

    /**
     * The spec for this model is in Jay's spreadsheet (ISAAC/resources/cem.xlsx).
     */
    private Element buildCetypeElement(Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations)
            throws ValidationException, IOException, ContradictionException {
        Element cetype = document.createElement("cetype");

        // Name attribute (1).
        StringMember nameAnnotation = getSingleAnnotation(focusConceptAnnotations,
                CEMMetadataBinding.CEM_TYPE_REFSET, StringMember.class);
        if (nameAnnotation == null) {
            LOG.info("No CEM_TYPE_REFSET member found.");
        } else {
            Preconditions.checkNotNull(nameAnnotation, "No CEM_TYPE_REFSET member found.");
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
                Element qual = buildCompositionElement("qual", qualAnnotation);
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
                Element mod = buildCompositionElement("mod", modAnnotation);
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
                Element att = buildCompositionElement("att", attAnnotation);
                cetype.appendChild(att);
            }
        }

        // TODO: Constraint elements (0-M).

        return cetype;
    }

    @SuppressWarnings("unused")
    private Element buildConstraintElement(NidNidStringMember constraintAnnotation) {
        Element e = document.createElement("constraint");

        // TODO: Path attribute.  Not currently imported.

        // TODO: Value attribute.  Not currently imported.

        return e;
    }

    private Element buildCompositionElement(String elementName, NidStringMember compositionRefex)
            throws ValidationException, IOException, ContradictionException {
        Element e = document.createElement(elementName);

        // Type attribute.
        Attr typeAttr = document.createAttribute("type");
        String type = compositionRefex.getString1();
        typeAttr.setNodeValue(type);
        e.setAttributeNode(typeAttr);

        // Name attribute.
        Attr nameAttr = document.createAttribute("name");
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

        return e;
    }

    private Element buildDataElement(NidMember dataAnnotation)
            throws ValidationException, IOException {
        Element data = document.createElement("data");

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
        Attr typeAttr = document.createAttribute("type");
        typeAttr.setNodeValue(type);
        data.setAttributeNode(typeAttr);

        return data;
    }

    private Element buildKeyElement(StringMember keyAnnotation) {
        Element key = document.createElement("key");

        // Code attribute.
        Attr codeAttr = document.createAttribute("code");
        String code = keyAnnotation.getString1();
        codeAttr.setNodeValue(code);
        key.setAttributeNode(codeAttr);

        return key;
    }

    private Attr buildNameAttr(StringMember nameAttribute) {
        Attr nameAttr = document.createAttribute("name");

        String name = nameAttribute.getString1();
        nameAttr.setNodeValue(name);

        return nameAttr;
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

    @SuppressWarnings("unused")
    private List<NidNidStringMember> getConstraintAnnotations(
            Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations,
            ConceptSpec refsetSpec)
            throws ValidationException, IOException {

        // Filter members of CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.
        List<NidNidStringMember> annotations = filterAnnotations(focusConceptAnnotations,
                CEMMetadataBinding.CEM_CONSTRAINTS_REFSET, NidNidStringMember.class);

        // Filter again, keep those having the specified refset as their concept extension.
        List<NidNidStringMember> filtered = Lists.newArrayList();
        for (NidNidStringMember annotation : annotations) {
            if (refsetSpec.getNid() == annotation.getC1Nid()) {
                filtered.add(annotation);
            }
        }

        return filtered;
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

    /**
     * Helper method to filter the specified {@link Collection} of annotations
     * by calling {@link #filterAnnotations(Collection, ConceptSpec, Class)},
     * and then perform a sanity check that there is at most one.
     *
     * @return The sole annotation, or {@code null} if none found.
     * @throws An {@link IllegalStateException} if there is more than one annotation found.
     */
    private <T> T getSingleAnnotation(
            Collection<? extends RefexChronicleBI<?>> annotations,
            ConceptSpec refsetSpec, Class<T> type)
            throws ValidationException, IOException {

        // Filter members of the specified refset.
        List<T> filtered = filterAnnotations(annotations, refsetSpec, type);

        // Should be 0-1.
        int filteredCount = filtered.size();
        Preconditions.checkState(filteredCount <= 1,
                "Expected 0-1 annotations for refset nid " + refsetSpec.getNid() +
                ", found " + filteredCount);

        // Return annotation, or null if none.
        if (filteredCount == 0) {
            return null;
        } else {
            return filtered.get(0);
        }
    }

    /**
     * Helper method to iterate through the specified {@link Collection} of
     * annotations, keeping those belonging to the specified {@link ConceptSpec}.
     * Also performs a sanity check that annotations are instances of the
     * specified {@code type}.
     *
     * @return A new {@link List} of filtered annotations. May be empty.
     * @throws An {@link IllegalStateException} if annotations are not
     *         instances of the specified {@code type}.
     */
    private <T> List<T> filterAnnotations(
            Collection<? extends RefexChronicleBI<?>> annotations,
            ConceptSpec refsetSpec, Class<T> type)
            throws ValidationException, IOException {
        List<T> filtered = Lists.newArrayList();

        for (RefexChronicleBI<?> annotation : annotations) {

            // Filter on specified refset.
            if (annotation.getAssemblageNid() != refsetSpec.getNid()) {
                continue;
            }

            // Expect member type.
            Preconditions.checkState(type.isAssignableFrom(annotation.getClass()),
                    "Expected " + type + "!  Actual type is " + annotation.getClass());
            @SuppressWarnings("unchecked")
            T member = (T) annotation;

            // What we want.
            filtered.add(member);
        }

        return filtered;
    }

    private Element buildRootElement() {
        return document.createElement("ceml");
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

        return transformer;
    }

    private Collection<? extends RefexChronicleBI<?>> getLatestAnnotations(ComponentChronicleBI<?> conceptChronicle)
            throws IOException, ContradictionException {

        // Get latest version.
        ComponentVersionBI latestVersion = conceptChronicle.getVersion(getVC());

        // Print out annotations.
        return latestVersion.getAnnotations();
    }

}
