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
package gov.va.isaac.gui.refexViews.refexCreation;

import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.interfaces.gui.views.RefexCreationViewI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * {@link RefexCreationWizard}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
public class RefexCreationWizard implements RefexCreationViewI, IsaacViewWithMenusI
{
	private RefexCreationWizard() throws IOException
	{
		//created by HK2
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		ArrayList<MenuItemI> menus = new ArrayList<>();
		MenuItemI mi = new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				showView(parent);
			}
			
			@Override
			public int getSortOrder()
			{
				return 20;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Define Refex Assemblage";
			}
			
			@Override
			public String getMenuId()
			{
				return "defineRefexExtensionMenu";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
		};
		menus.add(mi);
		return menus;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.setScene(new Scene(new ScreensController(), 600, 400));
		stage.setTitle("Define Refex Assemblage");
		stage.getScene().getStylesheets().add(RefexCreationWizard.class.getResource("/isaac-shared-styles.css").toString());
		stage.show();
	}
}
