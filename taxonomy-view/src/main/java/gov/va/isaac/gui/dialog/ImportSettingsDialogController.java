package gov.va.isaac.gui.dialog;

import gov.va.isaac.gui.App;
import gov.va.isaac.model.InformationModelType;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;


/**
 * Controller class for {@link ImportSettingsDialog}.
 *
 * @author ocarlsen
 */
public class ImportSettingsDialogController {

    private final ImportSettingsDialog inputDialog;
    private final App app;
    private final ComboBox<InformationModelType> modelTypeCombo;
    private final Label fileSelectionLabel;
    private final VBox root;

    public ImportSettingsDialogController(ImportSettingsDialog inputDialog, App app) {
        super();
        this.inputDialog = inputDialog;
        this.app = app;

        // Model type widgets.
        Label modelTypeLabel = new Label("Clinical Information Model:");
        this.modelTypeCombo = new ComboBox<>(InformationModelType.asObservableList());
        HBox modelTypeBox = new HBox();
        modelTypeBox.getChildren().addAll(modelTypeLabel, modelTypeCombo);

        // File selection widgets.
        Button fileSelectionButton = buildFileSelectionButton("Select File:");
        this.fileSelectionLabel = new Label();
        HBox fileSelectionBox = new HBox();
        fileSelectionBox.getChildren().addAll(fileSelectionButton, fileSelectionLabel);

        // Ok, Cancel buttons.
        Button okButton = buildOkButton("Ok");
        Button cancelButton = buildCancelButton("Cancel");
        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(okButton, cancelButton);

        // Assemble UI.
        this.root = new VBox();
        root.getChildren().addAll(modelTypeBox, fileSelectionBox, buttonBox);

        // Set minimum dimensions.
        root.setMinHeight(400);
        root.setMinWidth(400);
    }

    public Parent getRoot() {
        return root;
    }

    private void handleFileSelection() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CIM files (*.cim)", "*.cim");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog.
        File file = fileChooser.showOpenDialog(inputDialog);
        if (file != null) {
            fileSelectionLabel.setText(file.getPath());
        }
    }

    private void handleOk() {
        InformationModelType modelType = modelTypeCombo.getValue();
        String fileName = fileSelectionLabel.getText();

        // Show ImportView if both are set.
        // TODO: Show warning dialog.
        if ((modelType != null) && (fileName != null)) {
            inputDialog.close();
            app.showImportView(modelType, fileName);
        }
    }

    private void handleCancel() {
        inputDialog.close();
    }

    private Button buildCancelButton(String label) {
        Button b = new Button(label);
        b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent ev) {
                handleCancel();
            }
        });
        return b;
    }

    private Button buildOkButton(String label) {
        Button b = new Button(label);
        b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent ev) {
                handleOk();
            }
        });
        return b;
    }

    private Button buildFileSelectionButton(String label) {
        Button b = new Button(label);
        b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent ev) {
                handleFileSelection();
            }
        });
        return b;
    }
}
