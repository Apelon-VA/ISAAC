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
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.converter.UML2ModelConverter;
import gov.va.isaac.models.util.ImporterBase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ImportHandler} for importing FHIM models from an XML file.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public class FHIMImporter extends ImporterBase implements ImportHandler {

    /**  The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(FHIMImporter.class);

    /**
     * Instantiates an empty {@link FHIMImporter}.
     */
    public FHIMImporter() {
        super();
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.ie.ImportHandler#importModel(java.io.File)
     */
    @Override
    public InformationModel importModel(File file) throws Exception {
        LOG.info("Preparing to import FHIM model from: " + file.getName());

        // Make sure in background thread.
        FxUtils.checkBackgroundThread();

        InformationModelService service = getInformationModelService();

        /**
        // Get focus concept.
        String focusConceptUuid = "215fd598-e21d-3e27-a0a2-8e23b1b36dfc";
        ConceptChronicleBI focusConcept = getDataStore().getConcept(UUID.fromString(focusConceptUuid));
        LOG.info("focusConcept: " + focusConcept.toString());

        // Throw exception if import already performed.
        ConceptChronicleBI modelsRefsetConcept = getDataStore().getConcept(FHIMMetadataBinding.FHIM_MODELS_REFSET.getNid());
        Collection<? extends RefexChronicleBI<?>> modelsRefsetMembers = modelsRefsetConcept.getRefsetMembers();
        if (! modelsRefsetMembers.isEmpty()) {
            int focusConceptNid = focusConcept.getNid();
            for (RefexChronicleBI<?> modelsRefsetMember : modelsRefsetMembers) {
                Preconditions.checkState(modelsRefsetMember.getReferencedComponentNid() != focusConceptNid,
                        "FHIM import has already been performed on " + focusConceptUuid);
            }
        }
         **/
        // Load UML model from file.
        String packageName = "VitalSigns";
        Package umlModel = loadModel(file, packageName);

        // Abort if not available.
        if (umlModel == null) {
            throw new IllegalStateException("No UML Package found: " + packageName);
        }

        // Locate "BloodPressure" package.
        packageName = "BloodPressure";
        Package bloodPressurePackage = umlModel.getNestedPackage(packageName);

        // Abort if not available.
        if (bloodPressurePackage == null) {
            throw new IllegalStateException("No UML Package found: " + packageName);
        }

        // Parse into FHIM model.
        UML2ModelConverter converter = new UML2ModelConverter();
        FHIMInformationModel infoModel = converter.createInformationModel(bloodPressurePackage);

        service.saveInformationModel(infoModel);

        LOG.info("Ending import of FHIM model from: " + file.getName());

        return infoModel;
    }


    /**
     * Load model from XMI.
     *
     * @param file the file
     * @param packageName the package name
     * @return the package
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Package loadModel(File file, String packageName) throws IOException {

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

        // Look for Package with specified name.
        EList<EObject> contents = resource.getContents();
        for (EObject content : contents) {
            if (content instanceof Package) {
                Package model = (Package) content;
                if (model.getName().equals(packageName)) {
                    LOG.info("Loaded '" + model.getQualifiedName() + "' from '" + fileURI + "'.");
                    return model;
                }
            }
        }

        return null;
    }

}
