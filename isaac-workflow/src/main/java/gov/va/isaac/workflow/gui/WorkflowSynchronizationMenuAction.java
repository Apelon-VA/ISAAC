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
 * WorkflowSynchronizationMenuAction
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow.gui;

import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.IsaacViewWithMenusI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import javafx.application.Platform;
import javafx.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.Window;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkflowSynchronizationMenuAction
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@Singleton
public class WorkflowSynchronizationMenuAction implements IsaacViewWithMenusI
{
	private final Logger logger = LoggerFactory.getLogger(WorkflowSynchronizationMenuAction.class);

	private LocalWorkflowRuntimeEngineBI wfEngine_;

	private WorkflowSynchronizationMenuAction() throws IOException
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
				synchronize(parent.getScene().getRoot());
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
				return "Synchronize Workflow";
			}
			
			@Override
			public String getMenuId()
			{
				return "synchronizeWorkflowMenu";
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
	
	private LocalWorkflowRuntimeEngineBI getWorkflowEngine() {
		if (wfEngine_ == null)
		{
			wfEngine_ = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
		}
		
		return wfEngine_;
	}

	private void synchronize(Node root) {
		// TODO: enable BusyPopover for synchronize()
		final BusyPopover synchronizePopover = BusyPopover.createBusyPopover("Synchronizing workflow...", root);
		
		Utility.execute(() -> {
			try
			{
				getWorkflowEngine().synchronizeWithRemote();
			}
			catch (Exception e)
			{
				logger.error("Unexpected error synchronizing workflow", e);
			} finally {
				Platform.runLater(() ->  { synchronizePopover.hide(); });
			}
		});
	}
}