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

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.ValidBooleanBinding;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 * {@link ChangePasswordDialogController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ChangePasswordDialogController
{
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private Label userLabel;
	@FXML private PasswordField newPassword;
	@FXML private PasswordField newPasswordRepeat;
	@FXML private PasswordField currentPassword;
	@FXML private GridPane layoutPane;
	@FXML private Button okButton;
	@FXML private Button cancelButton;
	
	private ValidBooleanBinding currentPasswordValid_, newPasswordValid_;
	private UserProfileManager upm_;

	@FXML
	void initialize()
	{
		assert userLabel != null : "fx:id=\"userLabel\" was not injected: check your FXML file 'PasswordChange.fxml'.";
		assert newPassword != null : "fx:id=\"newPassword\" was not injected: check your FXML file 'PasswordChange.fxml'.";
		assert newPasswordRepeat != null : "fx:id=\"newPasswordRepeat\" was not injected: check your FXML file 'PasswordChange.fxml'.";
		assert currentPassword != null : "fx:id=\"currentPassword\" was not injected: check your FXML file 'PasswordChange.fxml'.";
		
		upm_ = AppContext.getService(UserProfileManager.class);
		
		currentPasswordValid_ = new ValidBooleanBinding()
		{
			{
				bind(currentPassword.textProperty());
			}
			@Override
			protected boolean computeValue()
			{
				if (upm_.revalidatePassword(currentPassword.getText()))
				{
					clearInvalidReason();
					return true;
				}
				else
				{
					setInvalidReason("Invalid password");
					return false;
				}
			}
		};
		
		ErrorMarkerUtils.setupErrorMarkerAndSwap(currentPassword, layoutPane, currentPasswordValid_);
		
		newPasswordValid_ = new ValidBooleanBinding()
		{
			{
				bind(newPassword.textProperty(), newPasswordRepeat.textProperty());
			}
			@Override
			protected boolean computeValue()
			{
				if (newPassword.getText().length() > 0 && newPassword.getText().equals(newPasswordRepeat.getText()))
				{
					clearInvalidReason();
					return true;
				}
				else
				{
					setInvalidReason("The new passwords must match, and be non-empty");
					return false;
				}
			}
		};
		
		ErrorMarkerUtils.setupErrorMarkerAndSwap(newPassword, layoutPane, newPasswordValid_);
		
		okButton.disableProperty().bind(currentPasswordValid_.and(newPasswordValid_).not());
		
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
		cancelButton.setOnAction((event) ->
		{
			layoutPane.getScene().getWindow().hide();
		});
		
		okButton.setDefaultButton(true);
		okButton.setOnAction((event) ->
		{
			upm_.getCurrentlyLoggedInUserProfile().setPassword(currentPassword.getText(), newPassword.getText());
			layoutPane.getScene().getWindow().hide();
		});
	}

	public void aboutToShow()
	{
		userLabel.setText(AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUser());
		newPassword.setText("");
		newPasswordRepeat.setText("");
		currentPassword.setText("");
		Platform.runLater(() -> currentPassword.requestFocus());
	}
}
