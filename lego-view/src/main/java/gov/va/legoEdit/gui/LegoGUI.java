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
package gov.va.legoEdit.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.PopupViewI;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LegoGUI - Just a Utility-style class that lets us run the operations 
 * needed by sprint 2.  Can also be run directly (has a main method) for test purposes.
 * 
 * Much of this will need to be redone / moved as the dependecy injection and app interfaces
 * become established.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class LegoGUI extends Application implements PopupViewI
{
	Logger logger = LoggerFactory.getLogger(LegoGUI.class);
	
	private void showSummaryPane(LegoList legoList)
	{
		LegoSummaryPane lsp = new LegoSummaryPane(legoList);

		Stage stage = new Stage();
		stage.setScene(new Scene(lsp));
		stage.getScene().getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		stage.setTitle("Lego List Viewer");
		stage.show();
	}

	public void showFileImportChooser(Window parent) throws IOException
	{
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("LEGO xml Files (*.xml)", "*.xml"));
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
		List<File> files = fc.showOpenMultipleDialog(parent);
		if (files != null && files.size() > 0)
		{
			for (String id : showImportDialog(files, parent))
			{
				showSummaryPane(BDBDataStoreImpl.getInstance().getLegoListByID(id));
			}
		}
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
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		stage.setScene(scene);
		idc.importFiles(filesToImport);
		return idc.getImportedLegoListsIDs();
	}

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, IOException
	{
		System.out.println("Launching");
		AppContext.setup();
		// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
		Field f = Hk2Looker.class.getDeclaredField("looker");
		f.setAccessible(true);
		f.set(null, AppContext.getServiceLocator());
		System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, new File("../isaac-app/berkeley-db").getAbsolutePath());
		launch(args);
	}
	
	
//	public static void shutdown()
//	{
//		Hk2Looker.get().getService(BdbTerminologyStore.class).shutdown();
//	}
	
	/**
	 * Null can be sent in to request an export of all legos...
	 * @param legoLists
	 * @throws IOException 
	 */
	protected static void export(List<LegoList> legoLists, Window parent) throws IOException
	{
			Stage exportStage = new Stage();
			exportStage.initModality(Modality.WINDOW_MODAL);
			exportStage.initOwner(parent);
			exportStage.initStyle(StageStyle.UTILITY);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(ExportDialogController.class.getResource("ExportDialog.fxml"));
			Scene scene = new Scene((Parent) loader.load(ExportDialogController.class.getResourceAsStream("ExportDialog.fxml")));
			ExportDialogController exportDC = loader.getController();
			scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
			exportStage.setScene(scene);
			exportDC.exportFiles(legoLists);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		showFileImportChooser(stage.getOwner());
		//shutdown();
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
				showView(parent);
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
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		try
		{
			showFileImportChooser(parent);
		}
		catch (IOException e)
		{
			AppContext.getCommonDialogs().showErrorDialog("Could not display the file chooser", e);
		}
	}
}
