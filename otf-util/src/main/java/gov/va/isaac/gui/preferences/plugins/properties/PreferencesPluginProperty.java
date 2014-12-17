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
package gov.va.isaac.gui.preferences.plugins.properties;

import gov.va.isaac.config.profiles.UserProfile;
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

	final PropertyAction<T, C> binder;

	final PropertyAction<T, C> controlPersistedValueSetter;

	final PropertyAction<T, C> guiFormattingApplicator;

	// Helper methods
	public void bindPropertyToControl() {
		if (binder != null) {
			binder.apply(this);
		}
	}
	public void setControlPersistedValue() {
		if (controlPersistedValueSetter != null) {
			controlPersistedValueSetter.apply(this);
		}
	}
	public void applyGuiFormatting() {
		if (guiFormattingApplicator != null) {
			guiFormattingApplicator.apply(this);
		}
	}
	public String getStringValueOfDefault() {
		return getStringConverter().convertToString(readFromDefaults());
	}
	
	/**
	 * @return T reads property value from persisted preferences
	 * 
	 * This method is not meant to interact with writeToUnpersistedPreferences(UserProfile userProfile)
	 * It is meant solely to initialize the Control,
	 * so there is no need for it to read from a UserProfile that may have been changed in memory
	 */
	public abstract T readFromPersistedPreferences();
	
	public abstract T readFromDefaults();
	
	/**
	 * @param userProfile
	 * 
	 * This method is not meant to interact with readFromPersistedPreferences().
	 * It is meant solely to be used just before persisting the entire preferences file,
	 * so before any property values are written for a given plugin,
	 * the entire property file should be loaded in order to ensure that values previously modified
	 * by other plugins are not overwritten with stale data loaded earlier by this one.
	 */
	public abstract void writeToUnpersistedPreferences(UserProfile userProfile);

	/**
	 * @param name
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlPersistedValueSetter
	 * @param guiFormattingApplicator
	 */
	protected PreferencesPluginProperty(String name,
			C control,
			Property<T> property,
			ValidBooleanBinding validator,
			StringConverter<T> stringConverter,
			PropertyAction<T, C> binder,
			PropertyAction<T, C> controlPersistedValueSetter,
			PropertyAction<T, C> guiFormattingApplicator) {
		this(
				new Label(name),
				control,
				property,
				validator,
				stringConverter,
				binder,
				controlPersistedValueSetter,
				guiFormattingApplicator);
	}

	/**
	 * @param label
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlPersistedPreferenceValueSetter
	 * @param guiFormattingApplicator
	 */
	protected PreferencesPluginProperty(Label label,
			C control,
			Property<T> property,
			ValidBooleanBinding validator,
			StringConverter<T> stringConverter,
			PropertyAction<T, C> binder,
			PropertyAction<T, C> controlPersistedPreferenceValueSetter,
			PropertyAction<T, C> guiFormattingApplicator) {
		super();
		this.name = label.getText();
		this.label = label;
		this.control = control;
		this.property = property;
		this.validator = validator;
		this.stringConverter = stringConverter;
		this.binder = binder;
		this.controlPersistedValueSetter = controlPersistedPreferenceValueSetter;
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
	 * @return the controlPersistedValueSetter
	 */
	public PropertyAction<T, C> getControlPersistedPreferenceValueSetter() {
		return controlPersistedValueSetter;
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