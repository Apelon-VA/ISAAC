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
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.cem.exporter.CEMExporter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A dialog for displaying an information model details.
 *
 * @author ocarlsen
 */
public class InformationModelDetailsPane extends GridPane {

    private static final Logger LOG = LoggerFactory.getLogger(InformationModelDetailsPane.class);

    private final Label modelNameLabel = new Label();
    private final TextArea modelXmlTextArea = new TextArea();

    public InformationModelDetailsPane() {
        super();

        // GUI placeholders.
        GridPaneBuilder builder = new GridPaneBuilder(this);
        builder.addRow("Information Model: ", modelNameLabel);
        builder.addRow(new Separator());
        builder.addRow(modelXmlTextArea);

        modelXmlTextArea.setEditable(false);

        setConstraints();

        // Set minimum dimensions.
        setMinHeight(200);
        setMinWidth(600);
    }

    public void displayModel(InformationModel informationModel) {
        Preconditions.checkNotNull(informationModel);

        // Make sure in application thread.
        FxUtils.checkFxUserThread();

        if (informationModel.getType() == InformationModelType.CEM) {
            displayCEM((CEMInformationModel) informationModel);
        } else {
            throw new UnsupportedOperationException(informationModel.getType() +
                    " display not yet supported in ISAAC.");
        }
    }

    private void displayCEM(final CEMInformationModel cemModel) {

        // Do work in background.
        Task<String> task = new Task<String>() {

            @Override
            protected String call() throws Exception {

                // Do work.
                OutputStream out = new ByteArrayOutputStream();
                CEMExporter exporter = new CEMExporter(out);
                UUID conceptUUID = cemModel.getConceptUUID();
                exporter.exportModel(conceptUUID );
                return out.toString();
            }

            @Override
            protected void succeeded() {

                // Update UI.
                modelNameLabel.setText(cemModel.getName());
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
        this.getScene().cursorProperty().bind(cursorBinding);

        Thread t = new Thread(task, "Display_" + cemModel.getName());
        t.setDaemon(true);
        t.start();
    }

    private void setConstraints() {

        // Column 1 has empty constraints.
        this.getColumnConstraints().add(new ColumnConstraints());

        // Column 2 should grow to fill space.
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column2);

        // Rows 1-2 have empty constraints.
        this.getRowConstraints().add(new RowConstraints());
        this.getRowConstraints().add(new RowConstraints());

        // Row 3 should grow to fill space.
        RowConstraints row5 = new RowConstraints();
        row5.setVgrow(Priority.ALWAYS);
        this.getRowConstraints().add(row5);
    }
}
