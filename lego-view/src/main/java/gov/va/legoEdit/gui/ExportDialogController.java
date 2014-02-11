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

import gov.va.isaac.gui.util.Images;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.util.Utility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ExportDialogController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class ExportDialogController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(ExportDialogController.class);

	@FXML// fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader
	@FXML// fx:id="chooseDirectory"
	private Button chooseDirectory; // Value injected by FXMLLoader
	@FXML// fx:id="chooseTransform"
	private Button chooseTransform; // Value injected by FXMLLoader
	@FXML// fx:id="detailedMessage"
	private TextArea detailedMessage; // Value injected by FXMLLoader
	@FXML// fx:id="exportButton"
	private Button exportButton; // Value injected by FXMLLoader
	@FXML// fx:id="exportTo"
	private TextField exportTo; // Value injected by FXMLLoader
	@FXML //  fx:id="exportToStack"
    private StackPane exportToStack; // Value injected by FXMLLoader
	@FXML// fx:id="exportType"
	private ComboBox<String> exportType; // Value injected by FXMLLoader
	@FXML// fx:id="fileExtension"
	private TextField fileExtension; // Value injected by FXMLLoader
	@FXML// fx:id="fileExtensionStack"
	private StackPane fileExtensionStack; // Value injected by FXMLLoader
	@FXML// fx:id="progress"
	private ProgressBar progress; // Value injected by FXMLLoader
	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader

	private BooleanProperty exportRunning = new SimpleBooleanProperty(false);
	private BooleanBinding exportToValid;
	private BooleanBinding fileExtensionValid;
	private List<LegoList> legoListsToExport;

	private volatile boolean requestCancel = false;
	static private File initialTransformDirectory = null;
	static private File initialExportToDirectory = null;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert chooseDirectory != null : "fx:id=\"chooseDirectory\" was not injected: check your FXML file 'ExportDialog.fxml'.";
		assert chooseTransform != null : "fx:id=\"chooseTransform\" was not injected: check your FXML file 'ExportDialog.fxml'.";
		assert detailedMessage != null : "fx:id=\"detailedMessage\" was not injected: check your FXML file 'ExportDialog.fxml'.";
		assert exportButton != null : "fx:id=\"exportButton\" was not injected: check your FXML file 'ExportDialog.fxml'.";
		assert exportType != null : "fx:id=\"exportType\" was not injected: check your FXML file 'ExportDialog.fxml'.";
		assert progress != null : "fx:id=\"progress\" was not injected: check your FXML file 'ExportDialog.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'ExportDialog.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected

		chooseDirectory.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				DirectoryChooser dc = new DirectoryChooser();
				if (initialExportToDirectory != null)
				{
					dc.setInitialDirectory(initialExportToDirectory);
				}
				File file = dc.showDialog(rootPane.getScene().getWindow());
				if (file != null)
				{
					initialExportToDirectory = file.getParentFile();
					exportTo.setText(file.getAbsolutePath());
				}
			}
		});

		chooseTransform.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				FileChooser fc = new FileChooser();
				if (initialTransformDirectory != null)
				{
					fc.setInitialDirectory(initialTransformDirectory);
				}
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XSLT Transform files (*.xslt)", "*.xslt"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XSL Transform files (*.xsl)", "*.xsl"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
				File file = fc.showOpenDialog(rootPane.getScene().getWindow());
				if (file != null)
				{
					initialTransformDirectory = file.getParentFile();
					if (exportType.getItems().size() == 3)
					{
						exportType.getItems().add(file.getAbsolutePath());
					}
					else
					{
						exportType.getItems().set(3, file.getAbsolutePath());
					}
					exportType.getSelectionModel().select(3);
				}
			}
		});

		exportType.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (exportType.getSelectionModel().getSelectedIndex() == 0)
				{
					fileExtension.setText(".xml");
				}
				else if (exportType.getSelectionModel().getSelectedIndex() == 1)
				{
					fileExtension.setText(".html");
				}
				else if (exportType.getSelectionModel().getSelectedIndex() == 2)
				{
					fileExtension.setText(".html");
				}
				else if (exportType.getSelectionModel().getSelectedIndex() == 3)
				{
					fileExtension.setText("");
				}
			}
		});
		
		exportType.getItems().add("Export as XML");
		exportType.getItems().add("Export as XHTML");
		exportType.getItems().add("Export as browser viewable XML");

		exportButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (exportRunning.get())
				{
					return;
				}
				if (exportButton.getText().equals("Close"))
				{
					((Stage) rootPane.getScene().getWindow()).close();
				}
				else
				{
					exportRunning.set(true);
					requestCancel = false;

					StreamSource ss = null;
					if (exportType.getSelectionModel().getSelectedIndex() == 0)
					{
						// leave null
					}
					else if (exportType.getSelectionModel().getSelectedIndex() == 1)
					{
						ss = new StreamSource(LegoXMLUtils.class.getResourceAsStream("/xslTransforms/LegoListToXHTML.xslt"));
					}
					else if (exportType.getSelectionModel().getSelectedIndex() == 2)
					{
						ss = new StreamSource(LegoXMLUtils.class.getResourceAsStream("/xslTransforms/xmlRenderedAsHTML.xslt"));
					}
					else if (exportType.getSelectionModel().getSelectedIndex() == 3)
					{
						ss = new StreamSource(new File(exportType.getSelectionModel().getSelectedItem()));
					}
					else
					{
						logger.error("Oops");
						return;
					}

					ExportRunnable r = new ExportRunnable(new File(exportTo.getText()), ss, fileExtension.getText());
					Utility.tpe.execute(r);
				}
			}
		});

		cancelButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (!exportRunning.get())
				{
					((Stage) rootPane.getScene().getWindow()).close();
				}
				else
				{
					requestCancel = true;
				}
			}
		});

		exportTo.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				exportToValid.invalidate();
			}
		});

		fileExtension.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				fileExtensionValid.invalidate();
			}
		});

		exportToValid = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				if (exportTo.getText().length() > 0 && new File(exportTo.getText()).isDirectory())
				{
					return true;
				}
				return false;
			}
		};

		fileExtensionValid = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				return fileExtension.getText().length() > 0;
			}
		};
		
		ImageView exportToInvalidImage = Images.EXCLAMATION.createImageView();
		exportToInvalidImage.visibleProperty().bind(exportToValid.not());
		Tooltip exportToInvalidReason = new Tooltip("A valid export folder is required");
		Tooltip.install(exportToInvalidImage, exportToInvalidReason);
		exportToStack.getChildren().add(exportToInvalidImage);
		StackPane.setAlignment(exportToInvalidImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(exportToInvalidImage, new Insets(0.0, 5.0, 0.0, 0.0));
		
		ImageView fileExtensionInvalidImage = Images.EXCLAMATION.createImageView();
		fileExtensionInvalidImage.visibleProperty().bind(fileExtensionValid.not());
		Tooltip fileExtensionInvalidReason = new Tooltip("The output file extension is required");
		Tooltip.install(fileExtensionInvalidImage, fileExtensionInvalidReason);
		fileExtensionStack.getChildren().add(fileExtensionInvalidImage);
		StackPane.setAlignment(fileExtensionInvalidImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(fileExtensionInvalidImage, new Insets(0.0, 5.0, 0.0, 0.0));

		exportType.getSelectionModel().select(0);
		exportButton.disableProperty().bind(exportRunning.or(exportToValid.not()).or(fileExtensionValid.not()));
		
		chooseDirectory.disableProperty().bind(exportRunning);
		chooseTransform.disableProperty().bind(exportRunning);
		exportType.disableProperty().bind(exportRunning);
		fileExtension.disableProperty().bind(exportRunning);
	}
	
	public void exportFiles(List<LegoList> legoLists)
	{
		legoListsToExport = legoLists;
		progress.setProgress(0.0);
		exportToValid.invalidate();
		fileExtensionValid.invalidate();
		detailedMessage.clear();
		exportButton.setText("Export");
		((Stage) rootPane.getScene().getWindow()).show();
		((Stage) rootPane.getScene().getWindow()).setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				requestCancel = true;
			}
		});
	}

	private class ExportRunnable implements Runnable
	{
		private File exportTo;
		private StreamSource xslExportTransform;
		String fileExtension;
		Transformer transformer = null;
		boolean useTidy = false;

		public ExportRunnable(File exportTo, StreamSource xslExportTransform, String fileExtension)
		{
			this.exportTo = exportTo;
			this.xslExportTransform = xslExportTransform;
			this.fileExtension = fileExtension;
		}
		int count = 0;
		StringBuilder status = new StringBuilder();

		@Override
		public void run()
		{
			try
			{
				if (xslExportTransform != null)
				{
					TransformerFactory tf = TransformerFactory.newInstance();
					transformer = tf.newTransformer(xslExportTransform);
				}

				if (!fileExtension.startsWith("."))
				{
					fileExtension = "." + fileExtension;
				}
				if (exportType.getSelectionModel().getSelectedIndex() == 1)
				{
					useTidy = true;
				}

				if (legoListsToExport == null)
				{
					Iterator<LegoList> legoListsIteratorToExport = BDBDataStoreImpl.getInstance().getLegoLists();
					while (legoListsIteratorToExport.hasNext())
					{
						if (requestCancel)
						{
							status.append("Cancelled");
							break;
						}
						process(legoListsIteratorToExport.next());
						
						status.append(System.getProperty("line.separator"));
						status.append(System.getProperty("line.separator"));
					}
				}
				else
				{
					for (final LegoList ll : legoListsToExport)
					{
						if (requestCancel)
						{
							status.append("Cancelled");
							break;
						}
						process(ll);
	
						status.append(System.getProperty("line.separator"));
						status.append(System.getProperty("line.separator"));
						count++;
					}
				}

			}
			catch (Exception e)
			{
				logger.error("Unexpected Error Exporting", e);
				status.append("Error: " + e);
			}
			finally
			{
				legoListsToExport = null;
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						detailedMessage.appendText(status.toString());
						detailedMessage.appendText(System.getProperty("line.separator") + System.getProperty("line.separator"));
						detailedMessage.appendText("Export Complete");
						progress.setProgress(1.0);
						exportButton.setText("Close");
						exportRunning.set(false);
						exportButton.requestFocus();
					}
				});
			}
		}
		
		private void process(LegoList ll)
		{
			status.append("Exporting " + ll.getGroupName());
			final String temp = status.toString();
			status.setLength(0);
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (legoListsToExport == null)
					{
						progress.setProgress(-1.0);
					}
					else {
						progress.setProgress((double) count / (double) legoListsToExport.size());
					}
					if (temp.length() > 0)
					{
						detailedMessage.appendText(temp);
					}
				}
			});

			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(new File(exportTo, ll.getGroupName() + fileExtension));
				LegoXMLUtils.transform(ll, fos, transformer, useTidy);
			}
			catch (Exception e)
			{
				status.append("Error Exporting: " + e);
				logger.error("Error during export of " + ll.getGroupName(), e);
			}
			finally
			{
				if (fos != null)
				{
					try
					{
						fos.close();
					}
					catch (IOException e)
					{
						logger.error("Error closing file", e);
					}
				}
			}
		}
	}
}
