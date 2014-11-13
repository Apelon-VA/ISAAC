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
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link DefinitionController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DefinitionController implements PanelControllers {
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	
	@FXML private TextField fsn;
	@FXML private Button continueCreation;
	@FXML private TextArea prefTerm;
	@FXML private VBox addParentButtonVBox;
	@FXML private RadioButton isFullyDefined;
	@FXML private VBox removeParentButtonVBox;
	@FXML private VBox parentVBox;
	@FXML private BorderPane conceptCreationPane;
	@FXML private Button cancelCreation;
	@FXML private GridPane gridPane;
	@FXML private RadioButton isPrimitive;
	@FXML private ToggleGroup conceptTypeGroup;
	

	
	ScreensController processController;
	
	private StringBinding prefTermInvalidReason, fsnInvalidReason;
	private BooleanBinding allValid;
	private Map<Node, ConceptNode> nodeToConMap = new HashMap<>();
	private UpdateableBooleanBinding parentsBinding;

	private final Logger logger = LoggerFactory.getLogger(DefinitionController.class);

	@Override
	public void initialize() {
		
		logger.debug("Creating a DefinitionController");
		cancelCreation.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					((Stage)conceptCreationPane.getScene().getWindow()).close();
				}});
		
		continueCreation.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					processValues();
					processController.setScreen(ScreensController.COMPONENTS_SCREEN);
				}
			});
		
		prefTermInvalidReason = new StringBinding()
		{
			{
				bind(prefTerm.textProperty());
			}
			@Override
			protected String computeValue()
			{
				if (prefTerm.getText().trim().isEmpty())
				{
					return "The Preferred Term is required";
				}
				else
				{
					return "";
				}
			}
		};

		fsnInvalidReason = new StringBinding()
		{
			{
				bind(fsn.textProperty());
			}
			@Override
			protected String computeValue()
			{
				if (fsn.getText().trim().isEmpty())
				{
					return "The Fully Specified Name is required";
				}
				else
				{
					int frontParenCount = countChar(fsn.getText(), "(");
					int backParenCount = countChar(fsn.getText(), ")");

					if (frontParenCount != 1 || backParenCount != 1) 
					{
						// TODO: Put this back after demo and only throw if under SCT
						// return "FSNs must have a single set of parenthesis to define the semantic tag";
						return "";
					} else if (fsn.getText().trim().indexOf(")") != fsn.getText().trim().length() - 1) {
						return "FSNs may not have characters available after their semantic tag";
					} else {
						return "";
					}
				}
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

		parentsBinding = new UpdateableBooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				for (ConceptNode n : nodeToConMap.values())
				{
					ConceptNode on = (ConceptNode) n;
					if (!on.isValid().get())
					{
						return false;
					}
				}
				
				if (nodeToConMap.values().size() == 0)
				{
					return false;
				}
				return true;
			}
		};
		for (ConceptNode n : nodeToConMap.values())
		{
			parentsBinding.addBinding(((ConceptNode) n).isValid());
		}
		parentsBinding.invalidate();

		
		allValid = new BooleanBinding()
		{
			{
				bind(prefTermInvalidReason, fsnInvalidReason, parentsBinding);
			}
			@Override
			protected boolean computeValue()
			{
				if (fsnInvalidReason.get().isEmpty() && prefTermInvalidReason.get().isEmpty() && parentsBinding.get())
				{
					return true;
				}
				return false;
			}
		};
		
		continueCreation.disableProperty().bind(allValid.not());
		
		StackPane sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(fsn, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(fsn, sp, fsnInvalidReason);
		
		sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(prefTerm, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(prefTerm, sp, prefTermInvalidReason);
		
		addNewParentHandler();
		
// TODO - Handle with HBox?
//		VBox.setVgrow(parentConcept.getNode(), Priority.ALWAYS);

	}

	@Override
	public void finishInit(ScreensController screenParent){
		processController = screenParent;
	}

	@Override
	public void processValues() {
		List<ConceptVersionBI> parents = new ArrayList<>();
		
		for (Node parentNode : parentVBox.getChildren()) {
			ConceptNode parent = nodeToConMap.get(parentNode);
			parents.add(parent.getConcept());	
		}
		processController.getWizard().setConceptDefinitionVals(fsn.getText().trim(), prefTerm.getText().trim(), parents,
												isPrimitive.isSelected()); 
	}

	private void removeNewParentHandler(int idx) {
		Node parentNode = parentVBox.getChildren().get(idx);
		ConceptNode parentConcept = nodeToConMap.get(parentNode);
		nodeToConMap.remove(parentNode);
		parentsBinding.removeBinding(parentConcept.isValid());
		parentsBinding.invalidate();

		parentVBox.getChildren().remove(idx);
		addParentButtonVBox.getChildren().remove(idx);
		removeParentButtonVBox.getChildren().remove(idx);
		
		handleRemoveButtonVisibility();
	}

	private void addNewParentHandler() {
		// Add new Concept Node
		ConceptNode newParentCon = new ConceptNode(null, true);
		parentVBox.getChildren().add(newParentCon.getNode());
		nodeToConMap.put(newParentCon.getNode(), newParentCon);
		
		parentsBinding.addBinding(newParentCon.isValid());
		parentsBinding.invalidate();

		// Add new Add Parent Button
		Button addParentButton = new Button("+");
		addParentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				addNewParentHandler();
			}
		});
		
		addParentButtonVBox.getChildren().add(addParentButton);
		
		// Add new Remove Parent Button
		Button removeParentButton = new Button("-");
		removeParentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				int idx = removeParentButtonVBox.getChildren().indexOf(e.getSource());
				removeNewParentHandler(idx);
			}
		});
		removeParentButtonVBox.getChildren().add(removeParentButton);
		
		handleRemoveButtonVisibility();
	}

	private void handleRemoveButtonVisibility() {
		int size = removeParentButtonVBox.getChildren().size();
		
		if (size == 1) {
			((Button)removeParentButtonVBox.getChildren().get(0)).setVisible(false);
		} else {
			for (Node n : removeParentButtonVBox.getChildren()) {
				((Button)n).setVisible(true);
			}
		}
	}
}

