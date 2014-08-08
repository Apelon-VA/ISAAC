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
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
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
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * ConceptModelingPopup
 * 
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */

@Service
@PerLookup
public class RelationshipModelingPopup extends ModelingPopup
{
	private GridPane gp_;
	private ConceptNode otherEndCon;
	private ConceptNode typeCon;
	private TextField groupNum;
	private ChoiceBox<SimpleDisplayConcept> refinabilityCon;
	private ChoiceBox<SimpleDisplayConcept> characteristicCon;
	private RelationshipVersionBI rel;
	private boolean isDestination = false;
	private Label otherConcept;
	private Label title;

	public RelationshipModelingPopup() {
		
	}
	
	public void setDestination(boolean isDestination) {
		this.isDestination  = isDestination;
	}
	
	@Override
	protected void finishInit()
	{
		rel = (RelationshipVersionBI)origComp;
		
		if (!isDestination) {
			otherEndCon.set(WBUtility.getConceptVersion(rel.getDestinationNid()));
		} else {
			otherEndCon.set(WBUtility.getConceptVersion(rel.getOriginNid()));
		}
		
		typeCon.set(WBUtility.getConceptVersion(rel.getTypeNid()));
		
		groupNum.setText(String.valueOf(rel.getGroup()));
		
		refinabilityCon.getSelectionModel().select(new SimpleDisplayConcept(WBUtility.getConceptVersion(rel.getRefinabilityNid())));
		characteristicCon.getSelectionModel().select(new SimpleDisplayConcept(WBUtility.getConceptVersion(rel.getCharacteristicNid())));
		
		if (isDestination) {
			otherConcept.setText("Origin Concept");
			title.setText("Modify Destination Relationship");
		}

	}
	
	@Override 
	protected void setupTopItems(VBox topItems) {
		gp_ = new GridPane();
		refinabilityCon  = new ChoiceBox<>();
		characteristicCon  = new ChoiceBox<>();
		otherEndCon = new ConceptNode(null, true);
		typeCon = new ConceptNode(null, true);
		groupNum = new TextField();

		popupTitle = "Modify Source Relationship";

		title = new Label(popupTitle);
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
		otherConcept = new Label("Destination Concept");
		otherConcept.getStyleClass().add("boldLabel");
		gp_.add(otherConcept, 0, 0);

		otherEndCon.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
			@Override
			public void changed(ObservableValue ov, ConceptVersionBI oldVal, ConceptVersionBI newVal) {
				if (!otherEndCon.isValid().getValue()) {
					reasonSaveDisabled_.set(otherEndCon.getInvalidReason().getValue());
				} else  if ((!isDestination && rel.getDestinationNid() != newVal.getNid()) ||
					(isDestination && rel.getOriginNid() != newVal.getNid())) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});
		gp_.add(otherEndCon.getNode(), 1, 0);

		
		// Setup Term (Row #2)
		Label type = new Label("Relationship Type");
		type.getStyleClass().add("boldLabel");
		gp_.add(type, 0, 1);

		typeCon.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
			@Override
			public void changed(ObservableValue ov, ConceptVersionBI oldVal, ConceptVersionBI newVal) {
				if (!typeCon.isValid().getValue()) {
					reasonSaveDisabled_.set(typeCon.getInvalidReason().getValue());
				} else if (rel.getTypeNid() != newVal.getNid()) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});
		gp_.add(typeCon.getNode(), 1, 1);

		
		
		// Setup LangCode (Row #3)
		Label refinability = new Label("Refinability");
		refinability.getStyleClass().add("boldLabel");
		gp_.add(refinability, 0, 2);
		
		refinabilityCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.NOT_REFINABLE_RF2));
		refinabilityCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2));
		refinabilityCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2));
		refinabilityCon.valueProperty().addListener(new ChangeListener<SimpleDisplayConcept>() {
			@Override
			public void changed(ObservableValue ov, SimpleDisplayConcept oldVal, SimpleDisplayConcept newVal) {
				if (rel.getRefinabilityNid() != newVal.getNid()) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});
		gp_.add(refinabilityCon, 1, 2);

		// Setup Initial Cap (Row #4)
		Label characteristic = new Label("Characteristic Type?");
		characteristic.getStyleClass().add("boldLabel");
		gp_.add(characteristic, 0, 3);

		characteristicCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2));
		characteristicCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2));
		characteristicCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2));
		characteristicCon.valueProperty().addListener(new ChangeListener<SimpleDisplayConcept>() {
			@Override
			public void changed(ObservableValue ov, SimpleDisplayConcept oldVal, SimpleDisplayConcept newVal) {
				if (rel.getCharacteristicNid() != newVal.getNid()) {
					modificationMade.set(true);
					
					if (!passesQA()) {
						reasonSaveDisabled_.set("Failed QA");
					}
				} else {
					modificationMade.set(false);
					reasonSaveDisabled_.set("Cannot save unless original content changed");
				}
			}
		});

		gp_.add(characteristicCon, 1, 3);
		
		
		// Group (Row #5)
		Label group = new Label("Group");
		group.getStyleClass().add("boldLabel");
		gp_.add(group, 0, 4);

		groupNum.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				int newGroup = 0; 
				try {
					newGroup = Integer.parseInt(newVal);
					 if (newGroup < 0) {
						reasonSaveDisabled_.set("Group must be 0 or greater");
					} else if (rel.getGroup() != newGroup) {
						modificationMade.set(true);
						
						if (!passesQA()) {
							reasonSaveDisabled_.set("Failed QA");
						}
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} catch (NumberFormatException e) {
					reasonSaveDisabled_.set("Must select an integer");
				} 
			}
		});
		gp_.add(groupNum, 1, 4);

		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.NEVER);
		cc.setMinWidth(FxUtils.calculateNecessaryWidthOfBoldLabel(characteristic));
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
				addBinding(modificationMade);
			}

			@Override
			protected boolean computeValue()
			{
				if (modificationMade.get())
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

			int otherEndConNid = otherEndCon.getConcept().getNid(); 
			int typeConNid = typeCon.getConcept().getNid(); 
			int group = Integer.parseInt(groupNum.getText());
			int refNid = refinabilityCon.getSelectionModel().getSelectedItem().getNid();
			int charNid = characteristicCon.getSelectionModel().getSelectedItem().getNid(); 

			RelationshipCAB dcab;
			if (!isDestination) {
				dcab = new RelationshipCAB(rel.getOriginNid(), typeConNid, otherEndConNid, group, RelationshipType.getRelationshipType(refNid, charNid), rel, WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			} else {
				dcab = new RelationshipCAB(otherEndConNid, typeConNid, rel.getDestinationNid(), group, RelationshipType.getRelationshipType(refNid, charNid), rel, WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
			}

			RelationshipChronicleBI dcbi = WBUtility.getBuilder().constructIfNotCurrent(dcab);
			
			if (!isDestination) {
				WBUtility.addUncommitted(rel.getOriginNid());
			} else {
				WBUtility.addUncommitted(otherEndConNid);
			}
		}
		catch (Exception e)
		{
			logger_.error("Error saving relationship", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected error", "There was an error saving the Relationship", e.getMessage(), this);
		}
	}
	

	@Override 
	protected boolean passesQA() {
		return true;
	}
}