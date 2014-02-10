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

import gov.va.isaac.gui.ExtendedAppContext;
import gov.va.isaac.gui.AppController;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.util.InformationModelTypeStringConverter;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;


/**
 * Controller class for {@link ImportSettingsDialog}.
 *
 * @author ocarlsen
 */
public class ImportSettingsDialogController {

    @FXML private ComboBox<InformationModelType> modelTypeCombo;
    @FXML private Label fileSelectionLabel;

    private ImportSettingsDialog importSettingsDialog;
    private AppController appController;

    public void setVariables(ImportSettingsDialog importSettingsDialog, AppController appController) {
        this.importSettingsDialog = importSettingsDialog;
        this.appController = appController;
    }

    @FXML
    public void initialize() {

        // Populate modelTypeCombo.
        modelTypeCombo.setConverter(new InformationModelTypeStringConverter());
        modelTypeCombo.setItems(InformationModelType.asObservableList());
    }

    /**
     * Handler for file selection button.
     */
    public void handleFileSelection() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CIM files (*.cim)", "*.cim");
        fileChooser.getExtensionFilters().add(extFilter);
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(xmlFilter);

        // Show open file dialog.
        File file = fileChooser.showOpenDialog(importSettingsDialog);
        if (file != null) {
            fileSelectionLabel.setText(file.getPath());
        }
    }

    /**
     * Handler for ok button.
     */
    public void handleOk() {
        InformationModelType modelType = modelTypeCombo.getValue();
        String fileName = fileSelectionLabel.getText();

        // Validate settings, show warning dialog if there is a problem.
        String title = "Oops!";
        if (modelType == null) {
            ExtendedAppContext.getCommonDialogs().showInformationDialog(title, "Please select a model type.");
            return;
        }
        if ((fileName == null) || (fileName.isEmpty())) {
        	ExtendedAppContext.getCommonDialogs().showInformationDialog(title, "Please select a file to import.");
            return;
        }

        importSettingsDialog.close();
        appController.showImportView(modelType, fileName);
    }

    /**
     * Handler for cancel button.
     */
    public void handleCancel() {
        importSettingsDialog.close();
    }
}
