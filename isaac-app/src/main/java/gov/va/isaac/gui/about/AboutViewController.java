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

import java.util.Map;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.IsaacAppConfigWrapper;
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
	private Logger kjoel = LoggerFactory.getLogger(AboutViewController.class);

	private @FXML BorderPane borderPane_;
	private GridPane mainGridPane_;

	private @FXML Button okButton_;
	
	private AboutView stage_;
	
	public AboutViewController() {
	}

	@FXML
	void initialize()
	{
		assert borderPane_ != null : "fx:id=\"borderPane_\" was not injected: check your FXML file 'AboutView.fxml'.";
		assert okButton_ != null : "fx:id=\"okButton_\" was not injected: check your FXML file 'AboutView.fxml'.";

		borderPane_.setMaxWidth(Double.MAX_VALUE);
		mainGridPane_ = new GridPane();
		
		borderPane_.setCenter(mainGridPane_);

		okButton_.setOnAction((e) -> stage_.close());
	}

	void setStage(AboutView stage) {
		this.stage_ = stage;
	}
	
	public void aboutToShow()
	{
		// Load values here
		
		IsaacAppConfigWrapper appConfig = (IsaacAppConfigWrapper) AppContext.getService(IsaacAppConfigWrapper.class);

		mainGridPane_.getChildren().clear();
		int mainGridPaneRowCount = 0;
		
		// version
		// DB loaded
		// license for software
		// license for DB content
		// license statements like "this software includes things developed by Apache, etc"
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label("Application"));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("Release Version"), new Label(getReleaseVersion()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("Extension Namespace"), new Label(getExtensionNamespace()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("Dependencies"), new Label(getDependencies()));

		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("SCM"), new Label(appConfig.getScmUrl()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("ISAAC version"), new Label(appConfig.getIsaacVersion()));

		// App Licenses
		for (Map<String, String> licenseInfo : appConfig.getAppLicenses()) {
			if (licenseInfo.get("url") != null && licenseInfo.get("url").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " URL"), new Label(licenseInfo.get("url")));
			}
			if (licenseInfo.get("comment") != null && licenseInfo.get("comment").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " Comment"), new Label(licenseInfo.get("comment")));
			}
		}


		mainGridPane_.addRow(mainGridPaneRowCount++, new Label("Database"));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("DB Type"), new Label(appConfig.getDbType()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("DB Version"), new Label(appConfig.getDbVersion()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("DB Classifier"), new Label(appConfig.getDbClassifier()));

		// DB Licenses
		for (Map<String, String> licenseInfo : appConfig.getDbLicenses()) {
			if (licenseInfo.get("url") != null && licenseInfo.get("url").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " URL"), new Label(licenseInfo.get("url")));
			}
			if (licenseInfo.get("comment") != null && licenseInfo.get("comment").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " Comment"), new Label(licenseInfo.get("comment")));
			}
		}
	}
	
	private String getReleaseVersion() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		
		return loggedIn.getReleaseVersion();
	}
	
	private String getExtensionNamespace() {

		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		
		return loggedIn.getExtensionNamespace();
	}
	
	private String getDependencies() {
		return "http://apelon-va.github.io/ISAAC-PA/app/dependencies.html";
	}
}
