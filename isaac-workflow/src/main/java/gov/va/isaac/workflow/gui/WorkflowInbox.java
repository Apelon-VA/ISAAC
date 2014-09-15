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
package gov.va.isaac.workflow.gui;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

/**
 * {@link WorkflowInbox}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
public class WorkflowInbox implements DockedViewI, IsaacViewWithMenusI
{
	WorkflowInboxController controller_;

	private WorkflowInbox() throws IOException
	{
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// We don't currently have any custom menus with this view
		ArrayList<MenuItemI> menus = new ArrayList<>();
		return menus;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.ViewI#getView()
	 */
	@Override
	public Region getView() {
		//init had to be delayed, because the current init runs way to slow, and hits the DB in the JavaFX thread.
		if (controller_ == null)
		{
			synchronized (this)
			{
				if (controller_ == null)
				{
					try
					{
						controller_ = WorkflowInboxController.init();
					}
					catch (IOException e)
					{
						//LOG.error("Unexpected error initializing the Search View", e);
						return new Label("oops - check logs");
					}
				}
			}
			
		}
		return controller_.getView();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getMenuBarMenuToShowView()
	 */
	@Override
	public MenuItemI getMenuBarMenuToShowView() {
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
				return "Workflow Inbox";
			}
			
			@Override
			public String getMenuId()
			{
				return "workflowInboxMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
			
			@Override
			public Image getImage()
			{
				return Images.INBOX.getImage();
			}
		};
		return menuItem;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Workflow Inbox";
	}
}