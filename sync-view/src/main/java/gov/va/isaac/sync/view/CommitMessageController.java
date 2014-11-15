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
package gov.va.isaac.sync.view;

import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 * {@link CommitMessageController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CommitMessageController
{
	@FXML private Button cancelButton;
	@FXML private GridPane layoutPane;
	@FXML private Button okButton;
	@FXML private TextArea commitMessage;

	private Consumer<String> sendResultTo_;

	@FXML
	void initialize()
	{
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CommitMessage.fxml'.";
		assert layoutPane != null : "fx:id=\"layoutPane\" was not injected: check your FXML file 'CommitMessage.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'CommitMessage.fxml'.";
		assert commitMessage != null : "fx:id=\"username\" was not injected: check your FXML file 'CommitMessage.fxml'.";

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
		okButton.setOnAction((event) -> {
			sendResultTo_.accept(commitMessage.getText());
			layoutPane.getScene().getWindow().hide();
		});
	}

	public void aboutToShow(Consumer<String> sendResultTo)
	{
		sendResultTo_ = sendResultTo;
		Platform.runLater(() -> this.commitMessage.requestFocus());
	}

	public void windowClosed()
	{
		sendResultTo_.accept(null);
	}
}
