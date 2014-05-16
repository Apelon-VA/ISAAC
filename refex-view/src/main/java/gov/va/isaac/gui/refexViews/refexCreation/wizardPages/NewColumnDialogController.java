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

import gov.va.isaac.AppContext;
import gov.va.isaac.util.WBUtility;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link NewColumnDialogController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class NewColumnDialogController implements Initializable
{
	@FXML private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML private TextArea newColName;
	@FXML private TextArea newColDesc;
	@FXML private Button commitButton;
	@FXML private Button cancelButton;
	
	private ConceptChronicleBI newColumnConcept = null;

	private static final Logger logger = LoggerFactory.getLogger(NewColumnDialogController.class);

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{

		commitButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				createNewColumnConcept();
				((Stage) rootPane.getScene().getWindow()).close();
			}

			private void createNewColumnConcept() {
				try {
					newColumnConcept = RefexDynamicColumnInfo.createNewRefexDynamicColumnInfoConcept(newColDesc.getText().trim(), 
						newColName.getText().trim(), WBUtility.getEC(), WBUtility.getViewCoordinate());
				} catch (InvalidCAB e) {
					AppContext.getCommonDialogs().showInformationDialog("New Concept Error", "Concept Already Exists", rootPane.getScene().getWindow());
					newColumnConcept = null;
				} catch (Exception e1) {
					logger.error("Unable to create concept in database", e1);
				}
			}
		});

		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});
	}
	
	public ConceptChronicleBI getNewColumnConcept() {
		return newColumnConcept;
	}

	public void setName(String name) {
		if (name != null) {
			newColName.setText(name);
			newColName.setEditable(false);
		}
		
	}

}
