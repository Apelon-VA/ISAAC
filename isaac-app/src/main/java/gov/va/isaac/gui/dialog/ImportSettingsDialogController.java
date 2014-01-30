package gov.va.isaac.gui.dialog;

import gov.va.isaac.gui.App;
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
    private App app;

    public void setVariables(ImportSettingsDialog importSettingsDialog, App app) {
        this.importSettingsDialog = importSettingsDialog;
        this.app = app;
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
            app.getAppContext().getAppUtil().showInformationDialog(title, "Please select a model type.");
            return;
        }
        if ((fileName == null) || (fileName.isEmpty())) {
            app.getAppContext().getAppUtil().showInformationDialog(title, "Please select a file to import.");
            return;
        }

        importSettingsDialog.close();
        app.showImportView(modelType, fileName);
    }

    /**
     * Handler for cancel button.
     */
    public void handleCancel() {
        importSettingsDialog.close();
    }
}
