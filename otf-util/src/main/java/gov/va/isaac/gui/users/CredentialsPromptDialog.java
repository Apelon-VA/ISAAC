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
package gov.va.isaac.gui.users;

import gov.va.isaac.AppContext;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * {@link CredentialsPromptDialog}
 *
 * A Simple dialog to allow the user to specify the correct username and password for remote operations, 
 * such as Workflow sync or Git / SVN sync.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class CredentialsPromptDialog extends Stage
{
	private CredentialsPromptDialogController controller = null;

	private CredentialsPromptDialog() throws IOException
	{
		//HK2 should call this
		super();

		setTitle("Credentials Needed");
		setResizable(true);

		Stage owner = AppContext.getMainApplicationWindow().getPrimaryStage();
		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		initStyle(StageStyle.UTILITY);
		
		setOnCloseRequest((event) ->
		{
			controller.windowClosed();
		});

		// Load from FXML.
		URL resource = this.getClass().getResource("CredentialsPrompt.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(CredentialsPromptDialog.class.getResource("/isaac-shared-styles.css").toString());
		setScene(scene);
		sizeToScene();

		this.controller = loader.getController();
	}

	/**
	 * Request the credentials dialog to appear
	 * @param username - the current username (optional)
	 * @param password - the current password (optional)
	 * @param description - The text to explain what the requested credentials are for
	 * @param sendResultTo - The callback of what to do when they click OK.  Calls back with null if they click cancel.
	 */
	public void showView(String username, String password, String description, Consumer<Credentials> sendResultTo)
	{
		controller.aboutToShow(username, password, description, sendResultTo);
		show();
		Platform.runLater(() -> requestFocus());
	}
}
