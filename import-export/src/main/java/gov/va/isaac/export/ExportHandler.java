package gov.va.isaac.export;

import gov.va.isaac.gui.util.FxUtils;

import java.io.File;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Class for handling the ISAAC export functionality.
 *
 * @author ocarlsen
 */
public class ExportHandler {

    /**
     * Method called by the ISAAC application to perform the export.
     * Will be invoked on a background thread.
     */
    public void doExport(BdbTerminologyStore dataStore, File exportFile) throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // TODO: Implement by Alo/Dan.
    }
}
