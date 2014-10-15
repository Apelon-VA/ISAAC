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
import gov.va.isaac.gui.util.GridPaneBuilder;
import gov.va.isaac.ie.ExportFileHandler;
import gov.va.isaac.model.ExportType;
import gov.va.isaac.util.ProgressEvent;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.WBUtility;

import java.io.File;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A GUI for handling exports.
 *
 * @author bcarlsenca
 */
@SuppressWarnings("restriction")
public class ExportView extends GridPane {

  /** The Constant LOG. */
  static final Logger LOG = LoggerFactory.getLogger(ExportView.class);

  /** The model type label. */
  private final Label exportTypeLabel = new Label();

  /** The path name label. */
  private final Label pathNameLabel = new Label();

  /** The folder name label. */
  private final Label folderNameLabel = new Label();

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

  /** The export file handler. */
  ExportFileHandler exportFileHandler = null;

  /**
   * Instantiates an empty {@link ExportView}.
   */
  public ExportView() {
    super();

    // GUI placeholders.
    GridPaneBuilder builder = new GridPaneBuilder(this);
    builder.addRow("Export Type: ", exportTypeLabel);
    builder.addRow("Path Name: ", pathNameLabel);
    builder.addRow("Folder Name: ", folderNameLabel);
    builder.addRow("Progress: ", progressBar);
    progressBar.setMinWidth(400);
    builder.addRow("Status: ", statusLabel);
    builder.addRow("Result: ", resultLabel);
    builder.addRow("", cancelButton);
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
   * Perform an export according the specifed parameters.
   *
   * @param exportType the export type
   * @param pathNid the path nid
   * @param folderName the file name
   * @param zipChecked the flag indicating whether to compress output
   */
  public void doExport(ExportType exportType, int pathNid,
    final String folderName, boolean zipChecked) {
    Preconditions.checkNotNull(exportType);
    Preconditions.checkNotNull(folderName);

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    // Update UI.
    exportTypeLabel.setText(exportType.getDisplayName());
    pathNameLabel.setText(WBUtility.getConPrefTerm(pathNid));
    folderNameLabel.setText(folderName);

    File folder = new File(folderName);
    // Inject into an ExportFileHandler.
    exportFileHandler =
        new ExportFileHandler(pathNid, exportType, folder, zipChecked);

    // Do work in background.
    task = new ExporterTask(exportFileHandler);

    // Bind cursor to task state.
    ObjectBinding<Cursor> cursorBinding =
        Bindings.when(task.runningProperty()).then(Cursor.WAIT)
            .otherwise(Cursor.DEFAULT);
    this.getScene().cursorProperty().bind(cursorBinding);

    taskThread = new Thread(task, "Exporter_" + exportType);
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
    exportFileHandler.doCancel();
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
  class ExporterTask extends Task<Boolean> {

    /** The export handler. */
    ExporterTask(ExportFileHandler exportFileHandler) {
      // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#call()
     */
    @Override
    protected Boolean call() throws Exception {

      ProgressListener listener = new ProgressListener() {
        @Override
        public void updateProgress(ProgressEvent pe) {
          Platform.runLater(() -> {
            progressBar.setProgress(((double) pe.getProgress()) / 100);
            statusLabel.setText(pe.getNote());
          });
        }
      };
      Platform.runLater(() -> {
        statusLabel.setText("Starting...");
        progressBar.setProgress(0);
      });
      exportFileHandler.doExport(listener);
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
        resultLabel.setText("Successfully cancelled export");
      } else {
        statusLabel.setText("");
        resultLabel.setText("Successfully exported data");
      }
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
      // leave last comment on failure, e.g. do not set status label here
      resultLabel.setText("Failed to export data");

      String title = ex.getClass().getName();
      String msg = String.format("Unexpected error exporting to file");
      LOG.error(msg, ex);
      AppContext.getCommonDialogs()
          .showErrorDialog(title, msg, ex.getMessage());
    }

  }
}
