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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.ValidBooleanBinding;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * {@link LightWeightDialogs}
 *
 * Just a simple login window. Initially used ControlsFX for this... but that broke a bunch of other things - so
 * reproduced the 'lightweight' login window here.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LightWeightDialogs
{
	public static Node buildLoginDialog(Consumer<Boolean> loginSuccessful)
	{
		BorderPane bp = init("Login", true);

		HBox mainContent = new HBox();

		ImageView keys = Images.KEYS.createImageView();
		HBox.setMargin(keys, new Insets(10));

		mainContent.getChildren().add(keys);

		StackPane userStack = new StackPane();
		userStack.setMaxWidth(Double.MAX_VALUE);
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
		userStack.getChildren().add(userName);

		ImageView userImage = Images.USER.createImageView();
		StackPane.setAlignment(userImage, Pos.CENTER_LEFT);
		StackPane.setMargin(userImage, new Insets(0, 0, 0, 3));
		userStack.getChildren().add(userImage);

		StackPane passwordStack = new StackPane();
		PasswordField txPassword = new PasswordField();
		txPassword.setPromptText("Password");
		txPassword.setPadding(new Insets(5, 0, 5, 20));
		final ValidBooleanBinding passwordEmpty = new ValidBooleanBinding()
		{
			{
				bind(txPassword.textProperty());
				setComputeOnInvalidate(true);
			}

			@Override
			protected boolean computeValue()
			{
				if (txPassword.getText().length() == 0)
				{
					setInvalidReason("Empty passwords are not allowed");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};

		ErrorMarkerUtils.setupErrorMarker(txPassword, passwordStack, passwordEmpty);
		ImageView lockImage = Images.LOCK.createImageView();
		StackPane.setMargin(lockImage, new Insets(0, 0, 0, 3));
		passwordStack.getChildren().add(lockImage);
		StackPane.setAlignment(lockImage, Pos.CENTER_LEFT);

		Label lbMessage = new Label("");
		lbMessage.getStyleClass().addAll("dialog-message-banner");
		lbMessage.setVisible(false);
		lbMessage.setManaged(false);

		final VBox content = new VBox(10);
		content.getChildren().add(lbMessage);
		content.getChildren().add(userStack);
		content.getChildren().add(passwordStack);
		HBox.setHgrow(content, Priority.ALWAYS);
		HBox.setMargin(content, new Insets(10));

		mainContent.getChildren().add(content);
		mainContent.setMaxWidth(Double.MAX_VALUE);

		bp.setCenter(mainContent);

		HBox buttons = new HBox();
		buttons.setMaxWidth(Double.MAX_VALUE);
		Region r = new Region();
		HBox.setHgrow(r, Priority.ALWAYS);
		buttons.getChildren().add(r);  //filler
		Button cancelButton = new Button("Cancel");
		cancelButton.setPadding(new Insets(5, 20, 5, 20));
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
		Button loginButton = new Button("Login");
		loginButton.setDefaultButton(true);
		loginButton.setPadding(new Insets(5, 20, 5, 20));
		loginButton.disableProperty().bind(passwordEmpty.not());

		buttons.setPadding(new Insets(10));
		buttons.setSpacing(5);
		buttons.getChildren().add(cancelButton);
		buttons.getChildren().add(loginButton);

		bp.setBottom(buttons);

		cancelButton.setOnAction((event) -> bp.setVisible(false));

		loginButton.setOnAction((event) -> {
			if (ExtendedAppContext.getService(UserProfileManager.class).authenticateBoolean(userName.getValue(), txPassword.getText()))
			{
				lbMessage.setVisible(false);
				lbMessage.setManaged(false);
				bp.setVisible(false);
				loginSuccessful.accept(true);
			}
			else
			{
				lbMessage.setVisible(true);
				lbMessage.setManaged(true);
				lbMessage.setText("Incorrect Password");
				loginSuccessful.accept(false);
				bp.autosize();
			}
		});
		
		txPassword.setOnAction((event) -> loginButton.fire());

		Platform.runLater(() -> txPassword.requestFocus());
		bp.autosize();
		return bp;
	}

	public static Node buildLoadingDialog()
	{
		BorderPane bp = init("Please wait", false);

		bp.setCenter(new Label("Loading the database"));
		ProgressBar pb = new ProgressBar(-1);
		
		pb.setMaxWidth(Double.MAX_VALUE);
		bp.setBottom(pb);
		BorderPane.setMargin(bp.getCenter(), new Insets(20));
		BorderPane.setMargin(bp.getBottom(), new Insets(0, 20, 20, 20));
		bp.autosize();
		return bp;
	}

	private static BorderPane init(String title, boolean showCloseButton)
	{
		BorderPane bp = new BorderPane();
		bp.setMinWidth(400);
		bp.setMaxWidth(400);
		bp.setMaxHeight(Control.USE_PREF_SIZE);
		bp.getStyleClass().add("dialog");

		HBox titleBar = new HBox();
		titleBar.setPadding(new Insets(5));
		titleBar.getStyleClass().add("dialog-window-header");
		bp.setTop(titleBar);

		Label login = new Label(title);
		login.setTextFill(Paint.valueOf("#ebebeb"));
		login.setFont(new Font(15));
		login.setPadding(new Insets(5, 5, 5, 2));
		titleBar.getChildren().add(login);

		if (showCloseButton)
		{
			Region r = new Region();
			HBox.setHgrow(r, Priority.ALWAYS);
			titleBar.getChildren().add(r);  //filler
			
			Button b = new Button();
			b.setMnemonicParsing(false);
			b.setStyle("-fx-cursor:hand");
			b.getStyleClass().add("window-close-button");
			b.setMinSize(17, 17);
			b.setPrefSize(17, 17);
			b.setMaxHeight(Double.MAX_VALUE);
			b.setPadding(new Insets(4.5));
			b.setScaleShape(false);
			b.setAlignment(Pos.CENTER);
			b.setOnAction((event) -> bp.setVisible(false));
			titleBar.getChildren().add(b);
		}
		return bp;
	}
}
