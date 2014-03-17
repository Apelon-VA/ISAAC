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
package gov.va.isaac.models.fhim.exporter;

import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.FHIMInformationModel.Attribute;
import gov.va.isaac.models.fhim.FHIMInformationModel.Class;
import gov.va.isaac.models.fhim.FHIMInformationModel.Enumeration;
import gov.va.isaac.models.fhim.FHIMInformationModel.External;
import gov.va.isaac.models.fhim.FHIMInformationModel.Generalization;
import gov.va.isaac.models.fhim.FHIMInformationModel.Multiplicity;
import gov.va.isaac.models.fhim.FHIMInformationModel.Type;
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;
import gov.va.isaac.models.util.ExporterBase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int.NidIntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Class for exporting a FHIM model to an XML {@link File}.
 *
 * @author ocarlsen
 */
public class FHIMExporter extends ExporterBase {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMExporter.class);

    private final OutputStream outputStream;

    private final Map<Integer, Enumeration> nidEnumerationMap = Maps.newHashMap();
    private final Map<Integer, Class> nidClassMap = Maps.newHashMap();
    private final Map<Integer, Attribute> nidAttributeMap = Maps.newHashMap();
    private final Map<Integer, External> nidExternalMap;

    public FHIMExporter(OutputStream outputStream) throws ValidationException, IOException {
        super();
        this.outputStream = outputStream;

        // Build Nid-Externals map.
        nidExternalMap = buildNidExternalsMap();
    }

    public void exportModel(UUID conceptUUID) throws Exception {
        LOG.info("Starting export of FHIM model");

        // Get chronicle for concept.
        ComponentChronicleBI<?> focusConcept = getDataStore().getComponent(conceptUUID);
        LOG.debug("focusConcept="+focusConcept);

        // Get all annotations on the specified concept.
        Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations = getLatestAnnotations(focusConcept);

        // Parse into FHIM model.
        FHIMInformationModel infoModel = createInformationModel(focusConceptAnnotations);

        // Abort if not available.
        if (infoModel == null) {
            LOG.warn("No FHIM model to export on " + conceptUUID);
            return;
        }

        // TODO: Convert to UML model in the style of FHIM.

        // TODO: Write UML model to OutputStream.

        LOG.info("Ending export of FHIM model");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private FHIMInformationModel createInformationModel(
            Collection<? extends RefexChronicleBI<?>> focusConceptAnnotations)
            throws ValidationException, IOException, ContradictionException {

        // Model name.
        StringMember modelAnnotation = getSingleAnnotation(focusConceptAnnotations,
                FHIMMetadataBinding.FHIM_MODELS_REFSET, StringMember.class);
        if (modelAnnotation == null) {
            LOG.info("No FHIM_MODELS_REFSET member found.");
            return null;
        }

        String name = modelAnnotation.getString1();
        FHIMInformationModel infoModel = new FHIMInformationModel(name);

        // Get all annotations on the model annotation.
        Collection<? extends RefexChronicleBI<?>> modelAnnotations = getLatestAnnotations(modelAnnotation);

        // Enumerations.
        List<StringMember> enumAnnotations = filterAnnotations(modelAnnotations,
                FHIMMetadataBinding.FHIM_ENUMERATIONS_REFSET, StringMember.class);
        if (enumAnnotations.isEmpty()) {
            LOG.info("No FHIM_ENUMERATIONS_REFSET member found.");
        } else {
            for (StringMember enumAnnotation : enumAnnotations) {
                Enumeration enumeration = buildEnumeration(enumAnnotation);
                infoModel.addEnumeration(enumeration);
            }
        }

        // Stub Classes first, they will be used as types later.
        List<StringMember> classAnnotations = filterAnnotations(modelAnnotations,
                FHIMMetadataBinding.FHIM_CLASSES_REFSET, StringMember.class);
        if (classAnnotations.isEmpty()) {
            LOG.info("No FHIM_CLASSES_REFSET member found.");
        } else {
            for (StringMember classAnnotation : classAnnotations) {

                // Stub for now, we will flesh out later.
                Class clazz = new Class(classAnnotation.getString1());

                // Keep track for later.
                nidClassMap.put(classAnnotation.getNid(), clazz);
            }
        }

        // Now that we have the requisite types, we can flesh out the rest.
        for (StringMember classAnnotation : classAnnotations) {
            Class c = buildClass(classAnnotation);
            infoModel.addClass(c);
        }

        return infoModel;
    }

    private Class buildClass(StringMember classAnnotation)
            throws ValidationException, IOException, ContradictionException {
        int nid = classAnnotation.getNid();
        Class c = getClass(nid);
        LOG.debug("Class: " + c.getName());

        // Attributes.
        Collection<NidStringMember> attributeAnnotations = filterAnnotations(
                getLatestAnnotations(classAnnotation),
                FHIMMetadataBinding.FHIM_ATTRIBUTES_REFSET,
                NidStringMember.class);
        for (NidStringMember attributeAnnotation : attributeAnnotations) {
            Attribute a = buildAttribute(attributeAnnotation);
            c.addAttribute(a);
        }

        // Generalizations.
        Collection<NidMember> generalizationAnnotations = filterAnnotations(
                getLatestAnnotations(classAnnotation),
                FHIMMetadataBinding.FHIM_GENERALIZATIONS_REFSET,
                NidMember.class);
        for (NidMember generalizationAnnotation : generalizationAnnotations) {
            Generalization g = buildGeneralization(classAnnotation, generalizationAnnotation);
            c.addGeneralization(g);
        }

        return c;
    }

    private Generalization buildGeneralization(StringMember classAnnotation, NidMember generalizationAnnotation) {
        LOG.debug("Generalization: ");

        // Source.
        int sourceNid = classAnnotation.getNid();
        Type source = getTypeForNid(sourceNid);
        LOG.debug("    source: " + source.getName());

        // Target.
        int targetNid = generalizationAnnotation.getC1Nid();
        Type target = getTypeForNid(targetNid);
        LOG.debug("    target: " + target.getName());

        return new Generalization(source, target);
    }

    private Attribute buildAttribute(NidStringMember attributeAnnotation)
            throws IOException, ContradictionException {
        String name = attributeAnnotation.getString1();

        // Type.
        int typeNid = attributeAnnotation.getNid1();
        Type type = getTypeForNid(typeNid);

        Attribute a = new Attribute(name, type);
        LOG.debug("Attribute: " + a.getName());

        // Get all annotations on the attribute annotation.
        Collection<? extends RefexChronicleBI<?>> attributeAnnotations = getLatestAnnotations(attributeAnnotation);

        // DefaultValue.
        String defaultValue = getDefaultValue(attributeAnnotations);
        if (defaultValue != null) {
            LOG.debug("    defaultValue: " + defaultValue);
            a.setDefaultValue(defaultValue);
        }

        // Multiplicity
        Multiplicity multiplicity = buildMultiplicity(attributeAnnotations);
        if (multiplicity != null) {
            LOG.debug("    multiplicity: " + multiplicity);
            a.setMultiplicity(multiplicity);
        }

        return a;
    }

    private Multiplicity buildMultiplicity(
            Collection<? extends RefexChronicleBI<?>> attributeAnnotations)
            throws IOException, ContradictionException {
        List<NidIntMember> multiplicityAnnotations = filterAnnotations(attributeAnnotations,
                FHIMMetadataBinding.FHIM_MULTIPLICITY_REFSET, NidIntMember.class);

        // If no multiplicity annotations, abort.
        int multiplicityCount = multiplicityAnnotations.size();
        if (multiplicityCount == 0) {
            return null;
        }

        // Otherwise, expect exactly two multiplicity annotations: upper and lower.
        if (multiplicityCount != 2) {
            LOG.warn("Expected 2 multiplicity annotations, found " + multiplicityCount);
        }

        int upper = LiteralUnlimitedNatural.UNLIMITED;
        int lower = LiteralUnlimitedNatural.UNLIMITED;

        for (NidIntMember multiplicityAnnotation : multiplicityAnnotations) {
            int nid = multiplicityAnnotation.getC1Nid();
            if (nid == FHIMMetadataBinding.FHIM_LOWER.getNid()) {
                lower = multiplicityAnnotation.getInt1();
            } else if (nid == FHIMMetadataBinding.FHIM_UPPER.getNid()) {
                upper = multiplicityAnnotation.getInt1();
            } else {
                LOG.warn("Unexpected multiplicity metadata: " + multiplicityAnnotation);
            }
        }

        return new Multiplicity(lower, upper);
    }

    private String getDefaultValue(
            Collection<? extends RefexChronicleBI<?>> attributeAnnotations)
            throws ValidationException, IOException {
        List<StringMember> defaultValueAnnotations = filterAnnotations(attributeAnnotations,
                FHIMMetadataBinding.FHIM_DEFAULTVALUES_REFSET, StringMember.class);

        // If no default value annotations, abort.
        int defaultValueCount = defaultValueAnnotations.size();
        if (defaultValueCount == 0) {
            return null;
        }

        // Otherwise, expect exactly one default value annotation.
        if (defaultValueCount != 1) {
            LOG.warn("Expected 1 default value annotation, found " + defaultValueCount);
        }

        StringMember defaultValueAnnotation = defaultValueAnnotations.get(0);
        return defaultValueAnnotation.getString1();
    }

    private Type getTypeForNid(int nid) {
        Type t = null;

        // Class?
        t = getClass(nid);
        if (t != null) {
            return t;
        }

        // Enumeration?
        t = getEnumeration(nid);
        if (t != null) {
            return t;
        }

        // Attribute?
        t = getAttribute(nid);
        if (t != null) {
            return t;
        }

        // External?
        t = getExternal(nid);
        if (t != null) {
            return t;
        }

        // Something wrong!
        throw new IllegalArgumentException("Could not find type for " + nid);
    }

    private Class getClass(int nid) {
        return nidClassMap.get(nid);
    }

    private Enumeration getEnumeration(int nid) {
        return nidEnumerationMap.get(nid);
    }

    private Attribute getAttribute(int nid) {
        return nidAttributeMap.get(nid);
    }

    private External getExternal(int nid) {
        return nidExternalMap.get(nid);
    }

    private Enumeration buildEnumeration(StringMember enumAnnotation)
            throws IOException, ContradictionException {
        String name = enumAnnotation.getString1();
        Enumeration e = new Enumeration(name);

        // Enumeration values.
        Collection<StringMember> valueAnnotations = filterAnnotations(
                getLatestAnnotations(enumAnnotation),
                FHIMMetadataBinding.FHIM_ENUMERATIONVALUES_REFSET,
                StringMember.class);
        for (StringMember valueAnnotation : valueAnnotations) {
            String value = valueAnnotation.getString1();
            e.addLiteral(value);
        }

        // Keep track for later.
        int nid = enumAnnotation.getNid();
        nidEnumerationMap.put(nid, e);

        return e;
    }


    private static Map<Integer, External> buildNidExternalsMap()
            throws ValidationException, IOException {
        Map<Integer, External> m = Maps.newHashMap();

        External e = null;

        // Code.
        e = new External("Code", FHIMMetadataBinding.FHIM_CODE);
        m.put(FHIMMetadataBinding.FHIM_CODE.getNid(), e);

        // ObservationQualifier.
        e = new External("ObservationQualifier", FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER);
        m.put(FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER.getNid(), e);

        // ObservationStatement.
        e = new External("ObservationStatement", FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT);
        m.put(FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT.getNid(), e);

        // PhysicalQuantity.
        e = new External("PhysicalQuantity", FHIMMetadataBinding.FHIM_PHYSICALQUANTITY);
        m.put(FHIMMetadataBinding.FHIM_PHYSICALQUANTITY.getNid(), e);

        // PulsePosition.
        e = new External("PulsePosition", FHIMMetadataBinding.FHIM_PULSEPOSITION);
        m.put(FHIMMetadataBinding.FHIM_PULSEPOSITION.getNid(), e);

        return m;
    }
}
