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
package gov.va.isaac.gui;

import gov.va.isaac.gui.dialog.ExportSettingsDialog;
import gov.va.isaac.gui.dialog.ImportSettingsDialog;
import gov.va.isaac.gui.importview.ImportView;
import gov.va.isaac.gui.interfaces.DockedViewI;
import gov.va.isaac.gui.interfaces.IsaacViewI;
import gov.va.isaac.gui.interfaces.MenuItemI;
import gov.va.isaac.gui.treeview.SctTreeItem;
import gov.va.isaac.gui.treeview.SctTreeView;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.model.InformationModelType;
import gov.va.models.cem.importer.CEMMetadataCreator;

import java.util.Hashtable;
import java.util.TreeSet;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.inject.Inject;

import org.glassfish.hk2.api.IterableProvider;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link App}.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AppController {

    private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

    @FXML private Menu importExportMenu;
    @FXML private Menu panelsMenu;
    @FXML private MenuItem taxonomyViewMenuItem;
    @FXML private SplitPane mainSplitPane;
    @FXML private BorderPane taxonomyViewPane;
    @FXML private BorderPane appBorderPane;
    @FXML private MenuBar menuBar;

    private SctTreeView sctTree;
    private boolean shutdown = false;
    private Stage importStage;
    @Inject
    private IterableProvider<IsaacViewI> moduleViews_;
    @Inject
    private IterableProvider<DockedViewI> dockedViews_;

    //Just a hashed view of all of the menus
    private final Hashtable<String, Menu> allMenus_ = new Hashtable<>();

    @FXML
    public void initialize() {

        AppContext.getServiceLocator().inject(this);

        // The FXML file puts all views into the split pane.  Remove them for starters.
        mainSplitPane.getItems().remove(taxonomyViewPane);

        //index these for ease in adding module menus

        for (Menu menu : menuBar.getMenus())
        {
            allMenus_.put(menu.getId(), menu);
        }

        //Sort them...
        TreeSet<MenuItemI> menusToAdd = new TreeSet<>();
        for (IsaacViewI view : moduleViews_)
        {
            for (MenuItemI menuItem : view.getMenuBarMenus())
            {
                menusToAdd.add(menuItem);
            }
        }

        for (final MenuItemI menuItemsToCreate : menusToAdd)
        {
            Menu parentMenu = allMenus_.get(menuItemsToCreate.getParentMenuId());
            if (parentMenu == null)
            {
                LOG.error("Cannot add module menu '" + menuItemsToCreate.getMenuId() + "' because the specified parent menu doesn't exist");
            }
            else
            {
                MenuItem menuItem = new MenuItem();
                menuItem.setId(menuItemsToCreate.getMenuId());
                menuItem.setText(menuItemsToCreate.getMenuName());
                menuItem.setMnemonicParsing(menuItemsToCreate.enableMnemonicParsing());
                menuItem.setOnAction(new EventHandler<ActionEvent>()
                {

                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        menuItemsToCreate.handleMenuSelection(appBorderPane.getScene().getWindow());
                    }
                });
                parentMenu.getItems().add(menuItem);
            }
        }
    }

    public void finishInit() {
        // Make sure in application thread.
        FxUtils.checkFxUserThread();

        // Enable the menus.
        importExportMenu.setDisable(false);
        panelsMenu.setDisable(false);

        for (final DockedViewI dv : dockedViews_)
        {
            try
            {
                Menu parentMenu = allMenus_.get(dv.getMenuBarMenuToShowView().getParentMenuId());
                if (parentMenu == null)
                {
                    LOG.error("Cannot add module menu '" + dv.getMenuBarMenuToShowView().getMenuId() + "' because the specified parent menu doesn't exist");
                }
                else
                {
                    final BorderPane bp = buildPanelForView(dv);
                    //TODO this isn't honoring sort order... need to sort all of the menus from the DockedViewI at once....
                    MenuItem mi = new MenuItem();
                    mi.setText(dv.getMenuBarMenuToShowView().getMenuName());
                    mi.setId(dv.getMenuBarMenuToShowView().getMenuId());
                    mi.setMnemonicParsing(dv.getMenuBarMenuToShowView().enableMnemonicParsing());
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            //This is a convenience call... not expected to actually show the view.
                            dv.getMenuBarMenuToShowView().handleMenuSelection(appBorderPane.getScene().getWindow());

                            if (!mainSplitPane.getItems().contains(bp))
                            {
                                bp.setVisible(true);
                                mainSplitPane.getItems().add(bp);
                            }
                        }

                    });
                    mi.disableProperty().bind(bp.visibleProperty());
                    parentMenu.getItems().add(mi);
                }
            }
            catch (Exception e)
            {
                Log.error("Unexpected error configuring DockedViewI " + (dv == null ? "?" : dv.getViewTitle()), e);
            }
        }

     // Stages for other views.
        this.importStage = buildImportStage(ExtendedAppContext.getMainApplicationWindow().getPrimaryStage());
    }

    public void shutdown() {
        LOG.info("Shutting down");
        shutdown = true;

        SctTreeView.shutdown();
        SctTreeItem.shutdown();

        LOG.info("Finished shutting down");
    }

    public void handleImportMenuItem() {
        try {
            ImportSettingsDialog importSettingsDialog = new ImportSettingsDialog(this);
            importSettingsDialog.show();
        } catch (Exception ex) {
            String title = ex.getClass().getName();
            String msg = String.format("Unexpected error showing ImportSettingsDialog");
            LOG.error(msg, ex);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
        }
    }

    public void handleExportMenuItem() {
        try {
            ExportSettingsDialog exportSettingsDialog = new ExportSettingsDialog();
            exportSettingsDialog.show();
        } catch (Exception ex) {
            String title = ex.getClass().getName();
            String msg = String.format("Unexpected error showing ExportSettingsDialog");
            LOG.error(msg, ex);
            AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
        }
    }

    public void handleTaxonomyViewMenuItem() {
        if (! taxonomyViewVisible()) {
            mainSplitPane.getItems().add(0, taxonomyViewPane);
            taxonomyViewMenuItem.setDisable(true);

            // Load tree if not already done.
            if (! (taxonomyViewPane.getCenter() instanceof SctTreeView)) {
                loadSctTree();
            }
        }
    }

    public void handleTaxonomyViewClose() {
        mainSplitPane.getItems().remove(taxonomyViewPane);
        taxonomyViewMenuItem.setDisable(false);
    }

    private BorderPane buildPanelForView(DockedViewI dockedView)
    {
        final BorderPane bp = new BorderPane();
        bp.setVisible(false);
        AnchorPane ap = new AnchorPane();
        ap.getStyleClass().add("headerBackground");

        Label l = new Label(dockedView.getViewTitle());
        AnchorPane.setLeftAnchor(l, 5.0);
        AnchorPane.setTopAnchor(l, 5.0);
        ap.getChildren().add(l);

        Button b = new Button();
        b.setMnemonicParsing(false);
        b.setStyle("-fx-cursor:hand");
        b.getStyleClass().add("tab-close-button");
        b.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                hidePanelView(bp);
            }
        });
        AnchorPane.setTopAnchor(b, 5.0);
        AnchorPane.setRightAnchor(b, 3.0);
        ap.getChildren().add(b);

        bp.setTop(ap);
        bp.setCenter(dockedView.getView(appBorderPane.getScene().getWindow()));
        return bp;
    }

    private void hidePanelView(BorderPane bp)
    {
        bp.setVisible(false);
        mainSplitPane.getItems().remove(bp);
    }

    private boolean taxonomyViewVisible() {
        return mainSplitPane.getItems().contains(taxonomyViewPane);
    }

    private void loadSctTree() {

        // Do work in background.
        Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>() {

            @Override
            protected ConceptChronicleDdo call() throws Exception {
                LOG.info("Loading root concept");
                ConceptChronicleDdo rootConcept = ExtendedAppContext.getDataStore().getFxConcept(
                        Taxonomies.SNOMED.getUuids()[0],
                        StandardViewCoordinates.getSnomedInferredLatest(),
                        VersionPolicy.ACTIVE_VERSIONS,
                        RefexPolicy.REFEX_MEMBERS,
                        RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                LOG.info("Finished loading root concept");

                return rootConcept;
            }

            @Override
            protected void succeeded() {
                ConceptChronicleDdo result = this.getValue();
                sctTree = new SctTreeView(result);
                taxonomyViewPane.setCenter(sctTree);
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = "Unexpected error loading root concept";
                String msg = ex.getClass().getName();
                LOG.error(title, ex);

                // Show dialog unless we're shutting down.
                if (! shutdown) {
                    AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
                }
            }
        };

        Thread t = new Thread(task, "Root_Concept_Load");
        t.setDaemon(true);
        t.start();
    }

     public void handleCreateMetadataMenuItem() throws Exception {

         // Do work in background.
         Task<Void> task = new Task<Void>() {

             @Override
             protected Void call() throws Exception {
                 new CEMMetadataCreator().createMetadata();

                 return null;
             }

             @Override
             protected void succeeded() {
                 AppContext.getCommonDialogs().showInformationDialog("Success", "Successfully created metadata.");
             }

             @Override
             protected void failed() {
                 Throwable ex = getException();
                 String msg = "Unexpected error creating metadata: ";
                 LOG.error(msg, ex);
                 AppContext.getCommonDialogs().showErrorDialog(msg, ex);
             }
         };

         // Bind cursor to task state.
         ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty())
                 .then(Cursor.WAIT)
                 .otherwise(Cursor.DEFAULT);
         Scene scene = AppContext.getMainApplicationWindow().getPrimaryStage().getScene();
         scene.getRoot().cursorProperty().bind(cursorBinding);

         Thread t = new Thread(task, "CreateMetadata");
         t.setDaemon(true);
         t.start();
     }

     public void showImportView(InformationModelType modelType, String fileName) {

         // Make sure in application thread.
         FxUtils.checkFxUserThread();

         try {
             ImportView importView = new ImportView();

             importStage.setScene(new Scene(importView));
             if (importStage.isShowing()) {
                 importStage.toFront();
             } else {
                 importStage.show();
             }

             importView.doImport(modelType, fileName);

         } catch (Exception ex) {
             String title = ex.getClass().getName();
             String message = "Unexpected error displaying import view";
             LOG.warn(message, ex);
             ExtendedAppContext.getCommonDialogs().showErrorDialog(title, message, ex.getMessage());
         }
     }

     private Stage buildImportStage(Stage owner) {
         // Use dialog for now, so Alo/Dan can use it.
         Stage stage = new Stage();
         stage.initModality(Modality.NONE);
         stage.initOwner(owner);
         stage.initStyle(StageStyle.DECORATED);
         stage.setTitle("Import View");

         return stage;
     }
}
