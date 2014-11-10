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

import gov.va.legoEdit.model.schemaModel.LegoList;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * {@link ExportDialog}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ExportDialog
{
	/**
	 * Null can be sent in to request an export of all legos...
	 * @param legoLists
	 * @throws IOException 
	 */
	public ExportDialog(List<LegoList> legoLists, Window parent) throws IOException
	{
		Stage exportStage = new Stage();
		exportStage.initModality(Modality.WINDOW_MODAL);
		exportStage.initOwner(parent);
		exportStage.initStyle(StageStyle.UTILITY);
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ExportDialogController.class.getResource("ExportDialog.fxml"));
		Scene scene = new Scene((Parent) loader.load(ExportDialogController.class.getResourceAsStream("ExportDialog.fxml")));
		ExportDialogController exportDC = loader.getController();
		scene.getStylesheets().add(ExportDialog.class.getResource("/isaac-shared-styles.css").toString());
		exportStage.setScene(scene);
		exportStage.sizeToScene();
		exportDC.exportFiles(legoLists);
	}
}
