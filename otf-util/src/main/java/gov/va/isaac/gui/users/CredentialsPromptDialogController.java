/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.users;

import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 * {@link CredentialsPromptDialogController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CredentialsPromptDialogController
{
	@FXML private Button cancelButton;
	@FXML private PasswordField password;
	@FXML private Label detailsLabel;
	@FXML private GridPane layoutPane;
	@FXML private Button okButton;
	@FXML private TextField username;

	private Consumer<Credentials> sendResultTo_;

	@FXML
	void initialize()
	{
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CredentialsPrompt.fxml'.";
		assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'CredentialsPrompt.fxml'.";
		assert detailsLabel != null : "fx:id=\"detailsLabel\" was not injected: check your FXML file 'CredentialsPrompt.fxml'.";
		assert layoutPane != null : "fx:id=\"layoutPane\" was not injected: check your FXML file 'CredentialsPrompt.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'CredentialsPrompt.fxml'.";
		assert username != null : "fx:id=\"username\" was not injected: check your FXML file 'CredentialsPrompt.fxml'.";

		cancelButton.setCancelButton(true);
		//JavaFX is silly:  https://javafx-jira.kenai.com/browse/RT-39145#comment-434189
		cancelButton.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode() == KeyCode.ENTER)
				{
					event.consume();
					cancelButton.fire();
				}
			}
		});
		cancelButton.setOnAction((event) -> {
			layoutPane.getScene().getWindow().hide();
			sendResultTo_.accept(null);
		});

		okButton.setDefaultButton(true);
		okButton.setOnAction((event) -> 
		{
			sendResultTo_.accept(new Credentials(username.getText(), password.getText()));
			layoutPane.getScene().getWindow().hide();
		});
	}

	public void aboutToShow(String username, String password, String description, Consumer<Credentials> sendResultTo)
	{
		this.username.setText(username == null ? "" : username);
		this.password.setText(password == null ? "" : password);
		detailsLabel.setText(description);
		sendResultTo_ = sendResultTo;
		Platform.runLater(() -> this.username.requestFocus());
	}

	public void windowClosed()
	{
		sendResultTo_.accept(null);
	}
}
