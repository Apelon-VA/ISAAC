package gov.va.isaac.gui.dialog;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A {@link Stage} which can be used to show an error dialog.
 *
 * @author ocarlsen
 */
public class ErrorDialog extends Stage {

    private final ErrorDialogController controller;

    public ErrorDialog(Stage owner) throws IOException {
        super();

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);

        // Load from FXML.
        URL resource = ErrorDialogController.class.getResource("ErrorDialog.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        setScene(new Scene(root));

        this.controller = loader.getController();
    }

    public void setVariables(String title, String message, String details) {
        this.setTitle(title);
        controller.setMessageText(message);
        controller.setDetailsText(details);
    }
}
