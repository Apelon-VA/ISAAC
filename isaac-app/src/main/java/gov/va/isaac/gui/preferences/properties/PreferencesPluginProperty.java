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
 * PreferencesPluginProperty
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences.properties;

import gov.va.isaac.util.ValidBooleanBinding;
import javafx.beans.property.Property;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

/**
 * PreferencesPluginProperty
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class PreferencesPluginProperty<T, C extends Control> {
	public interface PropertyAction<T, C extends Control> {
		void apply(PreferencesPluginProperty<T, C> property);
	}
	public static interface StringConverter<T> {
		String convertToString(T value);
	}

	final String name; // used for label

	final Label label;
	final C control; // used for entry or display

	final Property<T> property; // property for maintaining value

	protected ValidBooleanBinding validator;

	final StringConverter<T> stringConverter;

	//final PropertyAndControl<T, C> binder;
	final PropertyAction<T, C> binder;

	final PropertyAction<T, C> controlCurrentValueSetter;

	final PropertyAction<T, C> guiFormattingApplicator;

	public abstract T readFromPreferences();
	public abstract T readFromDefaults();
	public abstract void writeToPreferences();

	/**
	 * @param name
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlCurrentValueSetter
	 * @param guiFormattingApplicator
	 */
	public PreferencesPluginProperty(String name,
			C control,
			Property<T> property,
			ValidBooleanBinding validator,
			StringConverter<T> stringConverter,
			PropertyAction<T, C> binder,
			PropertyAction<T, C> controlCurrentValueSetter,
			PropertyAction<T, C> guiFormattingApplicator) {
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

	/**
	 * @param label
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlCurrentPreferenceValueSetter
	 * @param guiFormattingApplicator
	 */
	public PreferencesPluginProperty(Label label,
			C control,
			Property<T> property,
			ValidBooleanBinding validator,
			StringConverter<T> stringConverter,
			PropertyAction<T, C> binder,
			PropertyAction<T, C> controlCurrentPreferenceValueSetter,
			PropertyAction<T, C> guiFormattingApplicator) {
		super();
		this.name = label.getText();
		this.label = label;
		this.control = control;
		this.property = property;
		this.validator = validator;
		this.stringConverter = stringConverter;
		this.binder = binder;
		this.controlCurrentValueSetter = controlCurrentPreferenceValueSetter;
		this.guiFormattingApplicator = guiFormattingApplicator;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the label
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * @return the control
	 */
	public C getControl() {
		return control;
	}

	/**
	 * @return the property
	 */
	public Property<T> getProperty() {
		return property;
	}

	/**
	 * @return the validator
	 */
	public ValidBooleanBinding getValidator() {
		return validator;
	}
	/**
	 * @param validator
	 */
	public void setValidator(ValidBooleanBinding validator) {
		this.validator = validator;
	}
	
	/**
	 * @return the stringConverter
	 */
	public StringConverter<T> getStringConverter() {
		if (stringConverter == null) {
			return new StringConverter<T>() {
				@Override
				public String convertToString(T value) {
					return value != null ? value.toString() : null;
				}
			};
		}

		return stringConverter;
	}

	/**
	 * @return the binder
	 */
	public PropertyAction<T, C> getBinder() {
		return binder;
	}

	/**
	 * @return the controlCurrentValueSetter
	 */
	public PropertyAction<T, C> getControlCurrentPreferenceValueSetter() {
		return controlCurrentValueSetter;
	}
	/**
	 * @return the guiFormattingApplicator
	 */
	public PropertyAction<T, C> getGuiFormattingApplicator() {
		//			GridPane.setHgrow(property.getLabel(), Priority.NEVER);
		//			GridPane.setFillWidth(property.getControl(), true);
		//			GridPane.setHgrow(property.getControl(), Priority.ALWAYS);
		return guiFormattingApplicator;
	}
}