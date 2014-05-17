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

import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.refexViews.refexCreation.PanelControllers;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * 
 * {@link ColumnController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ColumnController implements PanelControllers {
	@FXML private ResourceBundle resources;
	@FXML private Button nextButton;
	@FXML private Button cancelButton;
	@FXML private TextField defaultValue;
	@FXML private ChoiceBox<Choice> typeOption;
	@FXML private Button backButton;
	@FXML private BorderPane columnDefinitionPane;
	@FXML private Label columnTitle;
	@FXML private HBox columnNameHolder;
	@FXML private TextArea columnDescription;
	@FXML private Button newColNameButton;
	@FXML private CheckBox isMandatory;
	@FXML private GridPane gridPane;


	static ViewCoordinate vc = null;
	private static int currentCol = 0;
	ScreensController processController;
	private Object defaultValueObject = null;
	private ConceptNode columnNameSelection;
	private BooleanBinding allValid_;
	private SimpleStringProperty defaultValueInvalidReason_ = new SimpleStringProperty("");

	private ObservableList<SimpleDisplayConcept> columnNameChoices = new ObservableListWrapper<>(new ArrayList<SimpleDisplayConcept>());
	private Function<ConceptVersionBI, String> colNameReader_ = (conceptVersion) -> 
	{
		//other fields don't matter, just using this to read back the description.
		RefexDynamicColumnInfo rdc = new RefexDynamicColumnInfo(-1, conceptVersion.getPrimordialUuid(), null, null);
		return rdc.getColumnName();
	};
	
	
	private static final Logger logger = LoggerFactory.getLogger(ColumnController.class);

	@Override
	public void initialize() {		
		assert nextButton != null : "fx:id=\"nextButton\" was not injected: check your FXML file 'column.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'column.fxml'.";
		assert newColNameButton != null : "fx:id=\"newColNameButton\" was not injected: check your FXML file 'column.fxml'.";
		assert defaultValue != null : "fx:id=\"defaultValue\" was not injected: check your FXML file 'column.fxml'.";
		assert typeOption != null : "fx:id=\"typeOption\" was not injected: check your FXML file 'column.fxml'.";
		assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'column.fxml'.";
		assert columnNameHolder != null : "fx:id=\"columnNameHolder\" was not injected: check your FXML file 'column.fxml'.";
		assert columnTitle != null : "fx:id=\"columnTitle\" was not injected: check your FXML file 'column.fxml'.";
		assert isMandatory != null : "fx:id=\"isMandatory\" was not injected: check your FXML file 'column.fxml'.";
		assert columnDescription != null : "fx:id=\"columnDescription\" was not injected: check your FXML file 'column.fxml'.";
		assert columnDefinitionPane != null : "fx:id=\"columnDefinitionPane\" was not injected: check your FXML file 'column.fxml'.";
		assert gridPane != null : "fx:id=\"gridPane\" was not injected: check your FXML file 'column.fxml'.";

		vc = WBUtility.getViewCoordinate();

		columnDescription.setEditable(false);
		
		columnNameSelection = new ConceptNode(null, true, columnNameChoices, colNameReader_);
		
		columnNameHolder.getChildren().add(columnNameSelection.getNode());
		HBox.setHgrow(columnNameSelection.getNode(), Priority.ALWAYS);

		initializeTypeConcepts();
		initializeColumnConcepts();
		
		allValid_ = new BooleanBinding()
		{
			{
				bind(columnNameSelection.getConceptProperty(), defaultValueInvalidReason_);
			}
			@Override
			protected boolean computeValue()
			{
				return columnNameSelection.isValid().get() && defaultValueInvalidReason_.get().length() == 0;
			}
		};
		
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage) columnDefinitionPane.getScene().getWindow()).close();
			}
		});
		
		nextButton.disableProperty().bind(allValid_.not());

		nextButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processValues();
				++currentCol;
				
				if (currentCol < processController.getWizard().getExtendedFieldsCount()) {
					processController.loadColumnScreen(currentCol);
					processController.setScreen(ScreensController.COLUMN_SCREEN);
				} else {
					processController.loadSummaryScreen();
					processController.setScreen(ScreensController.SUMMARY_SCREEN);
				}
			}
		});

		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (currentCol > 0) {
					--currentCol;
					processController.loadColumnScreen(currentCol);
					processController.setScreen(ScreensController.COLUMN_SCREEN);
				} else {
					processController.setScreen(ScreensController.DEFINITION_SCREEN);
				}
			}
		});
		
		newColNameButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				// Must Define Name & Desc
				createNewColumnConcept();
			}
		});
		
		columnNameSelection.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>()
		{
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> observable, ConceptVersionBI oldValue, ConceptVersionBI newValue)
			{
				if (newValue != null)
				{
					//other fields don't matter, just using this to read back the description.
					RefexDynamicColumnInfo rdc = new RefexDynamicColumnInfo(-1, newValue.getPrimordialUuid(), null, null);
					columnDescription.setText(rdc.getColumnDescription());
				}
				else
				{
					columnDescription.setText("");
				}
			}
		});
		
		defaultValue.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				defaultValueInvalidReason_.set(verifyDefaultValue());
			}
		});
		
		typeOption.valueProperty().addListener(new ChangeListener<Choice>()
		{
			@Override
			public void changed(ObservableValue<? extends Choice> observable, Choice oldValue, Choice newValue)
			{
				defaultValueInvalidReason_.set(verifyDefaultValue());
				if (newValue.getEnumToken() == RefexDynamicDataType.POLYMORPHIC.getTypeToken())
				{
					defaultValue.setText("");
					defaultValue.setDisable(true);
				}
				else
				{
					defaultValue.setDisable(false);
				}
			}
		});
		
		StackPane sp = new StackPane();
		ErrorMarkerUtils.swapComponents(defaultValue, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(defaultValue, sp, defaultValueInvalidReason_);
	}
	
	private void initializeTypeConcepts() {

		RefexDynamicDataType[] columnTypes = RefexDynamicDataType.values();
		
		for (RefexDynamicDataType type : columnTypes) {
			if (type == RefexDynamicDataType.UNKNOWN)
			{
				continue;
			}
			typeOption.getItems().add(new Choice(type));
		}
		typeOption.getSelectionModel().selectFirst();
	}

	private void createNewColumnConcept() {
		try {
			NewColumnDialog dialog = new NewColumnDialog(processController.getScene().getWindow());
			dialog.showAndWait();

			ConceptChronicleBI newCon = dialog.getNewColumnConcept();
			
			if (newCon != null) {
				
				columnNameChoices.add(new SimpleDisplayConcept(newCon.getVersion(vc), colNameReader_));
				columnNameSelection.set(newCon.getVersion(vc));
			}
		} catch (IOException | ContradictionException e) {
			logger.error("Unexpected error creating new column concept", e);
		}
	}

	private void initializeColumnConcepts() {
		try {
			ConceptVersionBI colCon = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_COLUMNS.getNid());
			ArrayList<ConceptVersionBI> colCons = WBUtility.getAllChildrenOfConcept(colCon, false);

			for (ConceptVersionBI col : colCons) {
				columnNameChoices.add(new SimpleDisplayConcept(col, colNameReader_));
			}
			columnNameSelection.set(columnNameChoices.get(0));
		} catch (Exception e1) {
			logger.error("Unable to access column concepts", e1);
		}
	}

	@Override
	public void finishInit(ScreensController screenParent) {
		processController = screenParent;
		setupColumnDef();

		if (processController.getWizard().previouslyFilledOut(currentCol)) {
			updatePreviousContent();
		}
	}
	
	private void updatePreviousContent() {
		RefexDynamicDataType type = processController.getWizard().getColumnTypeToken(currentCol);
		ConceptVersionBI name = processController.getWizard().getColumnName(currentCol);
		Object defVal = processController.getWizard().getColumnDefaultValue(currentCol);
		boolean isMand = processController.getWizard().isColumnMandatory(currentCol);

		//TODO this is currently triggering an infinite loop in Dans ConceptNode code... Dan needs to finish debugging this... 
		//The Platform.runLater should _NOT_ be necessary, but it seems to prevent the infinite loop for now.
		Platform.runLater(() -> {columnNameSelection.set(name);});
		typeOption.getSelectionModel().select(new Choice(type));
		defaultValue.setText(defVal == null ? "" : defVal.toString());
		isMandatory.setSelected(isMand);
	}

	@Override
	public void processValues() {
		
		ConceptVersionBI colCon = columnNameSelection.getConcept();
		RefexDynamicDataType type = RefexDynamicDataType.getFromToken(typeOption.getSelectionModel().getSelectedItem().getEnumToken());
		processController.getWizard().setColumnVals(currentCol, colCon, type, defaultValueObject, isMandatory.isSelected());
	}

	private String verifyDefaultValue() {
		RefexDynamicDataType dataType = RefexDynamicDataType.getFromToken(typeOption.getSelectionModel().getSelectedItem().getEnumToken());
		String defVal = defaultValue.getText().trim();
		
		if (defVal.length() == 0)
		{
			return "";
		}

		if (dataType == RefexDynamicDataType.BOOLEAN) {
			if (!defVal.equalsIgnoreCase("true") && !defVal.equalsIgnoreCase("false")) {
					return "Default Value is not a valid BOOLAN as specified by Column Type.  It must be True or False (case insensitive)";
				} 
			else {
				//TODO this should come from a boolean picker, not a text field.
				defaultValueObject = new Boolean(defVal);
			}
		} else if (dataType == RefexDynamicDataType.DOUBLE) {
			try {
				defaultValueObject = Double.valueOf(defVal);
			} catch (Exception e) {
				return "Default Value is not a valid DOUBLE as specified by Column Type";
			}
		} else if (dataType == RefexDynamicDataType.FLOAT) {
			try {
				defaultValueObject = Float.valueOf(defVal);
			} catch (Exception e) {
				return "Default Value is not a valid FLOAT as specified by Column Type";
			}
		} else if (dataType == RefexDynamicDataType.INTEGER) {
			try {
				defaultValueObject = Integer.valueOf(defVal);
			} catch (Exception e) {
				return "Default Value is not a valid INTEGER as specified by Column Type";
			}
		} else if (dataType == RefexDynamicDataType.LONG) {
			try {
				defaultValueObject = Long.valueOf(defVal);
			} catch (Exception e) {
				return "Default Value is not a valid LONG as specified by Column Type";
			}
		} else if (dataType == RefexDynamicDataType.NID) {
			//TODO this needs to come from a conceptNode field.
			int nid = Integer.MAX_VALUE;
			try {
				nid = Integer.valueOf(defVal);
			} catch (Exception e) {
				return "Default Value is not a valid NID as specified by Column Type.  It must be an INTEGER";
			}

			if (nid >= 0) {
				return "Default Value is not a valid NID.  It must be less than zero";
			} else {
				ConceptVersionBI comp = WBUtility.getConceptVersion(nid);
				if (comp == null) {
					return "Default Value is not a valid NID.  The value does not refer to a component in the database";
				} else {
					defaultValueObject = nid;
				}
			}
		} else if (dataType == RefexDynamicDataType.STRING) {
			try {
				defaultValueObject = String.valueOf(defVal);
			} catch (Exception e) {
				return "Default Value is not a valid STRING as specified by Column Type";
			}
		} else if (dataType == RefexDynamicDataType.UUID) {
			if (!Utility.isUUID(defVal))
			{
				return "Default Value is not a valid UUID as specified by Column Type.";
			}
			else
			{
				defaultValueObject = UUID.fromString(defVal);
			}
		} else if (dataType == RefexDynamicDataType.BYTEARRAY) {
			// TODO  rebuild this data field - text field isn't appropriate for all..  this needs to come from a file chooser.
		} else if (dataType == RefexDynamicDataType.POLYMORPHIC) {
			// not applicable
			defaultValueObject = null;
		} else if (dataType == RefexDynamicDataType.UNKNOWN) {
			logger.error("Invalid case");
		}
		return "";
	}

	private void setupColumnDef() {
		columnTitle.setText("Column #" + (currentCol + 1) + " Definition");
	}

	public void setColumnNumber(int colNum) {
		currentCol = colNum;
	}
}

class Choice {
	Integer RefexDynamicDataType;
	String displayString;
	private int enumToken = Integer.MAX_VALUE;

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