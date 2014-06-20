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
package gov.va.isaac.gui.listview;

import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 * {@link ListBatchOperationsRunnerController}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class ListBatchOperationsRunnerController
{
	private static ObservableList<SimpleDisplayConcept> changeList;
	@FXML private Button okButton;
	@FXML private Label titleLabel;
	@FXML private ProgressBar progressBar;
	@FXML private Label statusLabel;
	@FXML private TextArea summary;
	@FXML private BorderPane root;

	private BooleanProperty cancelRequested_;
	private volatile boolean finished_ = false;
	
	/**
	 * Cancel requested should be passed in set to false, if the user clicks cancel, this will change it to true
	 * @param observableList 
	 */
	public static ListBatchOperationsRunnerController init(BooleanProperty cancelRequested, ObservableList<SimpleDisplayConcept> items) throws IOException
	{
		// Load from FXML.
		URL resource = ListBatchOperationsRunnerController.class.getResource("ListBatchOperationsRunner.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		ListBatchOperationsRunnerController lborc = loader.getController();
		lborc.cancelRequested_ = cancelRequested;
		changeList = items;
		
		return lborc;
	}

	@FXML
	public void initialize()
	{
		progressBar.setProgress(0);
		summary.setEditable(false);
		
		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (finished_)
				{
					root.getScene().getWindow().hide();
				}
				else
				{
					cancelRequested_.set(true);
					okButton.setDisable(true);
					okButton.setText("Cancelling");
				}
			}
		});
	}
	/**
	 * JavaFX Threadsafe
	 */
	public void finished()
	{
		finished_ = true;
		Platform.runLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				okButton.setDisable(false);  //Just in case a race condition causes us to cancel-request and finish at the same time
				progressBar.progressProperty().unbind();
				progressBar.setProgress(1.0);  //In case it didn't end right
				titleLabel.setText("");
				statusLabel.setText("Done");
			}
		});
	}
	
	/**
	 * JavaFX Threadsafe
	 */
	public void setTitle(final String operationTitle)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				titleLabel.setText(operationTitle);
			}
		});
	}
	
	public StringProperty getMessageProperty()
	{
		return statusLabel.textProperty();
	}
	
	public DoubleProperty getProgressProperty()
	{
		return progressBar.progressProperty();
	}
	
	public TextArea getSummary()
	{
		return summary;
	}

	public Parent getRoot()
	{
		return root;
	}
}
