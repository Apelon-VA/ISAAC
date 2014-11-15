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
import javafx.stage.Stage;
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
  Stage stage;

  /**
   * Sets the variables.
   *
   * @param stagel the variables
   */
  public void setVariables(Stage stagel) {
    this.stage = stagel;
  }

  /**
   * Initialize.
   */
  @FXML
  public void initialize() {
    // n/a
  }

  /**
   * Handler for ok button.
   */
  public void handleOk() {
    showView();
  }

  /**
   * Show view.
   */
  private void showView() {

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    try {
      ClassifierView classifierView = new ClassifierView();
      stage.setScene(new Scene(classifierView));
      if (stage.isShowing()) {
        stage.toFront();
      } else {
        stage.show();
      }

      ((Stage) stage.getScene().getWindow())
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
    stage.close();
  }

}
