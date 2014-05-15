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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Node;
import javafx.scene.layout.Region;
/**
 * 
 * {@link SummaryController2}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class SummaryController2 implements PanelControllers {
    @FXML private Label actualRefexName;
    @FXML private Label actualRefexDescription;
	@FXML private Label actualParentConcept;
	@FXML private Label actualRefexType;
	@FXML private BorderPane summaryPane;
    @FXML private BorderPane mainPane;
	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private Button commitButton;

	static ViewCoordinate vc = null;
	static ScreensController processController;

	private static final Logger logger = LoggerFactory.getLogger(SummaryController2.class);

	@Override
	public void initialize() {
		assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'Untitled'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'Untitled'.";
		assert startOverButton != null : "fx:id=\"startOverButton\" was not injected: check your FXML file 'Untitled'.";
		assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'Untitled'.";
		assert actualRefexType != null : "fx:id=\"actualRefexType\" was not injected: check your FXML file 'Untitled'.";
		assert summaryPane != null : "fx:id=\"summaryPane\" was not injected: check your FXML file 'Untitled'.";
		assert actualParentConcept != null : "fx:id=\"actualParentConcept\" was not injected: check your FXML file 'Untitled'.";
	}


	@SuppressWarnings("restriction")
	private void setupColumnContent() {
		VBox columns = new VBox(15);
		columns.setAlignment(Pos.TOP_CENTER);
		
		for (int i = 0; i < processController.getWizard().getExtendedFieldsCount(); i++) {
			String colName = processController.getWizard().getColumnName(i);
			String colDesc = processController.getWizard().getColumnDescription(i);
			String colType = processController.getWizard().getColumnType(i);
			String colDefaultValue = processController.getWizard().getColumnDefaultValue(i).toString();
			String colIsMandatory = processController.getWizard().getColumnIsMandatory(i);

			BorderPane bp = new BorderPane();

			// Create Column header
			Label colDef = createColumnHeader(i);	
			bp.setTop(colDef);

			// Create Left Values (Name/Type/Mand)
			bp.setLeft(createLeftPane(colName, colType, colIsMandatory));
			
			// Create Right Values (Description)
			bp.setCenter(createRightPane("Description", colDesc));

			if (colDefaultValue.length() > 0) {
				bp.setRight(createRightPane("Default Value", colDefaultValue));
			} else {
				// Handle Two Panes Only
			}
			
			columns.getChildren().add(bp);
		}
		
		mainPane.setBottom(columns);
	}

	private HBox createLeftPane(String colName, String colType, String colIsMand) {
		HBox columnSide = new HBox(5);
		columnSide.setAlignment(Pos.TOP_LEFT);
		columnSide.setFillHeight(true);

		List<Label> labelList = new ArrayList();
		labelList.add(createLabel("Name: "));
		labelList.add(createLabel("Type: "));
		labelList.add(createLabel("Mandatory: "));

		List<Label> valueList = new ArrayList();
		valueList.add(new Label(colName));
		valueList.add(new Label(colType));
		valueList.add(new Label(colIsMand));
		
		VBox labels = createVBox(labelList);
		VBox values = createVBox(valueList);
		values.prefWidth(Region.USE_COMPUTED_SIZE);
		values.minWidth(Region.USE_PREF_SIZE);

		columnSide.getChildren().add(labels);
		columnSide.getChildren().add(values);
		columnSide.prefWidth(Region.USE_COMPUTED_SIZE);
		columnSide.minWidth(Region.USE_PREF_SIZE);
		
		return columnSide;
	}

	private VBox createRightPane(String title, String value) {
		VBox columnSide = new VBox();
		columnSide.setAlignment(Pos.TOP_CENTER);
		columnSide.setFillWidth(true);

		Label l = createLabel(title + ":");
		Label v = new Label(value);
		v.setWrapText(true);
		v.prefWidth(Region.USE_COMPUTED_SIZE);
		v.minWidth(Region.USE_PREF_SIZE);

		columnSide.getChildren().add(l);
		columnSide.getChildren().add(v);
		
		columnSide.prefWidth(Region.USE_COMPUTED_SIZE);
		columnSide.minWidth(Region.USE_PREF_SIZE);

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
		Label columnHeader = new Label("Column Definition #" + (column + 1));
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