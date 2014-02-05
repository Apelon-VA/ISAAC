package gov.va.isaac.gui;

import gov.va.isaac.gui.dialog.ExportSettingsDialog;
import gov.va.isaac.gui.dialog.ImportSettingsDialog;
import gov.va.isaac.gui.searchview.SearchViewController;
import gov.va.isaac.gui.treeview.SctTreeItem;
import gov.va.isaac.gui.treeview.SctTreeView;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.legoEdit.gui.LegoGUI;
import gov.va.models.cem.importer.CEMMetadataCreator;

import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for {@link App}.
 *
 * @author ocarlsen
 */
public class AppController {

    private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

    @FXML private Menu importExportMenu;
    @FXML private Menu panelsMenu;
    @FXML private MenuItem taxonomyViewMenuItem;
    @FXML private MenuItem searchViewMenuItem;
    @FXML private SplitPane mainSplitPane;
    @FXML private BorderPane taxonomyViewPane;
    @FXML private BorderPane searchViewPane;

    private AppContext appContext;
    private App app;
    private SctTreeView sctTree;
    private boolean shutdown = false;

    @FXML
    public void initialize() {
        // The FXML file puts all views into the split pane.  Remove them for starters.
        mainSplitPane.getItems().remove(taxonomyViewPane);
        mainSplitPane.getItems().remove(searchViewPane);
    }

    public void setAppContext(AppContext appContext, App app) {
        this.appContext = appContext;
        this.app = app;

        // Make sure in application thread.
        FxUtils.checkFxUserThread();

        // Enable the menus.
        importExportMenu.setDisable(false);
        panelsMenu.setDisable(false);

        // Finish updating the UI.
        try {
            SearchViewController searchViewController = SearchViewController.newInstance(appContext);
            searchViewPane.setCenter(searchViewController.getRoot());
        } catch (IOException ex) {
            String message = "Could not load Search View";
            LOG.warn(message, ex);
            appContext.getAppUtil().showErrorDialog(message, ex);
        }
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
            ImportSettingsDialog importSettingsDialog = new ImportSettingsDialog(appContext, app);
            importSettingsDialog.show();
        } catch (Exception ex) {
            String title = ex.getClass().getName();
            String msg = String.format("Unexpected error showing ImportSettingsDialog");
            LOG.error(msg, ex);
            appContext.getAppUtil().showErrorDialog(title, msg, ex.getMessage());
        }
    }

    public void handleLegoImportMenuItem() {
        try {
            LegoGUI.showFileImportChooser(appContext.getAppUtil().getPrimaryStage());
        } catch (Exception ex) {
            String title = ex.getClass().getName();
            String msg = String.format("Unexpected error showing LEGO Import Dialog");
            LOG.error(msg, ex);
            appContext.getAppUtil().showErrorDialog(title, msg, ex.getMessage());
        }
    }

    public void handleExportMenuItem() {
        try {
            ExportSettingsDialog exportSettingsDialog = new ExportSettingsDialog(appContext);
            exportSettingsDialog.show();
        } catch (Exception ex) {
            String title = ex.getClass().getName();
            String msg = String.format("Unexpected error showing ExportSettingsDialog");
            LOG.error(msg, ex);
            appContext.getAppUtil().showErrorDialog(title, msg, ex.getMessage());
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

    public void handleSearchViewMenuItem() {
        if (! searchViewVisible()) {

            int position = (taxonomyViewVisible() ? 1 : 0);

            mainSplitPane.getItems().add(position, searchViewPane);
            searchViewMenuItem.setDisable(true);
        }
    }

    public void handleSearchViewClose() {
        mainSplitPane.getItems().remove(searchViewPane);
        searchViewMenuItem.setDisable(false);
    }

    private boolean searchViewVisible() {
        return mainSplitPane.getItems().contains(searchViewPane);
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
                ConceptChronicleDdo rootConcept = appContext.getDataStore().getFxConcept(
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
                sctTree = new SctTreeView(appContext, result);
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
                    AppController.this.appContext.getAppUtil().showErrorDialog(title, msg, ex.getMessage());
                }
            }
        };

        Thread t = new Thread(task, "Root_Concept_Load");
        t.setDaemon(true);
        t.start();
    }

     public void handleCreateMetadataMenuItem() throws Exception {
         try {
             CEMMetadataCreator.createMetadata(appContext);
         } catch (Exception ex) {
             String title = ex.getClass().getName();
             String msg = String.format("Unexpected error creating metadata");
             LOG.error(msg, ex);
             appContext.getAppUtil().showErrorDialog(title, msg, ex.getMessage());
         }
     }
}
