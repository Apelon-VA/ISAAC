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
package gov.va.isaac.gui;

import static impl.org.controlsfx.i18n.Localization.getString;
import static org.controlsfx.dialog.Dialog.ACTION_CANCEL;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.util.Images;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

/**
 * {@link LightWeightLogin}
 *
 * This code is mostly cribbed from ControlsFX - and their Login Dialog - but unfortunately, 
 * it wasn't flexible enough - so I had to reproduce it here, and change it a bit (dropdown, no stack trace printing, 
 * etc)
 * 
 * Also - all of this is in flux - the next version of JavaFX will have built in Dialog support, 
 * so this may need to be revisited.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("deprecation")
public class LightWeightLogin
{
	public static void showLoginDialog(Object owner, Consumer<Boolean> loginSuccessful)
	{
		StackPane sp = new StackPane();
		sp.setMaxWidth(Double.MAX_VALUE);
		ChoiceBox<String> userName = new ChoiceBox<>();
		userName.setMaxWidth(Double.MAX_VALUE);
		userName.setItems(ExtendedAppContext.getService(UserProfileManager.class).getUsersWithProfiles());
		userName.getSelectionModel().select(0);
		userName.setPadding(new Insets(0, 0, 0, 15));
		String preselect = ExtendedAppContext.getService(UserProfileManager.class).getLastLoggedInUser();
		if (preselect != null)
		{
			userName.getSelectionModel().select(preselect);
		}
		sp.getChildren().add(userName);
		
		ImageView userImage = Images.USER.createImageView();
		StackPane.setAlignment(userImage, Pos.CENTER_LEFT);
		StackPane.setMargin(userImage, new Insets(0, 0, 0, 3));
		sp.getChildren().add(userImage);

		CustomPasswordField txPassword = (CustomPasswordField) TextFields.createClearablePasswordField();
		txPassword.setLeft(Images.LOCK.createImageView());

		Label lbMessage = new Label("");
		lbMessage.getStyleClass().addAll("message-banner"); //$NON-NLS-1$
		lbMessage.setVisible(false);
		lbMessage.setManaged(false);

		final VBox content = new VBox(10);
		content.getChildren().add(lbMessage);
		content.getChildren().add(sp);
		content.getChildren().add(txPassword);

		DialogAction actionLogin = new DialogAction(getString("login.dlg.login.button"), null, false, false, true) //$NON-NLS-1$
		{
			{
				ButtonBar.setType(this, ButtonType.OK_DONE);
				setEventHandler(this::handleAction);
			}

			protected void handleAction(ActionEvent ae)
			{
				Dialog dlg = (Dialog) ae.getSource();
				//	try {
				if (ExtendedAppContext.getService(UserProfileManager.class).authenticateBoolean(userName.getValue(), txPassword.getText()))
				{
					lbMessage.setVisible(false);
					lbMessage.setManaged(false);
					dlg.hide();
					dlg.setResult(this);
					loginSuccessful.accept(true);
				}
				else
				{
					lbMessage.setVisible(true);
					lbMessage.setManaged(true);
					lbMessage.setText("Incorrect Password");
					dlg.shake();
					loginSuccessful.accept(false);
				}
			}

			@Override
			public String toString()
			{
				return "LOGIN";  //$NON-NLS-1$
			};
		};

		Dialog d = new Dialog(owner, "Login", true);

		d.setContent(content);

		d.setResizable(false);
		d.setIconifiable(false);
		d.setGraphic(Images.KEYS.createImageView());

		d.getActions().setAll(actionLogin, ACTION_CANCEL);
		txPassword.setPromptText("Password");
		txPassword.setText(new String(""));

		ValidationSupport validationSupport = new ValidationSupport();
		Platform.runLater(() -> 
		{
			String requiredFormat = "'%s' is required"; //$NON-NLS-1$
			validationSupport.registerValidator(txPassword, Validator.createEmptyValidator(String.format(requiredFormat, "Password")));
			actionLogin.disabledProperty().bind(validationSupport.invalidProperty());
			txPassword.requestFocus();
		});

		d.show();
	}

}
