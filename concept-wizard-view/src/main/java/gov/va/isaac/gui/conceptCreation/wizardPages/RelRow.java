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
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
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
	TextField group;
	
	private BooleanBinding rowValid;

	public RelRow()
	{
		relationship = new ConceptNode(null, true);
		target = new ConceptNode(null, true);
		
		//TODO add validation icons / reasons
		type = new ChoiceBox<String>(FXCollections.observableArrayList("Role", "Qualifier"));
		group = new TextField("0");
		
		rowValid = new BooleanBinding()
		{
			{
				bind(relationship.isValid(), target.isValid(), type.valueProperty(), group.textProperty());
			}
			@Override
			protected boolean computeValue()
			{
				//TODO validate group
				return (relationship.isValid().get() && target.isValid().get() && type.getSelectionModel().getSelectedItem() != null);
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
	public ChoiceBox<String> getTypeNode()
	{
		return type;
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
	public TextField getGroupNode()
	{
		return group;
	}
	
	public int getGroup()
	{
		return Integer.parseInt(group.getText().trim());
	}
}
