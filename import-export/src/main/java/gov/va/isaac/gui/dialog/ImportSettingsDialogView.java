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

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImportSettingsDialogView
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class ImportSettingsDialogView implements PopupViewI
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportSettingsDialogView.class);
	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		ArrayList<MenuItemI> menus = new ArrayList<>();
		menus.add(new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				showView(parent);
			}

			@Override
			public int getSortOrder()
			{
				return 1;
			}

			@Override
			public String getParentMenuId()
			{
				return "importExportMenu";
			}

			@Override
			public String getMenuName()
			{
				return "IMPORTER";
			}

			@Override
			public String getMenuId()
			{
				return "createImporterMenuItem";
			}

			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
		});
		return menus;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		try
		{
			ImportSettingsDialog importSettingsDialog = new ImportSettingsDialog(parent);
			importSettingsDialog.show();
		}
		catch (Exception ex)
		{
			String title = ex.getClass().getName();
			String msg = String.format("Unexpected error showing ImportSettingsDialog");
			LOG.error(msg, ex);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
		}
	}
}
