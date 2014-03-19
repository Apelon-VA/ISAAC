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
import gov.va.isaac.models.fhim.importer.FHIMMetadataBinding;

import java.io.IOException;
import java.util.Map;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Class to create a UML {@link Package} from a {@link FHIMInformationModel}.
 *
 * @author ocarlsen
 */
public class Model2UMLConverter {

    private static final Logger LOG = LoggerFactory.getLogger(Model2UMLConverter.class);

    private final Map<FHIMInformationModel.Enumeration, Enumeration> modelEnumerationMap = Maps.newHashMap();
    private final Map<FHIMInformationModel.Class, Class> modelClassMap = Maps.newHashMap();
    private final Map<FHIMInformationModel.External, Class> externalClassMap = Maps.newHashMap();

    public Model2UMLConverter() {
        super();
    }

    public Package createUMLModel(FHIMInformationModel infoModel)
            throws ValidationException, IOException {
        String name = infoModel.getName();
        Package vitalSignsPkg = createPackage("VitalSigns");
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

        // Now flesh out Classes.
        for (FHIMInformationModel.Class classModel : infoModel.getClasses()) {
            createClass(pkg, classModel);
        }

        return vitalSignsPkg;
    }

    private Class createClass(Package pkg, FHIMInformationModel.Class classModel)
            throws ValidationException, IOException {
        Class clazz = modelClassMap.get(classModel);

        LOG.debug("Class: " + clazz.getName());

        // Attributes.
        for (Attribute attributeModel : classModel.getAttributes()) {
            createProperty(clazz, attributeModel);
        }

        // Generalizations.
        for (FHIMInformationModel.Generalization generalizationModel : classModel.getGeneralizations()) {
            createGeneralization(clazz, generalizationModel);
        }

        return clazz;
    }

    private Generalization createGeneralization(Class clazz,
            FHIMInformationModel.Generalization generalizationModel)
            throws ValidationException, IOException {
        FHIMInformationModel.Type typeModel = generalizationModel.getTarget();
        Type type = getTypeForModel(clazz.getPackage(), typeModel);

        // Expect type to be an instance of Class.
        if (type instanceof Class) {
            Class general = (Class) type;

            LOG.debug("Generalization: " + general.getName());

            return clazz.createGeneralization(general);
        }

        // Something wrong!
        throw new IllegalArgumentException("Unexpected type: " + type);
    }

    private Property createProperty(Class clazz, Attribute attributeModel)
            throws ValidationException, IOException {
        String name = attributeModel.getName();

        // Type.
        FHIMInformationModel.Type typeModel = attributeModel.getType();
        Type type = getTypeForModel(clazz.getPackage(), typeModel);
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

    private Type getTypeForModel(Package pkg, FHIMInformationModel.Type typeModel)
            throws ValidationException, IOException {
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
            type = getExternalClass(pkg, (External) typeModel);
            if (type != null) {
                return type;
            }
        }

        // Something wrong!
        throw new IllegalArgumentException("Could not find type for '" + typeModel + "'");
    }

    private Class getExternalClass(Package pkg, FHIMInformationModel.External externalModel)
            throws ValidationException, IOException {
        Class c = externalClassMap.get(externalModel);

        if (c == null) {
            ConceptSpec conceptSpec = externalModel.getConceptSpec();
            c = createExternalClass(pkg, conceptSpec);
            externalClassMap.put(externalModel, c);
        } else {
            LOG.trace("Cache hit: " + externalModel);
        }

        return c;
    }

    private Class createExternalClass(Package pkg, ConceptSpec conceptSpec)
            throws ValidationException, IOException {
        String className = null;

        // Find values for class name.
        int externalNid = conceptSpec.getNid();
        if (externalNid == FHIMMetadataBinding.FHIM_CODE.getNid()) {
            className = "Code";
        } else if (externalNid == FHIMMetadataBinding.FHIM_PHYSICALQUANTITY.getNid()) {
            className = "PhysicalQuantity";
        } else if (externalNid == FHIMMetadataBinding.FHIM_OBSERVATIONQUALIFIER.getNid()) {
            className = "ObservationQualifier";
        } else if (externalNid == FHIMMetadataBinding.FHIM_OBSERVATIONSTATEMENT.getNid()) {
            className = "ObservationStatement";
        } else if (externalNid == FHIMMetadataBinding.FHIM_PULSEPOSITION.getNid()) {
            className = "PulsePosition";
        } else {
            throw new IllegalArgumentException("Unrecognized conceptSpec: " + conceptSpec);
        }

        return pkg.createOwnedClass(className, false);  // Not abstract.
    }
}
