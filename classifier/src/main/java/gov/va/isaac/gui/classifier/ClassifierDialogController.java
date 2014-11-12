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
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

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

  /** The path combo. */
  @FXML
  private ComboBox<ConceptChronicleBI> pathCombo;

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

    // Populate pathCombo

    ObservableList<ConceptChronicleBI> paths =
        FXCollections.observableArrayList(new ArrayList<ConceptChronicleBI>());
    try {
      List<ConceptChronicleBI> pathConcepts = WBUtility.getPathConcepts();
      Iterators.removeIf(pathConcepts.iterator(),
          new Predicate<ConceptChronicleBI>() {

            @Override
            public boolean apply(ConceptChronicleBI arg0) {
              try {
                return arg0.getVersion(WBUtility.getViewCoordinate())
                    .getPreferredDescription().getText()
                    .startsWith(TermAux.SNOMED_CORE.getDescription() + " ");
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

  }

  /**
   * Handler for ok button.
   */
  public void handleOk() {
    int pathNid = pathCombo.getValue().getConceptNid();
    // Can only classify SNOMED at this time
    try {
      if (pathNid != Snomed.SNOMED_RELEASE_PATH.getNid()) {
        throw new UnsupportedOperationException(
            "Classifier only supports SNOMED path at this time.");
      }
      showView(pathNid);
    } catch (ValidationException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    
  }

  /**
   * Show view.
   *
   * @param pathNid the path nid
   */
  private void showView(int pathNid) {

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
      classifierView.doClassify(pathNid);

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
