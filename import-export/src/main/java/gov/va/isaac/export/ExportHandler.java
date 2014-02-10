package gov.va.isaac.export;

import gov.va.isaac.gui.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.models.cem.exporter.CEMExporter;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Class for handling the ISAAC export functionality.
 *
 * @author ocarlsen
 */
public class ExportHandler {

    private final AppContext appContext;

    public ExportHandler(AppContext appContext) throws ValidationException, IOException {
        super();
        this.appContext = appContext;
    }

    /**
     * Method called by the ISAAC application to perform the export. Will be
     * invoked on a background thread.
     */
    public void doExport(File file) throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get "Blood pressure taking (procedure)" concept.
        UUID conceptUUID = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");

        // Export CEM model to file.
        CEMExporter exporter = new CEMExporter(appContext);
        exporter.exportModel(conceptUUID, file);
    }
}
