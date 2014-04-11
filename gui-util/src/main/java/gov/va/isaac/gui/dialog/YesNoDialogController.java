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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.interfaces.utility.DialogResponse;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * 
 * {@link YesNoDialogController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class YesNoDialogController implements Initializable
{
	@FXML//  fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML// fx:id="icon"
	private ImageView icon; // Value injected by FXMLLoader
	@FXML// fx:id="noButton"
	private Button noButton; // Value injected by FXMLLoader
	@FXML// fx:id="question"
	private Label question; // Value injected by FXMLLoader
	@FXML// fx:id="yesButton"
	private Button yesButton; // Value injected by FXMLLoader

	private DialogResponse answer = null;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert icon != null : "fx:id=\"icon\" was not injected: check your FXML file 'YesNoDialog.fxml'.";
		assert noButton != null : "fx:id=\"noButton\" was not injected: check your FXML file 'YesNoDialog.fxml'.";
		assert question != null : "fx:id=\"question\" was not injected: check your FXML file 'YesNoDialog.fxml'.";
		assert yesButton != null : "fx:id=\"yesButton\" was not injected: check your FXML file 'YesNoDialog.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected

		yesButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				YesNoDialogController.this.answer = DialogResponse.YES;
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});

		noButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				YesNoDialogController.this.answer = DialogResponse.NO;
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});
	}

	public void init(String question)
	{
		this.question.setText(question);
		this.answer = null;
		noButton.requestFocus();
	}

	public DialogResponse getAnswer()
	{
		return answer;
	}
}
