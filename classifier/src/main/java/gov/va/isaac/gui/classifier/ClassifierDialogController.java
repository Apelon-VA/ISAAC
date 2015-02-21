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
package gov.va.isaac.gui.classifier;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link ClassifierDialog}.
 *
 * @author bcarlsenca
 */
@SuppressWarnings("restriction")
public class ClassifierDialogController {

  /** The Constant LOG. */
  static final Logger LOG = LoggerFactory
      .getLogger(ClassifierDialogController.class);

  /** The stage. */
  Stage classifierStage;

  /** The dialog. */
  ClassifierDialog dialog;

  /** The incremental classify flag. */
  boolean incrementalClassify;

  /**
   * Sets the variables.
   *
   * @param dialog the dialog
   * @param parent the parent
   */
  public void setVariables(ClassifierDialog dialog, Window parent) {
    this.dialog = dialog;
    this.incrementalClassify = dialog.getIncrementalClassify();
    this.classifierStage = buildClassifierStage(parent);
  }

  /**
   * Initialize.
   */
  @FXML
  public void initialize() {
    // n/a
  }

  /**
   * Builds the classifier stage.
   *
   * @param owner the owner
   * @return the stage
   */
  private Stage buildClassifierStage(Window owner) {

    // Use dialog for now, so Alo/Dan can use it.
    Stage stage = new Stage();
    stage.initModality(Modality.NONE);
    stage.initOwner(owner);
    stage.initStyle(StageStyle.DECORATED);
    stage.setTitle((incrementalClassify ? "Incremental " : "")
        + "Classifier View");

    return stage;
  }

  /**
   * Handler for ok button.
   */
  public void handleOk() {
    dialog.close();
    showClassifierView();
  }

  /**
   * Show classifier view.
   */
  private void showClassifierView() {

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    try {
      ClassifierView classifierView = new ClassifierView(incrementalClassify);
      classifierStage.setScene(new Scene(classifierView));
      if (classifierStage.isShowing()) {
        classifierStage.toFront();
      } else {
        classifierStage.show();
      }

      ((Stage) classifierStage.getScene().getWindow())
          .setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
              classifierView.doCancel();
            }
          });
      classifierView.doClassify();

    } catch (Exception ex) {
      String title = ex.getClass().getName();
      String message = "Unexpected error displaying classify view";
      LOG.warn(message, ex);
      AppContext.getCommonDialogs().showErrorDialog(title, message,
          ex.getMessage());
    }
  }

  /**
   * Handler for cancel button.
   */
  public void handleCancel() {
    dialog.close();
  }

}
