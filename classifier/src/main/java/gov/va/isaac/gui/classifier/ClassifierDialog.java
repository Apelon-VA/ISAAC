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

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A {@link Stage} which can be used to show a classifier dialog
 *
 * @author bcarlsenca
 */
@SuppressWarnings("restriction")
public class ClassifierDialog extends Stage {

  /** The controller. */
  private final ClassifierDialogController controller;

  /**
   * Instantiates an empty {@link ClassifierDialog}.
   * @param parent the parent
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ClassifierDialog(Window parent) throws IOException {
    super();

    setTitle("Classifier");
    setResizable(true);

    Stage owner = AppContext.getMainApplicationWindow().getPrimaryStage();
    initOwner(owner);
    initModality(Modality.WINDOW_MODAL);
    initStyle(StageStyle.UTILITY);

    // Load from FXML.
    URL resource = this.getClass().getResource("ClassifierDialog.fxml");
    FXMLLoader loader = new FXMLLoader(resource);
    Parent root = (Parent) loader.load();
    Scene scene = new Scene(root);
    setScene(scene);

    controller = loader.getController();
    controller.setVariables(this, parent);
  }
}
