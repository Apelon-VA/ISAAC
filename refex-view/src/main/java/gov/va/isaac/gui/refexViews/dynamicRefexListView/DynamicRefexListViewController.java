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
package gov.va.isaac.gui.refexViews.dynamicRefexListView;

import gov.va.isaac.gui.SimpleDisplayConcept;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

/**
 * {@link DynamicRefexListViewController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class DynamicRefexListViewController
{
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private ListView<SimpleDisplayConcept> refexList;
	@FXML private ChoiceBox<?> refexStyleFilter;
	@FXML private Label refexStyleLabel;
	@FXML private AnchorPane rootPane;
	@FXML private Button clearFilterButton;
	@FXML private TextField descriptionMatchesFilter;
	@FXML private Button viewUsage;
	@FXML private Label statusLabel;
	@FXML private Label selectedRefexDescriptionLabel;
	@FXML private ListView<?> extensionFields;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Label selectedRefexNameLabel;
	@FXML private Region conceptNodeFilterPlaceholder;
	
	protected static DynamicRefexListViewController construct() throws IOException
	{
		// Load from FXML.
		URL resource = DynamicRefexListViewController.class.getResource("DynamicRefexListView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		return loader.getController();
	}

	@FXML
	void initialize()
	{
		assert refexList != null : "fx:id=\"refexList\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert refexStyleFilter != null : "fx:id=\"refexStyleFilter\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert refexStyleLabel != null : "fx:id=\"refexStyleLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert clearFilterButton != null : "fx:id=\"clearFilterButton\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert descriptionMatchesFilter != null : "fx:id=\"descriptionMatchesFilter\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert viewUsage != null : "fx:id=\"viewUsage\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert selectedRefexDescriptionLabel != null : "fx:id=\"selectedRefexDescriptionLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert extensionFields != null : "fx:id=\"extensionFields\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert executeOperationsToolbar != null : "fx:id=\"executeOperationsToolbar\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert selectedRefexNameLabel != null : "fx:id=\"selectedRefexNameLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert conceptNodeFilterPlaceholder != null : "fx:id=\"conceptNodeFilterPlaceholder\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
	}
	
	public Region getRoot()
	{
		return rootPane;
	}
}
