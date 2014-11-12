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
package gov.va.isaac.gui.conceptViews.modeling;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.jfree.util.Log;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * DescriptionModelingPopup
 * 
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */

@Service
@PerLookup
public class DescriptionModelingPopup extends ModelingPopup
{
	private ChoiceBox<String> isCapCb;
	private ChoiceBox<String> languageCodeCb;
	private ChoiceBox<String> typeCb;
	private TextField termTf;
	private DescriptionVersionBI<?> desc;
	private SimpleBooleanProperty langCodeNewSelected;
	private SimpleBooleanProperty textNewSelected;
	private SimpleBooleanProperty typeNewSelected;
	private SimpleBooleanProperty isCapNewSelected;

	@Override
	protected void finishInit()
	{
		desc = (DescriptionVersionBI<?>)origComp;
		termTf.setText(desc.getText());

		if (!desc.isUncommitted() || (desc.isUncommitted() && desc.getVersions().size() > 1)) {
			termTf.setEditable(false);
			termTf.setBackground(notEditableBackground);
		}
		
		try {
			if (desc.getTypeNid() == SnomedMetadataRf2.SYNONYM_RF2.getNid()) {
				typeCb.getSelectionModel().select("Synonym");
			} else if (desc.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid()) {
				typeCb.getSelectionModel().select("Definition");
			}
		} catch (IOException e) {
			logger_.error("Cannot access Description Type: " + typeCb.getSelectionModel().getSelectedItem());
		}
		
		languageCodeCb.getSelectionModel().select(desc.getLang());

		if (desc.isInitialCaseSignificant()) {
			isCapCb.getSelectionModel().select("True");
		} else {
			isCapCb.getSelectionModel().select("False");
		}
		
		row = 0;
		
		try {
			ComponentChronicleBI<?> chronicle = desc.getChronicle();
			DescriptionVersionBI<?> displayVersion = (DescriptionVersionBI<?>) chronicle.getVersion(WBUtility.getViewCoordinate());

			if (chronicle.isUncommitted()) {
				displayVersion = (DescriptionVersionBI<?>) WBUtility.getLastCommittedVersion(chronicle);
			}

			createOriginalLabel(WBUtility.getConceptVersion(displayVersion.getTypeNid()).getPreferredDescription().getText());
			createOriginalLabel(displayVersion.getText());
			createOriginalLabel(displayVersion.getLang());
			createOriginalLabel((displayVersion.isInitialCaseSignificant()) ? "True" : "False");
		} catch (Exception e) {
			Log.error("Cannot access Pref Term for attributes of relationship: "  + desc.getPrimordialUuid(), e);
		}
		
		setupGridPaneConstraints();
	}

	@Override 
	protected void setupTopItems(VBox topItems) {
		isCapCb  = new ChoiceBox<>();
		languageCodeCb  = new ChoiceBox<>();
		termTf = new TextField();
		typeCb= new ChoiceBox<>();
		langCodeNewSelected = new SimpleBooleanProperty(false);
		textNewSelected = new SimpleBooleanProperty(false);
		typeNewSelected = new SimpleBooleanProperty(false);
		isCapNewSelected = new SimpleBooleanProperty(false);
		
		popupTitle = "Modify Description";

		setupGridPane(topItems);

		// Setup Type (Row #1)
		setupType();
		
		// Setup Term (Row #2)
		setupTerm();
		
		// Setup LangCode (Row #3)
		createLangCode();
		
		// Setup Initial Cap (Row #4)
		setupInitCap();
	}

	private void setupInitCap() {
		createTitleLabel("Is Initial Capitilization");

		if (desc == null) {
			isCapCb.getItems().add(SELECT_VALUE);
		}
		isCapCb.getItems().add("True");
		isCapCb.getItems().add("False");
		isCapCb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
				if (desc != null) {
					 if ((desc.isInitialCaseSignificant() && newVal.equals("False")) ||
						(!desc.isInitialCaseSignificant() && newVal.equals("True"))) {
						 modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (!newVal.equals(SELECT_VALUE)) {
					isCapNewSelected.set(true);
				} else {
					isCapNewSelected.set(false);
				}
			}
		});

		gp_.add(isCapCb, 2, row);
				
		row++;
	}

	private void createLangCode() {
		createTitleLabel("Language Code");
		
		Set<String> noDialectCodes = new HashSet<>();
		for (LanguageCode val : LanguageCode.values()) {
			noDialectCodes.add(val.getFormatedLanguageNoDialectCode());
		}
		if (desc == null) {
			languageCodeCb.getItems().add(SELECT_VALUE);
		}

		languageCodeCb.getItems().addAll(noDialectCodes);
		languageCodeCb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
				if (desc != null) {
					if (!desc.getLang().equals(newVal.toString())) { 
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (!newVal.equals(SELECT_VALUE)) {
					langCodeNewSelected.set(true);
				} else {
					langCodeNewSelected.set(false);
				}
			}
		});
		gp_.add(languageCodeCb, 2, row);

		row++;
	}

	private void setupTerm() {
		createTitleLabel("Current Term");

		termTf.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				if (desc != null) {
					if (!desc.getText().equals(newVal.toString())) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (newVal.trim().length() > 0) {
					textNewSelected.set(true);
				} else {
					textNewSelected.set(false);
				}
			}
		});
		gp_.add(termTf, 2, row);

		row++;
	}

	private void setupType() {
		createTitleLabel("Type");

		if (desc == null) {
			typeCb.getItems().add(SELECT_VALUE);
		}
		
		typeCb.getItems().add("Synonym");
		typeCb.getItems().add("Definition");
		typeCb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
				if (desc != null) {
					if (desc.getTypeNid() != getSelectedType()) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (!newVal.equals(SELECT_VALUE)) {
					typeNewSelected.set(true);
				} else {
					typeNewSelected.set(false);
				}
			}
		});
		gp_.add(typeCb, 2, row);
		row++;
	}

	@Override 
	protected void setupValidations() {
		allValid_ = new UpdateableBooleanBinding()
		{
			{
				if (desc != null) {
					addBinding(modificationMade);
				} else {
					addBinding(isCapNewSelected, langCodeNewSelected, textNewSelected, typeNewSelected);
				}
			}

			@Override
			protected boolean computeValue()
			{
				if ((desc != null && modificationMade.get()) ||
					(desc == null && isCapNewSelected.get() && langCodeNewSelected.get() && textNewSelected.get() && typeNewSelected.get())) 
				{
					reasonSaveDisabled_.set("");
					return true;
				}

				reasonSaveDisabled_.set("Cannot create new description until all values are specified");
				return false;
			}
		};
	}

	@Override 
	protected void addNewVersion()
	{	
		try
		{
			int type = getSelectedType(); 
			String langCode = languageCodeCb.getSelectionModel().getSelectedItem();
			
			String term = termTf.getText(); 
			boolean isInitCap = (isCapCb.getSelectionModel().getSelectedItem().equalsIgnoreCase("True")); 

			if (desc == null) {
				WBUtility.createNewDescription((desc != null) ? desc.getConceptNid() : conceptNid, type, LanguageCode.getLangCode(langCode), term, isInitCap);
			} else {
				if (desc.isUncommitted()) {
					ExtendedAppContext.getDataStore().forget(desc);
				}
				DescriptionCAB dcab = new DescriptionCAB((desc != null) ? desc.getConceptNid() : conceptNid, type, LanguageCode.getLangCode(langCode), term, isInitCap, desc, WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
	
				DescriptionChronicleBI dcbi = WBUtility.getBuilder().constructIfNotCurrent(dcab);
				WBUtility.addUncommitted(dcbi.getEnclosingConcept());
			}
		}
		catch (Exception e)
		{
			logger_.error("Error saving description", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "There was an error saving the Description", e.getMessage(), this);
		}
	}
	

	@Override 
	protected boolean passesQA() {
		return true;
	}

	private int getSelectedType() {
		try {
			if (typeCb.getSelectionModel().getSelectedItem().equalsIgnoreCase("Synonym")) {
				return SnomedMetadataRf2.SYNONYM_RF2.getNid();
			} else if (typeCb.getSelectionModel().getSelectedItem().equalsIgnoreCase("Definition")) {
				return Snomed.DEFINITION_DESCRIPTION_TYPE.getNid();
			}
		} catch (IOException e) {
			logger_.error("Cannot access Description Type: " + typeCb.getSelectionModel().getSelectedItem());
		}
		
		return 0;
	}
}