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
package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.ComboBoxSetupTool;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.util.WBUtility;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.slf4j.LoggerFactory;

/**
 * {@link FindAndReplaceController}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class FindAndReplaceController
{

	@FXML private CheckBox regexp;
	@FXML private TextField findText;
	@FXML private TextField replaceText;
	@FXML private ToggleGroup sdt;
	@FXML private RadioButton descriptionTypeAll;
	@FXML private RadioButton descriptionTypeSelected;
	@FXML private CheckBox descriptionTypeFSN;
	@FXML private CheckBox descriptionTypePT;
	@FXML private CheckBox descriptionTypeSynonym;
	@FXML private CheckBox caseSensitive;
	@FXML private ComboBox<SimpleDisplayConcept> retireAs;
	@FXML private ComboBox<SimpleDisplayConcept> searchInLanguage;
	@FXML private GridPane root;
	@FXML private GridPane optionsGridPane;
	@FXML private TitledPane optionsTitledPane;
	
	private StringProperty descriptionTypeInvalidReason = new SimpleStringProperty("");
	private BooleanBinding descriptionTypeSelectionValid;
	private StringProperty findTextInvalidReason = new SimpleStringProperty("");
	private BooleanBinding findTextValid;
	private BooleanBinding allFieldsValid;

	@FXML
	void initialize()
	{
		assert regexp != null : "fx:id=\"regexp\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert findText != null : "fx:id=\"findText\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert replaceText != null : "fx:id=\"replaceText\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert sdt != null : "fx:id=\"sdt\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeAll != null : "fx:id=\"descriptionTypeAll\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeFSN != null : "fx:id=\"descriptionTypeFSN\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypePT != null : "fx:id=\"descriptionTypePT\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeSynonym != null : "fx:id=\"descriptionTypeSynonym\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert descriptionTypeSelected != null : "fx:id=\"descriptionTypeSelected\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert caseSensitive != null : "fx:id=\"caseSensitive\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert retireAs != null : "fx:id=\"retireAs\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";
		assert searchInLanguage != null : "fx:id=\"searchInLanguage\" was not injected: check your FXML file 'FindAndReplaceController.fxml'.";

		FxUtils.preventColCollapse(root, 0);
		FxUtils.preventColCollapse(optionsGridPane, 0);
		FxUtils.preventColCollapse(optionsGridPane, 1);
		FxUtils.preventColCollapse(optionsGridPane, 2);
		
		//swap out some components, wrap them up in a stack pane so that we can set up the 
		//error and info markers.  Note, do this early, swapComponents isn't real smart, and might mess other things up
		StackPane sp = new StackPane();
		ErrorMarkerUtils.swapComponents(caseSensitive, sp, optionsGridPane);
		ErrorMarkerUtils.setupDisabledInfoMarker(caseSensitive, sp, new SimpleStringProperty("Not available during a Regular Expression search"));
		
		sp = new StackPane();
		ErrorMarkerUtils.swapComponents(descriptionTypeSelected, sp, optionsGridPane);
		ErrorMarkerUtils.setupErrorMarker(descriptionTypeSelected, sp, descriptionTypeInvalidReason);
		
		sp = new StackPane();
		ErrorMarkerUtils.swapComponents(findText, sp, root);
		ErrorMarkerUtils.setupErrorMarker(findText, sp, findTextInvalidReason);

		//Sigh - TitlePane doesn't advertise its size properly, unless you manually set the min sizes.
		//Add a couple listeners, do a bunch of hacking to work around it.
		//TODO file JavaFX bug
		optionsTitledPane.expandedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				//If open, bind it to its children content.  If closed, 26 is the value it needs for just the header.
				if (newValue)
				{
					optionsTitledPane.minHeightProperty().bind(optionsGridPane.heightProperty().add(26));
				}
				else
				{
					optionsTitledPane.minHeightProperty().unbind();
					optionsTitledPane.setMinHeight(26);
				}
			}
		});
		optionsTitledPane.heightProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				//When the height first changes from 0, bind it to the height of its child.
				if (optionsTitledPane.isExpanded())
				{
					optionsTitledPane.minHeightProperty().bind(optionsGridPane.heightProperty().add(26));
				}
				else
				{
					optionsTitledPane.setMinHeight(26);
				}
				//Don't need to listen to this anymore
				optionsTitledPane.heightProperty().removeListener(this);

				//Need to bounce it open and closed, to get the right initial sizes.  Not sure why.
				if (optionsTitledPane.isExpanded())
				{
					Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
							optionsTitledPane.setExpanded(false);
							optionsTitledPane.setExpanded(true);
						}
					});
				}
			}
		});
		
		//Bind various things together for validity
		descriptionTypeFSN.disableProperty().bind(descriptionTypeAll.selectedProperty());
		descriptionTypePT.disableProperty().bind(descriptionTypeAll.selectedProperty());
		descriptionTypeSynonym.disableProperty().bind(descriptionTypeAll.selectedProperty());
		caseSensitive.disableProperty().bind(regexp.selectedProperty());
		
		descriptionTypeSelectionValid = new BooleanBinding()
		{
			{
				bind(descriptionTypeFSN.selectedProperty(), descriptionTypePT.selectedProperty(), descriptionTypeSynonym.selectedProperty(), 
						descriptionTypeSelected.selectedProperty());
			}
			@Override
			protected boolean computeValue()
			{
				if (descriptionTypeSelected.isSelected() && 
						!(descriptionTypeFSN.isSelected() || descriptionTypePT.isSelected() || descriptionTypeSynonym.isSelected()))
				{
					descriptionTypeInvalidReason.set("When Description Type is 'Selected', you must choose at least one of "
							+ "'Fully Specified Name', 'Preferred Term' or 'Synonym'");
					return false;
				}
				descriptionTypeInvalidReason.set("");
				return true;
			}
		};
		
		findTextValid = new BooleanBinding()
		{
			{
				bind(findText.textProperty(), regexp.selectedProperty());
			}
			@Override
			protected boolean computeValue()
			{
				if (findText.getText().length() == 0)
				{
					findTextInvalidReason.set("The 'Find' field is required");
					return false;
				}
				else if (regexp.isSelected())
				{
					try
					{
						Pattern.compile(findText.getText());
						findTextInvalidReason.set("");
						return true;
					}
					catch (PatternSyntaxException e)
					{
						findTextInvalidReason.set("The entered value is not a valid regular expression");
						return false;
					}
				}
				findTextInvalidReason.set("");
				return true;
			}
		};

		searchInLanguage.getItems().add(new SimpleDisplayConcept("ANY"));
		searchInLanguage.getSelectionModel().select(0);
		
		ComboBoxSetupTool.setupComboBox(searchInLanguage);
		ComboBoxSetupTool.setupComboBox(retireAs);
		
		try
		{
			//84a0b03b-220c-3d69-8487-2e019c933687 Language type reference set
			for (ConceptVersionBI c : WBUtility.getAllChildrenOfConcept(WBUtility.getConceptVersion(UUID.fromString("84a0b03b-220c-3d69-8487-2e019c933687")).getNid()
					, true))
			{
				SimpleDisplayConcept sdc = new SimpleDisplayConcept(c);
				//TODO OTF Bug - OTF is broken, and doesn't even return hierarchies without duplicates https://jira.ihtsdotools.org/browse/OTFISSUE-21
				if (!searchInLanguage.getItems().contains(sdc))
				{
					searchInLanguage.getItems().add(sdc);
				}
			}
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(this.getClass()).error("Error populating 'Language' drop down", e);
			AppContext.getCommonDialogs().showErrorDialog("Error populating the 'Language' drop down", e);
			//this isn't fatal - 'ANY' will still be there.
		}

		allFieldsValid = findTextValid.and(descriptionTypeSelectionValid);
		
		try
		{
			//TODO these should come from user preferences
			retireAs.getItems().add(new SimpleDisplayConcept(WBUtility.getConceptVersion(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0])));
			retireAs.getItems().add(new SimpleDisplayConcept(WBUtility.getConceptVersion(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids()[0])));
			retireAs.getSelectionModel().select(0);
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(this.getClass()).error("Error populating 'Retire As' drop down", e);
			AppContext.getCommonDialogs().showErrorDialog("Error populating the 'Retire As' drop down", e);
			//JavaFX doesn't compute findTextValue, if no one is using it
			allFieldsValid = findTextValid.and(descriptionTypeSelectionValid.and(new SimpleBooleanProperty(false)));  
		}
	}
	
	protected BooleanBinding allFieldsValid()
	{
		return allFieldsValid;
	}

	protected GridPane getRoot()
	{
		return root;
	}
}
