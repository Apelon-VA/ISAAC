package gov.va.isaac.export;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.models.cem.importer.CEMMetadataBinding;

import java.io.File;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the ISAAC export functionality.
 *
 * @author ocarlsen
 */
public class ExportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExportHandler.class);

    /**
     * Method called by the ISAAC application to perform the export. Will be
     * invoked on a background thread.
     */
    public void doExport(BdbTerminologyStore dataStore, File exportFile) throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // CEM attributes (foundation metadata concept)
        UUID conceptUUID = CEMMetadataBinding.CEM_ATTRIBUTES.getUuids()[0];

        // Load as WB-style concept.
        LOG.info("Loading concept with UUID " + conceptUUID);
        ConceptChronicleBI concept = dataStore.getConcept(conceptUUID);
        LOG.info("Finished loading concept with UUID " + conceptUUID);
        LOG.info(concept.toLongString());

        // Load as FX-style concept.
        LOG.info("Loading FxConcept with UUID " + conceptUUID);
        ConceptChronicleDdo fxConcept = dataStore.getFxConcept(
                conceptUUID,
                StandardViewCoordinates.getSnomedInferredThenStatedLatest(),
                VersionPolicy.ACTIVE_VERSIONS,
                RefexPolicy.REFEX_MEMBERS,
                RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
        LOG.info("Finished loading FxConcept with UUID " + conceptUUID);
        LOG.info(fxConcept.toXml());
    }
}
