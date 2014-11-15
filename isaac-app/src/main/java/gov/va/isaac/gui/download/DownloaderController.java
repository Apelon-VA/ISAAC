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
package gov.va.isaac.gui.download;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * {@link DownloaderController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DownloaderController
{
	private static Logger log = LoggerFactory.getLogger(DownloaderController.class);

	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private Button cancel;
	@FXML private Button download;
	@FXML private PasswordField password;
	@FXML private Label statusLabel;
	@FXML private ProgressBar progressBar;
	@FXML private ComboBox<String> url;
	@FXML private TextField username;
	@FXML private GridPane paramGridPane;
	@FXML private Hyperlink link;
	
	Consumer<Boolean> callOnCompletion_;
	Stage stage_;
	ValidBooleanBinding urlValid_;
	DownloadUnzipTask dut;

	@FXML
	void initialize()
	{
		assert cancel != null : "fx:id=\"cancel\" was not injected: check your FXML file 'Downloader.fxml'.";
		assert download != null : "fx:id=\"download\" was not injected: check your FXML file 'Downloader.fxml'.";
		assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Downloader.fxml'.";
		assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'Downloader.fxml'.";
		assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'Downloader.fxml'.";
		assert url != null : "fx:id=\"url\" was not injected: check your FXML file 'Downloader.fxml'.";
		assert username != null : "fx:id=\"username\" was not injected: check your FXML file 'Downloader.fxml'.";
		
		cancel.setOnAction((event) -> 
		{
			if (dut != null)
			{
				dut.cancel();
			}
			else
			{
				stage_.hide();
				callOnCompletion_.accept(false);
			}
		});
		
		download.setOnAction((event) -> 
		{
			download.disableProperty().unbind();
			download.setDisable(true);
			url.setDisable(true);
			username.setDisable(true);
			password.setDisable(true);
			progressBar.setProgress(-1);
			statusLabel.setText("Downloading...");
			download();
		});
		
		urlValid_ = new ValidBooleanBinding()
		{
			{
				bind(url.getEditor().textProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				if (url.getEditor().getText().length() == 0)
				{
					setInvalidReason("The URL is required");
					return false;
				}
				else if (!url.getEditor().getText().trim().toLowerCase().matches("https{0,1}://.*"))
				{
					setInvalidReason("The URL must start with http:// or https://");
					return false;
				}
				
				try 
				{
					new URL(url.getEditor().getText());
					clearInvalidReason();
					return true;
				}
				catch (Exception e)
				{
					setInvalidReason("The entered text is not a valid URL.");
					return false;
				}
			}
		};
		
		download.disableProperty().bind(urlValid_.not());
		
		StackPane sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(url, sp, paramGridPane);
		ErrorMarkerUtils.setupErrorMarker(url, sp, urlValid_.getReasonWhyInvalid());
		
		url.getItems().add("https://va.maestrodev.com/archiva/repository/data-files/gov/va/isaac/db/solor-snomed/2014.11.12/solor-snomed-2014.11.12-active-only.bdb.zip");
		url.getItems().add("https://va.maestrodev.com/archiva/repository/data-files/gov/va/isaac/db/solor-snomed-loinc/2014.11.12/solor-snomed-loinc-2014.11.12-active-only.bdb.zip");
		url.getItems().add("https://va.maestrodev.com/archiva/repository/data-files/gov/va/isaac/db/solor-all/2014.11.12/solor-all-2014.11.12-active-only.bdb.zip");
		
		link.setOnAction((event) ->
		{
			AppContext.getMainApplicationWindow().browseURL(
					"https://csfe.aceworkspace.net/sf/frs/do/viewRelease/projects.veterans_administration_project/frs.isaac.isaac_databases");
		});
	}
	
	protected void setStage(Stage stage)
	{
		stage_ = stage;
	}
	
	protected void callOnCompletion(Consumer<Boolean> notifyOfResult)
	{
		callOnCompletion_ = notifyOfResult;
	}
	
	private void download()
	{
		try
		{
			dut = new DownloadUnzipTask(username.getText(), password.getText(), new URL(url.getEditor().getText()));
			dut.setOnSucceeded((event) -> 
			{
				taskFinished(dut);
			});
			
			dut.setOnFailed((event) -> 
			{
				taskFinished(dut);
			});
			dut.setOnCancelled((event) -> 
			{
				taskFinished(dut);
			});
			progressBar.progressProperty().bind(dut.progressProperty());
			statusLabel.textProperty().bind(dut.titleProperty());
			Utility.execute(dut);
		}
		catch (Exception e)
		{
			log.error("Unexpected error", e);
			AppContext.getCommonDialogs().showErrorDialog("Error during download", e.getClass().getName(), e.getMessage(), stage_);
		}
	}
	
	private void taskFinished(Task<?> task)
	{
		statusLabel.textProperty().unbind();
		progressBar.progressProperty().unbind();
		
		if (task.isCancelled() || task.getException() != null)
		{
			Platform.runLater(() ->
			{
				AppContext.getCommonDialogs().showErrorDialog("Error during download", "Error during download", 
						(task.isCancelled() ? "The download was cancelled" : task.getException().getMessage()));
				download.setDisable(false);
				download.disableProperty().bind(urlValid_.not());
				url.setDisable(false);
				username.setDisable(false);
				password.setDisable(false);
				progressBar.setProgress(0);
				statusLabel.setText("Please Enter Download Information");
			});
		}
		else
		{
			stage_.hide();
			callOnCompletion_.accept(true);
		}
		dut = null;
	}
}
