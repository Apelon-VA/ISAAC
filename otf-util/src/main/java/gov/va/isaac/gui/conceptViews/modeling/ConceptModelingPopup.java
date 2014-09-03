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
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * ConceptModelingPopup
 * 
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */

@Service
@PerLookup
public class ConceptModelingPopup extends ModelingPopup
{
	private SimpleBooleanProperty definedIsValid_;
	private ChoiceBox<String> cb;
	private ConceptAttributeVersionBI<?> attr;
	
	@Override
	protected void finishInit()
	{
		row = 0;
		attr = (ConceptAttributeVersionBI<?>)origComp;
		
		if (attr.isDefined()) {
			cb.getSelectionModel().select("True");
		} else {
			cb.getSelectionModel().select("False");
		}
	
		// TODO: Needs to reference previous commit, not component as-is before panel opened
		createOriginalLabel((attr.isDefined()) ? "True" : "False");
		
		setupGridPaneConstraints();
	}
	
	@Override 
	protected void setupTopItems(VBox topItems) {
		definedIsValid_ = new SimpleBooleanProperty(true);
		cb  = new ChoiceBox<>();

		popupTitle = "Modify Concept's Attributes";
		
		setupGridPane(topItems);

		setupIsDefined();
	}

	private void setupIsDefined() {
		createTitleLabel("Is Fully Defined");

		cb.getItems().add("True");
		cb.getItems().add("False");
		cb.valueProperty().addListener((ChangeListener<String>) (ov, oldVal, newVal) -> {
			if (attr.isDefined() && newVal.equals("False") ||
				!attr.isDefined() && newVal.equals("True")) {
				modificationMade.set(true);
				
				if (!passesQA()) {
					definedIsValid_.set(false);;
					reasonSaveDisabled_.set("Failed QA");
				}
			} else {
				modificationMade.set(false);
				reasonSaveDisabled_.set("Cannot save unless original content changed");
			}
		});

		gp_.add(cb, 2, row);
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
			boolean isDefined = (cb.getSelectionModel().getSelectedIndex() == 0); 
			ConceptAttributeAB cab = new ConceptAttributeAB(attr.getConceptNid(), isDefined, RefexDirective.EXCLUDE);
			// Need to add a fix for storing isDefined 
			
			ConceptAttributeChronicleBI cabi = WBUtility.getBuilder().constructIfNotCurrent(cab);
			
			WBUtility.addUncommitted(cabi.getEnclosingConcept());
		}
		catch (Exception e)
		{
			logger_.error("Error saving concept attributes", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "There was an error saving the concept attributes", e.getMessage(), this);
		}
	}

	@Override 
	protected boolean passesQA() {
		return true;
	}
}