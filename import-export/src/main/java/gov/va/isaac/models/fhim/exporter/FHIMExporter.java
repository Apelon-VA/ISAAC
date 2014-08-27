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
import gov.va.isaac.models.fhim.FHIMInformationModel.Association;
import gov.va.isaac.models.fhim.FHIMInformationModel.Attribute;
import gov.va.isaac.models.fhim.FHIMInformationModel.Class;
import gov.va.isaac.models.fhim.FHIMInformationModel.Dependency;
import gov.va.isaac.models.fhim.FHIMInformationModel.Enumeration;
import gov.va.isaac.models.fhim.FHIMInformationModel.External;
import gov.va.isaac.models.fhim.FHIMInformationModel.Generalization;
import gov.va.isaac.models.fhim.FHIMInformationModel.Multiplicity;
import gov.va.isaac.models.fhim.FHIMInformationModel.Type;
import gov.va.isaac.models.fhim.FHIMUmlConstants;
import gov.va.isaac.models.fhim.converter.Model2UMLConverter;
import gov.va.isaac.models.util.ExporterBase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string.NidNidStringMember;
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
public class FHIMExporter extends ExporterBase implements FHIMUmlConstants {

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
        nidExternalMap = buildNidExternalMap();
    }

    public void exportModel(UUID modelUUID) throws Exception {
        LOG.info("Starting export of FHIM model");

        // Get chronicle for component.
        ComponentChronicleBI<?> focusComponent = getDataStore().getComponent(modelUUID);
        LOG.debug("focusComponent="+focusComponent);

        // Abort if not available.
        if (focusComponent == null) {
            LOG.warn("No model found: " + modelUUID);
            return;
        }

        // Expect a RefexChronicle.
        if (! (focusComponent instanceof RefexChronicleBI<?>)) {
            LOG.warn("Expected FHIM model to ge a refex: " + modelUUID);
            return;
        }
        RefexChronicleBI<?> modelRefex = (RefexChronicleBI<?>) focusComponent;

        // Parse into FHIM model.
        FHIMInformationModel infoModel = createInformationModel(modelRefex);

        // Abort if not available.
        if (infoModel == null) {
            LOG.warn("No model found: " + modelUUID);
            return;
        }

        // Convert to UML model in the style of FHIM.
        Model2UMLConverter converter = new Model2UMLConverter();
        @SuppressWarnings("unused")
        Package umlModel = converter.createUMLModel(infoModel);

        // Write UML model to OutputStream.
        List<Package> pkgs = converter.getTopLevelPackages();
        saveModel(pkgs);

        LOG.info("Ending export of FHIM model");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private void saveModel(List<Package> pkgs) throws IOException {

        // Create a resource-set to contain the resource(s) that we are saving
        ResourceSet resourceSet = new ResourceSetImpl();

        // Initialize registrations of resource factories, library models,
        // profiles, Ecore metadata, and other dependencies required for
        // serializing and working with UML resources. This is only necessary in
        // applications that are not hosted in the Eclipse platform run-time, in
        // which case these registrations are discovered automatically from
        // Eclipse extension points.
        UMLResourcesUtil.init(resourceSet);

        // Create the output resource and add our model package to it.
//        Resource resource = resourceSet.createResource(uri);
        Resource resource = new XMIResourceImpl();
        for (Package pkg : pkgs) {
            resource.getContents().add(pkg);
        }

        // And save.
        Map<?, ?> options = null;   // No save options needed.
        resource.save(outputStream, options);
        outputStream.close();
    }

    private FHIMInformationModel createInformationModel(RefexChronicleBI<?> modelRefex)
            throws ValidationException, IOException, ContradictionException {

        // Model name, UUID.
        StringMember modelAnnotation = (StringMember) modelRefex;
        String modelName = modelAnnotation.getString1();
        UUID modelUUID = modelRefex.getPrimordialUuid();
        FHIMInformationModel infoModel = new FHIMInformationModel(modelName, modelUUID);
        LOG.debug("Model name: " + modelName);
        LOG.debug("Model UUID: " + modelUUID);

        // Get all annotations on the model annotation.
        Collection<? extends RefexChronicleBI<?>> modelAnnotations = getLatestAnnotations(modelRefex);

        /** TODO - BAC
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

        // Stub Classes first, they will be used as types.
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

        // Now flesh out Classes.
        for (StringMember classAnnotation : classAnnotations) {
            Class c = buildClass(classAnnotation);
            infoModel.addClass(c);
        }

        // Dependencies.
        List<NidNidStringMember> dependencyAnnotations = filterAnnotations(modelAnnotations,
                FHIMMetadataBinding.FHIM_DEPENDENCIES_REFSET, NidNidStringMember.class);
        if (dependencyAnnotations.isEmpty()) {
            LOG.info("No FHIM_DEPENDENCIES_REFSET member found.");
        } else {
            for (NidNidStringMember dependencyAnnotation : dependencyAnnotations) {
                Dependency d = buildDependency(dependencyAnnotation);
                infoModel.addDependency(d);
            }
        }

        // Create model-scoped Attributes, which will be used by Associations.
        Collection<NidStringMember> attributeAnnotations = filterAnnotations(
                modelAnnotations,
                FHIMMetadataBinding.FHIM_ATTRIBUTES_REFSET,
                NidStringMember.class);
        for (NidStringMember attributeAnnotation : attributeAnnotations) {
            buildAttribute(attributeAnnotation);
        }

        // Associations.
        List<StringMember> associationAnnotations = filterAnnotations(modelAnnotations,
                FHIMMetadataBinding.FHIM_ASSOCIATIONS_REFSET, StringMember.class);
        if (associationAnnotations.isEmpty()) {
            LOG.info("No FHIM_ASSOCIATIONS_REFSET member found.");
        } else {
            for (StringMember associationAnnotation : associationAnnotations) {
                Association a = buildAssociation(associationAnnotation);
                infoModel.addAssociation(a);
            }
        }
**/
        return infoModel;
    }

    private Association buildAssociation(StringMember associationAnnotation)
            throws ValidationException, IOException, ContradictionException {
        String name = associationAnnotation.getString1();
        Association a = new Association(name);
        LOG.debug("Association: " + name);
/** TODO - BAC
        // Association Ends.
        List<NidBooleanMember> associationEndAnnotations = filterAnnotations(
                getLatestAnnotations(associationAnnotation),
                FHIMMetadataBinding.FHIM_ASSOCIATIONENDS_REFSET, NidBooleanMember.class);
        if (associationEndAnnotations.isEmpty()) {
            LOG.info("No FHIM_ASSOCIATIONENDS_REFSET member found.");
        } else {
            for (NidBooleanMember associationEndAnnotation : associationEndAnnotations) {
                int memberEndNid = associationEndAnnotation.getNid1();
                boolean owned = associationEndAnnotation.getBoolean1();

                Attribute memberEnd = getAttribute(memberEndNid);
                if (memberEnd == null) {
                    LOG.warn("No memberEnd for " + memberEndNid);
                } else {
                    a.addMemberEnd(memberEnd);
                    a.setOwned(memberEnd, owned);
                }
            }
        }
**/
        return a;
    }

    private Dependency buildDependency(NidNidStringMember dependencyAnnotation) {
        String name = dependencyAnnotation.getStrValue();
        LOG.debug("Dependency: " + name);

        // Client.
        int clientNid = dependencyAnnotation.getC1Nid();
        Type client = getTypeForNid(clientNid);
        LOG.debug("    client: " + client.getName());

        // Supplier.
        int supplierNid = dependencyAnnotation.getC2Nid();
        Type supplier = getTypeForNid(supplierNid);
        LOG.debug("    supplier: " + supplier.getName());

        return new Dependency(name, client, supplier);
    }

    private Class buildClass(StringMember classAnnotation)
            throws ValidationException, IOException, ContradictionException {
        int nid = classAnnotation.getNid();
        Class c = getClass(nid);
        LOG.debug("Class: " + c.getName());

        /** TODO - BAC
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
**/
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

        // Visibility
        VisibilityKind visibility = getVisibility(attributeAnnotations);
        if (visibility != null) {
            LOG.debug("    visibility: " + visibility);
            a.setVisibility(visibility);
        }

        // Keep track for later.
        nidAttributeMap.put(attributeAnnotation.getNid(), a);

        return a;
    }

    private VisibilityKind getVisibility(Collection<? extends RefexChronicleBI<?>> attributeAnnotations)
            throws ValidationException, IOException {
      /** TODO - BAC
      StringMember visibilityAnnotation = getSingleAnnotation(attributeAnnotations,
                FHIMMetadataBinding.FHIM_VISIBILITY_REFSET, StringMember.class);

        // If none, abort.
        if (visibilityAnnotation == null) {
            return null;
        }

        String name = visibilityAnnotation.getString1();
        return VisibilityKind.valueOf(name);
        **/
      return null;
    }

    private Multiplicity buildMultiplicity(
            Collection<? extends RefexChronicleBI<?>> attributeAnnotations)
            throws IOException {
      /** TODO - BAC
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
        **/
      return null;
    }

    private String getDefaultValue(
            Collection<? extends RefexChronicleBI<?>> attributeAnnotations)
            throws ValidationException, IOException {
      /** TODO - BAC 
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
        **/ return null;
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
        LOG.debug("Attribute: " + e.getName());
/** TODO - BAC
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
**/
        return e;
    }


    private static Map<Integer, External> buildNidExternalMap()
            throws ValidationException, IOException {
        Map<Integer, External> m = Maps.newHashMap();

        External e = null;
/** TODO -BAC
        // Code.
        e = new External(CODE, FHIMMetadataBinding.FHIM_CODE);
        m.put(FHIMMetadataBinding.FHIM_CODE.getNid(), e);

        // ObservationQualifier.
        e = new External(OBSERVATION_QUALIFIER, FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER);
        m.put(FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER.getNid(), e);

        // ObservationStatement.
        e = new External(OBSERVATION_STATEMENT, FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT);
        m.put(FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT.getNid(), e);

        // PhysicalQuantity.
        e = new External(PHYSICAL_QUANTITY, FHIMMetadataBinding.FHIM_PHYSICALQUANTITY);
        m.put(FHIMMetadataBinding.FHIM_PHYSICALQUANTITY.getNid(), e);

        // PulsePosition.
        e = new External(PULSE_POSITION, FHIMMetadataBinding.FHIM_PULSEPOSITION);
        m.put(FHIMMetadataBinding.FHIM_PULSEPOSITION.getNid(), e);
**/
        return m;
    }
}
