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
package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.UpdateableBooleanBinding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.LoggerFactory;

/**
 * {@link RefsetValueUtils}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefsetValueUtils
{
	public enum RefsetType{rfBoolean("boolean"), rfConcept("concept"), rfInteger("integer"), rfString("string");
		
		private String niceName;
		
		RefsetType(String name)
		{
			this.niceName = name;
		}

		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString()
		{
			return niceName;
		}
	}
	
	
	private ComboBox<RefsetType> refsetType_ = new ComboBox<>();
	
	private Label refsetTypeLabel_ = new Label("Refset type");
	private Label valueLabel_ = new Label();
	private ChoiceBox<String> booleanValue_ = new ChoiceBox<>();
	private ConceptNode conceptValue_ = new ConceptNode(null, true);
	private TextField stringOrIntegerValue_ = new TextField();
	private StringProperty stringOrIntegerErrorMessage_ = new SimpleStringProperty();
	private Node wrappedStringOrIntegerValue_ = ErrorMarkerUtils.setupErrorMarker(stringOrIntegerValue_, stringOrIntegerErrorMessage_);
	
	private StackPane swappable = new StackPane();
	
	private UpdateableBooleanBinding isValid_;
	
	protected RefsetValueUtils(boolean indent)
	{
		if (indent)
		{
			refsetTypeLabel_.setPadding(new Insets(0, 0, 0, 10));
			valueLabel_.setPadding(new Insets(0, 0, 0, 10));
		}
		
		booleanValue_.getItems().add("True");
		booleanValue_.getItems().add("False");
		booleanValue_.getSelectionModel().select(0);
		
		for (RefsetType rt : RefsetType.values())
		{
			refsetType_.getItems().add(rt);
		}

		refsetType_.getSelectionModel().select(0);
		
		swappable.getChildren().add(booleanValue_);
		booleanValue_.setMaxWidth(Double.MAX_VALUE);
		swappable.getChildren().add(conceptValue_.getNode());
		swappable.getChildren().add(wrappedStringOrIntegerValue_);
		stringOrIntegerValue_.setMaxWidth(Double.MAX_VALUE);
		
		isValid_ = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				if (refsetType_.getValue() == RefsetType.rfConcept)
				{
					return conceptValue_.isValid().get();
				}
				else if (refsetType_.getValue() == RefsetType.rfInteger || refsetType_.getValue() == RefsetType.rfString)
				{
					if (StringUtils.isBlank(stringOrIntegerValue_.getText()))
					{
						stringOrIntegerErrorMessage_.set(refsetType_.getValue() == RefsetType.rfInteger ? "An integer must be provided" : "A String must be provided");
						return false;
					}
					if (refsetType_.getValue() == RefsetType.rfInteger)
					{
						try
						{
							Integer.parseInt(stringOrIntegerValue_.getText());
							stringOrIntegerErrorMessage_.set("");
							return true;
						}
						catch (NumberFormatException e)
						{
							stringOrIntegerErrorMessage_.set("The value must be an integer");
							return false;
						}
					}
					else if (refsetType_.getValue() == RefsetType.rfString)
					{
						stringOrIntegerErrorMessage_.set("");
						return true;
					}
					else
					{
						LoggerFactory.getLogger(this.getClass()).error("Design error!");
						return false;
					}
				}
				else if (refsetType_.getValue() == RefsetType.rfBoolean)
				{
					return true;
				}
				else
				{
					LoggerFactory.getLogger(this.getClass()).error("Design error!");
					return false;
				}
			}
		};
		
		refsetType_.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				isValid_.clearBindings();
				booleanValue_.setVisible(false);
				conceptValue_.getNode().setVisible(false);
				wrappedStringOrIntegerValue_.setVisible(false);
				
				if (refsetType_.getValue() == RefsetType.rfString || refsetType_.getValue() == RefsetType.rfInteger)
				{
					isValid_.addBinding(stringOrIntegerValue_.textProperty());
					valueLabel_.setText(refsetType_.getValue() == RefsetType.rfString ? "String value" : "Integer value");
					wrappedStringOrIntegerValue_.setVisible(true);
				}
				else if (refsetType_.getValue() == RefsetType.rfConcept)
				{
					isValid_.addBinding(conceptValue_.isValid());
					valueLabel_.setText("Concept value");
					conceptValue_.getNode().setVisible(true);
				}
				else if (refsetType_.getValue() == RefsetType.rfBoolean)
				{
					valueLabel_.setText("Boolean value");
					booleanValue_.setVisible(true);
				}
				else
				{
					LoggerFactory.getLogger(this.getClass()).error("Design error!");
				}
				isValid_.invalidate();
			}
		});
		refsetType_.getSelectionModel().select(1);
	}
	
	protected BooleanBinding isValid()
	{
		return isValid_;
	}
	
	protected void setup(GridPane gp, int startRow)
	{
		gp.add(refsetTypeLabel_, 0, startRow);
		gp.add(refsetType_, 1, startRow);
		refsetType_.setMaxWidth(Double.MAX_VALUE);
		
		gp.add(valueLabel_, 0, startRow + 1);
		gp.add(swappable, 1, startRow + 1);
		swappable.setMaxWidth(Double.MAX_VALUE);
	}
	
	protected void remove(GridPane gp)
	{
		gp.getChildren().remove(refsetTypeLabel_);
		gp.getChildren().remove(refsetType_);
		gp.getChildren().remove(valueLabel_);
		gp.getChildren().remove(swappable);
	}
	
	protected RefsetType getRefsetType()
	{
		return refsetType_.getValue();
	}
	
	protected int getIntegerValue()
	{
		return Integer.parseInt(stringOrIntegerValue_.getText());
	}
	
	protected String getStringValue()
	{
		return stringOrIntegerValue_.getText();
	}
	
	protected boolean getBooleanValue()
	{
		return booleanValue_.getValue().equals("True");
	}
	
	protected ConceptVersionBI getConceptValue()
	{
		return conceptValue_.getConcept();
	}
}
