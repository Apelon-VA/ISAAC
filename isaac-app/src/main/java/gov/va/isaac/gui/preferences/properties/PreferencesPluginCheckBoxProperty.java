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

/**
 * PreferencesPluginTextFieldProperty
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences.properties;

import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.util.ValidBooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * PreferencesPluginCheckBoxProperty
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class PreferencesPluginCheckBoxProperty extends PreferencesPluginProperty<Boolean, CheckBox> {
	private Logger logger = LoggerFactory.getLogger(PreferencesPluginCheckBoxProperty.class);
	
	/**
	 * @param label
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlCurrentValueSetter
	 * @param guiFormattingApplicator
	 */
	public PreferencesPluginCheckBoxProperty(
			Label label,
			CheckBox control,
			Property<Boolean> property,
			ValidBooleanBinding validator,
			StringConverter<Boolean> stringConverter,
			PropertyAction<Boolean, CheckBox> binder,
			PropertyAction<Boolean, CheckBox> controlCurrentValueSetter,
			PropertyAction<Boolean, CheckBox> guiFormattingApplicator) {
		super(
				label, 
				control, 
				property, 
				validator, 
				stringConverter, 
				binder,
				controlCurrentValueSetter, 
				guiFormattingApplicator);
	}
	
	/**
	 * @param label
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlCurrentValueSetter
	 * @param guiFormattingApplicator
	 */
	public PreferencesPluginCheckBoxProperty(
			String name,
			CheckBox control,
			Property<Boolean> property,
			ValidBooleanBinding validator,
			StringConverter<Boolean> stringConverter,
			PropertyAction<Boolean, CheckBox> binder,
			PropertyAction<Boolean, CheckBox> controlCurrentValueSetter,
			PropertyAction<Boolean, CheckBox> guiFormattingApplicator) {
		this(
				new Label(name),
				control, 
				property, 
				validator, 
				stringConverter, 
				binder,
				controlCurrentValueSetter, 
				guiFormattingApplicator);
	}

	public PreferencesPluginCheckBoxProperty(String name) {
		this(new Label(name));
	}

	public PreferencesPluginCheckBoxProperty(Label label) {
		super(
				label, 
				new CheckBox(), 
				new SimpleBooleanProperty(), 
				null, // validator handled below
				new StringConverter<Boolean>() {
					@Override
					public String convertToString(Boolean value) {
						return value != null ? value.toString() : null;
					}
				}, 
				new PropertyAction<Boolean, CheckBox>() {
					@Override
					public void apply(PreferencesPluginProperty<Boolean, CheckBox> property) {
						property.getProperty().bind(property.getControl().selectedProperty());
					}	
				},
				new PropertyAction<Boolean, CheckBox>() {
					@Override
					public void apply(PreferencesPluginProperty<Boolean, CheckBox> property) {
						property.getControl().selectedProperty().set(property.readFromPreferences());
					}
				}, 
				new PropertyAction<Boolean, CheckBox>() {
					@Override
					public void apply(PreferencesPluginProperty<Boolean, CheckBox> property) {
						GridPane.setHgrow(property.getLabel(), Priority.NEVER);
						GridPane.setFillWidth(property.getControl(), true);
						GridPane.setHgrow(property.getControl(), Priority.NEVER);
					}
				});
		validator = new ValidBooleanBinding() {
			{
				bind(getProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue() {
				if (getProperty().getValue() == null) {
					this.setInvalidReason("Null/unset/unselected " + name);
					logger.debug(getReasonWhyInvalid().get());

					TextErrorColorHelper.setTextErrorColor(label);

					return false;
				} else {
					TextErrorColorHelper.clearTextErrorColor(label);
				}

				this.clearInvalidReason();

				return true;
			}
		};
	}
}
