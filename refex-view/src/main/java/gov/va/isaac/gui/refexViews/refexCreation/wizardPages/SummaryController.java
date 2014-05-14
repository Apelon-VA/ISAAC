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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
		VBox allColumnsContent = new VBox(10);
		allColumnsContent.setAlignment(Pos.TOP_CENTER);

		for (int i = 0; i < processController.getWizard().getExtendedFieldsCount(); i++) {
			VBox colBox = createColumn(i);
			allColumnsContent.getChildren().add(colBox);
		}
		
		mainVBox.getChildren().add(allColumnsContent);
	}


	private VBox createColumn(int column) {
		String colDesc = processController.getWizard().getColumnDescription(column);
		String colType = processController.getWizard().getColumnType(column);

		VBox columnContent = new VBox();
		columnContent.setAlignment(Pos.TOP_CENTER);
		String colDefaultValue = processController.getWizard().getColumnDefaultValue(column);
		String colIsMandatory = processController.getWizard().getColumnIsMandatory(column);
		
		// Set Column header
		Label columnHeader = new Label("Description");
		Font headerFont = new Font("System Bold", 18);
		columnHeader.setFont(headerFont);
		columnHeader.setAlignment(Pos.CENTER);
		columnContent.getChildren().add(columnHeader);
		
		HBox splitColumn = new HBox(50);
		splitColumn.setAlignment(Pos.CENTER);
		
		// Left V/A Pairs
		HBox attrValBoxLeft = new HBox();
		attrValBoxLeft.setAlignment(Pos.CENTER);
		attrValBoxLeft.setLayoutX(0);
		attrValBoxLeft.setPrefWidth(200);

		// Create Attribute/Value VBoxes
		VBox columnAttributesLeft = new VBox();
		VBox columnValuesLeft = new VBox();
		columnAttributesLeft.setSpacing(5);
		columnValuesLeft.setSpacing(5);
		columnAttributesLeft.setAlignment(Pos.CENTER_LEFT);
		columnValuesLeft.setAlignment(Pos.CENTER_LEFT);
		columnAttributesLeft.setPadding(new Insets(0, 0, 0, 5));

		// Right V/A Pairs
		HBox attrValBoxRight = new HBox();
		attrValBoxRight.setAlignment(Pos.CENTER);
		attrValBoxRight.setPrefWidth(200);

		// Create Attribute/Value VBoxes
		VBox columnAttributesRight = new VBox();
		VBox columnValuesRight = new VBox();
		columnAttributesRight.setSpacing(5);
		columnValuesRight.setSpacing(5);
		columnAttributesRight.setAlignment(Pos.CENTER_LEFT);
		columnValuesRight.setAlignment(Pos.CENTER_LEFT);
		columnAttributesRight.setPadding(new Insets(0, 0, 0, 5));

		// Set Attribute Labels
		Label colDescAttrLabel = new Label("Description:");
		Label colTypeAttrLabel = new Label("Type:");
		Label colDefaultValueAttrLabel = new Label("Default Value:");
		Label colIsMandatoryAttrLabel = new Label("Is Mandatory:");

		// Set Attribute Label Fonts
		colDescAttrLabel.setFont(new Font("System Bold", 12));
		colTypeAttrLabel.setFont(new Font("System Bold", 12));
		colDefaultValueAttrLabel.setFont(new Font("System Bold", 12));
		colIsMandatoryAttrLabel.setFont(new Font("System Bold", 12));
		
		// Set Value Labels
		Label colDescLabel = new Label(colDesc);
		Label colTypeLabel = new Label(colType);
		Label colDefaultValueLabel = new Label(colDefaultValue);
		Label colIsMandatoryLabel = new Label(colIsMandatory);

		// Add Attribute Labels to Attribute VBox
		columnAttributesLeft.getChildren().add(colDescAttrLabel);
		columnAttributesLeft.getChildren().add(colIsMandatoryAttrLabel);
		columnAttributesRight.getChildren().add(colTypeAttrLabel);
		columnAttributesRight.getChildren().add(colDefaultValueAttrLabel);

		// Add Value Labels to Value VBox
		columnValuesLeft.getChildren().add(colDescLabel);
		columnValuesLeft.getChildren().add(colIsMandatoryLabel);
		columnValuesRight.getChildren().add(colTypeLabel);
		columnValuesRight.getChildren().add(colDefaultValueLabel);

		attrValBoxLeft.getChildren().add(columnAttributesLeft);
		attrValBoxLeft.getChildren().add(columnValuesLeft);
		attrValBoxRight.getChildren().add(columnAttributesRight);
		attrValBoxRight.getChildren().add(columnValuesRight);

		splitColumn.getChildren().add(attrValBoxLeft);
		splitColumn.getChildren().add(attrValBoxRight);
		
		columnContent.getChildren().add(splitColumn);
		
		return columnContent;
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