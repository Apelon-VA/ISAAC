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
import gov.va.isaac.ie.ImportHandler;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.cem.importer.CEMImporter;
import gov.va.isaac.models.fhim.importer.FHIMImporter;
import gov.va.isaac.models.hed.importer.HeDImporter;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.TransformerConfigurationException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A GUI for handling imports.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author bcarlsenca
 */
@SuppressWarnings("restriction")
public class ImportView extends GridPane {

  /** The Constant LOG. */
  static final Logger LOG = LoggerFactory.getLogger(ImportView.class);

  /** The model type label. */
  private final Label modelTypeLabel = new Label();

  /** The file name label. */
  private final Label fileNameLabel = new Label();

  /** The progress bar. */
  final javafx.scene.control.ProgressBar progressBar = new ProgressBar(0);

  /** The status label. */
  final Label statusLabel = new Label();

  /** The result label. */
  final Label resultLabel = new Label();

  /**
   * Instantiates an empty {@link ImportView}.
   */
  public ImportView() {
    super();

    // GUI placeholders.
    GridPaneBuilder builder = new GridPaneBuilder(this);
    builder.addRow("Information Model: ", modelTypeLabel);
    builder.addRow("File Name: ", fileNameLabel);
    builder.addRow("Progress: ", progressBar);
    progressBar.setMinWidth(400);
    builder.addRow("Status: ", statusLabel);
    builder.addRow("Result: ", resultLabel);

    setConstraints();

    // Set minimum dimensions.
    setMinHeight(100);
    setMinWidth(600);
  }

  /**
   * Do import for the specified type and file name.
   *
   * @param modelType the model type
   * @param fileName the file name
   * @throws TransformerConfigurationException 
   */
  public void doImport(InformationModelType modelType, final String fileName) throws TransformerConfigurationException {
    Preconditions.checkNotNull(modelType);
    Preconditions.checkNotNull(fileName);

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    // Update UI.
    modelTypeLabel.setText(modelType.getDisplayName());
    fileNameLabel.setText(fileName);

    // Instantiate appropriate importer class.
    ImportHandler importHandler = null;
    switch (modelType) {
      case CEM:
        importHandler = new CEMImporter();
        break;
      case HeD:
        importHandler = new HeDImporter();
        break;
      case FHIM:
        importHandler = new FHIMImporter();
        break;
      default:
        throw new UnsupportedOperationException(modelType.getDisplayName()
            + " import not yet supported in ISAAC.");
    }

    // Do work in background.
    Task<InformationModel> task = new ImporterTask(fileName, importHandler);

    // Bind cursor to task state.
    ObjectBinding<Cursor> cursorBinding =
        Bindings.when(task.runningProperty()).then(Cursor.WAIT)
            .otherwise(Cursor.DEFAULT);
    this.getScene().cursorProperty().bind(cursorBinding);

    Thread t = new Thread(task, "Importer_" + modelType);
    t.setDaemon(true);
    t.start();
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
   * Concrete {@link Task} for executing the import.
   *
   * @author ocarlsen
   * @author bcarlsenca
   */
  class ImporterTask extends Task<InformationModel> {

    /** The file name. */
    private final String fileName;

    /** The import handler. */
    private final ImportHandler importHandler;

    /**
     * Instantiates a {@link ImporterTask} from the specified parameters.
     *
     * @param fileName the file name
     * @param importHandler the import handler
     */
    ImporterTask(String fileName, ImportHandler importHandler) {
      this.fileName = fileName;
      this.importHandler = importHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#call()
     */
    @Override
    protected InformationModel call() throws Exception {
      InformationModel returnValue = null;
      // Do work - loop if .zip file case
      if (this.fileName.endsWith(".zip")) {
        ZipFile zipFile = new ZipFile(new File(this.fileName));
        int progress = 0;
        int maxProgress = Collections.list(zipFile.entries()).size();
        for (final ZipEntry entry : Collections.list(zipFile.entries())) {
          Platform.runLater(() -> {
            statusLabel.setText("Processing " + entry.getName());
          });
          final int progressFinal = progress;
          Platform.runLater(() -> {
            progressBar.setProgress((progressFinal*1.0) / maxProgress);
          });
          // Process each .zip or .uml file
          if (entry.getName().endsWith(".xml")
              || entry.getName().endsWith(".uml")) {
            InputStream stream = zipFile.getInputStream(entry);
            returnValue = importHandler.importModel(stream);
            stream.close();
          } else {
            Platform.runLater(() -> {
              statusLabel.setText("Skipping" + entry.getName() + ", "
                  + " wrong file type");
            });
          }
          progress++;
        }
        zipFile.close();

      } else {
        Platform.runLater(() -> {
          statusLabel.setText("Processing " + fileName);
        });
        Platform.runLater(() -> {
          progressBar.setProgress(.3);
        });
        returnValue = importHandler.importModel(new File(fileName));
      }
      Platform.runLater(() -> {
        statusLabel.setText("done");
      });
      Platform.runLater(() -> {
        progressBar.setProgress(1);
      });

      // return value
      return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#succeeded()
     */
    @Override
    protected void succeeded() {
      InformationModel result = this.getValue();

      // Update UI.
      progressBar.setProgress(1);
      statusLabel.setText("");
      resultLabel.setText("Successfully imported model.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.concurrent.Task#failed()
     */
    @Override
    protected void failed() {
      Throwable ex = getException();

      // Update UI.
      progressBar.setProgress(1);
      statusLabel.setText("");
      resultLabel.setText("Failed to import model.");

      // Show dialog.
      String title = ex.getClass().getName();
      String msg =
          String
              .format("Unexpected error importing from file \"%s\"", fileName);
      LOG.error(msg, ex);
      AppContext.getCommonDialogs()
          .showErrorDialog(title, msg, ex.getMessage());
    }
  }
}
