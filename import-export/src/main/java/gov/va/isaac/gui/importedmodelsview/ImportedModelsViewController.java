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
import gov.va.isaac.gui.dialog.ExportSettingsDialog;
import gov.va.isaac.gui.dialog.ImportSettingsDialogController;
import gov.va.isaac.gui.dialog.InformationModelDetailsDialog;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.FetchHandler;
import gov.va.isaac.interfaces.gui.views.InfoModelViewI;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.cem.CEMInformationModel;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;
import javafx.util.Callback;

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

    @FXML private BorderPane borderPane;
    @FXML private ComboBox<String> modelTypeCombo;
    @FXML private ProgressIndicator lookupProgress;
    @FXML private ListView<InformationModel> importedModelsListView;

    private Window parent;

    @FXML
    public void initialize() {

        // Populate modelTypeCombo.
        modelTypeCombo.setItems(gatherComboBoxItems());

        // Handle selection changes.
        modelTypeCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue,
                    String newValue) {
                InformationModelType modelType = getModelType(newValue);
                displayImportedModels(modelType);
            }
        });

        // Context menu for cells.
        importedModelsListView.setCellFactory(new Callback<ListView<InformationModel>, ListCell<InformationModel>>() {

            @Override
            public ListCell<InformationModel> call(final ListView<InformationModel> listView) {
                ListCell<InformationModel> cell = new InformationModelListCell();

                // Menu item to display as XML.
                MenuItem displayAsXmlMenuItem = new MenuItem("Display as XML");
                displayAsXmlMenuItem.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        InformationModel item = listView.getSelectionModel().getSelectedItem();
                        if (item != null) {
                            displayAsXML(item);
                        }
                    }
                });

                // Menu item to display as Refset.
                MenuItem displayAsRefsetMenuItem = new MenuItem("Display as Refset");
                displayAsRefsetMenuItem.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        InformationModel item = listView.getSelectionModel().getSelectedItem();
                        if (item != null) {
                            if (item.getType() == InformationModelType.CEM) {
                                InfoModelViewI imv = AppContext.getService(InfoModelViewI.class);
                                imv.setConcept(((CEMInformationModel) item).getFocusConceptUUID());
                                imv.showView(parent);
                            }
                        }
                    }
                });

                // Menu item to export.
                MenuItem exportMenuItem = new MenuItem("Export to File");
                exportMenuItem.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        InformationModel item = listView.getSelectionModel().getSelectedItem();
                        if (item != null) {
                            showExportSettingsDialog(item);
                        }
                    }
                });

                ContextMenu contextMenu = new ContextMenu(displayAsXmlMenuItem,
                        displayAsRefsetMenuItem, exportMenuItem);
                cell.setContextMenu(contextMenu);

                return cell;
            }
        });
    }

    public void setParent(Window parent) {
        this.parent = parent;

        // Start with ALL selected.
        modelTypeCombo.getSelectionModel().select(ALL);
    }

    public BorderPane getRoot() {
        return borderPane;
    }

    private void showExportSettingsDialog(InformationModel infoModel) {
        try {
            ExportSettingsDialog exportSettingsDialog = new ExportSettingsDialog(infoModel);
            exportSettingsDialog.show();
        } catch (Exception ex) {
            String msg = String.format("Unexpected error showing ExportSettingsDialog");
            LOG.error(msg, ex);
            AppContext.getCommonDialogs().showErrorDialog(msg, ex);
        }
    }

    private void displayAsXML(InformationModel informationModel) {

        // Make sure in application thread.
        FxUtils.checkFxUserThread();

        try {
            InformationModelDetailsDialog modelDetailsDialog = new InformationModelDetailsDialog(parent);
            modelDetailsDialog.show();
            modelDetailsDialog.displayModel(informationModel);
        } catch (Exception ex) {
            String msg = "Unexpected error displaying import view";
            LOG.warn(msg, ex);
            AppContext.getCommonDialogs().showErrorDialog(msg, ex);
        }
    }

    private void displayImportedModels(final InformationModelType modelType) {

        // Get this in scope.
        final String modelTypeName = (modelType != null ? modelType.getDisplayName() : ALL);

        // Do work in background.
        Task<List<InformationModel>> task = new Task<List<InformationModel>>() {

            @Override
            protected List<InformationModel> call() throws Exception {

                // Do work.
                FetchHandler fetchHandler = new FetchHandler();
                return fetchHandler.fetchModels(modelType);
            }

            @Override
            protected void succeeded() {

                // Update UI.
                List<InformationModel> informationModels = this.getValue();
                updateUI(informationModels);
           }

            @Override
            protected void failed() {

                // Show dialog.
                Throwable ex = getException();
                String msg = String.format("Unexpected error fetching models of type \"%s\"", modelTypeName);
                LOG.error(msg, ex);
                AppContext.getCommonDialogs().showErrorDialog(msg, ex);
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

    protected void updateUI(List<InformationModel> informationModels) {
        ObservableList<InformationModel> items = importedModelsListView.getItems();

        // Clear out old items.
        items.clear();

        // Add new items.
        items.addAll(informationModels);
    }

    private InformationModelType getModelType(String comboBoxItem) {
        for (InformationModelType modelType : InformationModelType.values()) {
            String displayName = modelType.getDisplayName();
            if (displayName.equals(comboBoxItem)) {
                return modelType;
            }
        }

        // Must have been "All".
        return null;
    }

    private ObservableList<String> gatherComboBoxItems() {
        ObservableList<String> items = FXCollections.observableArrayList();

        for (InformationModelType modelType : InformationModelType.values()) {
            String displayName = modelType.getDisplayName();
            items.add(displayName);
        }

        items.add(ALL);

        return items;
    }
}
