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
package gov.va.isaac.models.fhim.converter;

import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.FHIMInformationModel.Attribute;
import gov.va.isaac.models.fhim.FHIMInformationModel.External;
import gov.va.isaac.models.fhim.FHIMInformationModel.Multiplicity;
import gov.va.isaac.models.fhim.FHIMUmlConstants;
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;

import java.util.Iterator;
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
import com.google.common.collect.Maps;

/**
 * Class to create a {@link FHIMInformationModel} from a UML representation.
 *
 * @author ocarlsen
 */
public class UML2ModelConverter implements FHIMUmlConstants {

    private static final Logger LOG = LoggerFactory.getLogger(UML2ModelConverter.class);

    private final Map<String, FHIMInformationModel.Enumeration> nameEnumerationMap = Maps.newHashMap();
    private final Map<String, FHIMInformationModel.Class> nameClassMap = Maps.newHashMap();
    private final Map<Property, FHIMInformationModel.Attribute> propertyAttributeMap = Maps.newHashMap();
    private final Map<String, FHIMInformationModel.External> nameExternalMap = Maps.newHashMap();

    public FHIMInformationModel createInformationModel(Package pkg) {
        FHIMInformationModel infoModel = new FHIMInformationModel(pkg.getName());

        // Gather Classes & Enumerations first, they will be used as types later.
        EList<PackageableElement> elements = pkg.getPackagedElements();
        for (Iterator<PackageableElement> i = elements.iterator(); i.hasNext();) {
            PackageableElement element = i.next();
            if (element instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) element;
                LOG.debug("Enumeration: " + enumeration.getName());

                FHIMInformationModel.Enumeration enumerationModel = createEnumerationModel(enumeration);
                infoModel.addEnumeration(enumerationModel);

                // Keep track of it for re-use later.
                nameEnumerationMap.put(enumerationModel.getName(), enumerationModel);

                // Remove from packaged elements, it is fully modeled.
                i.remove();
            } else if (element instanceof Class) {
                Class clazz = (Class) element;

                // Stub for now, we will flesh out later.
                FHIMInformationModel.Class classModel = new FHIMInformationModel.Class(clazz.getName());

                // Keep track of it for re-use later.
                nameClassMap.put(classModel.getName(), classModel);
            }
        }

        // Now that we have the requisite types, we can flesh out the rest.
        for (PackageableElement element : elements) {
            if (element instanceof Class) {
                Class clazz = (Class) element;
                FHIMInformationModel.Class classModel = buildClassModel(clazz);
                infoModel.addClass(classModel);
            } else if (element instanceof Dependency) {
                Dependency dependency = (Dependency) element;
                FHIMInformationModel.Dependency dependencyModel = buildDependencyModel(dependency);
                infoModel.addDependency(dependencyModel);
            } else if (element instanceof Association) {
                Association association = (Association) element;
                FHIMInformationModel.Association associationModel = buildAssociationModel(association);
                infoModel.addAssociation(associationModel);
            } else {
                LOG.warn("Unrecognized element: " + element);
            }
        }

        return infoModel;
    }

    private FHIMInformationModel.Association buildAssociationModel(Association association) {
        String name = association.getName();
        FHIMInformationModel.Association associationModel = new FHIMInformationModel.Association(name);
        LOG.debug("Association: " + name);

        // Expect binary associations.
        Preconditions.checkArgument(association.isBinary());

        // "Owned" ends.
        EList<Property> ownedEnds = association.getOwnedEnds();

        // Member ends.
        EList<Property> memberEnds = association.getMemberEnds();
        for (Property memberEnd : memberEnds) {
            LOG.debug("    memberEnd: " + memberEnd.getName());
            FHIMInformationModel.Attribute memberEndModel = getAttributeModel(memberEnd);
            associationModel.addMemberEnd(memberEndModel);

            boolean owned = ownedEnds.contains(memberEnd);
            LOG.debug("    owned: " + owned);
            associationModel.setOwned(memberEndModel, owned);
        }

        return associationModel;
    }

    private FHIMInformationModel.Dependency buildDependencyModel(Dependency dependency) {
        String name = dependency.getName();
        LOG.debug("Dependency: " + name);

        // Expect exactly one client.
        EList<NamedElement> clients = dependency.getClients();
        int clientCount = clients.size();
        if (clientCount != 1) {
            LOG.warn("Expected 1 client, found " + clientCount);
        }
        NamedElement client = clients.get(0);
        String clientName = client.getName();
        LOG.debug("    client: " + clientName);
        FHIMInformationModel.Type clientModel = getTypeModel(clientName);

        // Expect exactly one supplier.
        EList<NamedElement> suppliers = dependency.getSuppliers();
        int supplierCount = suppliers.size();
        if (supplierCount != 1) {
            LOG.warn("Expected 1 supplier, found " + supplierCount);
        }
        NamedElement supplier = suppliers.get(0);
        String supplierName = supplier.getName();
        LOG.debug("    supplier: " + supplierName);
        FHIMInformationModel.Type supplierModel = getTypeModel(supplierName);

        return new FHIMInformationModel.Dependency(name, clientModel, supplierModel);
    }

    private FHIMInformationModel.Class buildClassModel(Class clazz) {
        String name = clazz.getName();
        LOG.debug("Class: " + name);

        FHIMInformationModel.Class classModel = nameClassMap.get(name);

        // Generalizations.
        EList<Generalization> generalizations = clazz.getGeneralizations();
        for (Generalization generalization : generalizations) {
            FHIMInformationModel.Generalization generalizationModel = createGeneralizationModel(generalization);
            classModel.addGeneralization(generalizationModel);
        }

        // Attributes.
        EList<Property> attributes = clazz.getOwnedAttributes();
        for (Property attribute : attributes) {
            FHIMInformationModel.Attribute attributeModel = getAttributeModel(attribute);
            classModel.addAttribute(attributeModel);
        }

        return classModel;
    }

    private FHIMInformationModel.Generalization createGeneralizationModel(Generalization generalization) {
        LOG.debug("    generalization: ");

        // Expect exactly one source of type Class.
        EList<Element> sources = generalization.getSources();
        int sourceCount = sources.size();
        if (sourceCount != 1) {
            LOG.warn("Expected 1 source, found " + sourceCount);
        }
        Class source = (Class) sources.get(0);
        String sourceName = source.getName();
        LOG.debug("        source: " + sourceName);
        FHIMInformationModel.Type sourceModel = getTypeModel(sourceName);

        // Expect exactly one target of type Class.
        EList<Element> targets = generalization.getTargets();
        int targetCount = targets.size();
        if (targetCount != 1) {
            LOG.warn("Expected 1 target, found " + targetCount);
        }
        Class target = (Class) targets.get(0);
        String targetName = target.getName();
        LOG.debug("        target: " + targetName);
        FHIMInformationModel.Type targetModel = getTypeModel(targetName);

        return new FHIMInformationModel.Generalization(sourceModel, targetModel);
    }

    private Attribute getAttributeModel(Property property) {
        Attribute attribute = propertyAttributeMap.get(property);
        if (attribute == null) {
            attribute = createAttributeModel(property);
            propertyAttributeMap.put(property, attribute);
        } else {
            LOG.trace("Cache hit: " + property);
        }
        return attribute;
    }

    private Attribute createAttributeModel(Property property) {
        String name = property.getName();
        LOG.debug("    attribute: " + name);

        // Attribute type.
        Type type = property.getType();
        String typeName = type.getName();
        FHIMInformationModel.Type typeModel = getTypeModel(typeName);

        Attribute attributeModel = new Attribute(name, typeModel);

        // Default value.
        ValueSpecification valueSpec = property.getDefaultValue();
        String defaultValue = (valueSpec != null ? valueSpec.stringValue() : null);
        if (defaultValue != null) {
            attributeModel.setDefaultValue(defaultValue);
        }

        // Multiplicity.
        int lower = property.getLower();
        int upper = property.getUpper();
        Multiplicity multiplicity = new Multiplicity(lower, upper);
        attributeModel.setMultiplicity(multiplicity);

        return attributeModel;
    }

    private FHIMInformationModel.Type getTypeModel(String typeName) {
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
        modelType = getExernal(typeName);
        if (modelType != null) {
            return modelType;
        }

        // Something wrong!
        throw new IllegalArgumentException("Could not find type for '" + typeName + "'");
    }

    private External getExernal(String typeName) {
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
        case CODE: return FHIMMetadataBinding.FHIM_CODE;
        case OBSERVATION_QUALIFIER: return FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER;
        case OBSERVATION_STATEMENT: return FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT;
        case PHYSICAL_QUANTITY: return FHIMMetadataBinding.FHIM_PHYSICALQUANTITY;
        case PULSE_POSITION: return FHIMMetadataBinding.FHIM_PULSEPOSITION;
        // TODO: Others as required.
        default: return null;
        }
    }

    private FHIMInformationModel.Enumeration createEnumerationModel(Enumeration enumeration) {
        String name = enumeration.getName();
        FHIMInformationModel.Enumeration enumerationModel = new FHIMInformationModel.Enumeration(name);

        EList<EnumerationLiteral> literals = enumeration.getOwnedLiterals();
        for (EnumerationLiteral literal : literals) {
            String literalName = literal.getName();
            LOG.debug("    literal: " + literalName);
            enumerationModel.addLiteral(literalName);
        }
        return enumerationModel;
    }
}
