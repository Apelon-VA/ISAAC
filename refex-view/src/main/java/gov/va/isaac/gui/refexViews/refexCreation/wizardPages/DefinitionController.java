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
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dialog.YesNoDialog;
import gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI;
import gov.va.isaac.gui.refexViews.refexCreation.RefexData;
import gov.va.isaac.gui.refexViews.refexCreation.ScreensController;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link DefinitionController}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DefinitionController implements PanelControllersI {
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private BorderPane refsetCreationPane;
	@FXML private ToggleGroup refexType;
	@FXML private RadioButton refexTypeRefset;
	@FXML private RadioButton refexAnnotationType;
	@FXML private TextField refexName;
	@FXML private TextField extensionCount;
	@FXML private TextArea refexDescription;
	@FXML private Button continueCreation;
	@FXML private Button cancelCreation;
	@FXML private HBox parentConceptHBox;
	@FXML private GridPane gridPane;
	
	Region scene_;
	ScreensController processController_;
	
	private ConceptNode parentConcept = null;
	
	private StringBinding refexDescriptionInvalidReason, refexNameInvalidReason, extensionCountInvalidReason;
	private BooleanBinding allValid;
	
	private RefexData wizardData;
	
	private static final Logger logger = LoggerFactory.getLogger(DefinitionController.class);

	@Override
	public void initialize() {
		
		assert continueCreation != null : "fx:id=\"continueCreation\" was not injected: check your FXML file 'definition.fxml'.";
		assert refexName != null : "fx:id=\"refexName\" was not injected: check your FXML file 'definition.fxml'.";
		assert refexTypeRefset != null : "fx:id=\"refexTypeRefset\" was not injected: check your FXML file 'definition.fxml'.";
		assert refexAnnotationType != null : "fx:id=\"refexAnnotationType\" was not injected: check your FXML file 'definition.fxml'.";
		assert refexType != null : "fx:id=\"refexType\" was not injected: check your FXML file 'definition.fxml'.";
		assert extensionCount != null : "fx:id=\"extensionCount\" was not injected: check your FXML file 'definition.fxml'.";
		assert refexDescription != null : "fx:id=\"refexDescription\" was not injected: check your FXML file 'definition.fxml'.";
		assert refsetCreationPane != null : "fx:id=\"refsetCreationPane\" was not injected: check your FXML file 'definition.fxml'.";
		assert cancelCreation != null : "fx:id=\"cancelCreation\" was not injected: check your FXML file 'definition.fxml'.";
		assert parentConceptHBox != null : "fx:id=\"parentConceptHBox\" was not injected: check your FXML file 'definition.fxml'.";
		
		extensionCount.setText("0");
		
		parentConcept = new ConceptNode(null, true);
		//this will cause it to look it up in a background thread...
		parentConcept.set(new SimpleDisplayConcept(RefexDynamic.REFEX_DYNAMIC_IDENTITY.getUuids()[0].toString()));
		
		parentConceptHBox.getChildren().add(parentConcept.getNode());
		HBox.setHgrow(parentConcept.getNode(), Priority.ALWAYS);

		cancelCreation.setOnAction(e -> 
		{
			((Stage)refsetCreationPane.getScene().getWindow()).close();
		});
		
		continueCreation.setOnAction(e -> {
			if (checkParentHierarchy()) 
			{
				processValues();
				processController_.showNextScreen();
			}
		});
		
		refexNameInvalidReason = new StringBinding()
		{
			{
				bind(refexName.textProperty());
			}
			@Override
			protected String computeValue()
			{
				if (refexName.getText().trim().isEmpty())
				{
					return "The Refex Name is required";
				}
				else
				{
					//This test isn't perfect... won't detect all cases where the FSN is actually identical to something that exists.
					//But it will detect the cases that will cause the blueprint construct to blowup
					UUID uuidWeWouldGetUponCreate = ConceptCB.computeComponentUuid(IdDirective.GENERATE_HASH, Arrays.asList(new String[] {refexName.getText().trim()}), 
							Arrays.asList(new String[] {refexName.getText().trim()}), null);
					if (ExtendedAppContext.getDataStore().hasUuid(uuidWeWouldGetUponCreate))
					{
						return "A concept already exists with this FSN";
					}
					else
					{
						return "";
					}
				}
			}
		};

		extensionCountInvalidReason = new StringBinding()
		{
			{
				bind(extensionCount.textProperty());
			}
			@Override
			protected String computeValue()
			{
				if (extensionCount.getText().trim().isEmpty())
				{
					return "The number of extension fields must be specified";
				}
				else
				{
					try
					{
						int c = Integer.parseInt(extensionCount.getText().trim());
						if (c < 0)
						{
							return "The extension field count must 0 or greater";
						}
						else
						{
							return "";
						}
					}
					catch (Exception e)
					{
						return "The extension field count must be an integer >= 0";
					}
				}
			}
		};
		
		refexDescriptionInvalidReason = new StringBinding()
		{
			{
				bind(refexDescription.textProperty());
			}
			@Override
			protected String computeValue()
			{
				if (refexDescription.getText().trim().isEmpty())
				{
					return "The Refex Description is required";
				}
				else
				{
					return "";
				}
			}
		};
		
		allValid = new BooleanBinding()
		{
			{
				bind(refexDescriptionInvalidReason, refexNameInvalidReason, extensionCountInvalidReason, parentConcept.isValid());
			}
			@Override
			protected boolean computeValue()
			{
				if (parentConcept.isValid().get() && refexDescriptionInvalidReason.get().isEmpty() && refexNameInvalidReason.get().isEmpty() 
						&& extensionCountInvalidReason.get().isEmpty())
				{
					return true;
				}
				return false;
			}
		};
		
		continueCreation.disableProperty().bind(allValid.not());
		
		StackPane sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(refexName, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(refexName, sp, refexNameInvalidReason);
		
		sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(refexDescription, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(refexDescription, sp, refexDescriptionInvalidReason);
		
		sp = new StackPane();
		ErrorMarkerUtils.swapGridPaneComponents(extensionCount, sp, gridPane);
		ErrorMarkerUtils.setupErrorMarker(extensionCount, sp, extensionCountInvalidReason);
	}

	public boolean checkParentHierarchy() {
		try 
		{
			if (!parentConcept.getConcept().isKindOf(WBUtility.getConceptVersion(RefexDynamic.REFEX_DYNAMIC_IDENTITY.getNid()))) 
			{
				YesNoDialog yn = new YesNoDialog(refsetCreationPane.getScene().getWindow());
				DialogResponse r = yn.showYesNoDialog("Continue?", "The parent concept you selected is not a descendent of the concept 'Dynamic Refexes'.\n"
						+ "Click Yes to continue using this concept, or No to go back and make changes.");
				if (DialogResponse.YES == r)
				{
					return true;
				}
				return false;
			}
			return true;
		} catch (IOException | ContradictionException e) 
		{
			logger.error("Unable to verify if concept is ancestor of Dynamic Refex Concept", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "Error Reading Parent Concept", e.getMessage(), refsetCreationPane.getScene().getWindow());
			return false;
		}
	}

	private void processValues() {
		int count = Integer.valueOf(extensionCount.getText().trim());
	
		if (wizardData == null)
		{
			wizardData = new RefexData(refexName.getText().trim(), refexDescription.getText().trim(), parentConcept.getConcept(), count, refexAnnotationType.isSelected());
		}
		else
		{
			wizardData.adjustColumnCount(count);
			wizardData.setRefexName(refexName.getText().trim());
			wizardData.setRefexDescription(refexDescription.getText().trim());
			wizardData.setParentConcept(parentConcept.getConcept());
			wizardData.setIsAnnotatedStyle(refexAnnotationType.isSelected());
		}
	}
	
	public RefexData getWizardData() {
		return wizardData;
	}

	/**
	 * @see gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI#finishInit(gov.va.isaac.gui.refexViews.refexCreation.ScreensController, javafx.scene.Parent)
	 */
	@Override
	public void finishInit(ScreensController screenController, Region parent)
	{
		processController_ = screenController;
		scene_ = parent;
	}

	/**
	 * @see gov.va.isaac.gui.refexViews.refexCreation.PanelControllersI#getParent()
	 */
	@Override
	public Region getParent()
	{
		return scene_;
	}
}

