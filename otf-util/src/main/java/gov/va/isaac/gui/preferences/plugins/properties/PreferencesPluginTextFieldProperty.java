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
package gov.va.isaac.gui.preferences.plugins.properties;

import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.util.ValidBooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * PreferencesPluginTextFieldProperty
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class PreferencesPluginTextFieldProperty extends PreferencesPluginProperty<String, TextField> {
	private Logger logger = LoggerFactory.getLogger(PreferencesPluginTextFieldProperty.class);
	
	/**
	 * @param label
	 * @param control
	 * @param property
	 * @param validator
	 * @param stringConverter
	 * @param binder
	 * @param controlPersistedValueSetter
	 * @param guiFormattingApplicator
	 */
	public PreferencesPluginTextFieldProperty(
			Label label,
			TextField control,
			Property<String> property,
			ValidBooleanBinding validator,
			StringConverter<String> stringConverter,
			PropertyAction<String, TextField> binder,
			PropertyAction<String, TextField> controlPersistedValueSetter,
			PropertyAction<String, TextField> guiFormattingApplicator) {
		super(
				label, 
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
	 * @param controlPersistedValueSetter
	 * @param guiFormattingApplicator
	 */
	public PreferencesPluginTextFieldProperty(
			String name,
			TextField control,
			Property<String> property,
			ValidBooleanBinding validator,
			StringConverter<String> stringConverter,
			PropertyAction<String, TextField> binder,
			PropertyAction<String, TextField> controlPersistedValueSetter,
			PropertyAction<String, TextField> guiFormattingApplicator) {
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
	
	public PreferencesPluginTextFieldProperty(String name, boolean emptyStringAllowed) {
		this(new Label(name), emptyStringAllowed);
	}

	public PreferencesPluginTextFieldProperty(String name) {
		this(new Label(name), false);
	}

	public PreferencesPluginTextFieldProperty(Label label, boolean emptyStringAllowed) {
		super(
				label, 
				new TextField(), 
				new SimpleStringProperty(), 
				null, // validator handled below
				new StringConverter<String>() {
					@Override
					public String convertToString(String value) {
						return value != null ? value.toString() : null;
					}
				}, 
				new PropertyAction<String, TextField>() {
					@Override
					public void apply(PreferencesPluginProperty<String, TextField> property) {
						property.getProperty().bind(property.getControl().textProperty());
					}	
				},
				new PropertyAction<String, TextField>() {
					@Override
					public void apply(PreferencesPluginProperty<String, TextField> property) {
						property.getControl().textProperty().set(property.readFromPersistedPreferences());
					}
				}, 
				new PropertyAction<String, TextField>() {
					@Override
					public void apply(PreferencesPluginProperty<String, TextField> property) {
						GridPane.setHgrow(property.getLabel(), Priority.NEVER);
						GridPane.setFillWidth(property.getControl(), true);
						GridPane.setHgrow(property.getControl(), Priority.ALWAYS);
					}
				});
		validator = new ValidBooleanBinding() {
			{
				bind(getProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue() {
				if (!emptyStringAllowed && StringUtils.isBlank(getProperty().getValue())) {
					this.setInvalidReason("unspecified value for " + name);
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
