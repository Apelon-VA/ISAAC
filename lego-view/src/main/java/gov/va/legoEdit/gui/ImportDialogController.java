/**
 * Copyright 2013
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

import gov.va.legoEdit.importer.ImportStatusCallback;
import gov.va.legoEdit.importer.LegoImporter;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ImportDialogController
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class ImportDialogController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(ImportDialogController.class);
	@FXML// fx:id="detailedMessage"
	private TextArea detailedMessage; // Value injected by FXMLLoader
	@FXML// fx:id="importName"
	private Label importName; // Value injected by FXMLLoader
	@FXML// fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader
	@FXML// fx:id="progress"
	private ProgressBar progress; // Value injected by FXMLLoader
	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	
	private List<String> importedLegoLists = null;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert detailedMessage != null : "fx:id=\"detailedMessage\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert importName != null : "fx:id=\"importName\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert progress != null : "fx:id=\"progress\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'ImportDialog.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected
		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});
	}

	public void importFiles(List<File> files)
	{
		okButton.setDisable(true);
		progress.setProgress(0.0);
		importName.setText("Importing initializing");
		((Stage) rootPane.getScene().getWindow()).setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				((Stage) rootPane.getScene().getWindow()).show();
				event.consume();
			}
		});

		ImportStatusCallback callback = new ImportStatusCallback()
		{
			@Override
			public void setProgress(final double currentProgress)
			{
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						progress.setProgress(currentProgress);
					}
				});

			}

			@Override
			public void setCurrentItemName(final String name)
			{
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						importName.setText(name);
					}
				});

			}

			@Override
			public void importComplete(List<String> importedLegoListsIDs)
			{
				importedLegoLists = importedLegoListsIDs;
				
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						okButton.setDisable(false);
						okButton.requestFocus();
					}
				});
			}

			@Override
			public void appendDetails(final String newerDetails)
			{
				if (newerDetails.length() > 0)
				{
					Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
							detailedMessage.appendText(newerDetails);
						}
					});
				}
			}
		};

		LegoImporter r = new LegoImporter(files, callback);
		Thread t = new Thread(r, "Import Thread");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		((Stage) rootPane.getScene().getWindow()).showAndWait();
	}

	public List<String> getImportedLegoListsIDs()
	{
		return importedLegoLists;
	}
}
