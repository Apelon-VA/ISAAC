package gov.va.isaac;

import gov.va.isaac.gui.SctTreeItem;
import gov.va.isaac.gui.SctTreeView;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.FileNotFoundException;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
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

	@FXML
	private BorderPane browserPane;

	private App app;
	private BdbTerminologyStore dataStore;
	private SctTreeView sctTree;
	private boolean shutdown = false;

	/**
	 * Called by the FXMLLoader when initialization is complete.
	 */
	@FXML
	public void initialize() {
		// Kick off a thread to open the DB connection.
		loadDataStore();
	}

	public void setApp(App app) {
		this.app = app;
	}

	public void shutdown() {
		LOG.info("Shutting down");
		shutdown = true;

		if (dataStore != null) {
			dataStore.shutdown();
		}

		SctTreeView.shutdown();
		SctTreeItem.shutdown();

		LOG.info("Finished shutting down");
	}

	private void loadDataStore() {

		// Do work in background.
		Task<ConceptChronicleDdo> task = new Task<ConceptChronicleDdo>() {

			@Override
			protected ConceptChronicleDdo call() throws Exception {
				LOG.info("Opening Workbench database");
				dataStore = getDataStore(System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY));
				LOG.info("Finished opening Workbench database");

				// Check if user shut down early.
				if (shutdown) {
					dataStore.shutdown();
					return null;
				}

				// Inject into dependent classes.
				WBUtility.setDataStore(dataStore);

				return dataStore.getFxConcept(
						Taxonomies.SNOMED.getUuids()[0],
						StandardViewCoordinates.getSnomedInferredLatest(),
						VersionPolicy.ACTIVE_VERSIONS,
						RefexPolicy.REFEX_MEMBERS,
						RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
			}

			@Override
			protected void succeeded() {
				ConceptChronicleDdo result = this.getValue();

				// User could have shut down early.
				if (result != null) {
				    AppContext appContext = new AppContext(app, dataStore);
					sctTree = new SctTreeView(appContext, result);
					browserPane.setCenter(sctTree);
				}
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
					app.showErrorDialog(title, message, details);
				} else {
					String title = "Unexpected error connecting to workbench database";
					String msg = ex.getClass().getName();
					String details = ex.getMessage();
					LOG.error(title, ex);
					app.showErrorDialog(title, msg, details);
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
}
