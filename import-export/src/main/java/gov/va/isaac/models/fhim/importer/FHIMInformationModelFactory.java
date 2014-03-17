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

import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.FHIMInformationModel.Attribute;
import gov.va.isaac.models.fhim.FHIMInformationModel.External;
import gov.va.isaac.models.fhim.FHIMInformationModel.Multiplicity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
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
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class to create a {@link FHIMInformationModel} from a UML representation.
 *
 * @author ocarlsen
 */
public class FHIMInformationModelFactory {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMInformationModelFactory.class);

    private final Map<String, FHIMInformationModel.Enumeration> nameEnumerationMap = Maps.newHashMap();
    private final Map<String, FHIMInformationModel.Class> nameClassMap = Maps.newHashMap();
    private final Map<Property, FHIMInformationModel.Attribute> propertyAttributeMap = Maps.newHashMap();
    private final Map<String, FHIMInformationModel.External> nameExternalMap = Maps.newHashMap();

    public FHIMInformationModel createInformationModel(Package umlPackage) {
        FHIMInformationModel infoModel = new FHIMInformationModel(umlPackage.getName());

        // Gather Classes & Enumerations first, they will be used as types later.
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

        // Attribute type.
        Type umlType = umlAttribute.getType();
        String typeName = umlType.getName();
        FHIMInformationModel.Type type = getType(typeName);

        Attribute attribute = new Attribute(name, type);

        // Default value.
        ValueSpecification valueSpec = umlAttribute.getDefaultValue();
        String defaultValue = (valueSpec != null ? valueSpec.stringValue() : null);
        if (defaultValue != null) {
            attribute.setDefaultValue(defaultValue);
        }

        // Multiplicity.
        int lower = umlAttribute.getLower();
        int upper = umlAttribute.getUpper();
        Multiplicity multiplicity = new Multiplicity(lower, upper);
        attribute.setMultiplicity(multiplicity);

        return attribute;
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
        case "ObservationStatement": return FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT;
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
}
