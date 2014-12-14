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

import gov.va.isaac.util.ValidBooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * PreferencesPluginTextFieldProperty
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class PreferencesPluginLabelProperty extends PreferencesPluginProperty<String, Label> {
	//private Logger logger = LoggerFactory.getLogger(PreferencesPluginLabelProperty.class);
	
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
	public PreferencesPluginLabelProperty(
			Label label,
			Label control,
			Property<String> property,
			ValidBooleanBinding validator,
			StringConverter<String> stringConverter,
			PropertyAction<String, Label> binder,
			PropertyAction<String, Label> controlCurrentValueSetter,
			PropertyAction<String, Label> guiFormattingApplicator) {
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
	public PreferencesPluginLabelProperty(
			String name,
			Label control,
			Property<String> property,
			ValidBooleanBinding validator,
			StringConverter<String> stringConverter,
			PropertyAction<String, Label> binder,
			PropertyAction<String, Label> controlCurrentValueSetter,
			PropertyAction<String, Label> guiFormattingApplicator) {
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

	public PreferencesPluginLabelProperty(String name) {
		this(new Label(name));
	}

	public PreferencesPluginLabelProperty(Label label) {
		super(
				label, 
				new Label(), 
				new SimpleStringProperty(), 
				null, // validator handled below
				new StringConverter<String>() {
					@Override
					public String convertToString(String value) {
						return value;
					}
				}, 
				new PropertyAction<String, Label>() {
					@Override
					public void apply(PreferencesPluginProperty<String, Label> property) {
						property.getProperty().bind(property.getControl().textProperty());
					}	
				},
				new PropertyAction<String, Label>() {
					@Override
					public void apply(PreferencesPluginProperty<String, Label> property) {
						property.getControl().textProperty().set(property.readFromPreferences());
					}
				}, 
				new PropertyAction<String, Label>() {
					@Override
					public void apply(PreferencesPluginProperty<String, Label> property) {
						GridPane.setHgrow(property.getLabel(), Priority.NEVER);
						GridPane.setFillWidth(property.getControl(), true);
						GridPane.setHgrow(property.getControl(), Priority.NEVER);
					}
				});
		validator = new ValidBooleanBinding() {
			{
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue() {
				this.clearInvalidReason();

				return true;
			}
		};
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.properties.PreferencesPluginProperty#writeToPreferences()
	 */
	@Override
	final public void writeToPreferences() {
		// noop
	}
}
