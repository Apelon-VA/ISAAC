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
import gov.va.isaac.models.InformationModelMetadata;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.exporter.CEMExporter;
import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.fhim.exporter.FHIMExporter;
import gov.va.isaac.models.hed.HeDInformationModel;
import gov.va.isaac.models.hed.exporter.HeDExporter;

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
 * @author bcarlsenca
 */
public class InformationModelDetailsDialogController {

  private static final Logger LOG = LoggerFactory
      .getLogger(InformationModelDetailsDialogController.class);

  @FXML
  private Label modelNameLabel;

  @FXML
  private Label modelTypeLabel;

  @FXML
  private Label focusConceptLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label importerNameLabel;

  @FXML
  private Label importDateLabel;

  @FXML
  private Label importPathLabel;

  @FXML
  private Label importModuleLabel;

  @FXML
  private TextArea modelXmlTextArea;

  @FXML
  private ProgressIndicator modelXmlProgress;

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
    } else if (informationModel.getType() == InformationModelType.HeD) {
      displayHeD((HeDInformationModel) informationModel);
    } else if (informationModel.getType() == InformationModelType.FHIM) {
      displayFHIM((FHIMInformationModel) informationModel);
    } else {
      throw new UnsupportedOperationException(informationModel.getType()
          + " display not yet supported in ISAAC.");
    }
  }

  private void displayFHIM(FHIMInformationModel fhimModel) {

    // Do work in background.
    Task<String> task = new DetailsTask(fhimModel) {

      @Override
      protected String call() throws Exception {

        // Do work.
        OutputStream out = new ByteArrayOutputStream();
        FHIMExporter exporter = new FHIMExporter(out);
        UUID modelUUID = infoModel.getUuid();
        exporter.exportModel(modelUUID);
        return out.toString();
      }
    };

    scheduleTask(fhimModel, task);
  }

  private void displayCEM(CEMInformationModel cemModel) {

    // Do work in background.
    Task<String> task = new DetailsTask(cemModel) {

      @Override
      protected String call() throws Exception {

        // Do work.
        OutputStream out = new ByteArrayOutputStream();
        CEMExporter exporter = new CEMExporter(out);
        UUID conceptUUID = infoModel.getUuid();
        exporter.exportModel(conceptUUID);
        return out.toString();
      }
    };

    scheduleTask(cemModel, task);
  }

  private void displayHeD(HeDInformationModel hedModel) {

    // Do work in background.
    Task<String> task = new DetailsTask(hedModel) {

      @Override
      protected String call() throws Exception {

        // Do work.
        OutputStream out = new ByteArrayOutputStream();
        HeDExporter exporter = new HeDExporter(out);
        UUID conceptUUID = infoModel.getUuid();
        exporter.exportModel(conceptUUID);
        return out.toString();
      }
    };

    scheduleTask(hedModel, task);
  }

  private void scheduleTask(InformationModel infoModel, Task<String> task) {
    // Bind cursor to task state.
    ObjectBinding<Cursor> cursorBinding =
        Bindings.when(task.runningProperty()).then(Cursor.WAIT)
            .otherwise(Cursor.DEFAULT);
    this.stage.getScene().cursorProperty().bind(cursorBinding);

    // Bind progress indicator to task state.
    modelXmlProgress.visibleProperty().bind(task.runningProperty());

    Thread t = new Thread(task, "Display_" + infoModel.getName());
    t.setDaemon(true);
    t.start();
  }

  /**
   * Common superclass {@link Task} for showing {@link InformationModel}
   * details.
   *
   * @author ocarlsen
   */
  private abstract class DetailsTask extends Task<String> {

    protected final InformationModel infoModel;

    protected DetailsTask(InformationModel infoModel) {
      super();
      this.infoModel = infoModel;
    }

    @Override
    protected void succeeded() {

      // Update UI.
      modelNameLabel.setText(infoModel.getName());
      modelTypeLabel.setText(infoModel.getType().getDisplayName());
      focusConceptLabel.setText("TBD - BAC");
      uuidLabel.setText(infoModel.getUuid().toString());

      InformationModelMetadata metadata = infoModel.getMetadata();
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
      InformationModelType modelType = infoModel.getType();
      String msg =
          String.format("Unexpected error displaying %s model \"%s\"",
              modelType, infoModel.getName());
      LOG.error(msg, ex);
      AppContext.getCommonDialogs()
          .showErrorDialog(title, msg, ex.getMessage());
    }
  }
}
