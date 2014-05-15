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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@FXML private ChoiceBox columnNameSelector = new ChoiceBox();
	@FXML private ChoiceBox columnDescSelector = new ChoiceBox();
	@FXML private Button newColNameButton;
	@FXML private Button newColDescButton;
	@FXML private CheckBox isMandatory;

	static ViewCoordinate vc = null;
	private static int currentCol = 0;
	ScreensController processController;
	Map<String, Map<String, Integer>> shortLongNameColumnMap = new HashMap();
	
	private static final Logger logger = LoggerFactory.getLogger(ColumnController.class);

	@Override
	public void initialize() {		
		
		vc = WBUtility.getViewCoordinate();

		initializeTypeConcepts();
		initializeColumnConcepts();

		resetProcess();
		columnDescSelector.setDisable(true);
		newColDescButton.setDisable(true);

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
		
		newColNameButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				// Must Define Name & Desc
				getNewColumnConcept(null);
			}
		});
		
		newColDescButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				// Must Define new Desc associated with existing name
				if (columnNameSelector.getSelectionModel().getSelectedIndex() > 0) {
					getNewColumnConcept(columnNameSelector.getSelectionModel().getSelectedItem().toString());
				} else {
					getNewColumnConcept(null);
				}
			}
		});
		
		columnNameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				columnDescSelector.setDisable(false);
				newColDescButton.setDisable(false);
				setupDescSelector(newVal);
			}
        });
	}
	
	private void initializeTypeConcepts() {

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

	private void getNewColumnConcept(String name) {
		try {
			NewColumnDialog dialog = new NewColumnDialog(processController.getScene().getWindow(), name);
			dialog.showAndWait();

			ConceptChronicleBI newCon = dialog.getNewColumnConcept();
			
			if (newCon != null) {
				String lName = newCon.getVersion(vc).getFullySpecifiedDescription().getText();
				String sName = newCon.getVersion(vc).getPreferredDescription().getText();
				updateShortLongNameMap(newCon.getVersion(vc));
				
				columnNameSelector.getItems().add(sName);
				columnNameSelector.getSelectionModel().select(columnNameSelector.getItems().size() - 1);

				columnDescSelector.getItems().add(lName);
				columnDescSelector.getSelectionModel().select(columnDescSelector.getItems().size() - 1);
}
		} catch (IOException | ContradictionException e) {
			logger.error("Unable to access new dialog box from application", e);
		}
	}

	private void setupDescSelector(String newVal) {
		try {
			columnDescSelector.getItems().clear();
			columnDescSelector.getItems().add("No selection");
			Map<String, Integer> descMap = shortLongNameColumnMap.get(newVal);
			
			if (descMap != null) {
				for (String desc : descMap.keySet()) {
					columnDescSelector.getItems().add(desc);
				}
	
				columnDescSelector.getSelectionModel().selectFirst();
			}
		} catch (Exception e) {
			logger.error("Unable to access concepts descriptions", e);
		}
	}

	private void setupNameSelector() {
		try {
			columnNameSelector.getItems().add("No selection");
			
			for (String name : shortLongNameColumnMap.keySet()) {
				columnNameSelector.getItems().add(name);
			}

			columnNameSelector.getSelectionModel().selectFirst();
		} catch (Exception e) {
			logger.error("Unable to access concepts names", e);
		}
	}

	private void initializeColumnConcepts() {
		try {
			ConceptVersionBI colCon = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_COLUMNS.getNid());
			ArrayList<ConceptVersionBI> colCons = WBUtility.getAllChildrenOfConcept(colCon, false);

			for (ConceptVersionBI col : colCons) {
				updateShortLongNameMap(col);
			}
			
			setupNameSelector();
		} catch (Exception e1) {
			logger.error("Unable to access column concepts", e1);
		}
	}

	private void updateShortLongNameMap(ConceptVersionBI col) {
		try {
			String lName = col.getVersion(vc).getFullySpecifiedDescription().getText();
			String sName = col.getVersion(vc).getPreferredDescription().getText();
			int nid = col.getNid();
			
			if (!shortLongNameColumnMap.containsKey(sName)) {
				Map<String, Integer> nameNidMap = new HashMap();
				shortLongNameColumnMap.put(sName, nameNidMap);
			}
			
			shortLongNameColumnMap.get(sName).put(lName, nid);	
		} catch (Exception e) {
			logger.error("Unable to access column concept for processing: " + col.getPrimordialUuid(), e);
		}
	}

	public void finishInit(ScreensController screenParent) {
		processController = screenParent;
	}

	@Override
	public void processValues() {
		
		String colName = columnNameSelector.getSelectionModel().getSelectedItem().toString();
		String colDesc = columnDescSelector.getSelectionModel().getSelectedItem().toString();
		
		ConceptVersionBI colCon = WBUtility.getConceptVersion(shortLongNameColumnMap.get(colName).get(colDesc));
		RefexDynamicDataType type = RefexDynamicDataType.getFromToken(typeOption.getSelectionModel().getSelectedItem().getEnumToken());
		
		processController.getWizard().setColumnVals(colCon, type, defaultValue.getText().trim(), isMandatory.isSelected());
	}

	@Override
	public boolean verifyValuesExist() {
		String errorMsg = null;
		if (columnNameSelector.getSelectionModel().getSelectedIndex() == 0) {
			errorMsg = "No column name selected";
		} else if (columnDescSelector.getSelectionModel().getSelectedIndex() == 0) {
			errorMsg = "No column description selected";
		} else if (typeOption.getSelectionModel().getSelectedItem().getEnumToken() == Integer.MAX_VALUE) {
			errorMsg = "No column type selected";
		} else {
			if (defaultValue.getText().trim().length() > 0) {
				errorMsg = verifyDefaultValue();
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

	private String verifyDefaultValue() {
		String errorMsg = null;
		RefexDynamicDataType enumToken = RefexDynamicDataType.getFromToken(typeOption.getSelectionModel().getSelectedItem().getEnumToken());
		
		if (enumToken == RefexDynamicDataType.BOOLEAN) {
			if (!defaultValue.getText().trim().equalsIgnoreCase("true") && 
				!defaultValue.getText().trim().equalsIgnoreCase("false")) {
				errorMsg = "Default Value is not a valid BOOLAN as specified by Column Type.  It must be True or False (case insensitive)";
			}
		} else if (enumToken == RefexDynamicDataType.DOUBLE) {
			try {
				Double.valueOf(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid DOUBLE as specified by Column Type";
			}
		} else if (enumToken == RefexDynamicDataType.FLOAT) {
			try {
				Float.valueOf(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid FLOAT as specified by Column Type";
			}
		} else if (enumToken == RefexDynamicDataType.INTEGER) {
			try {
				Integer.valueOf(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid INTEGER as specified by Column Type";
			}
		} else if (enumToken == RefexDynamicDataType.LONG) {
			try {
				Long.valueOf(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid LONG as specified by Column Type";
			}
		} else if (enumToken == RefexDynamicDataType.NID) {
			int defVal = Integer.MAX_VALUE;
			try {
				defVal = Integer.valueOf(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid NID as specified by Column Type.  It must be an INTEGER";
			}
			
			if (errorMsg == null) {
				if (defVal >= 0) {
					errorMsg = "Default Value is not a valid NID.  It must be less than zero";
				} else {
					if (WBUtility.getConceptVersion(defVal) == null) {
						errorMsg = "Default Value is not a valid NID.  The value does not refer to a component in the database";
					}
				}
			}
		} else if (enumToken == RefexDynamicDataType.STRING) {
			try {
				String.valueOf(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid STRING as specified by Column Type";
			}
		} else if (enumToken == RefexDynamicDataType.UUID) {
			UUID defVal = UUID.randomUUID();
			try {
				defVal = UUID.fromString(defaultValue.getText().trim());
			} catch (Exception e) {
				errorMsg = "Default Value is not a valid UUID as specified by Column Type.";
			}
			
			if (errorMsg == null) {
				if (WBUtility.getConceptVersion(defVal) == null) {
					errorMsg = "Default Value is not a valid NID.  The value does not refer to a component in the database";
				}
			}
		} else if (enumToken == RefexDynamicDataType.BYTEARRAY) {
			// TODO			
		} else if (enumToken == RefexDynamicDataType.POLYMORPHIC) {
			// TODO
		} else if (enumToken == RefexDynamicDataType.UNKNOWN) {
			// TODO			
		} 
		
		return errorMsg;
	}

	private void resetValues() {
		typeOption.getSelectionModel().selectFirst();
		defaultValue.clear();
		isMandatory.setSelected(false);
		columnNameSelector.getSelectionModel().selectFirst();

		columnDescSelector.getSelectionModel().selectFirst();
		columnDescSelector.setDisable(true);
		newColDescButton.setDisable(true);
		columnDescSelector.getItems().clear();
		columnDescSelector.getItems().add("No selection");
	}
	
	private void resetProcess() {
		currentCol = 0;
		setupColumnDef();
	}

	private void setupColumnDef() {
		columnTitle.setText("Column #" + (currentCol + 1) + " Definition");
	}
}

class Choice {
	Integer RefexDynamicDataType;
	String displayString;
	private int enumToken = Integer.MAX_VALUE;
	
	private static final Logger logger = LoggerFactory.getLogger(Choice.class);

	public Choice(RefexDynamicDataType type) {
		this.enumToken = type.getTypeToken();
		this.displayString = type.getDisplayName();
	}

	public Choice(String s) {
		this.displayString = s;
	}

	public int getEnumToken() {
		return enumToken;
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
				&& displayString.equals(choice.displayString) 
				&& enumToken == choice.enumToken;
	}

	@Override
	public int hashCode() {
		int result = new Integer(enumToken).hashCode();
		result = 31 * result
				+ (displayString != null ? displayString.hashCode() : 0);
		return result;
	}
}

