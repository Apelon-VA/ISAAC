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
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * {@link AddUserDialog}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class AddUserDialog extends Stage implements IsaacViewWithMenusI, PopupViewI
{
	private final AddUserDialogController controller;

	private AddUserDialog() throws IOException
	{
		//HK2 should call this
		super();

		setTitle("Create New ISAAC User");
		setResizable(true);

		Stage owner = AppContext.getMainApplicationWindow().getPrimaryStage();
		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		initStyle(StageStyle.UTILITY);

		// Load from FXML.
		URL resource = this.getClass().getResource("AddUser.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(AddUserDialog.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
		sizeToScene();

		this.controller = loader.getController();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		ArrayList<MenuItemI> result = new ArrayList<MenuItemI>();
		MenuItemI mi = new MenuItemI()
		{
			
			@Override
			public void handleMenuSelection(Window parent)
			{
				AddUserDialog.this.showView(parent);
			}
			
			@Override
			public int getSortOrder()
			{
				return 11;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Add New User";
			}
			
			@Override
			public String getMenuId()
			{
				return "addNewUser";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.USER.getImage();
			}

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getDisableBinding()
			 */
			@Override
			public ObservableValue<? extends Boolean> getDisableBinding()
			{
				return new MenuEnableControl();
			}
			
			
		};
		result.add(mi);
		return result;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		controller.aboutToShow();
		show();
		Platform.runLater(() -> requestFocus());
	}
	
	private class MenuEnableControl extends SimpleBooleanProperty
	{
		{
			//true to disable, false to enable
			set(true);
			AppContext.getService(UserProfileManager.class).registerLoginCallback((username) ->
			{
				if (AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().hasRole(RoleOption.ADMIN))
				{
					set(false);
				}
			});
		}
	}
}
