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
 * DescriptionRegexMatch
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder.node;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * DescriptionContains
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DescriptionContains extends SingleStringAssertionNode {
	/**
	 * 
	 */
	public DescriptionContains() {
		super();
	}

	/**
	 * @param str
	 */
	public DescriptionContains(String str) {
		super(str);
	}

	protected void setValidationChangeListeners() {
		stringProperty.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if (newValue != null && newValue.matches("[a-zA-Z0-9]*")) {
					isValidProperty.set(true);
				} else {
					isValidProperty.set(false);
				}
			}
		});
	}
	
	protected TextField constructNewStringInputField() {
		TextField newTextField = new TextField();
		newTextField.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					// validation rules
					// (! note 1 !) make sure that empty string (newValue.equals("")) 
					//   or initial text is always valid
					//   to prevent infinite cycle
	
					// If newValue is not valid for your rules
					if (newValue == null || !(newValue.matches("[0-9a-zA-Z]*"))) {
						((StringProperty)observable).setValue(oldValue);
					}
					// (! note 2 !) do not bind textProperty (textProperty().bind(someProperty))
					//   to anything in your code.  TextProperty implementation
					//   of StringProperty in TextFieldControl
					//   will throw RuntimeException in this case on setValue(string) call.
					//   Or catch and handle this exception.
				}
				);
	
		newTextField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue,
					String newValue) {
				DescriptionContains.this.setString(newValue);
			}
		});
		
		return newTextField;
	}
}
