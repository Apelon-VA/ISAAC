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

import java.beans.PropertyVetoException;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * {@link SummaryController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SummaryController implements PanelControllers {
	@FXML private TextField actualRefexName;
	@FXML private TextArea actualRefexDescription;
	@FXML private TextField actualParentConcept;
	@FXML private TextField actualRefexType;
	@FXML private BorderPane summaryPane;
	@FXML private GridPane columnGridPane;
	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private Button commitButton;
	@FXML private Button backButton;

	static ViewCoordinate vc = null;
	static ScreensController processController;

	private static final Logger logger = LoggerFactory.getLogger(SummaryController.class);

	@Override
	public void initialize() {
		assert actualRefexDescription != null : "fx:id=\"actualRefexDescription\" was not injected: check your FXML file 'summary.fxml'.";
		assert actualRefexName != null : "fx:id=\"actualRefexName\" was not injected: check your FXML file 'summary.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert startOverButton != null : "fx:id=\"startOverButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert backButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'summary.fxml'.";
		assert actualRefexType != null : "fx:id=\"actualRefexType\" was not injected: check your FXML file 'summary.fxml'.";
		assert summaryPane != null : "fx:id=\"summaryPane\" was not injected: check your FXML file 'summary.fxml'.";
		assert actualParentConcept != null : "fx:id=\"actualParentConcept\" was not injected: check your FXML file 'summary.fxml'.";
		assert columnGridPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'summary.fxml'.";

	}

	private void setupColumnContent() {
		VBox columns = new VBox(15);
		columns.setAlignment(Pos.TOP_CENTER);
		
		columnGridPane.getChildren().clear();
		int row = 0;
		for (int i = 0; i < processController.getWizard().getExtendedFieldsCount(); i++) {
			ConceptVersionBI col = processController.getWizard().getColumnName(i);
			String colType = processController.getWizard().getColumnType(i);
			Object colDefaultValue = processController.getWizard().getColumnDefaultValue(i);
			String colIsMandatory = processController.getWizard().getColumnIsMandatory(i);
			boolean hasDefaultValue = colDefaultValue != null && colDefaultValue.toString().length() > 0;

			if (row > 0)
			{
				Separator sep = new Separator(Orientation.HORIZONTAL);
				sep.setPadding(new Insets(10, 0, 10, 0));
				columnGridPane.add(sep, 0, row++, 3, 1);
				GridPane.setFillWidth(sep, true);
			}
			
			// Create Column header
			Label header = createColumnHeader(i);
			columnGridPane.add(header, 0, row++, 3, 1);
			GridPane.setHalignment(header, HPos.CENTER);

			RefexDynamicColumnInfo rdc = new RefexDynamicColumnInfo(-1, col.getPrimordialUuid(), null, null);
			
			//row 1
//			Label nameLabel = createLabel("Name: " + rdc.getColumnName(), true);
//			nameLabel.setFont(new Font(16));
			HBox nameBox = createColumnName(rdc.getColumnName());
			columnGridPane.add(nameBox, 0, row++, 3, 1);
			GridPane.setHalignment(nameBox, HPos.CENTER);
			
			//row 2
			Label descriptionLabel = createLabel("Description", true);
			columnGridPane.add(descriptionLabel, 0, row);
			GridPane.setHalignment(descriptionLabel, HPos.CENTER);
			
			columnGridPane.add(createLabel("Type", true), 1, row);
			columnGridPane.add(createLabel(colType, false), 2, row++);

			//row 3
			TextArea description = new TextArea(rdc.getColumnDescription());
			description.setEditable(false);
			description.setWrapText(true);
			description.setMaxHeight(90);
			columnGridPane.add(description, 0, row++, 1, 3);
			
			GridPane.setValignment(description, VPos.TOP);
			columnGridPane.add(createLabel("Mandatory", true), 1, row);
			columnGridPane.add(createLabel(colIsMandatory, false), 2, row++);
			
			//row 4
			Label l = createLabel("Default Value", true);
			columnGridPane.add(l, 1, row);
			GridPane.setValignment(l, VPos.TOP);

			if (hasDefaultValue)
			{
				l = createLabel(colDefaultValue.toString(), false);
				GridPane.setValignment(l, VPos.TOP);
				columnGridPane.add(l, 2, row++);
			} else {
				l = createLabel("<None>", false);
				GridPane.setValignment(l, VPos.TOP);
				columnGridPane.add(l, 2, row++);
			}
		}
		if (row == 0)
		{
			columnGridPane.add(createLabel("No Data Columns", true), 0, row, 3, 1);
			GridPane.setHalignment(columnGridPane.getChildren().get(0), HPos.CENTER);
		}
	}

	private Label createLabel(String val, boolean bold) {
		Label l = new Label(val);
		l.setWrapText(true);
		if (bold)
		{
			l.getStyleClass().add("boldLabel");
		}

		return l;
	}

	private Label createColumnHeader(int column) {
		Label columnHeader = new Label("Column Definition #" + (column + 1));
		Font headerFont = new Font("System Bold", 18);
		columnHeader.setFont(headerFont);
		columnHeader.setAlignment(Pos.CENTER);
		
		return columnHeader;
	}

	private HBox createColumnName(String colName) {
		HBox nameBox = new HBox(5);
		nameBox.setAlignment(Pos.CENTER);

		Label nameLabel = createLabel("Name", true);
		nameLabel.setFont(new Font(15));
		nameBox.getChildren().add(nameLabel);

		Label nameVal = new Label(colName);
		nameVal.setFont(new Font(15));
		nameBox.getChildren().add(nameVal);
		
		return nameBox;
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

		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)summaryPane.getScene().getWindow()).close();
			}
		});
	
		commitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processValues();
				((Stage)summaryPane.getScene().getWindow()).close();
			}
		});

		startOverButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processController.unloadScreen(ScreensController.SUMMARY_SCREEN);
				processController.setScreen(ScreensController.DEFINITION_SCREEN);
			}
		});
		
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processController.unloadScreen(ScreensController.SUMMARY_SCREEN);
				int fieldCount = processController.getWizard().getExtendedFieldsCount();
				if (fieldCount > 0) {
					processController.loadColumnScreen(fieldCount - 1);
					processController.setScreen(ScreensController.COLUMN_SCREEN);
				} else {
					processController.setScreen(ScreensController.DEFINITION_SCREEN);
				}
			}
		});
	}
		
	@Override
	public void processValues() {
		try {
			RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(actualRefexName.getText(),
					actualRefexName.getText(), actualRefexDescription.getText(), processController.getWizard().getColumnInfo(), 
					processController.getWizard().getParentConcept().getPrimordialUuid(), 
					processController.getWizard().isAnnotated());
		} catch (IOException | ContradictionException | InvalidCAB | PropertyVetoException e) {
			logger.error("Unable to create and/or commit refset concept and metadata", e);
			AppContext.getCommonDialogs().showErrorDialog("Error Creating Refex", "Unexpected error creating the Refex", e.getMessage(), summaryPane.getScene().getWindow());
		}
	}
}