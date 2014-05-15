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

import gov.va.isaac.gui.refexViews.refexCreation.PanelControllers;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link SummaryController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class SummaryController implements PanelControllers {
    @FXML private Label actualRefexName;
    @FXML private Label actualRefexDescription;
	@FXML private Label actualParentConcept;
	@FXML private Label actualRefexType;
	@FXML private Label actualRefCompDesc;
	@FXML private AnchorPane summaryPane;
	@FXML private VBox mainVBox;
	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private Button commitButton;

	static ViewCoordinate vc = null;
	static ScreensController processController;

	private static final Logger logger = LoggerFactory.getLogger(SummaryController.class);

	@Override
	public void initialize() {
		assert mainVBox != null : "fx:id=\"mainVBox\" was not injected: check your FXML file 'Untitled'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'Untitled'.";
		assert startOverButton != null : "fx:id=\"startOverButton\" was not injected: check your FXML file 'Untitled'.";
		assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefexType != null : "fx:id=\"actualRefexType\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefCompDesc != null : "fx:id=\"actualRefCompDesc\" was not injected: check your FXML file 'Untitled'.";
		assert summaryPane != null : "fx:id=\"summaryPane\" was not injected: check your FXML file 'Untitled'.";
		assert actualParentConcept != null : "fx:id=\"actualParentConcept\" was not injected: check your FXML file 'Untitled'.";
	}


	private void setupColumnContent() {
		for (int i = 0; i < processController.getWizard().getExtendedFieldsCount(); i++) {
			// Create Column header
			Label colDef = createColumnHeader(i);		
			mainVBox.getChildren().add(colDef);

			// Create Column Definition
			HBox colBox = createColumn(i);
			mainVBox.getChildren().add(colBox);
		}
	}


	@SuppressWarnings("restriction")
	private HBox createColumn(int column) {
		String colName = processController.getWizard().getColumnName(column);
		String colDesc = processController.getWizard().getColumnDescription(column);
		String colType = processController.getWizard().getColumnType(column);
		String colDefaultValue = processController.getWizard().getColumnDefaultValue(column);
		String colIsMandatory = processController.getWizard().getColumnIsMandatory(column);

		// Create Column Definition Holder
		HBox columnContent = new HBox(20);
		columnContent.setFillHeight(true);
		columnContent.setAlignment(Pos.TOP_LEFT);
		
		// Create Left Values
		HBox leftColumnAttributes = createLeftColumnValues(colName, colDesc);
		
		// Create Right Values
		HBox rightColumnAttributes = createRightColumnValues(colType, colIsMandatory, colDefaultValue);
		
		columnContent.getChildren().add(leftColumnAttributes);
		columnContent.getChildren().add(rightColumnAttributes);
		
		return columnContent;
	}


	private HBox createRightColumnValues(String colType, String colIsMand, String colDefVal) {
		HBox columnSide = new HBox();
		columnSide.setAlignment(Pos.TOP_LEFT);
		columnSide.setFillHeight(true);

		List<Label> labelList = new ArrayList();
		labelList.add(createLabel("Type: "));
		labelList.add(createLabel("Is Mandatory: "));
		if (colDefVal.length() > 0) {
			labelList.add(createLabel("Default Value: "));
		}
		
		List<Label> valueList = new ArrayList();
		valueList.add(new Label(colType));
		valueList.add(new Label(colIsMand));
		if (colDefVal.length() > 0) {
			Label colDefValValue = new Label(colDefVal);
			colDefValValue.setWrapText(true);
			valueList.add(colDefValValue);
		}

		VBox labels = createVBox(labelList);
		VBox values = createVBox(valueList);

		columnSide.getChildren().add(labels);
		columnSide.getChildren().add(values);

		return columnSide;
	}

	private HBox createLeftColumnValues(String colName, String colDesc) {
		HBox columnSide = new HBox();
		columnSide.setAlignment(Pos.TOP_LEFT);
		columnSide.setFillHeight(true);
		columnSide.setPrefWidth(300);
		columnSide.setMinWidth(columnSide.getPrefWidth());

		List<Label> labelList = new ArrayList();
		labelList.add(createLabel("Name: "));
		labelList.add(createLabel("Description: "));
		
		List<Label> valueList = new ArrayList();
		valueList.add(new Label(colName));
		Label colDescValue = new Label(colDesc);
		colDescValue.setWrapText(true);
		valueList.add(colDescValue);

		VBox labels = createVBox(labelList);
		VBox values = createVBox(valueList);
		columnSide.getChildren().add(labels);
		columnSide.getChildren().add(values);

		return columnSide;
	}

	private VBox createVBox(List<Label> labelList) {
		VBox box = new VBox(5);
		box.setAlignment(Pos.TOP_LEFT);
		
		for (Label l : labelList) {
			box.getChildren().add(l);
		}
		
		return box;
	}

	private Label createLabel(String val) {
		Label l = new Label(val);
		l.setFont(new Font("System Bold", 12));

		return l;
	}


	private Label createColumnHeader(int column) {
		Label columnHeader = new Label("Column Defintiion #" + (column + 1));
		Font headerFont = new Font("System Bold", 18);
		columnHeader.setFont(headerFont);
		columnHeader.setAlignment(Pos.CENTER);
		
		return columnHeader;
	}


	private void setupRefexContent() {
		actualRefexName.setText(processController.getWizard().getRefexName());
		actualRefexDescription.setText(processController.getWizard().getRefexDescription());
		actualParentConcept.setText(processController.getWizard().getParentConceptFsn());
		
		if (processController.getWizard().isAnnotated()) {
			actualRefexType.setText("Annotated");
		} else {
			actualRefexType.setText("Refset");
		}
	}

	@Override
	public void finishInit(ScreensController screenParent){
		processController = screenParent;

		setupRefexContent();
		setupColumnContent();

		vc = WBUtility.getViewCoordinate();

		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)summaryPane.getScene().getWindow()).close();
			}});
	
		commitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processValues();
				((Stage)summaryPane.getScene().getWindow()).close();
//				AppContext.getCommonDialogs().showInformationDialog("Creation Summary", "Refset Successfully Created");
		}
		});

		startOverButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processController.unloadScreen(ScreensController.SUMMARY_SCREEN);
				processController.setScreen(ScreensController.DEFINITION_SCREEN);
			}
		});
	}
		
	@Override
	public void processValues() {
		// TODO this isn't finished
		try {
			RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(actualRefexName.getText(),
					actualRefexDescription.getText(), "later", processController.getWizard().getColumnInfo(), null, 
					actualRefexType.getText().equals("Annotated"), WBUtility.getEC(), WBUtility.getViewCoordinate());
		} catch (IOException | ContradictionException | InvalidCAB | PropertyVetoException e) {
			logger.error("Unable to create and/or commit refset concept and metadata", e);
		}
		
	}
	
	@Override
	public boolean verifyValuesExist() {
		return true;
	}
}