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
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.legoEdit.gui.ExportDialog;
import gov.va.legoEdit.gui.ImportDialogController;
import gov.va.legoEdit.gui.LegoSummaryPane;
import gov.va.legoEdit.gui.legoFilterPane.LegoFilterPaneController;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.tk.Toolkit;

/**
 * {@link LegoListView}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service @Named(value=SharedServiceNames.DOCKED)
@Singleton
public class LegoListView implements DockedViewI
{
	private LegoFilterPaneController lfpc_;
	Logger logger = LoggerFactory.getLogger(LegoListView.class);
	
	private LegoListView()
	{
		//Created by HK2
		//delay init of lfpc_
		logger.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.ViewI#getView()
	 */
	@Override
	public Region getView()
	{
		if (lfpc_ == null)
		{
			lfpc_ = LegoFilterPaneController.init();
		}
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
						List<String> importedFiles = showImportDialog(files, parent);
						AppContext.getService(LegoListView.class).updateListView();
						if (importedFiles.size() < 5)
						{
							for (String id : importedFiles)
							{
								new LegoSummaryPane(BDBDataStoreImpl.getInstance().getLegoListByID(id)).show();
							}
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
				return ApplicationMenus.IMPORT_EXPORT.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Import Legos...";
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

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.IMPORT.getImage();
			}
		});
		
		menus.add(new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				try
				{
					new ExportDialog(null, parent);
				}
				catch (Exception e)
				{
					logger.error("Unexpected error exporting LegoList ", e);
					AppContext.getCommonDialogs().showErrorDialog("Error exporting Lego Lists", e);
				}
			}
			
			@Override
			public int getSortOrder()
			{
				return 6;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.IMPORT_EXPORT.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Export All Lego Lists...";
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

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.LEGO_EXPORT.getImage();
			}
		});
		
		menus.add(new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				try
				{
					ArrayList<LegoList> list = new ArrayList<>();
					for (LegoListByReference llbr : getCurrentlyDisplayedLegoLists())
					{
						list.add(BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID()));
					}
					new ExportDialog(list, parent);
				}
				catch (Exception e)
				{
					logger.error("Unexpected error exporting LegoList ", e);
					AppContext.getCommonDialogs().showErrorDialog("Error exporting Lego Lists", e);
				}
			}
			
			@Override
			public int getSortOrder()
			{
				return 7;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.IMPORT_EXPORT.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Export Filtered Lego Lists...";
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

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.LEGO_EXPORT.getImage();
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
				return ApplicationMenus.PANELS.getMenuId();
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
			
			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.LEGO_LIST_VIEW.getImage();
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
		if (Toolkit.getToolkit().isFxUserThread())
		{
			if (lfpc_ == null)
			{
				lfpc_ = LegoFilterPaneController.init();
			}
			lfpc_.reloadOptions();
			lfpc_.updateLegoList();
		}
		else
		{
			Platform.runLater(() -> 
			{
				if (lfpc_ == null)
				{
					lfpc_ = LegoFilterPaneController.init();
				}
				lfpc_.reloadOptions();
				lfpc_.updateLegoList();
			});
		}
	}
	
	public List<LegoListByReference> getCurrentlyDisplayedLegoLists()
	{
		if (lfpc_ == null)
		{
			lfpc_ = LegoFilterPaneController.init();
		}
		return lfpc_.getCurrentlyDisplayedLegoLists();
	}
}
