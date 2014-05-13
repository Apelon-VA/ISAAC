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
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.refexViews.refexCreation.PanelControllers;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link DefinitionController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class DefinitionController implements PanelControllers {
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private AnchorPane refsetCreationPane;
	@FXML private ToggleGroup refexType;
	@FXML private RadioButton refexTypeRefset;
	@FXML private RadioButton refexAnnotationType;
	@FXML private ToggleGroup mutable;
	@FXML private RadioButton refexNotMutable;
	@FXML private RadioButton refexIsMutable;
	@FXML private TextField refexName;
	@FXML private TextField extensionCount;
	@FXML private TextArea refexDescription;
	@FXML private Button continueCreation;
	@FXML private Button cancelCreation;
    @FXML private HBox parentConceptHBox;

    static ViewCoordinate vc = null;
	ScreensController processController;
	private ConceptNode parentConcept = null;
	
	private static final Logger logger = LoggerFactory.getLogger(DefinitionController.class);

	@Override
	public void initialize() {
		vc = WBUtility.getViewCoordinate();
		extensionCount.setText("0");
		
		try {
			ConceptVersionBI refsetIdentity = WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_IDENTITY.getNid());
			parentConcept = new ConceptNode(refsetIdentity, true);
		} catch (IOException e1) {
			parentConcept = new ConceptNode(null, true);
		}
		parentConceptHBox.getChildren().add(parentConcept.getNode());
			


		cancelCreation.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					((Stage)refsetCreationPane.getScene().getWindow()).close();
				}});
		
		continueCreation.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					if (verifyValuesExist()) {
						processValues();
						if (Integer.valueOf(extensionCount.getText().trim()) == 0) {
							processController.loadSummaryScreen();
							processController.setScreen(ScreensController.SUMMARY_SCREEN);
						} else {
							processController.setScreen(ScreensController.COLUMN_SCREEN);
						}
					}
				}

			});

	}

	@Override
	public void finishInit(ScreensController screenParent){
		processController = screenParent;
	}

	@Override
	public boolean verifyValuesExist() {
		String errorMsg = null;
		
		if (refexName.getText().trim().isEmpty() || 
			refexDescription.getText().trim().isEmpty() || 
			extensionCount.getText().trim().isEmpty() ||
			parentConcept.getConcept() == null) {
			errorMsg = "Must fill out all fields";
		} else {
			boolean isAncestor = true;
			try {
				isAncestor = parentConcept.getConcept().isKindOf(WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_IDENTITY.getNid()));
			} catch (IOException | ContradictionException e) {
				logger.error("Unable to verify if concept is ancestor of Dynamic Refex Concept", e);
				errorMsg = "Cannot identify parent Concept";
			}
			
			if (errorMsg == null) {
				if (!isAncestor) {
					errorMsg = "Parent concept must be descendent of Refex Identity";
				} else {
					try {
						Integer val = Integer.valueOf(extensionCount.getText().trim());
						if (val < 0) {
							errorMsg = "Number of extension fields must be either '0' or a positive integer";
						}
					} catch (Exception e) {
							errorMsg = "Number of extension fields must be either '0' or a positive integer";
					}
				}		
			}
		}
		
		if (errorMsg == null) {
			return true;
		} else {
			AppContext.getCommonDialogs().showInformationDialog("Bad or Missing Content", errorMsg);
			return false;
		}
	}

	@Override
	public void processValues() {
		int count = Integer.valueOf(extensionCount.getText().trim());
	
		processController.getWizard().setNewRefsetConceptVals(refexName.getText().trim(), refexDescription.getText().trim(), parentConcept.getConcept(),
												  count, refexAnnotationType.isSelected(), 
												  refexIsMutable.isSelected()); 
	}
}

