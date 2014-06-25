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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.conceptCreation.wizardPages;

import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;

/**
 * {@link TermRow}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class TermRow
{
	Node synonymNode;
	Node typeNode;
	SimpleStringProperty textFieldInvalidReason_ = new SimpleStringProperty("The Term is required");
	SimpleStringProperty typeFieldInvalidReason_ = new SimpleStringProperty("A Type selection is required");
	SimpleStringProperty termRowInvalidReason_ = new SimpleStringProperty("Must fill out all fields or none");

	TextField term;
	ChoiceBox<String> type;
	CheckBox initialCaseSignificant;
	
	private UpdateableBooleanBinding rowValid;
	
	public TermRow()
	{
		term = new TextField();
		term.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				
				String term = newValue.trim();
				
				if (term.length() == 0)
				{
					textFieldInvalidReason_.set("The Term is required");
				}
				else
				{
					int frontParenCount = countChar(term, "(");
					int backParenCount = countChar(term, ")");
	
					if (frontParenCount != 0 || backParenCount != 0)
					{
						//really?  That seems like an completely silly restriction.
						textFieldInvalidReason_.set("Cannot have parenthesis in synonym or it may be confused with the FSN");
						return;
					}
					else
					{
						textFieldInvalidReason_.set("");
					}
				}
			}
		});
		synonymNode = ErrorMarkerUtils.setupErrorMarker(term, textFieldInvalidReason_);
		
		type = new ChoiceBox<>(FXCollections.observableArrayList("", "Synonym", "Definition"));
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
		
		initialCaseSignificant = new CheckBox();
		initialCaseSignificant.minHeightProperty().bind(term.heightProperty());
		
		rowValid = new UpdateableBooleanBinding()
		{
			{	
				setComputeOnInvalidate(true);
				bind(textFieldInvalidReason_, typeFieldInvalidReason_);
			}
			@Override
			protected boolean computeValue()
			{
				return (textFieldInvalidReason_.get().length() == 0 && typeFieldInvalidReason_.get().length() == 0);
			}
		};
	}
	
	public BooleanBinding isValid()
	{
		return rowValid;
	}
	
	public Node getTermNode()
	{
		return synonymNode;
	}
	
	public Node getTypeNode()
	{
		return typeNode;
	}
	
	public String getTypeString()
	{
		return type.getValue();
	}
	
	public int getTypeNid()
	{
		try
		{
			if ("Synonym".equals(type.getSelectionModel().getSelectedItem()))
			{
				return SnomedMetadataRf2.SYNONYM_RF2.getNid();
			}
			else if ("Definition".equals(type.getSelectionModel().getSelectedItem()))
			{
				return Snomed.DEFINITION_DESCRIPTION_TYPE.getNid();
			}
			else
			{
				return -1;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}
	
	public Node getInitalCaseSigNode()
	{
		return initialCaseSignificant;
	}
	
	public boolean isInitialCaseSig()
	{
		return initialCaseSignificant.isSelected();
	}
	
	public String getTerm()
	{
		return term.getText();
	}
	
	

	


	private int countChar(String str, String c)
	{
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(c, idx)) != -1)
		{
			count++;
			idx += c.length();
		}
		return count;
	}
//	};
///*		langInvalidReason = new UpdateableStringBinding() 
//	{
//		@Override
//		protected String computeValue()
//		{
//			for (int i = 0; i < langVBox.getChildren().size(); i++)
//			{
//				// Check that not partially filled out
//				TextField tf = (TextField) langVBox.getChildren().get(i);
//				String lang = tf.getText().trim();
//				
//				String term = ((TextField)synonymVBox.getChildren().get(i)).getText().trim();
//				
//				if (!lang.isEmpty() && term.trim().isEmpty()) {
//					return "Cannot fill out Term and not Language";
//				} else if (!StringUtils.isAlpha(lang)) {
//					return "Language must be filled with only alphabetically letters";
//				} else if (lang.length() != 2) {
//					return "Language must be filled out with a 2-character string";
//				}
//			}
//
//			return "";
//		}
//	};
}
