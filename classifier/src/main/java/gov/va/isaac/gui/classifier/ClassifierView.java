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
import gov.va.isaac.classifier.Classifier;
import gov.va.isaac.classifier.SnomedSnorocketClassifier;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.OTFUtility;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A GUI for handling full classification runs.
 *
 * @author bcarlsenca
 */
@SuppressWarnings("restriction")
public class ClassifierView extends GridPane {

  /** The Constant LOG. */
  static final Logger LOG = LoggerFactory.getLogger(ClassifierView.class);

  /** The path name label. */
  private final Label rootName = new Label();

  /** The progress bar. */
  final javafx.scene.control.ProgressBar progressBar = new ProgressBar(0);

  /** The status label. */
  final Label statusLabel = new Label();

  /** The result label. */
  final Label resultLabel = new Label();

  /** The cancel button. */
  final Button cancelButton = new Button("Cancel");

  /** The exporter task. */
  Task<Boolean> task;

  /** the task thread */
  Thread taskThread = null;

  /** The request cancel. */
  boolean requestCancel = false;

  /** The classifier. */
  Classifier classifier = null;

  /**
   * Instantiates an empty {@link ClassifierView}.
   */
  public ClassifierView() {
    super();

    // GUI placeholders.
    this.setHgap(10);
    this.setVgap(10);
    this.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));
    GridPaneBuilder builder = new GridPaneBuilder(this);
    builder.addRow("Root Name: ", rootName);
    builder.addRow("Progress: ", progressBar);
    progressBar.setMinWidth(500);
    builder.addRow("Status: ", statusLabel);
    builder.addRow("Result: ", resultLabel);
    @SuppressWarnings("deprecation")
    javafx.scene.layout.HBox hbox =
        javafx.scene.layout.HBoxBuilder.create()
            .alignment(javafx.geometry.Pos.TOP_CENTER).children(cancelButton)
            .build();

    builder.addRow(hbox);
    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        doCancel();
      }
    });
    setConstraints();

    // Set minimum dimensions.
    setMinHeight(100);
    setMinWidth(600);
  }

  /**
   * Perform cycle check and classification.
   * @throws Exception if anything goes wrong
   */
  public void doClassify() throws Exception {

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    // Update UI.
    rootName.setText(OTFUtility.getConPrefTerm(Taxonomies.SNOMED.getLenient().getNid()));
    
    // Create classifier
    classifier = new SnomedSnorocketClassifier();
    
    // Do work in background.
    task = new ClassifyTask();

    // Bind cursor to task state.
    ObjectBinding<Cursor> cursorBinding =
        Bindings.when(task.runningProperty()).then(Cursor.WAIT)
            .otherwise(Cursor.DEFAULT);
    this.getScene().cursorProperty().bind(cursorBinding);

    taskThread = new Thread(task, "classify");
    taskThread.setDaemon(true);
    taskThread.start();
  }

  /**
   * Do cancel.
   */
  public void doCancel() {
    LOG.info("Cancel import.");
    if (requestCancel) {
      // complete the process
      ((Stage) getScene().getWindow()).close();
      return;
    }

    // Set requestCancel to true and cancel task
    requestCancel = true;
    classifier.cancel();
    cancelButton.setText("Close");
  }

  /**
   * Sets the FX constraints.
   */
  private void setConstraints() {

    // Column 1 has empty constraints.
    this.getColumnConstraints().add(new ColumnConstraints());

    // Column 2 should grow to fill space.
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setHgrow(Priority.ALWAYS);
    this.getColumnConstraints().add(column2);

    // Rows 1-4 have empty constraints.
    this.getRowConstraints().add(new RowConstraints());
    this.getRowConstraints().add(new RowConstraints());
    this.getRowConstraints().add(new RowConstraints());
    this.getRowConstraints().add(new RowConstraints());

    // Row 5 should
    RowConstraints row5 = new RowConstraints();
    row5.setVgrow(Priority.ALWAYS);
    this.getRowConstraints().add(row5);
  }

  /**
   * Inner class to handle the export task.
   */
  class ClassifyTask extends Task<Boolean> {

    /** The export handler. */
    ClassifyTask() {
      // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#call()
     */
    @Override
    protected Boolean call() throws Exception {

      // Create and wire the progress listener
      ProgressListener listener = new ProgressListener() {
        @Override
        public void updateProgress(ProgressEvent pe) {
          Platform.runLater(() -> {
            progressBar.setProgress(((double) pe.getProgress()) / 100);
            statusLabel.setText(pe.getNote());
          });
        }
      };
      classifier.addProgressListener(listener);

      Platform.runLater(() -> {
        statusLabel.setText("Starting...");
        progressBar.setProgress(0);
      });

      // Perform classification
      classifier.classify(Taxonomies.SNOMED.getLenient().getNid());

      Platform.runLater(() -> {
        statusLabel.setText("Finished");
        progressBar.setProgress(1);
      });
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#succeeded()
     */
    @Override
    protected void succeeded() {
      // Update UI.
      progressBar.setProgress(100);
      cancelButton.setText("Close");
      if (requestCancel) {
        resultLabel.setText("Successfully cancelled classify.");
      } else {
        statusLabel.setText("");
        resultLabel.setText("Successfully classified. ");
      }
      requestCancel = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#failed()
     */
    @Override
    protected void failed() {
      Throwable ex = getException();
      progressBar.setProgress(100);
      cancelButton.setText("Close");
      requestCancel = true;
      // leave last comment on failure, e.g. do not set status label here
      resultLabel.setText("Failed to classify.");

      String title = ex.getClass().getName();
      String msg = String.format("Unexpected error classifying.");
      LOG.error(msg, ex);
      AppContext.getCommonDialogs()
          .showErrorDialog(title, msg, ex.getMessage());
    }

  }

  /**
   * A utility for assembling a {@link GridPane}.
   */
  class GridPaneBuilder {

    private final GridPane gridPane;

    private int rowIndex = 0;

    public GridPaneBuilder(GridPane gridPane) {
      super();
      this.gridPane = gridPane;
    }

    public void addRow(String labelText, Node fxNode) {

      // Column 0.
      Label label = new Label(labelText);
      gridPane.add(label, 0, rowIndex);

      // Column 1.
      gridPane.add(fxNode, 1, rowIndex);

      // Increment row index.
      ++rowIndex;
    }

    /**
     * @param fxNode A component to span two columns.
     */
    public void addRow(Node fxNode) {
      gridPane.add(fxNode, 0, rowIndex++, 2, 1);
    }
  }

}
