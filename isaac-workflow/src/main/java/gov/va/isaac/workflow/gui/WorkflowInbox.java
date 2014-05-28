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
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
public class WorkflowInbox extends Stage implements PopupViewI
{
	WorkflowInboxController controller_;

	private WorkflowInbox() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("WorkflowInbox.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(WorkflowInbox.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.CONCEPT_VIEW.getImage());

		controller_ = loader.getController();
		
		setTitle("Workflow Inbox");
		setResizable(true);

		initOwner(AppContext.getMainApplicationWindow().getPrimaryStage());
		initModality(Modality.NONE);
		initStyle(StageStyle.DECORATED);

		setWidth(600);
		setHeight(400);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// We don't currently have any custom menus with this view
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
				return 25;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "View Workflow Inbox";
			}
			
			@Override
			public String getMenuId()
			{
				return "viewWorkflowInboxMenu";
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
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		show();
		controller_.loadContent();
	}
}