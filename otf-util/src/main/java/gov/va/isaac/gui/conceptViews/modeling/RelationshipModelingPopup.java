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
import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;

import java.util.UUID;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.jfree.util.Log;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * RelationshipModelingPopup
 * 
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */

@Service
@PerLookup
public class RelationshipModelingPopup extends ModelingPopup
{
	private ConceptNode otherEndCon;
	private ConceptNode typeCon;
	private TextField groupNum;
	private ChoiceBox<SimpleDisplayConcept> refinabilityCon;
	private ChoiceBox<SimpleDisplayConcept> characteristicCon;
	private RelationshipVersionBI<?> rel;
	private boolean isDestination = false;
	private Label otherConcept;
	private SimpleBooleanProperty otherConceptNewSelected;
	private SimpleBooleanProperty typeNewSelected;
	private SimpleBooleanProperty refineNewSelected;
	private SimpleBooleanProperty charNewSelected;
	private SimpleBooleanProperty groupNewSelected;

	public RelationshipModelingPopup() {
		
	}
	
	public void setDestination(boolean isDestination) {
		this.isDestination  = isDestination;
	}
	
	@Override
	protected void finishInit()
	{
		rel = (RelationshipVersionBI<?>)origComp;

		ConceptVersionBI typeToSet = WBUtility.getConceptVersion(rel.getTypeNid());
		ConceptVersionBI otherConceptToSet;
		if (!isDestination) {
			otherConceptToSet = WBUtility.getConceptVersion(rel.getDestinationNid());
		} else {
			otherConceptToSet = WBUtility.getConceptVersion(rel.getOriginNid());
		}
		
		if (!rel.isUncommitted() || (rel.isUncommitted() && rel.getVersions().size() > 1)) {
			typeCon.set(typeToSet);
			typeCon.disableEdit();
			
			otherEndCon.set(otherConceptToSet);
			otherEndCon.disableEdit();
		} else {
			typeCon.set(typeToSet);
			otherEndCon.set(otherConceptToSet);
		}		

		groupNum.setText(String.valueOf(rel.getGroup()));
		
		refinabilityCon.getSelectionModel().select(new SimpleDisplayConcept(WBUtility.getConceptVersion(rel.getRefinabilityNid())));
		characteristicCon.getSelectionModel().select(new SimpleDisplayConcept(WBUtility.getConceptVersion(rel.getCharacteristicNid())));
		
		if (isDestination) {
			otherConcept.setText("Origin");
			title.setText("Modify Destination Relationship");
		}

		row = 0;

		try {
			ComponentChronicleBI<?> chronicle = rel.getChronicle();
			RelationshipVersionBI<?> displayVersion = (RelationshipVersionBI<?>) chronicle.getVersion(WBUtility.getViewCoordinate());

			if (chronicle.isUncommitted()) {
				displayVersion = (RelationshipVersionBI<?>) WBUtility.getLastCommittedVersion(chronicle);

			}

			// TODO: Needs to reference previous commit, not component as-is before panel opened
			if (isDestination) {
				createOriginalLabel(WBUtility.getConceptVersion(displayVersion.getOriginNid()).getPreferredDescription().getText());
			} else {
				createOriginalLabel(WBUtility.getConceptVersion(displayVersion.getDestinationNid()).getPreferredDescription().getText());
			}
			createOriginalLabel(WBUtility.getConceptVersion(displayVersion.getTypeNid()).getPreferredDescription().getText());
			createOriginalLabel(WBUtility.getConceptVersion(displayVersion.getRefinabilityNid()).getPreferredDescription().getText());
			createOriginalLabel(WBUtility.getConceptVersion(displayVersion.getCharacteristicNid()).getPreferredDescription().getText());
			createOriginalLabel(String.valueOf(displayVersion.getGroup()));
		} catch (Exception e) {
			Log.error("Cannot access Pref Term for attributes of relationship: "  + rel.getPrimordialUuid(), e);
		}

		setupGridPaneConstraints();
	}
	
	@Override 
	protected void setupTopItems(VBox topItems) {
		refinabilityCon  = new ChoiceBox<>();
		characteristicCon  = new ChoiceBox<>();
		otherEndCon = new ConceptNode(null, true);
		groupNum = new TextField();
		
		ObservableList<SimpleDisplayConcept> typeConDropDownOptions = FXCollections.observableArrayList();
		typeConDropDownOptions.add(new SimpleDisplayConcept(WBUtility.getConceptVersion(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))));
		typeCon = new ConceptNode(null, true, typeConDropDownOptions, null);

		otherConceptNewSelected = new SimpleBooleanProperty(false);
		typeNewSelected = new SimpleBooleanProperty(false);
		refineNewSelected = new SimpleBooleanProperty(false);
		charNewSelected = new SimpleBooleanProperty(false);
		groupNewSelected = new SimpleBooleanProperty(false);

		popupTitle = "Modify Source Relationship";

		setupGridPane(topItems);
		
		// Setup Type (Row #1)
		setupOtherCon();

		// Setup Type (Row #2)
		setupType();
		
		// Setup Refinability (Row #3)
		setupRefinability();
		
		// Setup Characteristic (Row #4)
		setupCharacteristic();
		
		// Group (Row #5)
		setupGroup();
	}

	private void setupGroup() {
		createTitleLabel("Group");

		groupNum.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				// Test new or different value
				if (rel != null) {
					if (!Integer.toString(rel.getGroup()).equals(newVal)) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else {
					if (newVal.trim().length() > 0) {
						groupNewSelected.set(true);
					} else {
						groupNewSelected.set(false);
					}
				}
				
				// Test valid value
				int newGroup = 0; 
				if (modificationMade.get() || groupNewSelected.get()) {
					try {
						newGroup = Integer.parseInt(newVal);
						if (newGroup < 0) {
							reasonSaveDisabled_.set("Group must be 0 or greater");
						}
					} catch (NumberFormatException e) {
						reasonSaveDisabled_.set("Must select an integer");
					} 
				}
			}
		});
		gp_.add(groupNum, 2, row);
		row++;
	}

	private void setupCharacteristic() {
		createTitleLabel("Characteristic");

		if (rel == null) {
			characteristicCon.getItems().add(null);
		}
		characteristicCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2));
		characteristicCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2));
		characteristicCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2));
		characteristicCon.valueProperty().addListener(new ChangeListener<SimpleDisplayConcept>() {
			@Override
			public void changed(ObservableValue<? extends SimpleDisplayConcept> ov, SimpleDisplayConcept oldVal, SimpleDisplayConcept newVal) {
				if (rel != null) {
					if (rel.getCharacteristicNid() != newVal.getNid()) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (newVal != null) {
					charNewSelected.set(true);
				} else {
					charNewSelected.set(false);
				}

			}
		});

		gp_.add(characteristicCon, 2, row);
		row++;
	}

	private void setupRefinability() {
		createTitleLabel("Refinability");
		
		if (rel == null) {
			refinabilityCon.getItems().add(null);
		}
		refinabilityCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.NOT_REFINABLE_RF2));
		refinabilityCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2));
		refinabilityCon.getItems().add(new SimpleDisplayConcept(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2));
		refinabilityCon.valueProperty().addListener(new ChangeListener<SimpleDisplayConcept>() {
			@Override
			public void changed(ObservableValue<? extends SimpleDisplayConcept> ov, SimpleDisplayConcept oldVal, SimpleDisplayConcept newVal) {
				if (rel != null) {
					if (rel.getRefinabilityNid() != newVal.getNid()) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (newVal != null) {
					refineNewSelected.set(true);
				} else {
					refineNewSelected.set(false);
				}

			}
		});
		gp_.add(refinabilityCon, 2, row);
		row++;
	}

	private void setupType() {
		createTitleLabel("Type");

		typeCon.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> ov, ConceptVersionBI oldVal, ConceptVersionBI newVal) {
				if (rel != null) {
					if (rel.getTypeNid() != newVal.getNid()) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (newVal != null) {
					typeNewSelected.set(true);
				} else {
					typeNewSelected.set(false);
				}
					
				if (modificationMade.get() || typeNewSelected.get()) {
					if (!typeCon.isValid().getValue()) {
						reasonSaveDisabled_.set(typeCon.isValid().getReasonWhyInvalid().getValue());
					}
				}
			}
		});
		gp_.add(typeCon.getNode(), 2, row);
		row++;
	}

	private void setupOtherCon() {
		createTitleLabel("Destination");

		otherEndCon.getConceptProperty().addListener(new ChangeListener<ConceptVersionBI>() {
			@Override
			public void changed(ObservableValue<? extends ConceptVersionBI> ov, ConceptVersionBI oldVal, ConceptVersionBI newVal) {
				if (rel != null) {
					if ((!isDestination && rel.getDestinationNid() != newVal.getNid()) ||
						(isDestination && rel.getOriginNid() != newVal.getNid())) {
						modificationMade.set(true);
					} else {
						modificationMade.set(false);
						reasonSaveDisabled_.set("Cannot save unless original content changed");
					}
				} else if (newVal != null) {
					otherConceptNewSelected.set(true);
				} else {
					otherConceptNewSelected.set(false);
				}
					
				if (modificationMade.get() || otherConceptNewSelected.get()) {
					if (!otherEndCon.isValid().getValue()) {
						reasonSaveDisabled_.set(otherEndCon.isValid().getReasonWhyInvalid().getValue());
					}
				}
			}
		});
		gp_.add(otherEndCon.getNode(), 2, row);
		row++;
	}

	@Override 
	protected void setupValidations() {
		allValid_ = new UpdateableBooleanBinding()
		{
			{
				if (rel != null) {
					addBinding(modificationMade);
				} else {
					addBinding(otherConceptNewSelected, typeNewSelected, refineNewSelected, charNewSelected, groupNewSelected);
				}
			}

			@Override
			protected boolean computeValue()
			{
				if ((rel != null && modificationMade.get()) ||
					(rel == null && otherConceptNewSelected.get() && typeNewSelected.get() && refineNewSelected.get() && charNewSelected.get() && groupNewSelected.get())) 
					{
						reasonSaveDisabled_.set("");
						return true;
					}

				reasonSaveDisabled_.set("Cannot create new relationship until all values are specified");
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

			
			if (rel == null) {
				if (!isDestination) {
					WBUtility.createNewRelationship((rel != null) ? rel.getOriginNid() : conceptNid, typeConNid, otherEndConNid, group, RelationshipType.getRelationshipType(refNid, charNid));
				} else {
					WBUtility.createNewRelationship(otherEndConNid, typeConNid, (rel != null) ? rel.getDestinationNid() : conceptNid, group, RelationshipType.getRelationshipType(refNid, charNid));
				}
			} else {
				RelationshipCAB dcab;

				if (rel.isUncommitted()) {
					ExtendedAppContext.getDataStore().forget(rel);
				}
			
				if (!isDestination) {
					dcab = new RelationshipCAB((rel != null) ? rel.getOriginNid() : conceptNid, typeConNid, otherEndConNid, group, RelationshipType.getRelationshipType(refNid, charNid), rel, WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
				} else {
					dcab = new RelationshipCAB(otherEndConNid, typeConNid, (rel != null) ? rel.getDestinationNid() : conceptNid, group, RelationshipType.getRelationshipType(refNid, charNid), rel, WBUtility.getViewCoordinate(), IdDirective.PRESERVE, RefexDirective.EXCLUDE);
				}
	
				WBUtility.getBuilder().constructIfNotCurrent(dcab);
				
				if (!isDestination) {
					WBUtility.addUncommitted(rel.getOriginNid());
				} else {
					WBUtility.addUncommitted(otherEndConNid);
				}
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
		int charNid = characteristicCon.getSelectionModel().getSelectedItem().getNid();
		int refinNid = refinabilityCon.getSelectionModel().getSelectedItem().getNid();
		String errMsg = null;
		
		try {
			if (charNid == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getNid()) {
				if (refinNid != SnomedMetadataRf2.NOT_REFINABLE_RF2.getNid() && refinNid != SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getNid()) {
					errMsg = "Cannot have STATED Characteristic with Refinability other than NOT_REFINABLE or OPTIONAL_REFINABLE";
				}
			} else if (charNid == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getNid()) {
				if (refinNid != SnomedMetadataRf2.NOT_REFINABLE_RF2.getNid() && refinNid != SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getNid()) {
					errMsg = "Cannot have INFERRED Characteristic with Refinability other than NOT_REFINABLE or OPTIONAL_REFINABLE";
				}
			} else if (charNid == SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getNid()) {
				if (refinNid != SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getNid()) {
					errMsg = "Cannot have QUALIFYING Characteristic with Refinability other than MANDATORY";
				}
			} else if (charNid == SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getNid()) {
				if (refinNid != SnomedMetadataRf2.NOT_REFINABLE_RF2.getNid()) {
					errMsg = "Cannot have HISTORICAL Characteristic with Refinability other than NOT_REFINABLE";
				}
			}
		} catch (Exception e) {
			LOG.error("Cannot access basic char/refin values from database" , e);
		}
		
		if (errMsg != null) {
			AppContext.getCommonDialogs().showInformationDialog("Relationship failed QA", errMsg);
			return false;
		}
		
		return true;
	}
}