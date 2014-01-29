/**
 * Copyright 2014
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
package gov.va.legoEdit.gui;

import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.wb.WBDataStore;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
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
public class LegoGUI extends Application
{
	Logger logger = LoggerFactory.getLogger(LegoGUI.class);

	private static void showSummaryPane(LegoList legoList)
	{
		LegoSummaryPane lsp = new LegoSummaryPane(legoList);

		Stage stage = new Stage();
		stage.setScene(new Scene(lsp));
		stage.getScene().getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		stage.setTitle("Lego List Viewer");
		stage.show();
	}

	public static void showFileImportChooser(Window parent) throws IOException
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

	private static List<String> showImportDialog(List<File> filesToImport, Window parent) throws IOException
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

	public static void main(String[] args)
	{
		System.out.println("Launching");
		WBDataStore.openStore(new File("../isaac-app/berkeley-db"));
		launch(args);
	}
	
	public static void shutdown()
	{
		WBDataStore.shutdown();
	}
	
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
}
