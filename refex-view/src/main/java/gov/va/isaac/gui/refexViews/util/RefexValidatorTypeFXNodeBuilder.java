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
package gov.va.isaac.gui.refexViews.util;

import gov.va.isaac.gui.ConceptNode;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.NumberUtilities;
import gov.va.isaac.util.UpdateableBooleanBinding;
import gov.va.isaac.util.WBUtility;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicNid;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;

/**
 * {@link RefexValidatorTypeFXNodeBuilder}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexValidatorTypeFXNodeBuilder
{
	public static RefexValidatorTypeNodeDetails buildNodeForType(RefexDynamicValidatorType dt, RefexDynamicDataBI currentValue, UpdateableBooleanBinding allValid)
	{
		RefexValidatorTypeNodeDetails returnValue = new RefexValidatorTypeNodeDetails();
		if (RefexDynamicValidatorType.GREATER_THAN == dt || RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL == dt || RefexDynamicValidatorType.INTERVAL == dt
				|| RefexDynamicValidatorType.LESS_THAN == dt || RefexDynamicValidatorType.LESS_THAN_OR_EQUAL == dt || RefexDynamicValidatorType.REGEXP == dt)
		{
			TextField tf = new TextField();

			SimpleStringProperty valueInvalidReason = new SimpleStringProperty("");
			returnValue.boundToAllValid.add(valueInvalidReason);
			allValid.addBinding(valueInvalidReason);
			Node n = ErrorMarkerUtils.setupErrorMarker(tf, valueInvalidReason);

			tf.textProperty().addListener(new ChangeListener<String>()
			{
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
				{
					if (newValue.length() == 0)
					{
						valueInvalidReason.setValue("You must specify a value for this field");
					}
					else if (RefexDynamicValidatorType.GREATER_THAN == dt || RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL == dt
							|| RefexDynamicValidatorType.LESS_THAN == dt || RefexDynamicValidatorType.LESS_THAN_OR_EQUAL == dt)
					{
						try
						{
							returnValue.validatorData.set(NumberUtilities.wrapIntoRefexHolder(NumberUtilities.parseNumber(tf.getText().trim())));
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set(e.getMessage());
							returnValue.validatorData.set(null);
						}
					}
					else if (RefexDynamicValidatorType.INTERVAL == dt)
					{
						try
						{
							//Don't actually care if it passes the validator - just want to know if the validator can parse it.
							RefexDynamicString s = new RefexDynamicString(tf.getText());
							RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(0), s, null);
							returnValue.validatorData.set(s);
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set(e.getMessage());
							returnValue.validatorData.set(null);
						}
					}
					else if (RefexDynamicValidatorType.REGEXP == dt)
					{
						try
						{
							//Don't actually care if it passes the validator - just want to know if the validator can parse it.
							RefexDynamicString s = new RefexDynamicString(tf.getText());
							RefexDynamicValidatorType.REGEXP.passesValidator(new RefexDynamicString("a"), s, null);
							returnValue.validatorData.set(s);
							valueInvalidReason.set("");
						}
						catch (Exception e)
						{
							valueInvalidReason.set(e.getMessage());
							returnValue.validatorData.set(null);
						}
					}
					else
					{
						throw new RuntimeException("oops");
					}
				}
			});

			if (currentValue != null)
			{
				tf.setText(currentValue.getDataObject().toString());
			}
			if (currentValue == null)
			{
				valueInvalidReason.setValue("You must specify a value for this field");
			}
			returnValue.nodeForDisplay = n;
		}
		else if (RefexDynamicValidatorType.IS_CHILD_OF == dt || RefexDynamicValidatorType.IS_KIND_OF == dt)
		{
			ConceptNode cn = new ConceptNode(null, true);
			returnValue.boundToAllValid.add(cn.isValid().getReasonWhyInvalid());
			allValid.addBinding(cn.isValid().getReasonWhyInvalid());

			if (currentValue != null)
			{
				cn.set(WBUtility.getConceptVersion(((RefexDynamicNidBI) currentValue).getDataNid()));
			}

			returnValue.nodeForDisplay = cn.getNode();
			
			cn.getConceptProperty().addListener((change) ->
			{
				if (cn.getConceptProperty().get() == null)
				{
					returnValue.validatorData.set(null);
				}
				else
				{
					try
					{
						returnValue.validatorData.set(new RefexDynamicNid(cn.getConceptProperty().getValue().getNid()));
					}
					catch (Exception e)
					{
						returnValue.validatorData.set(null);
						cn.isValid().setInvalid("Unexpected error parsing concept");
					}
				}
			});
		}
		else if (RefexDynamicValidatorType.DROOLS == dt)
		{
			throw new RuntimeException("Not yet implemented");
		}
		else
		{
			throw new RuntimeException("Unexpected datatype " + dt);
		}
		return returnValue;
	}
}
