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
import gov.va.isaac.interfaces.gui.views.commonFunctionality.InfoModelViewI;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.util.CommonMenuBuilderI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.WBUtility;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleIntegerProperty;
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
 * @author bcarlsen
 */
public class ImportedModelsViewController {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory
      .getLogger(ImportSettingsDialogController.class);

  /** The Constant ALL. */
  private static final String ALL = "All";

  /** The border pane. */
  @FXML
  private BorderPane borderPane;

  /** The model type combo. */
  @FXML
  private ComboBox<String> modelTypeCombo;

  /** The lookup progress. */
  @FXML
  private ProgressIndicator lookupProgress;

  /** The imported models list view. */
  @FXML
  private ListView<InformationModel> importedModelsListView;

  /** The parent. */
  private Window parent;

  /**
   * Initialize.
   */
  @FXML
  public void initialize() {

    // Populate modelTypeCombo.
    modelTypeCombo.setItems(gatherComboBoxItems());

    // Handle selection changes.
    modelTypeCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable,
        String oldValue, String newValue) {
        InformationModelType modelType = getModelType(newValue);
        displayImportedModels(modelType);
      }
    });

    // Context menu for cells.
    importedModelsListView
        .setCellFactory(new Callback<ListView<InformationModel>, ListCell<InformationModel>>() {

          @Override
          public ListCell<InformationModel> call(
            final ListView<InformationModel> listView) {
            ListCell<InformationModel> cell = new InformationModelListCell();

            // Menu item to display as XML.
            MenuItem displayAsXmlMenuItem = new MenuItem("Display as XML");
            displayAsXmlMenuItem.setOnAction(new EventHandler<ActionEvent>() {

              @Override
              public void handle(ActionEvent event) {
                InformationModel item =
                    listView.getSelectionModel().getSelectedItem();
                if (item != null) {
                  displayAsXML(item);
                }
              }
            });

            // Menu item to display as Refset.
            MenuItem displayAsRefsetMenuItem =
                new MenuItem("Display as Refset");
            displayAsRefsetMenuItem
                .setOnAction(new EventHandler<ActionEvent>() {

                  @Override
                  public void handle(ActionEvent event) {
                    InformationModel item =
                        listView.getSelectionModel().getSelectedItem();
                    if (item != null) {
                      if (item.getType() == InformationModelType.CEM) {
                        InfoModelViewI imv =
                            AppContext.getService(InfoModelViewI.class);
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
                InformationModel item =
                    listView.getSelectionModel().getSelectedItem();
                if (item != null) {
                  showExportSettingsDialog(item);
                }
              }
            });

            CommonMenusDataProvider dp = new CommonMenusDataProvider() {
              @Override
              public String[] getStrings() {
                List<InformationModel> selected =
                    importedModelsListView.getSelectionModel()
                        .getSelectedItems();
                String descs[] = new String[selected.size()];
                for (int i = 0; i < selected.size(); ++i) {
                  descs[i] = selected.get(i).getName();
                }
                LOG.info("descs = " + descs);
                return descs;
              }
            };

            CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider() {
              @Override
              public Set<Integer> getNIds() {
                List<InformationModel> selected =
                    importedModelsListView.getSelectionModel()
                        .getSelectedItems();
                Set<Integer> nids = new HashSet<>();
                for (InformationModel model : selected) {
                  nids.add(WBUtility.getComponentChronicle(model.getUuid())
                      .getConceptNid());
                }
                LOG.info("nids = " + nids);
                return nids;
              }

              public IntegerExpression getObservableNidCount() {
                return new SimpleIntegerProperty(1);
              }
            };

            CommonMenuBuilderI menuBuilder =
                CommonMenus.CommonMenuBuilder.newInstance();
            menuBuilder
                .setMenuItemsToExclude(CommonMenus.CommonMenuItem.LIST_VIEW);
            List<MenuItem> menuItems =
                CommonMenus.getCommonMenus(menuBuilder, dp, nidProvider);
            menuItems.add(displayAsXmlMenuItem);
            menuItems.add(exportMenuItem);

            // BAC: skip this one, doesn't work right now:
            // displayAsRefsetMenuItem

            ContextMenu contextMenu =
                new ContextMenu(menuItems.toArray(new MenuItem[] {}));

            cell.setContextMenu(contextMenu);

            return cell;
          }
        });
  }

  /**
   * Sets the parent.
   *
   * @param parent the parent
   */
  public void setParent(Window parent) {
    this.parent = parent;

    // Start with ALL selected.
    modelTypeCombo.getSelectionModel().select(ALL);
  }

  /**
   * Returns the root.
   *
   * @return the root
   */
  public BorderPane getRoot() {
    return borderPane;
  }

  /**
   * Show export settings dialog.
   *
   * @param infoModel the info model
   */
  private void showExportSettingsDialog(InformationModel infoModel) {
    try {
      ExportSettingsDialog exportSettingsDialog =
          new ExportSettingsDialog(infoModel);
      exportSettingsDialog.show();
    } catch (Exception ex) {
      String msg =
          String.format("Unexpected error showing ExportSettingsDialog");
      LOG.error(msg, ex);
      AppContext.getCommonDialogs().showErrorDialog(msg, ex);
    }
  }

  /**
   * Display as xml.
   *
   * @param informationModel the information model
   */
  private void displayAsXML(InformationModel informationModel) {

    // Make sure in application thread.
    FxUtils.checkFxUserThread();

    try {
      InformationModelDetailsDialog modelDetailsDialog =
          new InformationModelDetailsDialog(parent);
      modelDetailsDialog.show();
      modelDetailsDialog.displayModel(informationModel);
    } catch (Exception ex) {
      String msg = "Unexpected error displaying import view";
      LOG.warn(msg, ex);
      AppContext.getCommonDialogs().showErrorDialog(msg, ex);
    }
  }

  /**
   * Display imported models.
   *
   * @param modelType the model type
   */
  private void displayImportedModels(final InformationModelType modelType) {

    // Get this in scope.
    final String modelTypeName =
        (modelType != null ? modelType.getDisplayName() : ALL);

    // Do work in background.
    Task<List<InformationModel>> task = new Task<List<InformationModel>>() {

      @Override
      protected List<InformationModel> call() throws Exception {

        // Do work.
        FetchHandler fetchHandler = new FetchHandler();
        List<InformationModel> list = fetchHandler.fetchModels(modelType);
        Collections.sort(list);
        return list;
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
        String msg =
            String.format("Unexpected error fetching models of type \"%s\"",
                modelTypeName);
        LOG.error(msg, ex);
        AppContext.getCommonDialogs().showErrorDialog(msg, ex);
      }
    };

    // Bind cursor to task state.
    ObjectBinding<Cursor> cursorBinding =
        Bindings.when(task.runningProperty()).then(Cursor.WAIT)
            .otherwise(Cursor.DEFAULT);
    parent.getScene().cursorProperty().bind(cursorBinding);

    // Bind progress indicator to task state.
    lookupProgress.visibleProperty().bind(task.runningProperty());

    Thread t = new Thread(task, "InformationModelFetcher_" + modelTypeName);
    t.setDaemon(true);
    t.start();
  }

  /**
   * Update ui.
   *
   * @param informationModels the information models
   */
  protected void updateUI(List<InformationModel> informationModels) {
    ObservableList<InformationModel> items = importedModelsListView.getItems();

    // Clear out old items.
    items.clear();

    // Add new items.
    items.addAll(informationModels);
  }

  /**
   * Returns the model type.
   *
   * @param comboBoxItem the combo box item
   * @return the model type
   */
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

  /**
   * Gather combo box items.
   *
   * @return the observable list
   */
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
