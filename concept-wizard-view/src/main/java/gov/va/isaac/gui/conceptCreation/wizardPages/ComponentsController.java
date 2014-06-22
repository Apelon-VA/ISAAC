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

import gov.va.isaac.gui.conceptCreation.PanelControllers;
import gov.va.isaac.gui.conceptCreation.ScreensController;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link ComponentsController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ComponentsController implements PanelControllers {
	@FXML private ResourceBundle resources;

	// Synonyms
	@FXML private VBox synonymVBox;
	@FXML private VBox descTypeVBox;
	@FXML private VBox caseVBox;
	@FXML private VBox addSynonymButtonVBox;
	@FXML private VBox removeSynonymButtonVBox;
	@FXML private GridPane synonymGridPane;

	// Relationships
	@FXML private VBox typeVBox;
	@FXML private VBox targetVBox;
	@FXML private VBox relationshipTypeVBox;
	@FXML private VBox groupVBox;
	@FXML private VBox addRelationshipButtonVBox;
	@FXML private VBox removeRelationshipButtonVBox;
	@FXML private GridPane relationshipGridPane;

	// Panel
	@FXML private AnchorPane componentsPane;
	@FXML private Button continueButton;
	@FXML private Button cancelButton;
	@FXML private Button backButton;
	
	private static final Logger LOG = LoggerFactory.getLogger(ComponentsController.class);
	
	static ViewCoordinate vc = null;
	ScreensController processController;

	private ArrayList<RelRow> relationships = new ArrayList<>();
	private ArrayList<TermRow> descriptions = new ArrayList<>();
	
	private UpdateableBooleanBinding allValid;

//	private int acceptableTypeNid;
//	private int synonymTypeNid;


	@Override
	public void initialize() {		
		vc = WBUtility.getViewCoordinate();
		
//		try {
//			acceptableTypeNid = SnomedMetadataRf2.ACCEPTABLE_RF2.getNid();
//			synonymTypeNid = SnomedMetadataRf2.SYNONYM_RF2.getNid();
//		} catch (Exception e) {
//			LOG.error("Unable to identify acceptable and synonym types.  Points to larger problem", e);
//		}
		
		// Buttons
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				((Stage)componentsPane.getScene().getWindow()).close();
			}});
	
		continueButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processValues();
				processController.loadSummaryScreen();
				processController.setScreen(ScreensController.SUMMARY_SCREEN);
			}
		});

		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				processController.setScreen(ScreensController.DEFINITION_SCREEN);
			}
		});
		
		
		allValid = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
			}
			
			@Override
			protected boolean computeValue()
			{
				for (RelRow rr : relationships)
				{
					if (!rr.isValid().get())
					{
						return false;
					}
				}
				if (relationships.size() == 0)
				{
					return false;
				}
				for (TermRow dr : descriptions)
				{
					if (!dr.isValid().get())
					{
						return false;
					}
				}
				if (descriptions.size() == 0)
				{
					return false;
				}
				return true;
			}
		};
		
		// Screen Components
		addNewSynonymHandler();
		addNewRelationshipHandler();
		
		continueButton.disableProperty().bind(allValid.not());
	}

	private void addNewSynonymHandler() {
		// Setup Term
		TermRow dr = new TermRow();
		descriptions.add(dr);
		allValid.addBinding(dr.isValid());
		synonymVBox.getChildren().add(dr.getTermNode());

		// Setup Acceptable
		descTypeVBox.getChildren().add(dr.getTypeNode());
		
		// Setup Case
		caseVBox.getChildren().add(dr.getInitalCaseSigNode());
		
		// Setup Add Button
		Button addSynonymButton = new Button("+");
		addSynonymButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				addNewSynonymHandler();
			}
		});
		
		addSynonymButtonVBox.getChildren().add(addSynonymButton);
		
		// Add new Remove Synonym Button
		Button removeSynonymButton = new Button("-");
		removeSynonymButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				int idx = removeSynonymButtonVBox.getChildren().indexOf(e.getSource());
				removeNewSynonymHandler(idx);
			}
		});
		removeSynonymButtonVBox.getChildren().add(removeSynonymButton);
		
		handleRemoveButtonVisibility(removeSynonymButtonVBox);
	}

	private void addNewRelationshipHandler() {
		
		RelRow rr = new RelRow();
		relationships.add(rr);
		allValid.addBinding(rr.isValid());
		
		// Setup relationship Type
		relationshipTypeVBox.getChildren().add(rr.getRelationshipNode().getNode());

		// Setup Target
		targetVBox.getChildren().add(rr.getTargetNode().getNode());

		// Setup type
		typeVBox.getChildren().add(rr.getTypeNode());
		
		// Group
		groupVBox.getChildren().add(rr.getGroupNode());
		
		// Add new Add Parent Button
		Button addRelationshipButton = new Button("+");
		addRelationshipButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				addNewRelationshipHandler();
			}
		});
		
		addRelationshipButtonVBox.getChildren().add(addRelationshipButton);
		
		// Add new Remove Relationship Button
		Button removeRelationshipButton = new Button("-");
		removeRelationshipButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				int idx = removeRelationshipButtonVBox.getChildren().indexOf(e.getSource());
				removeNewRelationshipHandler(idx);
			}
		});
		removeRelationshipButtonVBox.getChildren().add(removeRelationshipButton);

		handleRemoveButtonVisibility(removeRelationshipButtonVBox);
	}
	
	private void removeNewRelationshipHandler(int idx) {
		RelRow rr = relationships.remove(idx);
		allValid.removeBinding(rr.isValid());
		
		relationshipTypeVBox.getChildren().remove(idx);
		targetVBox.getChildren().remove(idx);
		typeVBox.getChildren().remove(idx);
		groupVBox.getChildren().remove(idx);
		addRelationshipButtonVBox.getChildren().remove(idx);
		removeRelationshipButtonVBox.getChildren().remove(idx);

		handleRemoveButtonVisibility(removeRelationshipButtonVBox);
	}
	
	private void removeNewSynonymHandler(int idx) {
		TermRow dr = descriptions.remove(idx);
		allValid.removeBinding(dr.isValid());

		synonymVBox.getChildren().remove(idx);
		descTypeVBox.getChildren().remove(idx);
		caseVBox.getChildren().remove(idx);

		addSynonymButtonVBox.getChildren().remove(idx);
		removeSynonymButtonVBox.getChildren().remove(idx);

		handleRemoveButtonVisibility(removeSynonymButtonVBox);
	}

	@Override
	public void finishInit(ScreensController screenPage) {
		processController = screenPage;
	}

	@Override
	public void processValues() {
		processController.getWizard().setConceptComponents(descriptions, relationships); 
	}

	private void handleRemoveButtonVisibility(VBox vb) {
		int size = vb.getChildren().size();
		
		if (size == 1) {
			((Button)vb.getChildren().get(0)).setVisible(false);
		} else {
			for (Node n : vb.getChildren()) {
				((Button)n).setVisible(true);
			}
		}
	}
}