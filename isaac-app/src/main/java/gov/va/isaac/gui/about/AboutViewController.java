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

/**
 * PreferencesViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.about;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AboutViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class AboutViewController {
	private Logger logger = LoggerFactory.getLogger(AboutViewController.class);

	private @FXML BorderPane borderPane_;
	private @FXML GridPane gridPane_;

	private @FXML Button okButton_;
	
	private AboutView stage_;
	
	public AboutViewController() {
	}

	@FXML
	void initialize()
	{
		assert borderPane_ != null : "fx:id=\"borderPane_\" was not injected: check your FXML file 'AboutView.fxml'.";
		assert gridPane_ != null : "fx:id=\"gridPane_\" was not injected: check your FXML file 'AboutView.fxml'.";
		assert okButton_ != null : "fx:id=\"okButton_\" was not injected: check your FXML file 'AboutView.fxml'.";

		borderPane_.setMaxWidth(Double.MAX_VALUE);

		okButton_.setOnAction((e) -> stage_.close());
	}

	void setStage(AboutView stage) {
		this.stage_ = stage;
	}
	
	public void aboutToShow()
	{
		// Load values here
		
		// version
		// DB loaded
		// license for software
		// license for DB content
		// license statements like "this software includes things developed by Apache, etc"
		gridPane_.getChildren().clear();
		int rowCount = 0;
				
		gridPane_.addRow(rowCount++, new Label("Release Version"), new Label(getReleaseVersion()));
		gridPane_.addRow(rowCount++, new Label("Extension Namespace"), new Label(getExtensionNamespace()));
	}
	
	private String getReleaseVersion() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		
		return loggedIn.getReleaseVersion();
	}
	
	private String getExtensionNamespace() {

		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		
		return loggedIn.getExtensionNamespace();
	}
}
