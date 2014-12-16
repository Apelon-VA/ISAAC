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
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * PreferencesPluginComboBoxProperty
 * 
 * Helper Constructors greatly simplify construction of properties
 * governed by ComboBox control.  The only additional work needed after construction
 * is population of the dropdown item list
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class PreferencesPluginComboBoxProperty<T> extends PreferencesPluginProperty<T, ComboBox<T>> {
	private Logger logger = LoggerFactory.getLogger(PreferencesPluginComboBoxProperty.class);

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
	protected PreferencesPluginComboBoxProperty(
			Label label,
			ComboBox<T> control,
			Property<T> property,
			ValidBooleanBinding validator,
			StringConverter<T> stringConverter,
			PropertyAction<T, ComboBox<T>> binder,
			PropertyAction<T, ComboBox<T>> controlPersistedValueSetter,
			PropertyAction<T, ComboBox<T>> guiFormattingApplicator) {
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
	protected PreferencesPluginComboBoxProperty(
			String name,
			ComboBox<T> control,
			Property<T> property,
			ValidBooleanBinding validator,
			StringConverter<T> stringConverter,
			PropertyAction<T, ComboBox<T>> binder,
			PropertyAction<T, ComboBox<T>> controlPersistedValueSetter,
			PropertyAction<T, ComboBox<T>> guiFormattingApplicator) {
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
	 * @param name
	 * 
	 * Constructor for simple String value ComboBox
	 */
	protected PreferencesPluginComboBoxProperty(String name) {
		this(new Label(name), (StringConverter<T>)null);
	}
	
	/**
	 * @param name
	 * @param stringConverter
	 * 
	 * Constructor for ComboBox that displays a String
	 * that must be derived/converted from its underlying value
	 */
	protected PreferencesPluginComboBoxProperty(
			String name,
			StringConverter<T> stringConverter) {
		this(new Label(name), stringConverter);
	}

	/**
	 * @param name
	 * 
	 * Constructor for simple String value ComboBox
	 * 
	 * Allows a preexisting Label to be used
	 */
	protected PreferencesPluginComboBoxProperty(Label label) {
		this(label, (StringConverter<T>)null);
	}

	/**
	 * @param name
	 * @param stringConverter
	 * 
	 * Constructor for ComboBox that displays a String
	 * that must be derived/converted from its underlying value
	 * 
	 * Allows a preexisting Label to be used
	 */
	protected PreferencesPluginComboBoxProperty(
			Label label,
			StringConverter<T> stringConverter) {
		super(
				label, 
				new ComboBox<T>(), 
				new SimpleObjectProperty<T>(), 
				null, // validator handled below
				stringConverter, 
				new PropertyAction<T, ComboBox<T>>() {
					@Override
					public void apply(PreferencesPluginProperty<T, ComboBox<T>> property) {
						property.getProperty().bind(property.getControl().getSelectionModel().selectedItemProperty());
					}	
				},
				new PropertyAction<T, ComboBox<T>>() {
					@Override
					public void apply(PreferencesPluginProperty<T, ComboBox<T>> property) {
						property.getControl().getSelectionModel().select(property.readFromPersistedPreferences());
					}
				}, 
				new PropertyAction<T, ComboBox<T>>() {
					@Override
					public void apply(PreferencesPluginProperty<T, ComboBox<T>> property) {
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
				if (getProperty().getValue() == null) {
					this.setInvalidReason("null/unset/unselected " + name);

					logger.debug(getReasonWhyInvalid().get());

					TextErrorColorHelper.setTextErrorColor(label);

					return false;
				} else {
					TextErrorColorHelper.clearTextErrorColor(label);
				}
				if (StringUtils.isEmpty(getStringConverter().convertToString(getProperty().getValue()))) {
					this.setInvalidReason("Invalid " + name);

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
		
		control.setCellFactory(new Callback<ListView<T>, ListCell<T>> () {
			@Override
			public ListCell<T> call(ListView<T> param) {
				final ListCell<T> cell = new ListCell<T>() {
					@Override
					protected void updateItem(T c, boolean emptyRow) {
						super.updateItem(c, emptyRow);

						if(c == null) {
							setText(null);
						}else {
							String diplay = getStringConverter().convertToString(c);
							setText(diplay);
						}
					}
				};

				return cell;
			}
		});
		control.setButtonCell(new ListCell<T>() {
			@Override
			protected void updateItem(T c, boolean emptyRow) {
				super.updateItem(c, emptyRow); 
				if (emptyRow) {
					setText("");
				} else {
					String diplay = getStringConverter().convertToString(c);
					setText(diplay);
				}
			}
		});
	}
}
