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

import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.conceptCreation.PanelControllers;
import gov.va.isaac.gui.conceptCreation.ScreensController;
import gov.va.isaac.util.UpdateableListBinding;
import gov.va.isaac.util.WBUtility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionRevision;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipRevision;
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
	@FXML private VBox acceptableVBox;
	@FXML private VBox caseVBox;
	@FXML private VBox langVBox;
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
	
	
	private Map<Node, ConceptNode> nodeToTargetMap = new HashMap<>();
	private Map<Node, ConceptNode> nodeToRelTypeMap = new HashMap<>();
	private ObservableList<String> relTypeOptions;

	private BooleanBinding allValid;
	private UpdateableListBinding termInvalidReason, langInvalidReason;

	private List<RelationshipRevision> rels = new ArrayList<>();
	private List<RelationshipType> relTypes = new ArrayList<>();
	private List<ConceptVersionBI> relTargets = new ArrayList<>();
	
	private int acceptableTypeNid;
	private int synonymTypeNid;


	@Override
	public void initialize() {		
		vc = WBUtility.getViewCoordinate();
		
		try {
			acceptableTypeNid = SnomedMetadataRf2.ACCEPTABLE_RF2.getNid();
			synonymTypeNid = SnomedMetadataRf2.SYNONYM_RF2.getNid();
		} catch (Exception e) {
			LOG.error("Unable to identify acceptable and synonym types.  Points to larger problem", e);
		}
		
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

//		setupQA();
		
		// Screen Components
		addNewSynonymHandler();
		addNewRelationshipHandler();
	}

	private void addNewSynonymHandler() {
		// Setup Term
		int lastItemIdx = synonymVBox.getChildren().size();
		TextField term = new TextField();
		synonymVBox.getChildren().add(term);
//		termInvalidReason.addBinding(term.textProperty());
//		termInvalidReason.invalidate();

//		StackPane sp = new StackPane();
//		SimpleStringProperty reason = (SimpleStringProperty)termInvalidReason.get(lastItemIdx);
//		TextField tf = (TextField) synonymVBox.getChildren().get(lastItemIdx);
//		ErrorMarkerUtils.swapVBoxComponents(tf, sp, synonymVBox);
//		ErrorMarkerUtils.setupErrorMarker(tf, sp, reason);

		// Setup Acceptable
		CheckBox acceptable = new CheckBox();
		acceptableVBox.getChildren().add(acceptable);
		
		// Setup Case
		CheckBox caseSens = new CheckBox();
		caseVBox.getChildren().add(caseSens);

		// Setup Lang
		TextField lang = new TextField();
		langVBox.getChildren().add(lang);
//		langInvalidReason.addBinding(term.textProperty());
//		langInvalidReason.invalidate();
		
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
		// Setup relationship Type
		ConceptNode relTypeConcept = new ConceptNode(null, true);
		relationshipTypeVBox.getChildren().add(relTypeConcept.getNode());
		nodeToRelTypeMap.put(relTypeConcept.getNode(), relTypeConcept);

		// Setup Target
		ConceptNode targetConcept = new ConceptNode(null, true);
		targetVBox.getChildren().add(targetConcept.getNode());
		nodeToTargetMap.put(targetConcept.getNode(), targetConcept);

		// Setup type
		relTypeOptions = FXCollections.observableArrayList("Role", "Qualifier");
		ChoiceBox<String> typeOption = new ChoiceBox<String>(relTypeOptions);
		typeVBox.getChildren().add(typeOption);
		
		// Group
		TextField group = new TextField("0");
		groupVBox.getChildren().add(group);
		
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
		Node relTypeNode = relationshipTypeVBox.getChildren().get(idx);
		nodeToRelTypeMap.remove(relTypeNode);
		relationshipTypeVBox.getChildren().remove(idx);

		Node targetNode = targetVBox.getChildren().get(idx);
		nodeToTargetMap.remove(targetNode);
		targetVBox.getChildren().remove(idx);

		typeVBox.getChildren().remove(idx);
		groupVBox.getChildren().remove(idx);

		addRelationshipButtonVBox.getChildren().remove(idx);
		removeRelationshipButtonVBox.getChildren().remove(idx);

		handleRemoveButtonVisibility(removeRelationshipButtonVBox);
	}
	
	private void removeNewSynonymHandler(int idx) {
		TextField term = (TextField)synonymVBox.getChildren().get(idx);
		TextField lang = (TextField)langVBox.getChildren().get(idx);
//		termInvalidReason.removeBinding(term.textProperty());
//		termInvalidReason.invalidate();
//		langInvalidReason.removeBinding(lang.textProperty());
//		langInvalidReason.invalidate();

		synonymVBox.getChildren().remove(idx);
		acceptableVBox.getChildren().remove(idx);
		caseVBox.getChildren().remove(idx);
		langVBox.getChildren().remove(idx);

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
		List<DescriptionRevision> descs = createNewDescriptions();
		createNewRelationships();
		
		processController.getWizard().setConceptComponents(descs, rels, relTypes, relTargets); 
	}

	private void createNewRelationships() {
		for (int i = 0; i < targetVBox.getChildren().size(); i++) {
			RelationshipRevision r = new RelationshipRevision();
			
			Node relTypeNode = relationshipTypeVBox.getChildren().get(i);
			ConceptNode relType = nodeToRelTypeMap.get(relTypeNode);
			
			if (relType.isValid().getValue()) {
				r.setTypeNid(relType.getConcept().getNid());
	
				Node targetNode = targetVBox.getChildren().get(i);
				ConceptNode target = nodeToTargetMap.get(targetNode);
				
				
				String groupStr = ((TextField)groupVBox.getChildren().get(i)).getText().trim();
				r.setGroup(new Integer(groupStr));
	
				rels.add(r);
				relTargets.add(target.getConcept());
				
				String selection = (String)((ChoiceBox)typeVBox.getChildren().get(i)).getSelectionModel().getSelectedItem();
				if (selection.equalsIgnoreCase("Role")) {
					relTypes.add(RelationshipType.STATED_ROLE);
				} else {
					relTypes.add(RelationshipType.QUALIFIER);
				}
			}
		}
	}

	private List<DescriptionRevision> createNewDescriptions() {
		List<DescriptionRevision> descs = new ArrayList<>();
		
		for (int i = 0; i < synonymVBox.getChildren().size(); i++) {
			DescriptionRevision d = new DescriptionRevision();
			
			if (!((TextField)synonymVBox.getChildren().get(i)).getText().trim().isEmpty()) {
				d.setText(((TextField)synonymVBox.getChildren().get(i)).getText().trim());
				d.setInitialCaseSignificant(((CheckBox)caseVBox.getChildren().get(i)).isSelected());
				d.setLang(((TextField)langVBox.getChildren().get(i)).getText().trim());
				
				if (((CheckBox)acceptableVBox.getChildren().get(i)).isSelected()) {
					d.setTypeNid(acceptableTypeNid);
				} else {
					d.setTypeNid(synonymTypeNid);
				}
	
				descs.add(d);
			}
		}

		return descs;
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


	private void setupQA() {
		// QA Handling
		termInvalidReason = new UpdateableListBinding() 
		{
			@Override
			protected ObservableList<SimpleStringProperty> computeValue()
			{
				ObservableList<SimpleStringProperty> returnList = FXCollections.observableArrayList();

				for (int i = 0; i < synonymVBox.getChildren().size(); i++)
				{
					// Check that not partially filled out
					TextField tf = (TextField) synonymVBox.getChildren().get(i);
					String term = tf.getText().trim();

					int frontParenCount = countChar(term, "(");
					int backParenCount = countChar(term, ")");

					String lang = "";
					if (i < langVBox.getChildren().size()) { 
						lang = ((TextField)langVBox.getChildren().get(i)).getText().trim();
					}
					
					if (!term.isEmpty() && lang.trim().isEmpty()) {
						returnList.add(new SimpleStringProperty("Cannot fill out Language and not Term"));
						invalidate();
					} else if (frontParenCount != 0 || backParenCount != 0) {
						returnList.add(new SimpleStringProperty("Cannot have parenthesis in synonym or it may be confused with the FSN"));
						invalidate();
					} else {
						returnList.add(new SimpleStringProperty(""));
					}
				}
				
				return returnList;
			}

			private int countChar(String str, String c) {
				int count = 0;
				int idx = 0;
				while ((idx = str.indexOf(c, idx)) != -1) {
					count++;
					idx += c.length();
				}
				return count;
			}
		};
/*		langInvalidReason = new UpdateableStringBinding() 
		{
			@Override
			protected String computeValue()
			{
				for (int i = 0; i < langVBox.getChildren().size(); i++)
				{
					// Check that not partially filled out
					TextField tf = (TextField) langVBox.getChildren().get(i);
					String lang = tf.getText().trim();
					
					String term = ((TextField)synonymVBox.getChildren().get(i)).getText().trim();
					
					if (!lang.isEmpty() && term.trim().isEmpty()) {
						return "Cannot fill out Term and not Language";
					} else if (!StringUtils.isAlpha(lang)) {
						return "Language must be filled with only alphabetically letters";
					} else if (lang.length() != 2) {
						return "Language must be filled out with a 2-character string";
					}
				}

				return "";
			}
		};
*/
		for (int i = 0; i < synonymVBox.getChildren().size(); i++)
		{
			// Check that not partially filled out
			TextField tf = (TextField) synonymVBox.getChildren().get(i);
			termInvalidReason.addBinding(tf.textProperty());
		}

		for (int i = 0; i < langVBox.getChildren().size(); i++)
		{
			TextField tf = (TextField) langVBox.getChildren().get(i);
//			langInvalidReason.addBinding(tf.textProperty());
		}

		termInvalidReason.invalidate();
//		langInvalidReason.invalidate();

		allValid = new BooleanBinding()
		{
			{
				bind(termInvalidReason);//, langInvalidReason);
			}
			@Override
			protected boolean computeValue()
			{
				if (termInvalidReason.get().isEmpty())// && langInvalidReason.get().isEmpty())
				{
					return true;
				}
				return false;
			}
		};
		
		continueButton.disableProperty().bind(allValid.not());
		
//		
//		sp = new StackPane();
//		ErrorMarkerUtils.swapComponents(prefTerm, sp, gridPane);
//		ErrorMarkerUtils.setupErrorMarker(prefTerm, sp, prefTermInvalidReason);
		
	}

}