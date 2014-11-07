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
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchView
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a> 
 */

@Service @Named(value=SharedServiceNames.DOCKED)
@Singleton
public class EnhancedSearchView implements DockedViewI
{
	private EnhancedSearchViewController svc_;
	private final Logger LOG = LoggerFactory.getLogger(EnhancedSearchView.class);
	
	protected EnhancedSearchView() throws IOException
	{
		//created by HK2
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView() {
		//init had to be delayed, because the current init runs way to slow, and hits the DB in the JavaFX thread.
		if (svc_ == null)
		{
			synchronized (this)
			{
				if (svc_ == null)
				{
					try
					{
						svc_ = EnhancedSearchViewController.init();
					}
					catch (IOException e)
					{
						LOG.error("Unexpected error initializing the Search View", e);
						return new Label("oops - check logs");
					}
				}
			}
			
		}
		return svc_.getRoot();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		//We don't currently have any custom menus with this view
		return new ArrayList<MenuItemI>();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getMenuBarMenuToShowView()
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
				return ApplicationMenus.PANELS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Enhanced Search";
			}
			
			@Override
			public String getMenuId()
			{
				return "enhancedSearchPanelMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
			
			@Override
			public Image getImage()
			{
				return Images.SEARCH.getImage();
			}
		};
		return menuItem;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle()
	{
		return "Enhanced Search";
	}
}
