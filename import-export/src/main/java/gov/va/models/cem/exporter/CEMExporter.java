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
import java.util.Map;
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
import com.google.common.collect.Maps;

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
        ComponentChronicleBI<?> concept = getDataStore().getComponent(conceptUUID);
        LOG.debug("concept="+concept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> annotations = getLatestAnnotations(concept);

        // Organize into Map for quick access.
        Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap = Maps.newHashMap();
        for (RefexChronicleBI<?> annotation : annotations) {
            LOG.debug("annotation="+annotation);

            // Map to List of annotations for each assemblage nid.
            int assemblageNid = annotation.getAssemblageNid();
            List<RefexChronicleBI<?>> l = nidAnnotationsMap.get(assemblageNid);
            if (l == null) {
                l = Lists.newArrayList();
                nidAnnotationsMap.put(assemblageNid, l);
            }
            l.add(annotation);
        }

        // Build a DOM tree in the style of CEM.
        this.document = buildDom();
        Element root = buildTree(conceptUUID, nidAnnotationsMap);
        document.appendChild(root);

        // Transform DOM tree into stream.
        OutputStream outputStream = System.out;  // TODO: Output to file when done.
        Transformer transformer = buildTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);

        LOG.info("Ending export of CEM model to: " + file.getName());
    }

    private Element buildTree(UUID conceptUUID, Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException, ContradictionException {
        Element root = buildRootElement();

        Element cetype = buildCetypeElement(conceptUUID, nidAnnotationsMap);
        root.appendChild(cetype);

        return root;
    }

    /**
     * As per the "JCoylePhD-FinalForPrint.pdf" file, these are the
     * Properties of the &lt;cetype&gt; element (see Table 3.15):
     * <li>Property Type Cardinality</li>
     * <li>name attribute one</li>
     * <li>base attribute zero to one</li>
     * <li>kind attribute one</li>
     * <li>key element zero to one</li>
     * <li>data element zero to one</li>
     * <li>qual element zero to many</li>
     * <li>mod element zero to many</li>
     * <li>att element zero to many</li>
     * <li>item element zero to many</li>
     * <li>constraint element zero to many</li>
     * <li>link element zero to many</li>
     * The actual spec is in Jay's spreadsheet (ISAAC/resources/cem.xlsx).
     */
    private Element buildCetypeElement(UUID conceptUUID, Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException, ContradictionException {
        Element cetype = document.createElement("cetype");

        // Name attribute (1).
        StringMember nameAnnotation = getStringMember(CEMMetadataBinding.CEM_TYPE_REFSET, nidAnnotationsMap);
        Preconditions.checkNotNull(nameAnnotation, "No CEM_TYPE_REFSET member found.");
        Attr nameAttr = buildNameAttr(nameAnnotation);
        cetype.setAttributeNode(nameAttr);

        // Key element (0-1).
        StringMember keyAnnotation = getStringMember(CEMMetadataBinding.CEM_KEY_REFSET, nidAnnotationsMap);
        if (keyAnnotation == null) {
            LOG.info("No CEM_KEY_REFSET member found.");
        } else {
            Element key = buildKeyElement(keyAnnotation);
            cetype.appendChild(key);
        }

        // Data element (0-1).
        NidMember dataAnnotation = getNidMember(CEMMetadataBinding.CEM_DATA_REFSET, nidAnnotationsMap);
        if (dataAnnotation == null) {
            LOG.info("No CEM_DATA_REFSET member found.");
        } else {
            Element data = buildDataElement(dataAnnotation);
            cetype.appendChild(data);
        }

        // Qual elements (0-M).
        List<NidStringMember> qualAnnotations = getCompositionAnnotations(
                CEMMetadataBinding.CEM_QUAL, nidAnnotationsMap);
        for (NidStringMember qualAnnotation : qualAnnotations) {
            Element qual = buildCompositionElement("qual", qualAnnotation);
            cetype.appendChild(qual);
        }

        // Mod elements (0-M).
        List<NidStringMember> modAnnotations = getCompositionAnnotations(
                CEMMetadataBinding.CEM_MOD, nidAnnotationsMap);
        for (NidStringMember modAnnotation : modAnnotations) {
            Element qual = buildCompositionElement("mod", modAnnotation);
            cetype.appendChild(qual);
        }

        // Att elements (0-M).
        List<NidStringMember> attAnnotations = getCompositionAnnotations(
                CEMMetadataBinding.CEM_ATTR, nidAnnotationsMap);
        for (NidStringMember attAnnotation : attAnnotations) {
            Element qual = buildCompositionElement("att", attAnnotation);
            cetype.appendChild(qual);
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

    private Element buildCompositionElement(String elementName, NidStringMember typeAnnotation)
            throws ValidationException, IOException, ContradictionException {
        Element e = document.createElement(elementName);

        // Type attribute.
        Attr typeAttr = document.createAttribute("type");
        String type = typeAnnotation.getString1();
        typeAttr.setNodeValue(type);
        e.setAttributeNode(typeAttr);

        // Name attribute.
        Attr nameAttr = document.createAttribute("name");
        String name = StringUtils.decapitalize(type);
        nameAttr.setNodeValue(name);
        e.setAttributeNode(nameAttr);

        // TODO: Card attribute (0-1).

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

    private NidMember getNidMember(ConceptSpec refset,
            Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException {

        RefexChronicleBI<?> annotation = getSingleAnnotation(refset, nidAnnotationsMap);
        if (annotation == null) {
            return null;
        }

        // Sanity check.
        Preconditions.checkState(annotation instanceof NidMember,
                "Expected NidMember!  Actual type is " + annotation.getClass());

        return (NidMember) annotation;
    }

    private StringMember getStringMember(ConceptSpec refset,
            Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException {

        RefexChronicleBI<?> annotation = getSingleAnnotation(refset, nidAnnotationsMap);
        if (annotation == null) {
            return null;
        }

        // Sanity check.
        Preconditions.checkState(annotation instanceof StringMember,
                "Expected StringMember!  Actual type is " + annotation.getClass());

        return (StringMember) annotation;
    }

    @SuppressWarnings("unused")
    private NidNidStringMember getConstraintAnnotation(NidStringMember owner, ConceptSpec constraint)
            throws ValidationException, IOException, ContradictionException {

        // Get annotations of owner.
        Collection<? extends RefexChronicleBI<?>> annotations = getLatestAnnotations(owner);

        List<NidNidStringMember> filtered = Lists.newArrayList();
        for (RefexChronicleBI<?> annotation : annotations) {

            // Filter on CEMMetadataBinding.CEM_CONSTRAINTS_REFSET assemblage.
            if (annotation.getAssemblageNid() != CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.getNid()) {
                continue;
            }

            // Expect NidNidStringMember.
            Preconditions.checkState(annotation instanceof NidNidStringMember,
                    "Expected NidNidStringMember!  Actual type is " + annotation.getClass());
            NidNidStringMember member = (NidNidStringMember) annotation;

            // Filter those belonging to the specified constraint.
            if (member.getC1Nid() != constraint.getNid()) {
                continue;
            }

            // Filter members of owner.
            if (member.getC2Nid() != owner.getNid()) {
                continue;
            }

            // What we want.
            filtered.add(member);
        }

        // Should be 0-1.
        int filteredCount = filtered.size();
        Preconditions.checkState(filteredCount <= 1,
                "Expected 0-1 annotations for constraint nid " + constraint.getNid() + ", found " + filteredCount);

        // Return annotation, or null if none.
        if (filteredCount == 0) {
            return null;
        } else {
            return filtered.get(0);
        }
    }

    /**
     * Helper method to return all the {@link RefexChronicleBI}s from the
     * {@link List} of annotations in the provided {@link Map} for the
     * {@link CEMMetadataBinding#CEM_CONSTRAINTS_REFSET} refset, filtered by the
     * specified {@link ConceptSpec}.
     */
    @SuppressWarnings("unused")
    private List<NidNidStringMember> getConstraintAnnotations(ConceptSpec refset,
            Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException {

        // Get members of CEMMetadataBinding.CEM_COMPOSITION_REFSET.
        int compositionRefsetNid = CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.getNid();
        List<RefexChronicleBI<?>> annotations = nidAnnotationsMap.get(compositionRefsetNid);

        // Iterate through and gather those belonging to the specified refset.
        List<NidNidStringMember> filtered = Lists.newArrayList();
        for (RefexChronicleBI<?> annotation : annotations) {

            // Expect NidStringMember.
            Preconditions.checkState(annotation instanceof NidNidStringMember,
                    "Expected NidNidStringMember!  Actual type is " + annotation.getClass());

            NidNidStringMember member = (NidNidStringMember) annotation;
            if (refset.getNid() == member.getC1Nid()) {
                filtered.add(member);
            }
        }

        return filtered;
    }

    /**
     * Helper method to return all the {@link RefexChronicleBI}s from the
     * {@link List} of annotations in the provided {@link Map} for the
     * {@link CEMMetadataBinding#CEM_COMPOSITION_REFSET} refset, filtered by the
     * specified {@link ConceptSpec}.
     */
    private List<NidStringMember> getCompositionAnnotations(ConceptSpec refset,
            Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException {

        // Get members of CEMMetadataBinding.CEM_COMPOSITION_REFSET.
        int compositionRefsetNid = CEMMetadataBinding.CEM_COMPOSITION_REFSET.getNid();
        List<RefexChronicleBI<?>> annotations = nidAnnotationsMap.get(compositionRefsetNid);

        // Iterate through and gather those belonging to the specified refset.
        List<NidStringMember> filtered = Lists.newArrayList();
        for (RefexChronicleBI<?> annotation : annotations) {

            // Expect NidStringMember.
            Preconditions.checkState(annotation instanceof NidStringMember,
                    "Expected NidStringMember!  Actual type is " + annotation.getClass());

            NidStringMember member = (NidStringMember) annotation;
            if (refset.getNid() == member.getC1Nid()) {
                filtered.add(member);
            }
        }

        return filtered;
    }

    /**
     * Helper method to return the sole {@link RefexChronicleBI} from the
     * {@link List} of annotations in the provided {@link Map} for the given
     * {@link ConceptSpec}, or {@code null} if none is present.
     *
     * Performs integrity check and throws an {@link IllegalStateException} if
     * there is more than one.
     */
    private RefexChronicleBI<?> getSingleAnnotation(ConceptSpec refset,
            Map<Integer, List<RefexChronicleBI<?>>> nidAnnotationsMap)
            throws ValidationException, IOException {

        // Get members of specified refset.
        int refsetNid = refset.getNid();
        List<RefexChronicleBI<?>> annotations = nidAnnotationsMap.get(refsetNid);

        // Should be 0-1.
        int annotationCount = annotations.size();
        Preconditions.checkState(annotationCount <= 1,
                "Expected 0-1 annotations for refsetNid " + refsetNid + ", found " + annotationCount);

        // Return annotation, or null if none.
        if (annotationCount == 0) {
            return null;
        } else {
            return annotations.get(0);
        }
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
