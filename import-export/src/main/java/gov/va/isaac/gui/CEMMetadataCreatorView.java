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
package gov.va.isaac.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.isaac.models.api.BdbInformationModelService;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.util.Utility;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.stage.Window;

import javax.inject.Singleton;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CEMMetadataCreatorView
 * 
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class CEMMetadataCreatorView implements PopupViewI
{
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
				return 4;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.ACTIONS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Create Metadata";
			}
			
			@Override
			public String getMenuId()
			{
				return "createMetadataMenuItem";
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
		// TODO should really pop up a window with a progress bar, cancel button, etc....

		// Do work in background.
		Task<Boolean> task = new Task<Boolean>()
		{
			@Override
			protected Boolean call() throws Exception
			{
			  BdbTerminologyStore dataStore = ExtendedAppContext.getDataStore();
			  InformationModelService service = new BdbInformationModelService(dataStore);
			  service.createMetadataConcepts();
			  return true;
			}

			@Override
			protected void succeeded()
			{
			    boolean metadataCreated = this.getValue();
			    if (metadataCreated) {
			        AppContext.getCommonDialogs().showInformationDialog("Success", "Successfully created metadata.");
			    } else {
			        AppContext.getCommonDialogs().showInformationDialog("Info", "Metadata has already been created.");
			    }
			}

			@Override
			protected void failed()
			{
				Throwable ex = getException();
				String msg = "Unexpected error creating metadata: ";
				LOG.error(msg, ex);
				AppContext.getCommonDialogs().showErrorDialog(msg, ex);
			}
		};

		// Bind cursor to task state.
		ObjectBinding<Cursor> cursorBinding = Bindings.when(task.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT);
		parent.getScene().cursorProperty().bind(cursorBinding);

		Utility.execute(task);
	}
}
