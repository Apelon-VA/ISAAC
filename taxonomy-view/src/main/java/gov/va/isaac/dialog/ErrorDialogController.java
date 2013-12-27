package gov.va.isaac.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import com.google.common.base.Preconditions;

public class ErrorDialogController {

	@FXML private AnchorPane rootPane;
	@FXML private TextArea detailsTextArea;
	@FXML private Label messageLabel;
	@FXML private Button okButton;

	@FXML
	public void initialize() {

		// Bind detailMessage layout to whether or not it is visible.
		detailsTextArea.managedProperty().bind(detailsTextArea.visibleProperty());

		okButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});
	}

	public void setMessageText(String message) {
		messageLabel.setText(Preconditions.checkNotNull(message));
	}

	public void setDetailsText(String details) {
		if (details == null || details.length() == 0) {
			this.detailsTextArea.setVisible(false);
		} else {
			this.detailsTextArea.setText(details);
			this.detailsTextArea.setVisible(true);
		}
	}
}
