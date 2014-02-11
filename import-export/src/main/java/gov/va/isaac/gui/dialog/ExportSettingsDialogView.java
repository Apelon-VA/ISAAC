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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.stage.Window;
import gov.va.isaac.gui.AppContext;
import gov.va.isaac.gui.interfaces.MenuItemI;
import gov.va.isaac.gui.interfaces.PopupViewI;

/**
 * ExportSettingsDialogView
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class ExportSettingsDialogView implements PopupViewI
{
	private static final Logger LOG = LoggerFactory.getLogger(ExportSettingsDialogView.class);

	/**
	 * @see gov.va.isaac.gui.interfaces.IsaacViewI#getMenuBarMenus()
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
				return 2;
			}

			@Override
			public String getParentMenuId()
			{
				return "importExportMenu";
			}

			@Override
			public String getMenuName()
			{
				return "EXPORTER";
			}

			@Override
			public String getMenuId()
			{
				return "createExporterMenuItem";
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
	 * @see gov.va.isaac.gui.interfaces.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		try
		{
			ExportSettingsDialog exportSettingsDialog = new ExportSettingsDialog();
			exportSettingsDialog.show();
		}
		catch (Exception ex)
		{
			String title = ex.getClass().getName();
			String msg = String.format("Unexpected error showing ExportSettingsDialog");
			LOG.error(msg, ex);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, ex.getMessage());
		}
	}
}
