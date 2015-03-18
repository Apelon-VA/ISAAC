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

import gov.va.isaac.AppContext;
import gov.va.isaac.config.IsaacAppConfigWrapper;
import gov.va.isaac.gui.htmlView.NativeHTMLViewer;
import gov.va.isaac.gui.util.CopyableLabel;
import gov.va.isaac.gui.util.WrappedLabeled;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;


/**
 * AboutViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class AboutViewController {

	private @FXML BorderPane borderPane_;
	//private GridPane mainGridPane_;

	private @FXML ScrollPane appTabScollPane_;
	private @FXML ScrollPane dbTabScollPane_;
	private @FXML ScrollPane dbDependenciesTabScollPane_;
	
	private GridPane appGridPane_;
	private GridPane dbGridPane_;
	private GridPane dbDependenciesGridPane_;
	
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

		//mainGridPane_ = new GridPane();
		appGridPane_ = new GridPane();
		appGridPane_.setHgap(10);
		appGridPane_.setVgap(10);
		appGridPane_.setPadding(new Insets(10, 10, 10, 10));
		appGridPane_.setMaxWidth(Double.MAX_VALUE);
		appTabScollPane_.setContent(appGridPane_);

		dbGridPane_ = new GridPane();
		dbGridPane_.setHgap(10);
		dbGridPane_.setVgap(10);
		dbGridPane_.setPadding(new Insets(10, 10, 10, 10));
		dbGridPane_.setMaxWidth(Double.MAX_VALUE);
		dbTabScollPane_.setContent(dbGridPane_);
		
		dbDependenciesGridPane_ = new GridPane();
		dbDependenciesGridPane_.setHgap(10);
		dbDependenciesGridPane_.setVgap(10);
		dbDependenciesGridPane_.setPadding(new Insets(10, 10, 10, 10));
		dbDependenciesGridPane_.setMaxWidth(Double.MAX_VALUE);
		dbDependenciesTabScollPane_.setContent(dbDependenciesGridPane_);

		okButton_.setOnAction((e) -> stage_.close());
	}

	void setStage(AboutView stage) {
		this.stage_ = stage;
	}
	
	public void aboutToShow()
	{
		// Load values here
		
		IsaacAppConfigWrapper appConfig = (IsaacAppConfigWrapper) AppContext.getService(IsaacAppConfigWrapper.class);
		
		appGridPane_.getChildren().clear();
		int appGridPaneRowCount = 0;
		
		// version
		// DB loaded
		// license for software
		// license for DB content
		// license statements like "this software includes things developed by Apache, etc"
		appGridPane_.addRow(appGridPaneRowCount++, new Label("Bundle Version"), WrappedLabeled.wrap(new CopyableLabel(AppContext.getAppConfiguration().getApplicationTitle() 
				+ " - " + AppContext.getAppConfiguration().getVersion())));
		appGridPane_.addRow(appGridPaneRowCount++, new Label("Dependencies"), WrappedLabeled.wrap(new Hyperlink(getDependencies())));
		appGridPane_.addRow(appGridPaneRowCount++, new Label("ISAAC version"), WrappedLabeled.wrap(new CopyableLabel(appConfig.getIsaacVersion())));
		appGridPane_.addRow(appGridPaneRowCount++, new Label("SCM"), WrappedLabeled.wrap(new Hyperlink(appConfig.getScmUrl())));
		

		// App Licenses
		for (Map<String, String> licenseInfo : appConfig.getAppLicenses()) {
			if (licenseInfo.get("comments") != null && licenseInfo.get("comments").length() > 0) {
				appGridPane_.addRow(appGridPaneRowCount++, new Label(licenseInfo.get("name")), WrappedLabeled.wrap(new CopyableLabel(
						formatLicenseComment(licenseInfo.get("comments")))));
			}
			if (licenseInfo.get("url") != null && licenseInfo.get("url").length() > 0) {
				appGridPane_.addRow(appGridPaneRowCount++, new Label(licenseInfo.get("name") + " URL"), WrappedLabeled.wrap(new Hyperlink(licenseInfo.get("url"))));
			}
		}
		
		for (Node node : appGridPane_.getChildren()) {
			configureGridPaneNode(node);
		}

		dbGridPane_.getChildren().clear();
		int dbGridPaneRowCount = 0;
		dbGridPane_.addRow(dbGridPaneRowCount++, new Label("DB Group"), new CopyableLabel(appConfig.getDbGroupId()));
		dbGridPane_.addRow(dbGridPaneRowCount++, new Label("DB Artifact"), new CopyableLabel(appConfig.getDbArtifactId()));
		dbGridPane_.addRow(dbGridPaneRowCount++, new Label("DB Version"), new CopyableLabel(appConfig.getDbVersion()));
		dbGridPane_.addRow(dbGridPaneRowCount++, new Label("DB Type"), new CopyableLabel(appConfig.getDbType()));
		dbGridPane_.addRow(dbGridPaneRowCount++, new Label("DB Classifier"), new CopyableLabel(appConfig.getDbClassifier()));

		// DB Licenses
		for (Map<String, String> licenseInfo : appConfig.getDbLicenses()) {
			if (licenseInfo.get("comments") != null && licenseInfo.get("comments").length() > 0) {
				dbGridPane_.addRow(dbGridPaneRowCount++, new Label(licenseInfo.get("name")), WrappedLabeled.wrap(new CopyableLabel(
						formatLicenseComment(licenseInfo.get("comments")))));
			}
			if (licenseInfo.get("url") != null && licenseInfo.get("url").length() > 0) {
				dbGridPane_.addRow(dbGridPaneRowCount++, new Label(licenseInfo.get("name") + " URL"), WrappedLabeled.wrap(new Hyperlink(licenseInfo.get("url"))));
			}
		}
		
		for (Node node : dbGridPane_.getChildren()) {
			configureGridPaneNode(node);
		}
		

		dbDependenciesGridPane_.getChildren().clear();
		int dbDependenciesGridPaneRowCount = 0;
		for (Map<String, String> dependency : appConfig.getDbDependencies()) {
			dbDependenciesGridPane_.addRow(dbDependenciesGridPaneRowCount++, new CopyableLabel(dependency.get("artifactId")));

			dbDependenciesGridPane_.addRow(dbDependenciesGridPaneRowCount++, new Label(), new Label("groupId"), new CopyableLabel(dependency.get("groupId")));
			dbDependenciesGridPane_.addRow(dbDependenciesGridPaneRowCount++, new Label(), new Label("version"), new CopyableLabel(dependency.get("version")));
			
			if (dependency.get("classifier") != null) {
				dbDependenciesGridPane_.addRow(dbDependenciesGridPaneRowCount++, new Label(), new Label("classifier"), 
						WrappedLabeled.wrap(new CopyableLabel(dependency.get("classifier"))));
			}
		}
		
		for (Node node : dbDependenciesGridPane_.getChildren()) {
			configureGridPaneNode(node, 2);
		}
	}
	
	private static String formatLicenseComment(String comment) {
		return comment.replaceAll("[\n\r]", "").replaceAll("\\W+", " ");
	}
	
	private static void configureGridPaneNode(Node node) {
		configureGridPaneNode(node, 1);
	}

	private static void configureGridPaneNode(Node node, int labelColumns) {
		int columnIndex = GridPane.getColumnIndex(node);

		if (columnIndex >= labelColumns)
		{
			GridPane.setHgrow(node, Priority.ALWAYS);
			GridPane.setFillWidth(node, true);
		}

		if (node instanceof Label) {
			Label label = (Label)node;
			if (columnIndex < labelColumns) {
				label.getStyleClass().add("boldLabel");
			}
		}

		if (node instanceof Hyperlink || node.getParent() instanceof Hyperlink) {
			Hyperlink hyperlink = (node instanceof Hyperlink ? (Hyperlink)node : (Hyperlink)node.getParent());
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
	
	private String getDependencies() {
		return "http://apelon-va.github.io/ISAAC-PA/app/dependencies.html";
	}
}
