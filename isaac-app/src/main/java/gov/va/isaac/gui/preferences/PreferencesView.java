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

/**
 * PreferencesView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import gov.va.isaac.AppContext;
import gov.va.isaac.gui.users.AddUserDialog;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;

/**
 * PreferencesView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@Singleton
public class PreferencesView extends Stage implements PopupViewI, IsaacViewWithMenusI {
	private PreferencesViewController controller = null;
	
	private PreferencesView() throws IOException
	{
		//HK2 should call this
		super();

		setTitle("View/Edit User Preferences");
		setResizable(true);

		Stage owner = AppContext.getMainApplicationWindow().getPrimaryStage();
		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);  //Changed to modal because it kept accidently hiding behind things for me on linux - maybe make a full fledged window
		initStyle(StageStyle.UTILITY);  //with a bottom tab instead (at the OS level)

		// Load from FXML.
		URL resource = this.getClass().getResource("PreferencesView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(AddUserDialog.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
		sizeToScene();

		this.controller = loader.getController();
		this.controller.setStage(this);
	}
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent) {
		controller.aboutToShow();
		show();
		Platform.runLater(() -> requestFocus());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus() {
		ArrayList<MenuItemI> result = new ArrayList<MenuItemI>();
		MenuItemI mi = new MenuItemI()
		{
			
			@Override
			public void handleMenuSelection(Window parent)
			{
				PreferencesView.this.showView(parent);
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
				return "Preferences";
			}
			
			@Override
			public String getMenuId()
			{
				return "preferences";
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
		};
		result.add(mi);
		return result;
	}
}
