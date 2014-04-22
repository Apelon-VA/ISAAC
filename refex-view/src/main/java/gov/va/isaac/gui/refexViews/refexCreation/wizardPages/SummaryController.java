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
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

/**
 * 
 * {@link SummaryController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class SummaryController implements PanelControllers {

	@FXML private AnchorPane summaryPane;
	@FXML private VBox mainVBox;

	@FXML private Label actualRefexName;
	@FXML private Label actualRefexDescription;
	@FXML private Label actualParentConcept;
	@FXML private Label actualReadOnly;
	@FXML private Label actualRefexType;
	@FXML private Label actualRefCompDesc;
	@FXML private Label actualRefCompType;

	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private Button commitButton;

	
	static ViewCoordinate vc = null;
	ScreensController processController;
	
	@Override
	public void initialize() {
		assert mainVBox != null : "fx:id=\"mainVBox\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefexName != null : "fx:id=\"actualRefexName\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefexDescription != null : "fx:id=\"actualRefexDescription\" was not injected: check your FXML file 'Untitled'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'Untitled'.";
		assert startOverButton != null : "fx:id=\"startOverButton\" was not injected: check your FXML file 'Untitled'.";
		assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefCompType != null : "fx:id=\"actualRefCompType\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefexType != null : "fx:id=\"actualRefexType\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefCompDesc != null : "fx:id=\"actualRefCompDesc\" was not injected: check your FXML file 'Untitled'.";
		assert summaryPane != null : "fx:id=\"summaryPane\" was not injected: check your FXML file 'Untitled'.";
		assert actualParentConcept != null : "fx:id=\"actualParentConcept\" was not injected: check your FXML file 'Untitled'.";
		assert actualReadOnly != null : "fx:id=\"actualReadOnly\" was not injected: check your FXML file 'Untitled'.";
	}


	private void setupColumnContent() {
		VBox allColumnsContent = new VBox();
		allColumnsContent.setSpacing(10);
		
		for (int i = 0; i < processController.getExtendedFieldsCount(); i++) {
			VBox colBox = createColumn(i);
			allColumnsContent.getChildren().add(colBox);
		}
		
		mainVBox.getChildren().add(allColumnsContent);
	}


	private VBox createColumn(int column) {
		String colDesc = processController.getColumnDescription(column);
		String colType = processController.getColumnType(column);
		
		if (column == 0) {
			actualRefCompDesc.setText(colDesc);
			actualRefCompType.setText(colType);
		} else {
			VBox columnContent = new VBox();
			columnContent.setAlignment(Pos.TOP_CENTER);
			String colDefaultValue = processController.getColumnDefaultValue(column);
			String colIsMandatory = processController.getColumnIsMandatory(column);
			
			// Set Column header
			Label columnHeader = new Label("Column #" + column + " Description");
			Font headerFont = new Font("System Bold", 18);
			columnHeader.setFont(headerFont);
			columnHeader.setAlignment(Pos.CENTER);
			columnContent.getChildren().add(columnHeader);
			
			HBox attrValBox = new HBox();
			attrValBox.setAlignment(Pos.CENTER);
			
			// Create Attribute/Value VBoxes
			VBox columnAttributes = new VBox();
			VBox columnValues = new VBox();
			columnAttributes.setSpacing(5);
			columnValues.setSpacing(5);
			columnAttributes.setAlignment(Pos.CENTER_LEFT);
			columnValues.setAlignment(Pos.CENTER_LEFT);
			columnAttributes.setPadding(new Insets(0, 0, 0, 5));

			// Set Attribute Labels
			Label colDescAttrLabel = new Label("Column #" + column + " Description:");
			Label colTypeAttrLabel = new Label("Column #" + column + " Type:");
			Label colDefaultValueAttrLabel = new Label("Column #" + column + " Default Value:");
			Label colIsMandatoryAttrLabel = new Label("Column #" + column + " Is Mandatory:");

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
			columnAttributes.getChildren().add(colDescAttrLabel);
			columnAttributes.getChildren().add(colTypeAttrLabel);
			columnAttributes.getChildren().add(colDefaultValueAttrLabel);
			columnAttributes.getChildren().add(colIsMandatoryAttrLabel);

			// Add Value Labels to Value VBox
			columnValues.getChildren().add(colDescLabel);
			columnValues.getChildren().add(colTypeLabel);
			columnValues.getChildren().add(colDefaultValueLabel);
			columnValues.getChildren().add(colIsMandatoryLabel);

			attrValBox.getChildren().add(columnAttributes);
			attrValBox.getChildren().add(columnValues);
			columnContent.getChildren().add(attrValBox);
		}
		
		return null;
	}


	private void setupRefexContent() {
		actualRefexName.setText(processController.getRefexName());
		actualRefexDescription.setText(processController.getRefexDescription());
		actualParentConcept.setText(processController.getParentConceptFsn());
		
		if (processController.isReadOnlyRefex()) {
			actualReadOnly.setText("Read-Only Refex");
		} else {
			actualReadOnly.setText("Mutable Refex");
		}
		
		if (processController.isAnnotated()) {
			actualRefexType.setText("Annotated");
		} else {
			actualRefexType.setText("Refset");
		}
	}


	@Override
	public void finishInit(ScreensController screenParent){
		processController = screenParent;

		
		vc = WBUtility.getViewCoordinate();

		setupRefexContent();
		setupColumnContent();

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
				processController.setScreen(ScreensController.DEFINITION_SCREEN);
			}
		});
	}
		
	@Override
	public void processValues() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean verifyValuesExist() {
		return true;
	}
}