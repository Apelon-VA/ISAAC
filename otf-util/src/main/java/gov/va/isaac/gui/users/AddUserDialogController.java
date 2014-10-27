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
import gov.va.isaac.config.generated.RoleOption;
import gov.va.isaac.config.generated.User;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.GenerateUsers;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;
import java.util.UUID;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AddUserDialogController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AddUserDialogController
{
	@FXML private TextField syncUserName;
	@FXML private Button cancelButton;
	@FXML private PasswordField password;
	@FXML private TextField fullNameUnique;
	@FXML private ListView<String> roles;
	@FXML private GridPane layoutPane;
	@FXML private TextField fullName;
	@FXML private Button okButton;
	@FXML private TextField userName;
	@FXML private TextField workflowUserName;
	@FXML private TextField uuid;

	private UserProfileManager upm_;
	private Logger logger = LoggerFactory.getLogger(AddUserDialogController.class);
	private ValidBooleanBinding fullNameUniqueValid_, userNameValid_, uuidValid_;

	@FXML
	void initialize()
	{
		assert syncUserName != null : "fx:id=\"syncUserName\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert fullNameUnique != null : "fx:id=\"fullNameUnique\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert roles != null : "fx:id=\"roles\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert layoutPane != null : "fx:id=\"layoutPane\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert fullName != null : "fx:id=\"fullName\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert userName != null : "fx:id=\"userName\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert workflowUserName != null : "fx:id=\"workflowUserName\" was not injected: check your FXML file 'AddUser.fxml'.";
		assert uuid != null : "fx:id=\"uuid\" was not injected: check your FXML file 'AddUser.fxml'.";
		
		for (RoleOption ro : RoleOption.values())
		{
			roles.getItems().add(ro.name());
		}

		upm_ = AppContext.getService(UserProfileManager.class);

		uuidValid_ = new ValidBooleanBinding()
		{
			{
				bind(uuid.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (uuid.getText().length() == 0 || Utility.isUUID(uuid.getText()))
				{
					if (uuid.getText().length() > 0 && AppContext.getService(BdbTerminologyStore.class).hasUuid(UUID.fromString(uuid.getText())))
					{
						setInvalidReason("If a UUID is specified, it must be unique");
						return false;
					}
					else
					{
						clearInvalidReason();
						return true;
					}
				}
				else
				{
					setInvalidReason("Invalid uuid");
					return false;
				}
			}
		};

		ErrorMarkerUtils.setupErrorMarkerAndSwap(uuid, layoutPane, uuidValid_);

		userNameValid_ = new ValidBooleanBinding()
		{
			{
				bind(userName.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (userName.getText().length() > 0 && !upm_.doesProfileExist(userName.getText()))
				{
					clearInvalidReason();
					return true;
				}
				else
				{
					setInvalidReason("The user name is required, and must be unique");
					return false;
				}
			}
		};

		ErrorMarkerUtils.setupErrorMarkerAndSwap(userName, layoutPane, userNameValid_);
		
		fullNameUniqueValid_ = new ValidBooleanBinding()
		{
			{
				bind(fullNameUnique.textProperty(), uuid.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (fullNameUnique.getText().length() > 0)
				{
					UUID userUuid;
					if (uuid.getText().length() > 0)
					{
						if (uuidValid_.get())
						{
							userUuid = UUID.fromString(uuid.getText());
						}
						else
						{
							setInvalidReason("If a UUID is specified, it must be valid.");
							return false;
						}
					}
					else
					{
						userUuid = GenerateUsers.calculateUserUUID(fullNameUnique.getText());
					}
					
					if (AppContext.getService(BdbTerminologyStore.class).hasUuid(userUuid))
					{
						setInvalidReason("The full name must be unique");
						return false;
					}
					else
					{
						clearInvalidReason();
						return true;
					}
				}
				else
				{
					setInvalidReason("The Full Name is required, and must be unique.  If a UUID is specified, it must be valid, and unique");
					return false;
				}
			}
		};

		ErrorMarkerUtils.setupErrorMarkerAndSwap(fullNameUnique, layoutPane, fullNameUniqueValid_);

		okButton.disableProperty().bind(fullNameUniqueValid_.and(userNameValid_).and(uuidValid_).not());

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
		});

		okButton.setDefaultButton(true);
		okButton.setOnAction((event) -> 
		{
			try
			{
				User u = new User();
				u.setFullName(fullName.getText());
				u.setPassword(password.getText());
				u.setSyncUserName(syncUserName.getText());
				u.setWorkflowUserName(workflowUserName.getText());
				u.setUniqueFullName(fullNameUnique.getText());
				u.setUniqueLogonName(userName.getText());
				u.setUUID(uuid.getText());
				for (String roleName : roles.getSelectionModel().getSelectedItems())
				{
					u.getRoles().add(RoleOption.fromValue(roleName));
				}
				upm_.createNewUser(u);
				layoutPane.getScene().getWindow().hide();
			}
			catch (Exception e)
			{
				logger.error("Error creating user", e);
				AppContext.getCommonDialogs().showErrorDialog("Unexpected error adding user", e);
			}
		});
	}

	public void aboutToShow()
	{
		syncUserName.setText("");
		password.setText("");
		fullNameUnique.setText("");
		fullName.setText("");
		userName.setText("");
		workflowUserName.setText("");
		uuid.setText("");
		roles.getSelectionModel().clearSelection();
		roles.getSelectionModel().select(RoleOption.USER.name());
		
		Platform.runLater(() -> fullNameUnique.requestFocus());
	}
}
