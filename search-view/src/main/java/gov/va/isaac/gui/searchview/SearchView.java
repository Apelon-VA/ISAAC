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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.searchview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import gov.va.isaac.gui.interfaces.DockedViewI;
import gov.va.isaac.gui.interfaces.MenuItemI;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * SearchView
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Service
@Singleton
public class SearchView implements DockedViewI
{
	private SearchViewController svc_;
	
	private SearchView() throws IOException
	{
		//created by HK2
		svc_ = SearchViewController.init();
	}
    /**
     * @see gov.va.isaac.gui.interfaces.DockedViewI#getView()
     */
    @Override
    public Region getView() {
        return svc_.getRoot();
    }
    
	/**
	 * @see gov.va.isaac.gui.interfaces.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		//We don't currently have any custom menus with this view
		return new ArrayList<MenuItemI>();
	}

	/**
	 * @see gov.va.isaac.gui.interfaces.DockedViewI#getMenuBarMenuToShowView()
	 */
	@Override
	public MenuItemI getMenuBarMenuToShowView()
	{
		MenuItemI menuItem = new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				//noop
			}
			
			@Override
			public int getSortOrder()
			{
				return 5;
			}
			
			@Override
			public String getParentMenuId()
			{
				return "panelsMenu";
			}
			
			@Override
			public String getMenuName()
			{
				return "SEARCH PANEL";
			}
			
			@Override
			public String getMenuId()
			{
				return "searchPanelMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
		};
		return menuItem;
	}

	/**
	 * @see gov.va.isaac.gui.interfaces.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle()
	{
		return "Snomed Search";
	}
}
