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
package gov.va.isaac.gui.index;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link IndexView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class IndexView implements IsaacViewWithMenusI {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexView.class);
	
	private IndexView() throws IOException
	{
		//HK2 should call this
		super();
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
				Utility.execute(() ->
				{
					try
					{
						//TODO should have a status bar somewhere to put this progress in
						LOG.info("Full reindex launched");
						ExtendedAppContext.getDataStore().index(null);
						LOG.info("Full reindex complete");
					}
					catch (IOException e)
					{
						LOG.error("Error launching reindex", e);
					}
				});
			}
			
			@Override
			public int getSortOrder()
			{
				return 15;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Reindex Database";
			}
			
			@Override
			public String getMenuId()
			{
				return "reindexDatabase";
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
				return Images.SEARCH2.getImage(); 
			}
		};
		result.add(mi);
		return result;
	}
}
