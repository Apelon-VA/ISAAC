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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.util.InformationModelTypeStringConverter;

import java.io.File;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link ImportSettingsDialog}.
 *
 * @author ocarlsen
 */
public class ImportSettingsDialogController {

  public static final String MODEL_TYPE_PROPERTY =
      "gov.va.isaac.gui.dialog.import-settings.model-type";

  public static final String FILE_SELECTION_PROPERTY =
      "gov.va.isaac.gui.dialog.import-settings.file-selection";

  private static final Logger LOG = LoggerFactory
      .getLogger(ImportSettingsDialogController.class);

  @FXML
  private ComboBox<InformationModelType> modelTypeCombo;

  @FXML
  private Label fileSelectionLabel;

  private ImportSettingsDialog importSettingsDialog;

  private Stage importStage;

  public void setVariables(ImportSettingsDialog importSettingsDialog,
    Window parent) {
    this.importSettingsDialog = importSettingsDialog;
    importStage = buildImportStage(parent);
  }

  @FXML
  public void initialize() {
    InformationModelTypeStringConverter converter =
        new InformationModelTypeStringConverter();

    // Populate modelTypeCombo.
    modelTypeCombo.setConverter(converter);
    modelTypeCombo.setItems(InformationModelType.asObservableList());

    // Properties to speed development.
    String modelTypeName = System.getProperty(MODEL_TYPE_PROPERTY);
    if (modelTypeName != null) {
      LOG.debug(MODEL_TYPE_PROPERTY + "=" + modelTypeName);
      InformationModelType modelType = converter.fromString(modelTypeName);
      modelTypeCombo.setValue(modelType);
    }
    String fileSelection = System.getProperty(FILE_SELECTION_PROPERTY);
    if (fileSelection != null) {
      LOG.debug(FILE_SELECTION_PROPERTY + "=" + fileSelection);
      fileSelectionLabel.setText(fileSelection);
    }
  }

  /**
   * Handler for file selection button.
   */
  public void handleFileSelection() {
    FileChooser fileChooser = new FileChooser();

    // Set extension filter.
    FileChooser.ExtensionFilter xmlFilter =
        new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml", "*.zip");
    FileChooser.ExtensionFilter umlFilter =
        new FileChooser.ExtensionFilter("UML files (*.uml)", "*.uml", "*.zip");
    FileChooser.ExtensionFilter allFilter =
        new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
    fileChooser.getExtensionFilters().addAll(xmlFilter, umlFilter, allFilter);

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
      AppContext.getCommonDialogs().showInformationDialog(title,
          "Please select a model type.");
      return;
    }
    if ((fileName == null) || (fileName.isEmpty())) {
      AppContext.getCommonDialogs().showInformationDialog(title,
          "Please select a file to import.");
      return;
    }

    importSettingsDialog.close();
    showImportView(modelType, fileName);
  }

  public void showImportView(InformationModelType modelType, String fileName) {

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    try {
      ImportView importView = new ImportView();
      importStage.setScene(new Scene(importView));
      if (importStage.isShowing()) {
        importStage.toFront();
      } else {
        importStage.show();
      }

      ((Stage) importStage.getScene().getWindow())
          .setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
              importView.doCancel();
            }
          });
      importView.doImport(modelType, fileName);

    } catch (Exception ex) {
      String title = ex.getClass().getName();
      String message = "Unexpected error displaying import view";
      LOG.warn(message, ex);
      AppContext.getCommonDialogs().showErrorDialog(title, message,
          ex.getMessage());
    }
  }

  private Stage buildImportStage(Window owner) {
    // Use dialog for now, so Alo/Dan can use it.
    Stage stage = new Stage();
    stage.initModality(Modality.NONE);
    stage.initOwner(owner);
    stage.initStyle(StageStyle.DECORATED);
    stage.setTitle("Import View");

    return stage;
  }

  /**
   * Handler for cancel button.
   */
  public void handleCancel() {
    importSettingsDialog.close();
  }
}
