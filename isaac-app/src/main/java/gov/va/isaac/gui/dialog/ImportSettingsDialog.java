package gov.va.isaac.gui.dialog;

import gov.va.isaac.gui.App;
import gov.va.isaac.gui.AppContext;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A {@link Stage} which can be used to show an import settings dialog.
 *
 * @author ocarlsen
 */
public class ImportSettingsDialog extends Stage {

    private final ImportSettingsDialogController controller;

    public ImportSettingsDialog(AppContext appContext, App app) throws IOException {
        super();
        setTitle("Import Settings");
        setResizable(false);

        Stage owner = appContext.getAppUtil().getPrimaryStage();
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);

        // Load from FXML.
        URL resource = this.getClass().getResource("ImportSettingsDialog.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root);
        setScene(scene);

        this.controller = loader.getController();
        controller.setVariables(this, app);
    }
}
