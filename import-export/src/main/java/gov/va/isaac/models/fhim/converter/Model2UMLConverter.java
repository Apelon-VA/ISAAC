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
import gov.va.isaac.models.fhim.FHIMUmlConstants;
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class to create a UML {@link Package} from a {@link FHIMInformationModel}.
 *
 * @author ocarlsen
 */
public class Model2UMLConverter implements FHIMUmlConstants {

    private static final Logger LOG = LoggerFactory.getLogger(Model2UMLConverter.class);

    private final Map<FHIMInformationModel.Enumeration, Enumeration> modelEnumerationMap = Maps.newHashMap();
    private final Map<FHIMInformationModel.Class, Class> modelClassMap = Maps.newHashMap();
    private final Map<FHIMInformationModel.Attribute, Property> modelPropertyMap = Maps.newHashMap();
    private final Map<ConceptSpec, Class> conceptSpecClassMap;

    private final Package clinicalObservationPkg;
    private final Package datatypesPkg;
    private final Package vitalSignsPkg;
    private final Package pulsePkg;

    public Model2UMLConverter() {
        super();

        // Create packages for external types.
        this.clinicalObservationPkg = createPackage("ClinicalObservation");
        this.datatypesPkg = createPackage("Datatypes");
        this.vitalSignsPkg = createPackage("VitalSigns");
        this.pulsePkg = vitalSignsPkg.createNestedPackage("Pulse");

        // Build ConceptSpec-Class map.
        conceptSpecClassMap = buildConceptSpecClassMap();
    }

    public List<Package> getTopLevelPackages() {
        return Lists.newArrayList(clinicalObservationPkg, datatypesPkg, vitalSignsPkg);
    }

    public Package createUMLModel(FHIMInformationModel infoModel) {
        String name = infoModel.getName();
        Package pkg = vitalSignsPkg.createNestedPackage(name);

        // Enumerations.
        for (FHIMInformationModel.Enumeration enumerationModel : infoModel.getEnumerations()) {
            Enumeration e = createEnumeration(pkg, enumerationModel);

            // Keep track of it for re-use later.
            modelEnumerationMap.put(enumerationModel, e);
        }

        // Stub Classes, they will be used as types.
        for (FHIMInformationModel.Class classModel : infoModel.getClasses()) {

            // Stub for now, we will flesh out later.
            String className = classModel.getName();
            Class c = pkg.createOwnedClass(className, false);  // Not abstract.

            // Keep track of it for re-use later.
            modelClassMap.put(classModel, c);
        }

        // Associations will create some Properties that are shared by Classes.
        for (FHIMInformationModel.Association asociationModel : infoModel.getAssociations()) {
            createAssociation(pkg, asociationModel);
        }

        // Now flesh out Classes.
        for (FHIMInformationModel.Class classModel : infoModel.getClasses()) {
            createClass(pkg, classModel);
        }

        // Dependencies.
        for (FHIMInformationModel.Dependency dependencyModel : infoModel.getDependencies()) {
            createDependency(pkg, dependencyModel);
        }

        return vitalSignsPkg;
    }

    private Association createAssociation(Package pkg, FHIMInformationModel.Association associationModel) {
        String name = associationModel.getName();
        LOG.debug("Association: " + name);

        // To create a binary Association, we need two ends.
        // Use the "owned" end to create an Association to the "unowned" end.
        // Expect one of each.
        Attribute ownedEndModel = null;
        Attribute unownedEndModel = null;
        for (FHIMInformationModel.Attribute memberEnd : associationModel.getMemberEnds()) {
            boolean owned = associationModel.isOwned(memberEnd);
            if (owned) {
                if (ownedEndModel == null) {
                    ownedEndModel = memberEnd;
                } else {
                    LOG.warn("Expected one ownedEndModel, found another: " + ownedEndModel);
                }
            } else {
                if (unownedEndModel == null) {
                    unownedEndModel = memberEnd;
                } else {
                    LOG.warn("Expected one unownedEndModel, found another: " + unownedEndModel);
                }
            }
        }

        // Sanity check.
        Preconditions.checkNotNull(ownedEndModel, "Expected to find an ownedEndModel!");
        Preconditions.checkNotNull(unownedEndModel, "Expected to find an unownedEndModel!");

        // Use "owned" end for end1.
        boolean end1IsNavigable = false;  // false=owned
        AggregationKind end1Aggregation = AggregationKind.NONE_LITERAL;
        String end1Name = ownedEndModel.getName();
        int end1Lower = ownedEndModel.getMultiplicity().getLower();
        int end1Upper = ownedEndModel.getMultiplicity().getUpper();
        Type end1Type = getTypeForModel(ownedEndModel.getType());

        // Use "unowned" end for end2.
        boolean end2IsNavigable = true;  // true=unowned
        AggregationKind end2Aggregation = AggregationKind.NONE_LITERAL;
        String end2Name = unownedEndModel.getName();
        int end2Lower = unownedEndModel.getMultiplicity().getLower();
        int end2Upper = unownedEndModel.getMultiplicity().getUpper();
        Type end2Type = getTypeForModel(unownedEndModel.getType());

        // The Type#createAssociation API is awkward...
        Association association = end1Type.createAssociation(
                end2IsNavigable, end2Aggregation, end2Name, end2Lower, end2Upper,
                end2Type, end1IsNavigable, end1Aggregation, end1Name, end1Lower, end1Upper);

        // Two Properties should have been created, one "owned" and one "unowned".
        EList<Property> ownedEnds = association.getOwnedEnds();
        EList<Property> memberEnds = association.getMemberEnds();
        Preconditions.checkState(ownedEnds.size() == 1);
        Preconditions.checkState(memberEnds.size() == 2);

        // Save the Properties that were created for later.
        for (Property memberEnd : memberEnds) {
            if (ownedEnds.contains(memberEnd)) {
                modelPropertyMap.put(ownedEndModel, memberEnd);
            } else {
                modelPropertyMap.put(unownedEndModel, memberEnd);
            }
        }

        return association;
    }

    private Dependency createDependency(Package pkg, FHIMInformationModel.Dependency dependencyModel) {
        String name = dependencyModel.getName();
        LOG.debug("Dependency: " + name);

        // Client.
        FHIMInformationModel.Type clientModel = dependencyModel.getClient();
        Type client = getTypeForModel(clientModel);
        LOG.debug("    client: " + client.getName());

        // Supplier.
        FHIMInformationModel.Type supplierModel = dependencyModel.getSupplier();
        Type supplier = getTypeForModel(supplierModel);
        LOG.debug("    supplier: " + supplier.getName());

        Dependency dependency = client.createDependency(supplier);
        dependency.setName(name);

        return dependency;
    }

    private Class createClass(Package pkg, FHIMInformationModel.Class classModel) {
        Class clazz = modelClassMap.get(classModel);

        LOG.debug("Class: " + clazz.getName());

        // Attributes.
        for (Attribute attributeModel : classModel.getAttributes()) {
            getProperty(clazz, attributeModel);
        }

        // Generalizations.
        for (FHIMInformationModel.Generalization generalizationModel : classModel.getGeneralizations()) {
            createGeneralization(clazz, generalizationModel);
        }

        return clazz;
    }

    private Generalization createGeneralization(Class clazz,
            FHIMInformationModel.Generalization generalizationModel) {
        FHIMInformationModel.Type typeModel = generalizationModel.getTarget();
        Type type = getTypeForModel(typeModel);

        // Expect type to be an instance of Class.
        if (type instanceof Class) {
            Class general = (Class) type;

            LOG.debug("Generalization: " + general.getName());

            return clazz.createGeneralization(general);
        }

        // Something wrong!
        throw new IllegalArgumentException("Unexpected type: " + type);
    }

    private Property getProperty(Class clazz, Attribute attributeModel) {
        Property property = modelPropertyMap.get(attributeModel);
        if (property == null) {
            property = createProperty(clazz, attributeModel);
            modelPropertyMap.put(attributeModel, property);
        } else {
            LOG.trace("Cache hit: " + attributeModel);
        }
        return property;
    }

    private Property createProperty(Class clazz, Attribute attributeModel) {
        String name = attributeModel.getName();

        // Type.
        FHIMInformationModel.Type typeModel = attributeModel.getType();
        Type type = getTypeForModel(typeModel);
        Property property = clazz.createOwnedAttribute(name, type);

        LOG.debug("Property: " + property.getName());

        // DefaultValue.
        String defaultValue = attributeModel.getDefaultValue();
        if (defaultValue != null) {
            LOG.debug("    defaultValue: " + defaultValue);
            property.setStringDefaultValue(defaultValue);
        }

        // Upper, lower.
        FHIMInformationModel.Multiplicity multiplicity = attributeModel.getMultiplicity();
        if (multiplicity != null) {
            int upper = multiplicity.getUpper();
            property.setUpper(upper);
            int lower = multiplicity.getLower();
            property.setLower(lower);
        }

        return property;
    }

    private Enumeration createEnumeration(Package pkg, FHIMInformationModel.Enumeration enumerationModel) {
        String name = enumerationModel.getName();
        Enumeration enumeration = pkg.createOwnedEnumeration(name);

        LOG.debug("Enumeration: " + enumeration.getName());

        // EnumerationLiterals.
        for (String literal : enumerationModel.getLiterals()) {
            createEnumerationLiteral(enumeration, literal);
        }

        return enumeration;
    }

    private EnumerationLiteral createEnumerationLiteral(Enumeration enumeration, String name) {
        EnumerationLiteral literal = enumeration.createOwnedLiteral(name);

        LOG.debug("EnumerationLiteral: " + literal.getName());

        return literal;
    }


    private Package createPackage(String name) {
        Package pkg = UMLFactory.eINSTANCE.createPackage();
        pkg.setName(name);

        LOG.debug("Package: " + pkg.getName());

        return pkg;
    }

    private Type getTypeForModel(FHIMInformationModel.Type typeModel) {
        Type type = null;

        // Try Enumerations first.
        type = modelEnumerationMap.get(typeModel);
        if (type != null) {
            return type;
        }

        // Try Classes next.
        type = modelClassMap.get(typeModel);
        if (type != null) {
            return type;
        }

        // Try OTF metadata concept type.
        if (typeModel instanceof External) {
            ConceptSpec conceptSpec = ((External) typeModel).getConceptSpec();
            type = conceptSpecClassMap.get(conceptSpec);
            if (type != null) {
                return type;
            }
        }

        // Something wrong!
        throw new IllegalArgumentException("Could not find type for '" + typeModel + "'");
    }

    private Map<ConceptSpec, Class> buildConceptSpecClassMap() {
        Map<ConceptSpec, Class> m = Maps.newHashMap();

        m.put(FHIMMetadataBinding.FHIM_CODE,
                datatypesPkg.createOwnedClass(CODE, false));  // Not abstract.
        m.put(FHIMMetadataBinding.FHIM_PHYSICALQUANTITY,
                datatypesPkg.createOwnedClass(PHYSICAL_QUANTITY, false));  // Not abstract.
        m.put(FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER,
                clinicalObservationPkg.createOwnedClass(OBSERVATION_QUALIFIER, false));  // Not abstract.
        m.put(FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT,
                clinicalObservationPkg.createOwnedClass(OBSERVATION_STATEMENT, false));  // Not abstract.
        m.put(FHIMMetadataBinding.FHIM_PULSEPOSITION,
                pulsePkg.createOwnedClass(PULSE_POSITION, false));  // Not abstract.

        return m;
    }
}
