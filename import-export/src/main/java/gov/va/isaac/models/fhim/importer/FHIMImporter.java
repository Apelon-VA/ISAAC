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
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for importing a FHIM model from an XMI {@link File}.
 *
 * @author ocarlsen
 */
public class FHIMImporter extends ImporterBase implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FHIMImporter.class);

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

        String name = umlPackage.getName();
        FHIMInformationModel infoModel = new FHIMInformationModel(name , null);

        // Iterate through package and populate information model.
        EList<PackageableElement> elements = umlPackage.getPackagedElements();
        for (PackageableElement element : elements) {
            if (element instanceof Class) {
                Class c = (Class) element;
                System.out.println("Class: " + c.getName());

                EList<Generalization> generals = c.getGeneralizations();
                for (Generalization general : generals) {
                    System.out.println("general: " + general);
                    System.out.println("sources: " + general.getSources());
                    System.out.println("targets: " + general.getTargets());
                }

            } else if (element instanceof Enumeration) {
                Enumeration e = (Enumeration) element;
                System.out.println("Enumeration: " + e.getName());
            } else if (element instanceof Dependency) {
                Dependency d = (Dependency) element;
                System.out.println("Dependency: " + d.getName());
                EList<NamedElement> clients = d.getClients();
                System.out.println("clients: " + clients);
                EList<NamedElement> suppliers = d.getSuppliers();
                System.out.println("suppliers: " + suppliers);
            } else if (element instanceof Association) {
                Association a = (Association) element;
                System.out.println("Association: " + a.getName());
                System.out.println("isBinary="+a.isBinary());

                EList<Property> memberEnds = a.getMemberEnds();
                for (Property memberEnd : memberEnds) {
                    String endName = memberEnd.getName();
                    Type type = memberEnd.getType();
                    System.out.println("memberEnd: " + endName + " : " + type);
                }
                EList<Property> ownedEnds = a.getOwnedEnds();
                for (Property ownedEnd : ownedEnds) {
                    String endName = ownedEnd.getName();
                    Type type = ownedEnd.getType();
                    System.out.println("ownedEnd: " + endName + " : " + type);
                    System.out.println("isNavigable="+ownedEnd.isNavigable());
                }
            } else {
                System.err.println("Unrecognized element: " + element);
            }
        }

        return infoModel;
    }

    @SuppressWarnings("unused")
    private ConceptSpec getDataType(String dataTypeName) {
        switch (dataTypeName) {
        case "PhysicalQuantity": return FHIMMetadataBinding.FHIM_PHYSICALQUANTITY;
        // TODO: Others as required.
        default: return null;
        }
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
