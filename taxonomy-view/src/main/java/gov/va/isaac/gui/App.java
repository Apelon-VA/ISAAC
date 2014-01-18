package gov.va.isaac.gui;

import gov.va.isaac.gui.dialog.ErrorDialog;
import gov.va.isaac.gui.dialog.SnomedConceptView;
import gov.va.isaac.gui.importview.ImportView;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.gui.util.WBUtility;
import gov.va.isaac.model.InformationModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.UUID;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.tk.Toolkit;

/**
 * Taxonomy viewer app class.
 *
 * @author ocarlsen
 */
public class App extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private Stage primaryStage;
    private AppController controller;
    private ErrorDialog errorDialog;
    private boolean shutdown = false;
    private BdbTerminologyStore dataStore;
    private AppContext appContext;
    private Stage importStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        URL resource = this.getClass().getResource("App.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        this.controller = loader.getController();

        primaryStage.getIcons().add(Images.APPLICATION.getImage());
        primaryStage.setTitle("Taxonomy Viewer");
        primaryStage.setScene(new Scene(root));

        // Set minimum dimensions.
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);

        // Handle window close event.
        primaryStage.setOnHiding(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                shutdown();
            }
        });

        // Reusable error dialog.
        this.errorDialog = new ErrorDialog(primaryStage);

        primaryStage.show();

        // Reduce size to fit in user's screen.
        // (Need to do after stage is shown, because otherwise
        // the primary stage width & height are NaN.)
        Screen screen = Screen.getPrimary();
        double screenW = screen.getVisualBounds().getWidth();
        double screenH = screen.getVisualBounds().getHeight();
        if (primaryStage.getWidth() > screenW) {
            LOG.debug("Resizing width to " + screenW);
            primaryStage.setWidth(screenW);
        }
        if (primaryStage.getHeight() > screenH) {
            LOG.debug("Resizing height to " + screenH);
            primaryStage.setHeight(screenH);
        }

        // Kick off a thread to open the DB connection.
        loadDataStore(System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showErrorDialog(final String title, final String message, final String details) {

        // Make sure in application thread.
        Toolkit.getToolkit().checkFxUserThread();

        errorDialog.setVariables(title, message, details);
        errorDialog.showAndWait();
    }

    public void showSnomedConceptDialog(final UUID conceptUUID) {

        // Do work in background.
        Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>() {

            @Override
            protected ConceptChronicleDdo call() throws Exception {
                LOG.info("Loading concept with UUID " + conceptUUID);
                ConceptChronicleDdo concept = dataStore.getFxConcept(
                        conceptUUID,
                        StandardViewCoordinates.getSnomedInferredLatest(),
                        VersionPolicy.ACTIVE_VERSIONS,
                        RefexPolicy.REFEX_MEMBERS,
                        RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                LOG.info("Finished loading concept with UUID " + conceptUUID);

                return concept;
            }

            @Override
            protected void succeeded() {
                ConceptChronicleDdo result = this.getValue();
                showSnomedConceptDialog(result);
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                String title = "Unexpected error loading concept with UUID " + conceptUUID;
                String msg = ex.getClass().getName();
                LOG.error(title, ex);
                showErrorDialog(title, msg, ex.getMessage());
            }
        };

        Thread t = new Thread(task, "Concept_Load_" + conceptUUID);
        t.setDaemon(true);
        t.start();
    }

    public void showSnomedConceptDialog(ConceptChronicleDdo concept) {

        // Make sure in application thread.
        Toolkit.getToolkit().checkFxUserThread();

        try {
            SnomedConceptView dialog = new SnomedConceptView(appContext, primaryStage);
            dialog.setConcept(concept);
            dialog.show();
        } catch (Exception ex) {
            String message = "Unexpected error displaying snomed concept view";
            LOG.warn(message, ex);
            showErrorDialog("Unexpected Error", message, ex.getMessage());
        }
    }

    public void showImportView(InformationModel informationModel, String fileName) {

        // Make sure in application thread.
        Toolkit.getToolkit().checkFxUserThread();

        try {
            ImportView importView = new ImportView(appContext);
            importView.setVariables(informationModel, fileName);

            importStage.setScene(new Scene(importView));
            if (importStage.isShowing()) {
                importStage.toFront();
            } else {
                importStage.show();
            }

            importView.doWork();

        } catch (Exception ex) {
            String message = "Unexpected error displaying import view";
            LOG.warn(message, ex);
            showErrorDialog("Unexpected Error", message, ex.getMessage());
        }
    }

    private Stage buildImportStage() {
        // Use dialog for now, so Alo/Dan can use it.
        // TODO: Use SplitPanes like LegoEditor.
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initOwner(primaryStage);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Import View");

        return stage;
    }

    private void loadDataStore(final String bdbFolderName) {

        // Do work in background.
        Task<BdbTerminologyStore> task = new Task<BdbTerminologyStore>() {

            @Override
            protected BdbTerminologyStore call() throws Exception {
                LOG.info("Opening Workbench database");
                BdbTerminologyStore dataStore = getDataStore(bdbFolderName);
                LOG.info("Finished opening Workbench database");

                // Check if user shut down early.
                if (shutdown) {
                    dataStore.shutdown();
                    return null;
                }

                return dataStore;
            }

            @Override
            protected void succeeded() {
                dataStore = this.getValue();
                appContext = new AppContext(App.this, dataStore);

                // Inject into dependent classes.
                WBUtility.setDataStore(dataStore);
                controller.setAppContext(appContext);
                importStage = buildImportStage();
            }

            @Override
            protected void failed() {
                Throwable ex = getException();

                // Display helpful dialog to users.
                if (ex instanceof FileNotFoundException) {
                    String title = "No Snomed Database";
                    String message = "The Snomed Database was not found.";
                    LOG.error(message, ex);
                    String details = "Please download the file\n\n"
                            + "https://mgr.servers.aceworkspace.net/apps/va-archiva/repository/all/org/ihtsdo/otf/tcc-test-data/3.0/"
                            + "\n\nand unzip it into\n\n"
                            + System.getProperty("user.dir")
                            + "\n\nand then restart the editor.";
                    showErrorDialog(title, message, details);

                    // Close app since no DB to load.
                    // (The #shutdown method will be also invoked by
                    // the handler we hooked up with Stage#setOnHiding.)
                    primaryStage.hide();

                } else {
                    String title = "Unexpected error connecting to workbench database";
                    String msg = ex.getClass().getName();
                    String details = ex.getMessage();
                    LOG.error(title, ex);
                    showErrorDialog(title, msg, details);
                }
            }
        };

        Thread t = new Thread(task, "SCT_DB_Open");
        t.setDaemon(true);
        t.start();
    }

    private BdbTerminologyStore getDataStore(String bdbFolderName) throws Exception {

        // Default value if null.
        if (bdbFolderName == null) {
            bdbFolderName = "berkeley-db";  // Hard-coded in BdbTerminologyStore.
        }

        // Sanity check.
        File bdbFolderPath = new File(bdbFolderName);
        if (! bdbFolderPath.exists()) {
            throw new FileNotFoundException(bdbFolderName);
        }

        return new BdbTerminologyStore();
    }

    private void shutdown() {
        LOG.info("Shutting down");
        shutdown = true;

        try {
            if (dataStore != null) {
                dataStore.shutdown();
            }
            if (controller != null) {
                controller.shutdown();
            }
        } catch (Exception ex) {
            String message = "Trouble shutting down";
            LOG.warn(message, ex);
            showErrorDialog("Oops!", message, ex.getMessage());
        }

        LOG.info("Finished shutting down");
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
