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
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;

/**
 * {@link RelRow}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RelRow
{
	ConceptNode relationship;
	ConceptNode target;
	ChoiceBox<String> type;
	Node typeNode;
	TextField group;
	Node groupNode;
	
	private UpdateableBooleanBinding rowValid;
	SimpleStringProperty groupFieldInvalidReason_ = new SimpleStringProperty("");
	SimpleStringProperty typeFieldInvalidReason_ = new SimpleStringProperty("A Type selection is required");
	
	// TODO add validation of this type (unless drools covers it)
	SimpleStringProperty typeTargetInvalidReason_ = new SimpleStringProperty("Relationship type may not be same as relationship target");

	public RelRow()
	{
		ObservableList<SimpleDisplayConcept> dropDownOptions = FXCollections.observableArrayList();
		dropDownOptions.add(new SimpleDisplayConcept(WBUtility.getConceptVersion(Snomed.IS_A.getUuids()[0])));
		relationship = new ConceptNode(null, true, dropDownOptions, null);
		target = new ConceptNode(null, true);
		
		//TODO add validation icons / reasons
		type = new ChoiceBox<String>(FXCollections.observableArrayList("Role", "Qualifier"));
		type.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				
				String type = newValue.trim();
				
				if (type.length() == 0)
				{
					typeFieldInvalidReason_.set("A Type selection is required");
				}
				else
				{
					typeFieldInvalidReason_.set("");
				}
			}
		});
		
		typeNode = ErrorMarkerUtils.setupErrorMarker(type, typeFieldInvalidReason_);
		
		group = new TextField("0");
		group.setMinWidth(45.0);  //TODO figure out why this is needed
		group.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				
				String group = newValue.trim();
				
				if (group.length() == 0)
				{
					groupFieldInvalidReason_.set("A group is required");
				}
				else
				{
					try
					{
						Integer.parseInt(group);
						groupFieldInvalidReason_.set("");
					}
					catch (NumberFormatException e)
					{
						groupFieldInvalidReason_.set("The group field must be a number");
					}
				}
			}
		});
		
		groupNode = ErrorMarkerUtils.setupErrorMarker(group, groupFieldInvalidReason_);
		rowValid = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				bind(relationship.isValid(), target.isValid(), typeFieldInvalidReason_, groupFieldInvalidReason_);
			}
			@Override
			protected boolean computeValue()
			{
				return (relationship.isValid().get() && target.isValid().get() && 
						typeFieldInvalidReason_.get().length() == 0 && groupFieldInvalidReason_.get().length() == 0);
			}
		};
	}

	public BooleanBinding isValid()
	{
		return rowValid;
	}
	
	/**
	 * @return the relationship
	 */
	public ConceptNode getRelationshipNode()
	{
		return relationship;
	}
	
	public int getRelationshipNid()
	{
		return relationship.getConcept().getNid();
	}

	public ConceptNode getTargetNode()
	{
		return target;
	}
	
	public int getTargetNid()
	{
		return target.getConcept().getNid();
	}

	/**
	 * @return the type
	 */
	public Node getTypeNode()
	{
		return typeNode;
	}

	public RelationshipType getType()
	{
		if ("Role".equalsIgnoreCase(type.getSelectionModel().getSelectedItem()))
		{
			return RelationshipType.STATED_ROLE;
		}
		else
		{
			return RelationshipType.QUALIFIER;
		}
	}

	/**
	 * @return the group
	 */
	public Node getGroupNode()
	{
		return groupNode;
	}
	
	public int getGroup()
	{
		return Integer.parseInt(group.getText().trim());
	}
}
