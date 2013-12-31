package gov.va.isaac;

import gov.va.isaac.dialog.ErrorDialog;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
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

	private AppController controller;
	private ErrorDialog errorDialog;
    private boolean shutdown = false;
    private BdbTerminologyStore dataStore;

	@Override
    public void start(Stage primaryStage) throws Exception {

        URL fxmlURL = this.getClass().getResource("App.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlURL);
        Parent root = (Parent) loader.load();
        this.controller = loader.getController();

        primaryStage.setTitle("Taxonomy Viewer");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();

        // Set minimum dimensions.
        primaryStage.setMinHeight(primaryStage.getHeight());
        primaryStage.setMinWidth(primaryStage.getWidth());

		// Handle window close event.
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				shutdown();
			}
		});

        // Reusable error dialog.
        this.errorDialog = new ErrorDialog(primaryStage);

        // Kick off a thread to open the DB connection.
        loadDataStore(System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY));
    }

	public void showErrorDialog(final String title, final String message, final String details) {

		// Make sure in application thread.
		Toolkit.getToolkit().checkFxUserThread();

		errorDialog.setVariables(title, message, details);
		errorDialog.showAndWait();
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

                // Inject into dependent classes.
                WBUtility.setDataStore(dataStore);
                AppContext appContext = new AppContext(App.this, dataStore);
                controller.setAppContext(appContext);
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

    protected BdbTerminologyStore getDataStore(String bdbFolderName) throws Exception {

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
