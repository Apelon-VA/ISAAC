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

import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a UML {@link Package} from a {@link FHIMInformationModel}.
 *
 * @author ocarlsen
 */
public class Model2UMLConverter {

    private static final Logger LOG = LoggerFactory.getLogger(Model2UMLConverter.class);

    public Package createUMLModel(FHIMInformationModel infoModel) {
        String name = infoModel.getName();
        Package pkg = createPackage(name);

        // Enumerations.
        for (FHIMInformationModel.Enumeration enumerationModel : infoModel.getEnumerations()) {
            createEnumeration(pkg, enumerationModel);
        }

        // TODO: Classes.

        // TODO: The rest.

        return pkg;
    }

    private static Enumeration createEnumeration(Package pkg, FHIMInformationModel.Enumeration enumerationModel) {
        String name = enumerationModel.getName();
        Enumeration enumeration = pkg.createOwnedEnumeration(name);
        LOG.debug("Enumeration: " + enumeration.getName());

        // EnumerationLiterals.
        for (String literal : enumerationModel.getLiterals()) {
            createEnumerationLiteral(enumeration, literal);
        }

        return enumeration;
    }

    private static EnumerationLiteral createEnumerationLiteral(Enumeration enumeration, String name) {
        EnumerationLiteral literal = enumeration.createOwnedLiteral(name);

        LOG.debug("EnumerationLiteral: " + literal.getName());

        return literal;
    }


    private static Package createPackage(String name) {
        Package pkg = UMLFactory.eINSTANCE.createPackage();
        pkg.setName(name);

        LOG.debug("Package: " + pkg.getName());

        return pkg;
    }
}
