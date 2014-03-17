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
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.InformationModel.Metadata;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.exporter.CEMExporter;
import gov.va.isaac.models.fhim.FHIMInformationModel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Controller class for {@link InformationModelDetailsDialog}.
 *
 * @author ocarlsen
 */
public class InformationModelDetailsDialogController {

    private static final Logger LOG = LoggerFactory.getLogger(InformationModelDetailsDialogController.class);

    @FXML private Label modelNameLabel;
    @FXML private Label modelTypeLabel;
    @FXML private Label focusConceptLabel;
    @FXML private Label uuidLabel;
    @FXML private Label importerNameLabel;
    @FXML private Label importDateLabel;
    @FXML private Label importPathLabel;
    @FXML private Label importModuleLabel;
    @FXML private TextArea modelXmlTextArea;
    @FXML private ProgressIndicator modelXmlProgress;

    private Stage stage;

    @FXML
    public void initialize() {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void displayModel(InformationModel informationModel) {
        Preconditions.checkNotNull(informationModel);

        // Make sure in application thread.
        FxUtils.checkFxUserThread();

        if (informationModel.getType() == InformationModelType.CEM) {
            displayCEM((CEMInformationModel) informationModel);
        } else if (informationModel.getType() == InformationModelType.FHIM) {
                displayFHIM((FHIMInformationModel) informationModel);
        } else {
            throw new UnsupportedOperationException(informationModel.getType() +
                    " display not yet supported in ISAAC.");
        }
    }

    private void displayFHIM(final FHIMInformationModel fhimModel) {

        // Do work in background.
        Task<String> task = new Task<String>() {

            @Override
            protected String call() throws Exception {

                // Do work.
                return "TODO";
            }

            @Override
            protected void succeeded() {

                // Update UI.
                modelNameLabel.setText(fhimModel.getName());
                modelTypeLabel.setText(fhimModel.getType().getDisplayName());
                focusConceptLabel.setText(fhimModel.getFocusConceptName());
                uuidLabel.setText(fhimModel.getFocusConceptUUID().toString());

                Metadata metadata = fhimModel.getMetadata();
                importerNameLabel.setText(metadata.getImporterName());
                importDateLabel.setText(TimeHelper.formatDate(metadata.getTime()));
                importPathLabel.setText(metadata.getPath().toString());
                importModuleLabel.setText(metadata.getModuleName());

                String modelXML = this.getValue();
                modelXmlTextArea.setText(modelXML);
           }

            @Override
            protected void failed() {

                // Show dialog.
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String msg = String.format("Unexpected error displaying FHIM model \"%s\"",
                        fhimModel.getName());
                LOG.error(msg, ex);
                AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
            }
        };

        // Bind cursor to task state.
        ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT);
        this.stage.getScene().cursorProperty().bind(cursorBinding);

        // Bind progress indicator to task state.
        modelXmlProgress.visibleProperty().bind(task.runningProperty());

        Thread t = new Thread(task, "Display_" + fhimModel.getName());
        t.setDaemon(true);
        t.start();
    }

    private void displayCEM(final CEMInformationModel cemModel) {

        // Do work in background.
        Task<String> task = new Task<String>() {

            @Override
            protected String call() throws Exception {

                // Do work.
                OutputStream out = new ByteArrayOutputStream();
                CEMExporter exporter = new CEMExporter(out);
                UUID conceptUUID = cemModel.getFocusConceptUUID();
                exporter.exportModel(conceptUUID );
                return out.toString();
            }

            @Override
            protected void succeeded() {

                // Update UI.
                modelNameLabel.setText(cemModel.getName());
                modelTypeLabel.setText(cemModel.getType().getDisplayName());
                focusConceptLabel.setText(cemModel.getFocusConceptName());
                uuidLabel.setText(cemModel.getFocusConceptUUID().toString());

                Metadata metadata = cemModel.getMetadata();
                importerNameLabel.setText(metadata.getImporterName());
                importDateLabel.setText(TimeHelper.formatDate(metadata.getTime()));
                importPathLabel.setText(metadata.getPath().toString());
                importModuleLabel.setText(metadata.getModuleName());

                String modelXML = this.getValue();
                modelXmlTextArea.setText(modelXML);
           }

            @Override
            protected void failed() {

                // Show dialog.
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String msg = String.format("Unexpected error displaying CEM model \"%s\"",
                        cemModel.getName());
                LOG.error(msg, ex);
                AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
            }
        };

        // Bind cursor to task state.
        ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT);
        this.stage.getScene().cursorProperty().bind(cursorBinding);

        // Bind progress indicator to task state.
        modelXmlProgress.visibleProperty().bind(task.runningProperty());

        Thread t = new Thread(task, "Display_" + cemModel.getName());
        t.setDaemon(true);
        t.start();
    }
}
