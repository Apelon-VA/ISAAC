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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.IsaacAppConfigWrapper;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.gui.htmlView.NativeHTMLViewer;
import gov.va.isaac.gui.util.CopyableLabel;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
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
	private Logger logger_ = LoggerFactory.getLogger(AboutViewController.class);

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
		mainGridPane_.setHgap(10);
		mainGridPane_.setPadding(new Insets(0, 10, 0, 10));
		
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
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("Release Version"), new CopyableLabel(getReleaseVersion()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("Extension Namespace"), new CopyableLabel(getExtensionNamespace()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("Dependencies"), new Hyperlink(getDependencies()));

		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("SCM"), new Hyperlink(appConfig.getScmUrl()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("ISAAC version"), new CopyableLabel(appConfig.getIsaacVersion()));

		// App Licenses
		for (Map<String, String> licenseInfo : appConfig.getAppLicenses()) {
			if (licenseInfo.get("url") != null && licenseInfo.get("url").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " URL"), new Hyperlink(licenseInfo.get("url")));
			}
			if (licenseInfo.get("comment") != null && licenseInfo.get("comment").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " Comment"), new CopyableLabel(licenseInfo.get("comment")));
			}
		}


		mainGridPane_.addRow(mainGridPaneRowCount++, new Label("Database"));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("DB Type"), new CopyableLabel(appConfig.getDbType()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("DB Version"), new CopyableLabel(appConfig.getDbVersion()));
		mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label("DB Classifier"), new CopyableLabel(appConfig.getDbClassifier()));

		// DB Licenses
		for (Map<String, String> licenseInfo : appConfig.getDbLicenses()) {
			if (licenseInfo.get("url") != null && licenseInfo.get("url").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " URL"), new Hyperlink(licenseInfo.get("url")));
			}
			if (licenseInfo.get("comment") != null && licenseInfo.get("comment").length() > 0) {
				mainGridPane_.addRow(mainGridPaneRowCount++, new Label(), new Label(licenseInfo.get("name") + " Comment"), new CopyableLabel(licenseInfo.get("comment")));
			}
		}
		
		for (Node node : mainGridPane_.getChildren()) {
			configureGridPaneNode(node);
		}
	}
	
	private static void configureGridPaneNode(Node node) {
		//System.out.println("Row: "+ GridPane.getRowIndex(node));
		int columnIndex = GridPane.getColumnIndex(node);
		
		switch(columnIndex) {
		case 0:
		case 1:
			if (node instanceof Label) {
				Label label = (Label)node;
				
				label.getStyleClass().add("boldLabel");
			}
			break;
			
		default:
			break;
		}

		if (node instanceof Hyperlink) {
			Hyperlink hyperlink = (Hyperlink)node;
			hyperlink.setOnAction(arg0 -> {
				try {
					NativeHTMLViewer.viewInBrowser(new URI(hyperlink.getText()));
				} catch (URISyntaxException e) {
					System.err.println("ERROR: while attempting to open browser on link \"" + hyperlink.getText() + "\" caught " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"");
					e.printStackTrace();
				}
			});
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
