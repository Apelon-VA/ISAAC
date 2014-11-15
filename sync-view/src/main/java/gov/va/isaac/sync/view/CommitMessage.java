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
package gov.va.isaac.sync.view;

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
import javafx.stage.Window;

/**
 * 
 * {@link CommitMessage}
 *
 * A Simple dialog to collect the commit message for sync
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class CommitMessage extends Stage
{
	private CommitMessageController controller = null;
	
	protected CommitMessage(Window parent)
	{
		super();

		try
		{
			setTitle("Commit Message Required");
			setResizable(true);

			initOwner(parent);
			initModality(Modality.WINDOW_MODAL);
			initStyle(StageStyle.UTILITY);

			setOnCloseRequest((event) -> {
				controller.windowClosed();
			});

			// Load from FXML.
			URL resource = this.getClass().getResource("CommitMessage.fxml");
			FXMLLoader loader = new FXMLLoader(resource);
			Parent root = (Parent) loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(CommitMessage.class.getResource("/isaac-shared-styles.css").toString());
			setScene(scene);
			sizeToScene();

			this.controller = loader.getController();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unexpected");
		}
	}
	
	protected void getMessage(Consumer<String> sendResultTo)
	{
		controller.aboutToShow(sendResultTo);
		show();
		Platform.runLater(() -> requestFocus());
	}
}
