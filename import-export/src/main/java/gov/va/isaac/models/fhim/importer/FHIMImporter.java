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
import java.util.Collection;
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
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

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
        FHIMInformationModelFactory factory = new FHIMInformationModelFactory();
        FHIMInformationModel infoModel = factory.createInformationModel(bloodPressurePackage);

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
