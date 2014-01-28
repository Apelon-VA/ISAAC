package gov.va.isaac.gui.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import com.google.common.base.Preconditions;

/**
 * Controller class for {@link InformationDialog}.
 *
 * @author ocarlsen
 */
public class InformationDialogController {

    @FXML private Label messageLabel;

    private InformationDialog informationDialog;

    @FXML
    public void initialize() {
    }

    public void setMessageText(String message) {
        messageLabel.setText(Preconditions.checkNotNull(message));
    }

    public void handleOk() {
        informationDialog.close();
    }

    public void setVariables(InformationDialog informationDialog) {
        this.informationDialog = informationDialog;
    }
}
