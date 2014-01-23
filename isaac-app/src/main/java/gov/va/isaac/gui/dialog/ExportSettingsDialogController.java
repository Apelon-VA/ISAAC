package gov.va.isaac.gui.dialog;

import gov.va.isaac.export.ExportHandler;
import gov.va.isaac.gui.AppContext;

import java.io.File;
import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controller class for {@link ExportSettingsDialog}.
 *
 * @author ocarlsen
 */
public class ExportSettingsDialogController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportSettingsDialogController.class);
    private static final String CIM_EXTENSION = ".cim";

    @FXML private Label folderSelectionLabel;
    @FXML private TextField fileSelectionTextField;

    private ExportSettingsDialog exportSettingsDialog;
    private AppContext appContext;

    public void setVariables(ExportSettingsDialog exportSettingsDialog, AppContext appContext) {
        this.exportSettingsDialog = exportSettingsDialog;
        this.appContext = appContext;
    }

    @FXML
    public void initialize() {
    }

    /**
     * Handler for folder selection button.
     */
    public void handleFolderSelection() {
        DirectoryChooser folderChooser = new DirectoryChooser();

        // Show dialog.
        File file = folderChooser.showDialog(exportSettingsDialog);
        if (file != null) {
            folderSelectionLabel.setText(file.getPath());
        }
    }

    /**
     * Handler for ok button.
     */
    public void handleOk() {
        String folderName = folderSelectionLabel.getText();
        String fileName = fileSelectionTextField.getText();

        // Perform exdport if both are set.
        // TODO: Show warning dialog if not.
        if ((folderName != null)  && (! folderName.isEmpty())
                && (fileName != null) && (! fileName.isEmpty())) {

            // Add ".cim" extension to fileName if it doesn't already have one.
            if (! fileName.endsWith(CIM_EXTENSION)) {
                fileName = fileName + CIM_EXTENSION;
            }

            performExport(folderName, fileName);
        }
    }

    /**
     * Handler for cancel button.
     */
    public void handleCancel() {
        exportSettingsDialog.close();
    }

    private void performExport(final String folderName, final String fileName) {

        // Do work in background.
        Task<Boolean> task = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {

                // Create empty file.
                File folder = new File(folderName);
                File file = new File(folder, fileName);

                // Sanity checks.
                if (file.exists()) {
                    throw new IOException("File already exists: " + file);
                }
                if (! file.createNewFile()) {
                    throw new IOException("Could not create file: " + file);
                }

                // Inject into an ExportHandler.
                ExportHandler exportHandler = new ExportHandler();
                exportHandler.doExport(appContext.getDataStore(), file);

                return true;
            }

            @Override
            protected void succeeded() {
                @SuppressWarnings("unused")
                Boolean result = this.getValue();

                // Show confirmation dialog.
                String title = "Export Complete";
                String message = String.format("Export to \"%s\" successful.", fileName);
                appContext.getAppUtil().showInformationDialog(title, message);

                exportSettingsDialog.close();
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String message = "Unexpected error performing export";
                LOG.warn(message, ex);
                appContext.getAppUtil().showErrorDialog(title, message, ex.getMessage());
            }
        };

        Thread t = new Thread(task, "Exporter_" + fileName);
        t.setDaemon(true);
        t.start();
    }
}
