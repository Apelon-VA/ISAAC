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
package gov.va.isaac.gui.about;

import gov.va.isaac.gui.users.AddUserDialog;
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
 * AboutView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@Singleton
public class AboutView extends Stage implements PopupViewI, IsaacViewWithMenusI {
	private AboutViewController controller = null;
	
	private AboutView() throws IOException
	{
		//HK2 should call this
		super();

		setTitle("About ISAAC");
		setResizable(true);

		initOwner(null);
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);  //with a bottom tab instead (at the OS level)
		getIcons().add(Images.INFORMATION.getImage());

		// Load from FXML.
		URL resource = this.getClass().getResource("AboutView.fxml");
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
				AboutView.this.showView(parent);
			}
			
			@Override
			public int getSortOrder()
			{
				return 15;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.HELP.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "About ISAAC";
			}
			
			@Override
			public String getMenuId()
			{
				return "about";
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
				return Images.INFORMATION.getImage(); 
			}
		};
		result.add(mi);
		return result;
	}
}
