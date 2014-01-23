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
 * A {@link Stage} which can be used to show an information dialog.
 *
 * @author ocarlsen
 */
public class InformationDialog extends Stage {

    private final InformationDialogController controller;

    public InformationDialog(Stage owner) throws IOException {
        super();
        setResizable(false);

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);

        // Load from FXML.
        URL resource = InformationDialog.class.getResource("InformationDialog.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        setScene(new Scene(root));

        this.controller = loader.getController();
        controller.setVariables(this);
    }

    public void setVariables(String title, String message) {
        this.setTitle(title);
        controller.setMessageText(message);
    }
}
