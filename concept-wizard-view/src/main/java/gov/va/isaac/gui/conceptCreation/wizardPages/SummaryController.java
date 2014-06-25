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
package gov.va.isaac.gui.conceptCreation.wizardPages;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptCreation.PanelControllers;
import gov.va.isaac.gui.conceptCreation.ScreensController;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
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
	
	@FXML private TextField conceptFSN;
	@FXML private TextField conceptPT;
	@FXML private TextField conceptPrimDef;
	@FXML private VBox parentVBox;
	
	@FXML private GridPane synonymGridPane;
	@FXML private Label noSynsLabel;
	@FXML private VBox termVBox;
	@FXML private VBox acceptVBox;
	@FXML private VBox caseVBox;
	@FXML private VBox langVBox;
	
	@FXML private GridPane relationshipGridPane;
	@FXML private Label noRelsLabel;
	@FXML private VBox relationshipVBox;
	@FXML private VBox relTypeVBox;
	@FXML private VBox targetVBox;
	@FXML private VBox qualRoleVBox;
	@FXML private VBox groupVBox;

	@FXML private BorderPane summaryPane;
	@FXML private Button cancelButton;
	@FXML private Button startOverButton;
	@FXML private Button commitButton;
	@FXML private Button backButton;

	static ViewCoordinate vc = null;
	static ScreensController processController;

	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryController.class);

	@Override
	public void initialize() {
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
				processController.setScreen(ScreensController.COMPONENTS_SCREEN);
			}
		});
	}
	
	private void setupConcept() {
		conceptFSN.setText(processController.getWizard().getConceptFSN());
		conceptPT.setText(processController.getWizard().getConceptPT());
		conceptPrimDef.setText(processController.getWizard().getConceptPrimDef());

		addAllParents(processController.getWizard().getParents());
	}

	private void setupSynonyms() {
		int synCount = processController.getWizard().getSynonymsCreated();
		
		if (synCount == 0) {
			noSynsLabel.setVisible(true);
			synonymGridPane.setVisible(false);
		} else {
			noSynsLabel.setVisible(false);
			synonymGridPane.setVisible(true);
			for (int i = 0; i < synCount; i++) {
				Label term = new Label(processController.getWizard().getTerm(i));
				Label accept = new Label(processController.getWizard().getTypeString(i));
				Label caseSens = new Label(processController.getWizard().getCaseSensitivity(i));
				Label lang = new Label(processController.getWizard().getLanguage(i));
	
				termVBox.getChildren().add(term);
				acceptVBox.getChildren().add(accept);
				caseVBox.getChildren().add(caseSens);
				langVBox.getChildren().add(lang);
			}
		}
	}
	
	private void setupRelationships() {
		int relCount = processController.getWizard().getRelationshipsCreated();
		
		if (relCount == 0) {
			noRelsLabel.setVisible(true);
			relationshipGridPane.setVisible(false);
		} else {
			noRelsLabel.setVisible(false);
			relationshipGridPane.setVisible(true);
			
			for (int i = 0; i < relCount; i++) {
				Label relType = new Label(processController.getWizard().getRelType(i));
				Label target = new Label(processController.getWizard().getTarget(i));
				Label qualRole = new Label(processController.getWizard().getQualRole(i));
				Label group = new Label(processController.getWizard().getGroup(i));
	
				relTypeVBox.getChildren().add(relType);
				targetVBox.getChildren().add(target);
				qualRoleVBox.getChildren().add(qualRole);
				groupVBox.getChildren().add(group);
			}
		}
	}

	private void addAllParents(List<ConceptVersionBI> parents) {
		try {
			for (ConceptVersionBI p : parents) {
				Label tf = new Label(p.getPreferredDescription().getText());
				parentVBox.getChildren().add(tf);
			}
				
		} catch (IOException | ContradictionException e) {
			LOGGER.error("Could not find preferred description of one or more parents", e);
		}
	}

	@Override
	public void finishInit(ScreensController screenParent){
		processController = screenParent;

		setupConcept();
		setupSynonyms();
		setupRelationships();
	}

	@Override
	public void processValues() {
		try {
			ConceptChronicleBI newCon = processController.getWizard().createNewConcept();
			
			for (int i = 0; i < processController.getWizard().getSynonymsCreated(); i++) {
					processController.getWizard().createNewDescription(newCon, i);
			}

			for (int i = 0; i < processController.getWizard().getRelationshipsCreated(); i++) {
				processController.getWizard().createNewRelationship(newCon, i);
			}
			WBUtility.addUncommitted(newCon.getNid());
			WBUtility.commit(newCon.getNid());
		} catch (IOException | InvalidCAB | ContradictionException e) {
			LOGGER.error("Unable to create and/or commit new concept", e);
			AppContext.getCommonDialogs().showErrorDialog("Error Creating Concept", "Unexpected error creating the Concept", e.getMessage(), summaryPane.getScene().getWindow());
		}
	}
}