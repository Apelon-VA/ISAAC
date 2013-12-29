package gov.va.isaac;

import gov.va.isaac.dialog.ErrorDialog;

import java.net.URL;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

	@Override
    public void start(Stage primaryStage) throws Exception {

        URL fxmlURL = this.getClass().getResource("App.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlURL);
        Parent root = (Parent) loader.load();
        this.controller = loader.getController();
        controller.setApp(this);

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
    }

	public void showErrorDialog(final String title, final String message, final String details) {

		// Make sure in application thread.
		Toolkit.getToolkit().checkFxUserThread();

		errorDialog.setVariables(title, message, details);
		errorDialog.showAndWait();
	}

	private void shutdown() {
		LOG.info("Shutting down");
		try {
			controller.shutdown();
		} catch (Exception ex) {
			String message = "Trouble shutting down";
			LOG.warn(message, ex);
			showErrorDialog("Oops!", message, ex.getMessage());
		}
		LOG.info("Finished shutting down");

		//System.exit(0);
	}

    public static void main(String[] args) {
        Application.launch(args);
    }
}
