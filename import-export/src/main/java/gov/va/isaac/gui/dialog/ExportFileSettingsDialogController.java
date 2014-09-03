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
import gov.va.isaac.gui.importview.ImportView;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.ExportFileHandler;
import gov.va.isaac.ie.ExportHandler;
import gov.va.isaac.model.ExportType;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.util.ExportTypeStringConverter;
import gov.va.isaac.util.InformationModelTypeStringConverter;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;


/**
 * Controller class for {@link ExportFileSettingsDialog}.
 *
 * @author ocarlsen
 */
public class ExportFileSettingsDialogController {

    public static final String MODEL_TYPE_PROPERTY = "gov.va.isaac.gui.dialog.import-settings.model-type";
    public static final String FILE_SELECTION_PROPERTY = "gov.va.isaac.gui.dialog.import-settings.file-selection";

    private static final Logger LOG = LoggerFactory.getLogger(ExportFileSettingsDialogController.class);

    @FXML private ComboBox<ExportType> modelTypeCombo;
    @FXML private ComboBox<ConceptChronicleBI> pathCombo;
    @FXML private Label folderSelectionLabel;

    private ExportFileSettingsDialog exportSettingsDialog;
    private Stage exportStage;

    public void setVariables(ExportFileSettingsDialog exportSettingsDialog, Window parent) {
        this.exportSettingsDialog = exportSettingsDialog;
        exportStage = buildExportStage(parent);
    }

    @FXML
    public void initialize() {
        ExportTypeStringConverter converter = new ExportTypeStringConverter();

        // Populate modelTypeCombo.
        modelTypeCombo.setConverter(converter);
        modelTypeCombo.setItems(ExportType.asObservableList());
        
        // Populate pathCombo
        
        ObservableList<ConceptChronicleBI> paths = FXCollections.observableArrayList(new ArrayList<ConceptChronicleBI>());
        try {
        	List<ConceptChronicleBI> pathConcepts = WBUtility.getPathConcepts();
        	Iterators.removeIf(pathConcepts.iterator(), new Predicate<ConceptChronicleBI>() {

				@Override
				public boolean apply(ConceptChronicleBI arg0) {
					try {
						return arg0.getVersion(WBUtility.getViewCoordinate()).getPreferredDescription().getText().startsWith(TermAux.SNOMED_CORE.getDescription() + " ");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ContradictionException e) {
						e.printStackTrace();
					}
					return false;
        		}
        		
        	});
			paths.addAll(pathConcepts);
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
        pathCombo.setItems(paths);

        // Properties to speed development.
        String modelTypeName = System.getProperty(MODEL_TYPE_PROPERTY);
        if (modelTypeName != null) {
            LOG.debug(MODEL_TYPE_PROPERTY + "=" + modelTypeName);
            ExportType modelType = converter.fromString(modelTypeName);
            modelTypeCombo.setValue(modelType);
        }
    }

    /**
     * Handler for folder selection button.
     */
    public void handleFolderSelection() {
        DirectoryChooser folderChooser = new DirectoryChooser();

        // Show dialog.
        File file = folderChooser.showDialog(exportStage);
        if (file != null) {
            folderSelectionLabel.setText(file.getPath());
        }
    }

    /**
     * Handler for ok button.
     */
    public void handleOk() {
        ExportType exportType = modelTypeCombo.getValue();
        String fileName = "eConcepts.jbin";
        String folderName = folderSelectionLabel.getText();
        int pathNid = pathCombo.getValue().getConceptNid();

        // Validate settings, show warning dialog if there is a problem.
        String title = "Oops!";
        if (exportType == null) {
            AppContext.getCommonDialogs().showInformationDialog(title, "Please select a export type.");
            return;
        }
        if ((folderName == null) || (folderName.isEmpty())) {
            AppContext.getCommonDialogs().showInformationDialog(title, "Please select a folder to export.");
            return;
        }

        exportSettingsDialog.close();
        performExport(exportType, pathNid, folderName, fileName);
    }

    private void performExport(ExportType exportType, int pathNid, final String folderName, final String fileName) {

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
                ExportFileHandler exportHandler = new ExportFileHandler();
				exportHandler.doExport(pathNid, exportType, file);

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

                exportStage.close();
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

        // Bind cursor to task state.
//        ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT);
//        exportStage.getScene().cursorProperty().bind(cursorBinding);

        Utility.execute(task);
    }
    
    private Stage buildExportStage(Window owner) {
        // Use dialog for now, so Alo/Dan can use it.
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Export View");

        return stage;
    }

    /**
     * Handler for cancel button.
     */
    public void handleCancel() {
        exportSettingsDialog.close();
    }
}
