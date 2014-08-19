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

package gov.va.isaac.gui.enhancedsearchview;


import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;



/**
 * 
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class IntegerField extends TextField {
	final private IntegerProperty value;
	final private int minValue;
	final private int maxValue;
	//expose an integer value property for the text field.
	public int getValue() { return value.getValue(); }
	public void setValue(int newValue) { value.setValue(newValue); }
	public IntegerProperty valueProperty() { return value; }
	
	public IntegerField() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
	}

	public IntegerField(int minValue, int maxValue, int initialValue) {
		if (minValue > maxValue)
			throw new IllegalArgumentException(
					"IntegerField min value " + minValue + " greater than max value " + maxValue
					);
		if (maxValue < minValue)
			throw new IllegalArgumentException(
					"IntegerField max value " + minValue + " less than min value " + maxValue
					);
		if (!((minValue <= initialValue) && (initialValue <= maxValue)))
			throw new IllegalArgumentException(
					"IntegerField initialValue " + initialValue + " not between " + minValue + " and " + maxValue
					);
		//initialize the field values.
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = new SimpleIntegerProperty(initialValue);
		setText(initialValue + "");
		final IntegerField intField = this;
		//make sure the value property is clamped to the required range
		//and update the field's text to be in sync with the value.
		value.addListener(new ChangeListener<Number>() {
			@Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null) {
					intField.setText("");
				} else {
					if (newValue.intValue() < intField.minValue) {
						value.setValue(intField.minValue);
						return;
					}
					if (newValue.intValue() > intField.maxValue) {
						value.setValue(intField.maxValue);
						return;
					}
					if (newValue.intValue() == 0 && (textProperty().get() == null || "".equals(textProperty().get()))) {
						//no action required, text property is already blank, we don't need to set it to 0.
					} else {
						intField.setText(newValue.toString());
					}
				}
			}
		});
		//restrict key input to numerals.
		this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent keyEvent) {
				if (!"0123456789".contains(keyEvent.getCharacter())) {
					keyEvent.consume();
				}
			}
		});
		//ensure any entered values lie inside the required range.
		this.textProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (newValue == null || "".equals(newValue)) {
					value.setValue(0);
					return;
				}
				final int intValue = Integer.parseInt(newValue);
				if (intField.minValue > intValue || intValue > intField.maxValue) {
					textProperty().setValue(oldValue);
				}
				value.set(Integer.parseInt(textProperty().get()));
			}
		});
	}
}