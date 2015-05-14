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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.workflow.engine.RemoteSynchronizer;
import gov.va.isaac.workflow.engine.SynchronizeResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
 * {@link WorkflowInbox}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service @Named(SharedServiceNames.DOCKED)
@Singleton
public class WorkflowInbox implements DockedViewI, IsaacViewWithMenusI
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowInbox.class);
	WorkflowInboxController controller_;
	private final Logger logger = LoggerFactory.getLogger(WorkflowInbox.class);

	private WorkflowInbox() throws IOException
	{
		LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
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
				final BusyPopover synchronizePopover = BusyPopover.createBusyPopover("Synchronizing workflow...", parent.getScene().getRoot());
				
				Utility.execute(() -> {
					try
					{
						SynchronizeResult sr = AppContext.getService(RemoteSynchronizer.class).blockingSynchronize();
						
						if (sr.hasError())
						{
							AppContext.getCommonDialogs().showErrorDialog("Error Synchronizing", "There were errors during synchronization", sr.getErrorSummary());
						}
					}
					catch (Exception e)
					{
						logger.error("Unexpected error synchronizing workflow", e);
					}
					finally 
					{
						Platform.runLater(() ->  { synchronizePopover.hide(); });
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
				return "Synchronize Workflow";
			}
			
			@Override
			public String getMenuId()
			{
				return "synchronizeWorkflowMenu";
			}
			
			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.SYNC_BLUE.getImage();
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
						LOG.error("Unexpected error initializing the Workflow Inbox View", e);
						return new Label("oops - check logs");
					}
				}
			}
			
		}
		return controller_.getView();
	}

	/**
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

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Workflow Inbox";
	}
	
	/**
	 * Inform the view that the data in the datastore has changed, and it should refresh itself.
	 */
	public void reloadContent()
	{
		if (controller_ != null)
		{
			if (Platform.isFxApplicationThread())
			{
				controller_.loadContent();
			}
			else
			{
				Platform.runLater(() -> controller_.loadContent());
			}
		}
	}
}