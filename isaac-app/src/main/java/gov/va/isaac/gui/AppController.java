package gov.va.isaac.gui;

import gov.va.isaac.gui.dialog.ExportSettingsDialog;
import gov.va.isaac.gui.dialog.ImportSettingsDialog;
import gov.va.isaac.gui.treeview.SctTreeItem;
import gov.va.isaac.gui.treeview.SctTreeView;
import gov.va.models.cem.importer.CEMMetadataCreator;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
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
    @FXML private BorderPane browserPane;

    private AppContext appContext;
    private App app;
    private SctTreeView sctTree;
    private boolean shutdown = false;

    public void setAppContext(AppContext appContext, App app) {
        this.appContext = appContext;
        this.app = app;

        // Enable the menus.
        importExportMenu.setDisable(false);

        // Kick off a thread to load the root concept.
        loadSctTree();
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

    public void handleExportMenuItem(ActionEvent ev) {
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
                browserPane.setCenter(sctTree);
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
         CEMMetadataCreator.createMetadata(appContext);
     }
}
