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
package gov.va.isaac.gui.importedmodelsview;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.ImportSettingsDialogController;
import gov.va.isaac.ie.FetchHandler;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.util.InformationModelTypeStringConverter;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;
import javafx.util.StringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the {@link ImportedModelsView}.
 *
 * @author ocarlsen
 */
public class ImportedModelsViewController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportSettingsDialogController.class);
    private static final String ALL = "All";

    /**
     * A {@link StringConverter} for the internal {@link ComboBox}.
     * Interprets a {@code null} as the {@link #ALL} value.
     */
    private static final class MyStringConverter extends InformationModelTypeStringConverter {

        @Override
        public String toString(InformationModelType modelType) {
            if (modelType == null) {
                return ALL;
            }
            return super.toString(modelType);
        }
    }

    @FXML private BorderPane borderPane;
    @FXML private ComboBox<InformationModelType> modelTypeCombo;
    @FXML private ProgressIndicator lookupProgress;
    @FXML private ListView<String> importedModelsListView;

    private final StringConverter<InformationModelType> converter = new MyStringConverter();

    private Window parent;

    @FXML
    public void initialize() {

        // Populate modelTypeCombo.
        modelTypeCombo.setConverter(converter);
        modelTypeCombo.setItems(gatherComboBoxItems());

        // Handle selection changes.
        modelTypeCombo.valueProperty().addListener(new ChangeListener<InformationModelType>() {
            @Override
            public void changed(ObservableValue<? extends InformationModelType> observable,
                    InformationModelType oldValue,
                    InformationModelType newValue) {
                displayImportedModels(newValue);
            }
        });
    }

    public void setParent(Window parent) {
        this.parent = parent;
    }

    public BorderPane getRoot() {
        return borderPane;
    }

    private void displayImportedModels(final InformationModelType modelType) {

        // Get this in scope.
        final String modelTypeName = (modelType != null ? modelType.getDisplayName() : ALL);

        // Do work in background.
        Task<List<String>> task = new Task<List<String>>() {

            @Override
            protected List<String> call() throws Exception {

                // Do work.
                FetchHandler fetchHandler = new FetchHandler();
                return fetchHandler.fetchModels(modelType);
            }

            @Override
            protected void succeeded() {

                // Update UI.
                List<String> modelTypes = this.getValue();
                updateUI(modelTypes);
           }

            @Override
            protected void failed() {

                // Show dialog.
                Throwable ex = getException();
                String title = ex.getClass().getName();
                String msg = String.format("Unexpected error fetching models of type \"%s\"", modelTypeName);
                LOG.error(msg, ex);
                AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
            }
        };

        // Bind cursor to task state.
        ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT);
        parent.getScene().cursorProperty().bind(cursorBinding);

        // Bind progress indicator to task state.
        lookupProgress.visibleProperty().bind(task.runningProperty());

        Thread t = new Thread(task, "InformationModelFetcher_" + modelTypeName);
        t.setDaemon(true);
        t.start();
    }

    protected void updateUI(List<String> results) {
        ObservableList<String> items = importedModelsListView.getItems();

        // Clear out old items.
        items.clear();

        // Add new items.
        items.addAll(results);
    }

    private ObservableList<InformationModelType> gatherComboBoxItems() {
        ObservableList<InformationModelType> items = FXCollections.observableArrayList();
        items.addAll(InformationModelType.values());

        // We add this null to be interpreted as "All".
        // However, the ComboBox will not use the StringConverter on null values.
        // Instead, it uses the prompt text.
        items.add(null);
        modelTypeCombo.setPromptText(ALL);

        return items;
    }
}
