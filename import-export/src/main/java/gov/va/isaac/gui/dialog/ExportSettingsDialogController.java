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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.export.ExportHandler;
import gov.va.isaac.gui.AppContext;
import gov.va.isaac.util.Utility;

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
    private static final String XML_EXTENSION = ".xml";

    @FXML private Label folderSelectionLabel;
    @FXML private TextField fileSelectionTextField;

    private ExportSettingsDialog exportSettingsDialog;

    public void setVariables(ExportSettingsDialog exportSettingsDialog) {
        this.exportSettingsDialog = exportSettingsDialog;
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
        // TODO: Show don't allow OK to be clicked if not both set....
        if ((folderName != null)  && (! folderName.isEmpty())
                && (fileName != null) && (! fileName.isEmpty())) {

            // Add ".cim" extension to fileName if it doesn't already have one.
            if (! fileName.endsWith(XML_EXTENSION)) {
                fileName = fileName + XML_EXTENSION;
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
                exportHandler.doExport(file);

                return true;
            }

            @Override
            protected void succeeded() {
                @SuppressWarnings("unused")
                Boolean result = this.getValue();

                // Show confirmation dialog.
                String title = "Export Complete";
                String message = String.format("Export to \"%s\" successful.", fileName);
                AppContext.getCommonDialogs().showInformationDialog(title, message);

                exportSettingsDialog.close();
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String message = "Unexpected error performing export";
                LOG.warn(message, ex);
                AppContext.getCommonDialogs().showErrorDialog(title, message, ex.getMessage());
            }
        };

        Utility.execute(task);
    }
}
