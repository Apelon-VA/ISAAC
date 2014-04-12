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
package gov.va.legoEdit.gui.legoListView;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.legoEdit.gui.ImportDialogController;
import gov.va.legoEdit.gui.LegoSummaryPane;
import gov.va.legoEdit.gui.legoFilterPane.LegoFilterPaneController;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link LegoListView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
@Singleton
public class LegoListView implements DockedViewI
{
	private LegoFilterPaneController lfpc_;
	
	private LegoListView()
	{
		//Created by HK2
		lfpc_ = LegoFilterPaneController.init();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.ViewI#getView()
	 */
	@Override
	public Region getView()
	{
		return lfpc_.getBorderPane();
	}

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
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("LEGO xml Files (*.xml)", "*.xml"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
				List<File> files = fc.showOpenMultipleDialog(parent);
				if (files != null && files.size() > 0)
				{
					try
					{
						for (String id : showImportDialog(files, parent))
						{
							AppContext.getService(LegoListView.class).updateListView();
							new LegoSummaryPane(BDBDataStoreImpl.getInstance().getLegoListByID(id)).show();
						}
					}
					catch (IOException e)
					{
						AppContext.getCommonDialogs().showErrorDialog("Could not display the file chooser", e);
					}
				}
			}
			
			@Override
			public int getSortOrder()
			{
				return 5;
			}
			
			@Override
			public String getParentMenuId()
			{
				return "importExportMenu";
			}
			
			@Override
			public String getMenuName()
			{
				return "LEGO IMPORTER";
			}
			
			@Override
			public String getMenuId()
			{
				return "legoImporterMenuItem";
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
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getMenuBarMenuToShowView()
	 */
	@Override
	public MenuItemI getMenuBarMenuToShowView()
	{
		return new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				//noop
			}
			
			@Override
			public int getSortOrder()
			{
				return 6;
			}
			
			@Override
			public String getParentMenuId()
			{
				return "panelsMenu";
			}
			
			@Override
			public String getMenuName()
			{
				return "Lego Lists";
			}
			
			@Override
			public String getMenuId()
			{
				return "legoListViewMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
		};
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle()
	{
		return "Lego Lists";
	}
	
	private List<String> showImportDialog(List<File> filesToImport, Window parent) throws IOException
	{
		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(parent);
		stage.initStyle(StageStyle.UTILITY);
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ImportDialogController.class.getResource("ImportDialog.fxml"));
		Scene scene = new Scene((Parent) loader.load(ImportDialogController.class.getResourceAsStream("ImportDialog.fxml")));
		ImportDialogController idc = loader.getController();
		scene.getStylesheets().add(LegoListView.class.getResource("/isaac-shared-styles.css").toString());
		stage.setScene(scene);
		idc.importFiles(filesToImport);
		return idc.getImportedLegoListsIDs();
	}
	
	public void updateListView()
	{
		lfpc_.reloadOptions();
		lfpc_.updateLegoList();
	}
}
