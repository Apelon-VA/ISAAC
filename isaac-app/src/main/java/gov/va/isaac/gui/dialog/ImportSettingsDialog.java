package gov.va.isaac.gui.dialog;

import gov.va.isaac.gui.App;
import gov.va.isaac.gui.AppContext;
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

    public ImportSettingsDialog(AppContext appContext, App app) {
        super();
        this.controller = new ImportSettingsDialogController(this, app);

        Stage owner = appContext.getAppUtil().getPrimaryStage();
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);

        Parent root = controller.getRoot();
        setScene(new Scene(root));
        setTitle("Import Settings");
    }
}
