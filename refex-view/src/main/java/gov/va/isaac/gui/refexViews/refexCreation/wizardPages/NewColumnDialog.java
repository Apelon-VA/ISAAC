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
package gov.va.isaac.gui.refexViews.refexCreation.wizardPages;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

/**
 * {@link NewColumnDialog}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class NewColumnDialog extends Stage
{
	private NewColumnDialogController ncdc_;

	public NewColumnDialog(Window owner) throws IOException
	{
		super();

		setTitle("New Attribute Creation");
		setResizable(true);

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		initStyle(StageStyle.UTILITY);

		// Load from FXML.
		URL resource = this.getClass().getResource("newColumn.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		setScene(scene);

		ncdc_ = loader.getController();
		
		//Problem on linux, where modal windows don't always stay on top...
		iconifiedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				Platform.runLater(() -> {
					toFront();
				});
			}
		});
	}
	
	public ConceptChronicleBI getNewColumnConcept() {
		return ncdc_.getNewColumnConcept();
	}
}
