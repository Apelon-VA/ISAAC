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
package gov.va.isaac.models.fhim.importer;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.ImportHandler;
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
import gov.va.isaac.models.fhim.converter.UML2ModelConverter;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Class for importing a FHIM model from an XMI {@link File}.
 *
 * @author ocarlsen
 */
public class FHIMImporter extends ImporterBase implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMImporter.class);

    private final Map<Enumeration, RefexChronicleBI<?>> enumerationRefexMap = Maps.newHashMap();
    private final Map<Class, RefexChronicleBI<?>> classRefexMap = Maps.newHashMap();
    private final Map<Attribute, RefexChronicleBI<?>> attributeRefexMap = Maps.newHashMap();

    public FHIMImporter() throws ValidationException, IOException {
        super();
    }

    @Override
    public ConceptChronicleBI importModel(File file) throws Exception {
        LOG.info("Preparing to import FHIM model from: " + file.getName());

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
            Preconditions.checkState(annotation.getAssemblageNid() != FHIMMetadataBinding.FHIM_MODELS_REFSET.getNid(),
                    "FHIM import has already been performed on " + focusConceptUuid);
        }

        // Load UML model from file.
        Package umlModel = loadModel(file);

        // Locate "BloodPressure" package
        Package bloodPressurePackage = umlModel.getNestedPackage("BloodPressure");

        // Parse into FHIM model.
        UML2ModelConverter converter = new UML2ModelConverter();
        FHIMInformationModel infoModel = converter.createInformationModel(bloodPressurePackage);

        // Annotate focusConcept with Refset members.
        annotateWithRefsets(focusConcept, infoModel);

        getDataStore().addUncommitted(focusConcept);
        getDataStore().commit();

        LOG.debug("Long form after commit:" + focusConcept.toLongString());
        LOG.info("Ending import of CEM model from: " + file.getName());

        return focusConcept;
    }

    private void annotateWithRefsets(ComponentChronicleBI<?> focusComponent,
            FHIMInformationModel infoModel)
            throws IOException, InvalidCAB, ContradictionException {

        // FHIM Models refset.
        String modelName = infoModel.getName();
        LOG.debug("Adding refex for model: " + modelName);
        RefexChronicleBI<?> modelRefex = addRefexInStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_MODELS_REFSET, modelName);

        // Persist Classes and Enumerations first, they will be used as types later.

        // FHIM Enumerations refset.
        List<Enumeration> enumerations = infoModel.getEnumerations();
        for (Enumeration enumeration : enumerations) {
            RefexChronicleBI<?> enumRefex = addEnumerationsRefsetMember(
                    modelRefex, enumeration);

            // FHIM EnumerationValues refset.
            List<String> values = enumeration.getLiterals();
            for (String value : values) {
                addEnumerationValuesRefsetMember(enumRefex, value);
            }
        }

        // FHIM Classes refset.
        List<Class> classes = infoModel.getClasses();
        for (Class clazz : classes) {
            addClassesRefsetMember(modelRefex, clazz);
        }

        // Now go back through and finish the Generalizations & Attributes.
        for (Class clazz : classes) {
            RefexChronicleBI<?> classRefex = Preconditions.checkNotNull(getClassRefex(clazz));

            // FHIM Generalizations refset.
            List<Generalization> generalizations = clazz.getGeneralizations();
            for (Generalization generalization : generalizations) {
                addGeneralizationsRefsetMember(classRefex, generalization);
            }

            // FHIM Attributes refset.
            List<Attribute> attributes = clazz.getAttributes();
            for (Attribute attribute : attributes) {
                RefexChronicleBI<?> attributeRefex = addAttributesRefsetMember(
                        classRefex, attribute);

                // FHIM DefaultValue refset.
                String defaultValue = attribute.getDefaultValue();
                if (defaultValue != null) {
                    addDefaultValueRefsetMember(attributeRefex, defaultValue);
                }

                // FHIM Multiplicity refset.
                Multiplicity multiplicity = attribute.getMultiplicity();
                if (multiplicity != null) {
                    int lower = multiplicity.getLower();
                    addMultiplicityRefsetMember(attributeRefex, FHIMMetadataBinding.FHIM_LOWER, lower);
                    int upper = multiplicity.getUpper();
                    addMultiplicityRefsetMember(attributeRefex, FHIMMetadataBinding.FHIM_UPPER, upper);
                }
            }
        }

        // Dependencies.
        List<Dependency> dependencies = infoModel.getDependencies();
        for (Dependency dependency : dependencies) {
            addDependenciesRefsetMember(modelRefex, dependency);
        }

        // Associations.
        List<Association> associations = infoModel.getAssociations();
        for (Association association : associations) {
            RefexChronicleBI<?> associationRefex = addAssociationsRefsetMember(modelRefex, association);

            // Association Ends.
            for (Attribute memberEnd : association.getMemberEnds()) {
                RefexChronicleBI<?> memberEndRefex = getAttributeRefex(memberEnd);

                // Sometimes the owned ends are orphaned and no attribute refset member
                // would have been created by this point.  Create one now.
                if (memberEndRefex == null) {
                    memberEndRefex = addAttributesRefsetMember(modelRefex, memberEnd);
                }

                boolean owned = association.isOwned(memberEnd);
                addAssociationEndsRefsetMember(associationRefex, memberEnd, owned);
            }
        }
    }

    private RefexChronicleBI<?> addAssociationEndsRefsetMember(RefexChronicleBI<?> focusComponent,
            Attribute memberEnd, boolean owned)
            throws IOException, InvalidCAB, ContradictionException {
        String memberEndName = memberEnd.getName();
        LOG.debug("Adding refex for memberEnd: " + memberEndName);

        RefexChronicleBI<?> memberEndRefex = getAttributeRefex(memberEnd);
        int memberEndNid = memberEndRefex.getNid();

        return addRefexInCidBooleanExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_ASSOCIATIONENDS_REFSET, memberEndNid, owned);
    }

    private RefexChronicleBI<?> addAssociationsRefsetMember(RefexChronicleBI<?> focusComponent,
            Association association)
            throws ValidationException, IOException, InvalidCAB, ContradictionException {
        String associationName = association.getName();
        LOG.debug("Adding refex for association: " + associationName);

        return addRefexInStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_ASSOCIATIONS_REFSET, associationName);
    }

    private RefexChronicleBI<?> addDependenciesRefsetMember(RefexChronicleBI<?> focusComponent,
            Dependency dependency)
            throws ValidationException, IOException, InvalidCAB, ContradictionException {
        String dependencyName = dependency.getName();
        LOG.debug("Adding refex for dependency: " + dependencyName);

        Type client = dependency.getClient();
        Type supplier = dependency.getClient();

        // Need to find appropriate NIDs.
        int clientNid = getNidForType(client);
        int supplierNid = getNidForType(supplier);

        return addRefexInCidCidStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_DEPENDENCIES_REFSET, clientNid, supplierNid, dependencyName);
    }

    private RefexChronicleBI<?> addMultiplicityRefsetMember(RefexChronicleBI<?> focusComponent,
            ConceptSpec multiplicityType, int value)
            throws IOException, InvalidCAB, ContradictionException {
        LOG.debug("Adding refex for multiplicity: " + value + " for type " + multiplicityType);
        return addRefexInCidIntExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_MULTIPLICITY_REFSET,
                multiplicityType, value);
    }

    private RefexChronicleBI<?> addDefaultValueRefsetMember(
            RefexChronicleBI<?> focusComponent, String defaultValue)
            throws IOException, InvalidCAB, ContradictionException {
        LOG.debug("Adding refex for default value: " + defaultValue);
        return addRefexInStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_DEFAULTVALUES_REFSET, defaultValue);
    }

    private RefexChronicleBI<?> addAttributesRefsetMember(
            RefexChronicleBI<?> focusComponent, Attribute attribute)
            throws ValidationException, IOException, InvalidCAB, ContradictionException {
        String attributeName = attribute.getName();
        LOG.debug("Adding refex for attribute: " + attributeName);

        // Need to find appropriate NID.
        Type attributeType = attribute.getType();
        int attributeTypeNid = getNidForType(attributeType);

        RefexChronicleBI<?> attributeRefex = addRefexInCidStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_ATTRIBUTES_REFSET, attributeTypeNid, attributeName);

        // Keep track of it for re-use later.
        attributeRefexMap.put(attribute, attributeRefex);

        return attributeRefex;
    }

    private RefexChronicleBI<?> addGeneralizationsRefsetMember(
            RefexChronicleBI<?> focusComponent, Generalization generalization)
            throws ValidationException, IOException, InvalidCAB, ContradictionException {
        Type source = generalization.getSource();
        Type target = generalization.getTarget();
        LOG.debug("Adding refex for generalization: " + source.getName() + " -> " + target.getName());

        // Need to find appropriate NID.
        int targetNid = getNidForType(target);

        return addRefexInCidExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_GENERALIZATIONS_REFSET, targetNid);
    }

    private RefexChronicleBI<?> addEnumerationValuesRefsetMember(
            RefexChronicleBI<?> focusComponent, String value)
            throws IOException, InvalidCAB, ContradictionException {
        LOG.debug("Adding refex for enumeration value: " + value);
        return addRefexInStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_ENUMERATIONVALUES_REFSET, value);
    }

    private RefexChronicleBI<?> addClassesRefsetMember(
            RefexChronicleBI<?> focusComponent, Class clazz)
            throws IOException, InvalidCAB, ContradictionException {
        String className = clazz.getName();
        LOG.debug("Adding refex for class: " + className);

        RefexChronicleBI<?> classRefex = addRefexInStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_CLASSES_REFSET, className);

        // Keep track of it for re-use later.
        classRefexMap.put(clazz, classRefex);

        return classRefex;
    }

    private RefexChronicleBI<?> addEnumerationsRefsetMember(
            RefexChronicleBI<?> focusComponent, Enumeration enumeration)
            throws IOException, InvalidCAB, ContradictionException {
        String name = enumeration.getName();
        LOG.debug("Adding refex for enumeration " + name);

        RefexChronicleBI<?> enumRefex = addRefexInStrExtensionRefset(focusComponent,
                FHIMMetadataBinding.FHIM_ENUMERATIONS_REFSET, name);

        // Keep track of it for re-use later.
        enumerationRefexMap.put(enumeration, enumRefex);

        return enumRefex;
    }

    private int getNidForType(Type type) throws ValidationException,
            IOException {
        if (type instanceof External) {
            return ((External) type).getConceptSpec().getNid();
        } else if (type instanceof Class) {
            Class c = (Class) type;
            RefexChronicleBI<?> typeRefex = Preconditions.checkNotNull(getClassRefex(c));
            return typeRefex.getNid();
        } else if (type instanceof Enumeration) {
            Enumeration e = (Enumeration) type;
            RefexChronicleBI<?> typeRefex = Preconditions.checkNotNull(getEnumerationRefex(e));
            return typeRefex.getNid();
        } else if (type instanceof Attribute) {
            Attribute a = (Attribute) type;
            RefexChronicleBI<?> attributeRefex = Preconditions.checkNotNull(getAttributeRefex(a));
            return attributeRefex.getNid();
        } else {
            throw new IllegalStateException("Unexpected target: " + type.getClass());
        }
    }

    private RefexChronicleBI<?> getClassRefex(Class c) {
        return classRefexMap.get(c);
    }

    private RefexChronicleBI<?> getEnumerationRefex(Enumeration e) {
        return enumerationRefexMap.get(e);
    }

    private RefexChronicleBI<?> getAttributeRefex(Attribute a) {
        return attributeRefexMap.get(a);
    }

    private Package loadModel(File file) throws IOException {

        URI fileURI = URI.createFileURI(file.getAbsolutePath());

        // Create a resource-set to contain the resource(s) that we are saving
        ResourceSet resourceSet = new ResourceSetImpl();

        // Initialize registrations of resource factories, library models,
        // profiles, Ecore metadata, and other dependencies required for
        // serializing and working with UML resources. This is only necessary in
        // applications that are not hosted in the Eclipse platform run-time, in
        // which case these registrations are discovered automatically from
        // Eclipse extension points.
        UMLResourcesUtil.init(resourceSet);

        Resource resource = resourceSet.createResource(fileURI);

        // And load.
        Map<?, ?> options = null;   // No load options needed.
        resource.load(options);
        Package model = (Package) resource.getContents().get(0);
        LOG.info("Loaded '" + model.getQualifiedName() + "' from '" + fileURI + "'.");

        return model;
    }

}
