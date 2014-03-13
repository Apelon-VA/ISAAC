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
import gov.va.isaac.models.fhim.FHIMInformationModel.Attribute;
import gov.va.isaac.models.fhim.FHIMInformationModel.External;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ValueSpecification;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class for importing a FHIM model from an XMI {@link File}.
 *
 * @author ocarlsen
 */
public class FHIMImporter extends ImporterBase implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMImporter.class);

    private final Map<String, FHIMInformationModel.Enumeration> nameEnumerationMap = Maps.newHashMap();
    private final Map<String, FHIMInformationModel.Class> nameClassMap = Maps.newHashMap();
    private final Map<Property, FHIMInformationModel.Attribute> propertyAttributeMap = Maps.newHashMap();
    private final Map<String, FHIMInformationModel.External> nameExternalMap = Maps.newHashMap();

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

        // Parse UML model into a POJO.
        FHIMInformationModel infoModel = createInformationModel(bloodPressurePackage);

        // Write POJO to database as Refsets.
        persistAsRefsets(focusConcept, infoModel);

        LOG.debug("Long form after commit:" + focusConcept.toLongString());
        LOG.info("Ending import of FHIM model from: " + file.getName());

        return focusConcept;
    }

    private void persistAsRefsets(ComponentChronicleBI<?> focusComponent,
            FHIMInformationModel infoModel)
            throws IOException, InvalidCAB, ContradictionException {

        // TODO: Implement
    }

    private FHIMInformationModel createInformationModel(Package umlPackage) {

        FHIMInformationModel infoModel = new FHIMInformationModel(umlPackage.getName() , null);

        // Gather Classes & Enumerations first, they will be used as types
        // by the Attributes & Relationships.
        EList<PackageableElement> umlElements = umlPackage.getPackagedElements();
        for (Iterator<PackageableElement> i = umlElements.iterator(); i.hasNext();) {
            PackageableElement umlElement = i.next();
            if (umlElement instanceof Enumeration) {
                Enumeration umlEnumeration = (Enumeration) umlElement;
                LOG.debug("Enumeration: " + umlEnumeration.getName());

                FHIMInformationModel.Enumeration enumeration = createEnumeration(umlEnumeration);
                infoModel.addEnumeration(enumeration);

                // Keep track of it for re-use later.
                nameEnumerationMap.put(enumeration.getName(), enumeration);

                // Remove from packaged elements, it is fully modeled.
                i.remove();
            } else if (umlElement instanceof Class) {
                Class umlClass = (Class) umlElement;

                // Stub for now, we will flesh out later.
                FHIMInformationModel.Class clazz = new FHIMInformationModel.Class(umlClass.getName());

                // Keep track of it for re-use later.
                nameClassMap.put(clazz.getName(), clazz);
            }
        }

        // Now that we have the requisite types, we can flesh out the rest.
        for (PackageableElement umlElement : umlElements) {
            if (umlElement instanceof Class) {
                Class umlClass = (Class) umlElement;
                FHIMInformationModel.Class clazz = buildClass(umlClass);
                infoModel.addClass(clazz);
            } else if (umlElement instanceof Dependency) {
                Dependency umlDependency = (Dependency) umlElement;
                FHIMInformationModel.Dependency dependency = buildDependency(umlDependency);
                infoModel.addDependency(dependency);
            } else if (umlElement instanceof Association) {
                Association umlAssociation = (Association) umlElement;
                FHIMInformationModel.Association association = buildAssociation(umlAssociation);
                infoModel.addAssociation(association);
            } else {
                LOG.warn("Unrecognized element: " + umlElement);
            }
        }

        return infoModel;
    }

    private FHIMInformationModel.Association buildAssociation(Association umlAssociation) {
        String name = umlAssociation.getName();
        LOG.debug("Association: " + name);

        // Expect binary associations.
        Preconditions.checkArgument(umlAssociation.isBinary());

        // Expect exactly one "owned" end.
        EList<Property> umlOwnedEnds = umlAssociation.getOwnedEnds();
        int umlOwnedEndCount = umlOwnedEnds.size();
        if (umlOwnedEndCount != 1) {
            LOG.warn("Expected 1 umlOwnedEnd, found " + umlOwnedEndCount);
        }
        Property umlOwnedEnd = umlOwnedEnds.get(0);
        LOG.debug("    umlOwnedEnd: " + umlOwnedEnd);
        FHIMInformationModel.Attribute ownedEnd = getAttribute(umlOwnedEnd);

        // Expect exactly one end left over after removing "owned" one.
        EList<Property> memberEnds = umlAssociation.getMemberEnds();
        List<Property> umlUnownedEnds = Lists.newArrayList(memberEnds);
        umlUnownedEnds.remove(umlOwnedEnd);
        int unownedEndCount = umlUnownedEnds.size();
        if (unownedEndCount != 1) {
            LOG.warn("Expected 1 unownedEnd, found " + unownedEndCount);
        }
        Property umlUnownedEnd = umlUnownedEnds.get(0);
        LOG.debug("    umlUnownedEnd: " + umlUnownedEnd);
        FHIMInformationModel.Attribute unownedEnd = getAttribute(umlUnownedEnd);

        return new FHIMInformationModel.Association(name, ownedEnd, unownedEnd);
    }

    private FHIMInformationModel.Dependency buildDependency(Dependency umlDependency) {
        String name = umlDependency.getName();
        LOG.debug("Dependency: " + name);

        // Expect exactly one client.
        EList<NamedElement> clients = umlDependency.getClients();
        int clientCount = clients.size();
        if (clientCount != 1) {
            LOG.warn("Expected 1 client, found " + clientCount);
        }
        NamedElement umlClient = clients.get(0);
        String clientName = umlClient.getName();
        LOG.debug("    client: " + clientName);
        FHIMInformationModel.Type client = getType(clientName);

        // Expect exactly one supplier.
        EList<NamedElement> suppliers = umlDependency.getSuppliers();
        int supplierCount = suppliers.size();
        if (supplierCount != 1) {
            LOG.warn("Expected 1 supplier, found " + supplierCount);
        }
        NamedElement umlSupplier = suppliers.get(0);
        String supplierName = umlSupplier.getName();
        LOG.debug("    supplier: " + supplierName);
        FHIMInformationModel.Type supplier = getType(supplierName);

        return new FHIMInformationModel.Dependency(name, client, supplier);
    }

    private FHIMInformationModel.Class buildClass(Class umlClass) {
        String name = umlClass.getName();
        LOG.debug("Class: " + name);

        FHIMInformationModel.Class clazz = nameClassMap.get(name);

        // Generalizations.
        EList<Generalization> umlGenerals = umlClass.getGeneralizations();
        for (Generalization umlGeneral : umlGenerals) {
            FHIMInformationModel.Generalization generalization = createGeneralization(umlGeneral);
            clazz.addGeneralization(generalization);
        }

        // Attributes.
        EList<Property> umlAttributes = umlClass.getOwnedAttributes();
        for (Property umlAttribute : umlAttributes) {
            FHIMInformationModel.Attribute attribute = getAttribute(umlAttribute);
            clazz.addAttribute(attribute);
        }

        return clazz;
    }

    private FHIMInformationModel.Generalization createGeneralization(Generalization umlGeneral) {
        LOG.debug("    generalization: ");

        // Expect exactly one source of type Class.
        EList<Element> sources = umlGeneral.getSources();
        int sourceCount = sources.size();
        if (sourceCount != 1) {
            LOG.warn("Expected 1 source, found " + sourceCount);
        }
        Class umlSource = (Class) sources.get(0);
        String sourceName = umlSource.getName();
        LOG.debug("        source: " + sourceName);
        FHIMInformationModel.Type source = getType(sourceName);

        // Expect exactly one target of type Class.
        EList<Element> targets = umlGeneral.getTargets();
        int targetCount = targets.size();
        if (targetCount != 1) {
            LOG.warn("Expected 1 target, found " + targetCount);
        }
        Class umlTarget = (Class) targets.get(0);
        String targetName = umlTarget.getName();
        LOG.debug("        target: " + targetName);
        FHIMInformationModel.Type target = getType(targetName);

        return new FHIMInformationModel.Generalization(source, target);
    }

    private Attribute getAttribute(Property umlAttribute) {
        Attribute attribute = propertyAttributeMap.get(umlAttribute);
        if (attribute == null) {
            attribute = createAttribute(umlAttribute);
            propertyAttributeMap.put(umlAttribute, attribute);
        } else {
            LOG.trace("Cache hit: " + umlAttribute);
        }
        return attribute;
    }

    private Attribute createAttribute(Property umlAttribute) {
        String name = umlAttribute.getName();
        LOG.debug("    attribute: " + name);

        // Need to figure correct model Type for attribute.
        Type umlType = umlAttribute.getType();
        String typeName = umlType.getName();
        FHIMInformationModel.Type type = getType(typeName);

        ValueSpecification valueSpec = umlAttribute.getDefaultValue();
        String defaultValue = (valueSpec != null ? valueSpec.stringValue() : "");

        return new Attribute(name, type, defaultValue);
    }

    private FHIMInformationModel.Type getType(String typeName) {
        FHIMInformationModel.Type modelType = null;

        // Try Enumerations first.
        modelType = nameEnumerationMap.get(typeName);
        if (modelType != null) {
            return modelType;
        }

        // Try Classes next.
        modelType = nameClassMap.get(typeName);
        if (modelType != null) {
            return modelType;
        }

        // Try OTF metadata concept type.
        modelType = getExernalType(typeName);
        if (modelType != null) {
            return modelType;
        }

        // Something wrong!
        throw new IllegalArgumentException("Could not find type for '" + typeName + "'");
    }

    private FHIMInformationModel.Type getExernalType(String typeName) {
        FHIMInformationModel.External external = nameExternalMap.get(typeName);
        if (external == null) {
            ConceptSpec conceptSpec = Preconditions.checkNotNull(getConceptSpec(typeName));
            external = new External(typeName, conceptSpec);
            nameExternalMap.put(typeName, external);
        } else {
            LOG.trace("Cache hit: " + typeName);
        }
        return external;
    }

    private ConceptSpec getConceptSpec(String dataTypeName) {
        switch (dataTypeName) {
        case "Code": return FHIMMetadataBinding.FHIM_CODE;
        case "ObservationQualifier": return FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER;
        case "ObservationStatement": return FHIMMetadataBinding.FHIM_CODE;
        case "PhysicalQuantity": return FHIMMetadataBinding.FHIM_PHYSICALQUANTITY;
        case "PulsePosition": return FHIMMetadataBinding.FHIM_PULSEPOSITION;
        // TODO: Others as required.
        default: return null;
        }
    }

    private FHIMInformationModel.Enumeration createEnumeration(Enumeration umlEnumeration) {
        String name = umlEnumeration.getName();
        FHIMInformationModel.Enumeration enumeration = new FHIMInformationModel.Enumeration(name);

        EList<EnumerationLiteral> umlLiterals = umlEnumeration.getOwnedLiterals();
        for (EnumerationLiteral umlLiteral : umlLiterals) {
            String literal = umlLiteral.getName();
            LOG.debug("    literal: " + literal);
            enumeration.addLiteral(literal);
        }
        return enumeration;
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

        // Copied from UML2 tutorial.
        resource.load(null);
        Package model = (Package) resource.getContents().get(0);
        LOG.info("Loaded '" + model.getQualifiedName() + "' from '" + fileURI + "'.");

        return model;
    }

}
