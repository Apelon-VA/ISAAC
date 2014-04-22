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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

/**
 * 
 * {@link NewColumnDialogController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class NewColumnDialogController implements Initializable
{
	@FXML//  fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML 
	private Button commitButton;
	@FXML
	private TextArea newColDesc;
	
	private ConceptChronicleBI newColumnConcept;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{

		commitButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				createNewColumnConcept(newColDesc);
			}

			private void createNewColumnConcept(TextArea newColDesc) {
				try {
					//TODO missing method
					System.out.println("fix me");
//					newColumnConcept = WBUtility.createNewConcept(RefexDynamic.REFEX_DYNAMIC_COLUMNS.getLenient(), 
//											   newColDesc.getText(), newColDesc.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public ConceptChronicleBI getNewColumnConcept() {
		return newColumnConcept;
	}

}
