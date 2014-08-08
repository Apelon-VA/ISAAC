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
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
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
	private SimpleBooleanProperty definedIsValid_;
	private GridPane gp_;
	private ChoiceBox<String> isCapCb;
	private ChoiceBox<String> languageCodeCb;
	private ChoiceBox<String> typeCb;
	private TextField termTf;
	private DescriptionVersionBI desc;
	private Label currentTerm;

	@Override
	protected void finishInit()
	{
		desc = (DescriptionVersionBI)origComp;
		currentTerm.setText(desc.getText());
		termTf.setText(desc.getText());

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
}
	
	@Override 
	protected void setupTopItems(VBox topItems) {
		definedIsValid_ = new SimpleBooleanProperty(true);
		gp_ = new GridPane();
		isCapCb  = new ChoiceBox<>();
		languageCodeCb  = new ChoiceBox<>();
		termTf = new TextField();
		typeCb= new ChoiceBox<>();
		
		popupTitle = "Modify Description";
		Label title = new Label(popupTitle);
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.prefWidthProperty().bind(topItems.widthProperty());
		topItems.getChildren().add(title);
		VBox.setMargin(title, new Insets(10, 10, 10, 10));

		gp_ = new GridPane();
		gp_.setHgap(10.0);
		gp_.setVgap(10.0);
		VBox.setMargin(gp_, new Insets(5, 5, 5, 5));
		topItems.getChildren().add(gp_);

		// Setup Type (Row #1)
		Label type = new Label("Type");
		type.getStyleClass().add("boldLabel");
		gp_.add(type, 0, 0);

		typeCb.getItems().add("Synonym");
		typeCb.getItems().add("Definition");
		typeCb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String oldVal, String newVal) {
				if (desc.getTypeNid() != getSelectedType()) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						definedIsValid_.set(false);;
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});
		gp_.add(typeCb, 1, 0);

		
		// Setup Term (Row #2)
		Label term = new Label("Current Term");
		term.getStyleClass().add("boldLabel");
		gp_.add(term, 0, 1);

		currentTerm = new Label();
		term.getStyleClass().add("boldLabel");
		gp_.add(currentTerm, 1, 1);

		termTf.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				if (!desc.getLang().equals(newVal.toString())) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						definedIsValid_.set(false);;
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});
		gp_.add(termTf, 0, 2);

		// Setup LangCode (Row #3)
		Label langCode = new Label("Language Code");
		langCode.getStyleClass().add("boldLabel");
		gp_.add(langCode, 0, 3);
		
		Set<String> noDialectCodes = new HashSet();
		for (LanguageCode val : LanguageCode.values()) {
			noDialectCodes.add(val.getFormatedLanguageNoDialectCode());
		}
		languageCodeCb.getItems().addAll(noDialectCodes);
		languageCodeCb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String oldVal, String newVal) {
				if (!desc.getLang().equals(newVal.toString())) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						definedIsValid_.set(false);;
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});
		gp_.add(languageCodeCb, 1, 3);

		// Setup Initial Cap (Row #4)
		Label isInitCap = new Label("Is Initial Capitilization?");
		isInitCap.getStyleClass().add("boldLabel");
		gp_.add(isInitCap, 0, 4);

		isCapCb.getItems().add("True");
		isCapCb.getItems().add("False");
		isCapCb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String oldVal, String newVal) {
				if (desc.isInitialCaseSignificant() && newVal.equals("False") ||
					!desc.isInitialCaseSignificant() && newVal.equals("True")) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						definedIsValid_.set(false);;
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});

		gp_.add(isCapCb, 1, 4);
				
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(isInitCap));
		gp_.getColumnConstraints().add(cc);

		cc = new ColumnConstraints();
		cc.setHgrow(Priority.ALWAYS);
		gp_.getColumnConstraints().add(cc);
	}

	@Override 
	protected void setupValidations() {
		allValid_ = new UpdateableBooleanBinding()
		{
			{
				addBinding(definedIsValid_, modificationMade);
			}

			@Override
			protected boolean computeValue()
			{
				if (definedIsValid_.get() && modificationMade.get())
				{
					reasonSaveDisabled_.set("");
					return true;
				}

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
			boolean isInitCap = (isCapCb.getSelectionModel().getSelectedIndex() == 0); 

			DescriptionCAB dcab = new DescriptionCAB(desc.getConceptNid(), type, LanguageCode.getLangCode(langCode), term, isInitCap, desc, WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);

			DescriptionChronicleBI dcbi = WBUtility.getBuilder().constructIfNotCurrent(dcab);
			WBUtility.addUncommitted(dcbi.getEnclosingConcept());
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
			if (typeCb.getSelectionModel().getSelectedIndex() == 0) {
				return SnomedMetadataRf2.SYNONYM_RF2.getNid();
			} else if (typeCb.getSelectionModel().getSelectedIndex() == 1) {
				return Snomed.DEFINITION_DESCRIPTION_TYPE.getNid();
			}
		} catch (IOException e) {
			logger_.error("Cannot access Description Type: " + typeCb.getSelectionModel().getSelectedItem());
		}
		
		return 0;
	}
}