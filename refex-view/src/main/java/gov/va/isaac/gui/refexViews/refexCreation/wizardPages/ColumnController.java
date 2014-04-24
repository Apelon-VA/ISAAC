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
import gov.va.isaac.gui.refexViews.refexCreation.PanelControllers;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;

/**
 * 
 * {@link ColumnController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class ColumnController implements PanelControllers {

	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private Button nextButton;
	@FXML private Button cancelButton;
	@FXML private TextField defaultValue;
	@FXML private ChoiceBox<Choice> typeOption = new ChoiceBox<Choice>();
	@FXML private Button backButton;
	@FXML private AnchorPane columnDefinitionPane;
	@FXML private Label columnTitle;
	@FXML private ChoiceBox<Choice> columnConSelector = new ChoiceBox<Choice>();
	@FXML private Button newColConceptButton;
	@FXML private CheckBox isMandatory;

	
	private static int currentCol = 0;

	static ViewCoordinate vc = null;
	ScreensController processController;

	@Override
	public void initialize() {		
		vc = WBUtility.getViewCoordinate();

		setupTypeConcepts();
		setupColumnConcepts();

		setupRefComp();

		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage) columnDefinitionPane.getScene().getWindow()).close();
			}
		});

		nextButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (verifyValuesExist()) {
					processValues();

					resetValues();
					if (++currentCol < processController.getWizard().getExtendedFieldsCount()) {
						setupColumnDef();
					} else {
						resetProcess();
						processController.loadSummaryScreen();
						processController.setScreen(ScreensController.SUMMARY_SCREEN);


					}
				}
			}
		});

		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				resetValues();
				resetProcess();
				processController.setScreen(ScreensController.DEFINITION_SCREEN);
			}
		});
		
		newColConceptButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				getNewColumnConcept();
			}
		});
	}

	private void setupTypeConcepts() {

		RefexDynamicDataType[] columnTypes = RefexDynamicDataType.values();
		typeOption.getItems().add(new Choice("No selection"));
		
		for (RefexDynamicDataType type : columnTypes) {
			if (type == RefexDynamicDataType.UNKNOWN)
			{
				continue;
			}
			typeOption.getItems().add(new Choice(type));
		}

		typeOption.getSelectionModel().selectFirst();
	}

	private void getNewColumnConcept() {
		try {
			NewColumnDialog dialog = new NewColumnDialog(processController.getScene().getWindow());
			dialog.showAndWait();

			ConceptChronicleBI newCon = dialog.getNewColumnConcept();
			
			if (newCon != null) {
				columnConSelector.getItems().add(new Choice(newCon));
				columnConSelector.getSelectionModel().select(columnConSelector.getItems().size() - 1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupColumnConcepts() {
		try {
			// TODO: REmove once RefexDynamic concepts added to official DB
			try {
				ConceptVersionBI colCon = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_COLUMNS.getNid());
			} catch (Exception e) {
				try {
					ConceptVersionBI refIdent = WBUtility.getConceptVersion(RefexDynamic.REFEX_IDENTITY.getNid());
					ConceptChronicleBI con = WBUtility.createNewConcept(refIdent, RefexDynamic.REFEX_DYNAMIC_COLUMNS.getDescription(), RefexDynamic.REFEX_DYNAMIC_COLUMNS.getDescription());
					
					System.out.println("UUID OF NEW CON: " + con.getPrimordialUuid());
				} catch (Exception e1) {
					e.printStackTrace();
				}
			}
				
			ConceptVersionBI colCon = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_COLUMNS.getNid());
			ArrayList<ConceptVersionBI> colCons = WBUtility.getAllChildrenOfConcept(colCon);

			columnConSelector.getItems().add(new Choice("No selection"));
			
			for (ConceptVersionBI con : colCons) {
				columnConSelector.getItems().add(new Choice(con));
			}

			columnConSelector.getSelectionModel().selectFirst();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void finishInit(ScreensController screenParent) {
		processController = screenParent;
	}

	@Override
	public void processValues() {
		ConceptVersionBI colCon = WBUtility.getConceptVersion(((Choice)columnConSelector.getSelectionModel().getSelectedItem()).getId());
 		RefexDynamicDataType type = null;
 		
 		if (currentCol > 0) { 
 			type = RefexDynamicDataType.getFromToken(((Choice)typeOption.getSelectionModel().getSelectedItem()).getId());
 		}
 		
		processController.getWizard().setReferencedComponentVals(colCon, type, defaultValue.getText().trim(), isMandatory.isSelected());
	}

	@Override
	public boolean verifyValuesExist() {
		String errorMsg = null;
		if (columnConSelector.getSelectionModel().getSelectedIndex() == 0) {
			errorMsg = "No concept selected";
		} else if (currentCol > 0 && typeOption.getSelectionModel().getSelectedIndex() == 0) {
			errorMsg = "No type selected";
		} else {
			if (typeOption.getValue().equals("Float")) {

				try {
					Float.valueOf(defaultValue.getText().trim());
				} catch (Exception e) {
					errorMsg = "Number of extension fields must be either '0' or a positive integer";
				}
			}
		}

		if (errorMsg == null) {
			return true;
		} else {
			AppContext.getCommonDialogs().showInformationDialog(
					"Bad or Missing Content", errorMsg);
			return false;
		}
	}

	private void resetValues() {
		columnConSelector.getSelectionModel().selectFirst();
		defaultValue.clear();
	}
	
	private void resetProcess() {
		setupRefComp();
	}

	private void setupRefComp() {
		currentCol = 0;
		typeOption.setDisable(true);
		defaultValue.setDisable(true);
		columnTitle.setText("Referenced Component Definition");
		isMandatory.setSelected(true);
		isMandatory.setDisable(true);
	}

	private void setupColumnDef() {
		typeOption.setDisable(false);
		defaultValue.setDisable(false);
		columnTitle.setText("Column #" + currentCol + " Definition");
		isMandatory.setSelected(false);
		isMandatory.setDisable(false);
	}
}

class Choice {
	Integer id;
	String displayString;

	public Choice(ConceptVersionBI con) {
		this.id = con.getNid();
		try {
			this.displayString = con.getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Choice(String s) {
		this.id = null;
		this.displayString = s;
	}

	public Choice(ConceptChronicleBI con) {
		this.id = con.getNid();
		try {
			this.displayString = con.getVersion(WBUtility.getViewCoordinate()).getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Choice(RefexDynamicDataType type) {
		this.id = type.getTypeToken();
		this.displayString = type.getDisplayName();
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return displayString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Choice choice = (Choice) o;
		return displayString != null
				&& displayString.equals(choice.displayString) || id != null
				&& id.equals(choice.id);
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result
				+ (displayString != null ? displayString.hashCode() : 0);
		return result;
	}
}

