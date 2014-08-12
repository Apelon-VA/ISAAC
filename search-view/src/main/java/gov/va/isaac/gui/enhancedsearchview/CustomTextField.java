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
 * CustomTextField
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview;

import javafx.beans.property.BooleanProperty; 
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;

/**
 * CustomTextField
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

public class CustomTextField extends TextField {
	/**
	 * numericOnly property if set, will allow accept only numeric input.
	 */
	private BooleanProperty numericOnly = new SimpleBooleanProperty(this,
			"numericOnly", false);

	public final boolean isNumericOnly() {
		return numericOnly.getValue();
	}

	public final void setNumericOnly(boolean value) {
		numericOnly.setValue(value);
	}

	public final BooleanProperty numericOnlyProperty() {
		return numericOnly;
	}

	/**
	 * maxSize property , determines the maximum size of the text that can be
	 * input.
	 */
	public IntegerProperty maxSize = new IntegerPropertyBase(1000) {

		@Override
		public String getName() {
			return "maxSize";
		}

		@Override
		public Object getBean() {
			return CustomTextField.this;
		}
	};

	public final IntegerProperty maxSizeProperty() {
		return maxSize;
	};

	public final int getMaxSize() {
		return maxSize.getValue();
	}

	public final void setMaxSize(int value) {
		maxSize.setValue(value);
	}

	/**
	 * this method is called when user inputs text into the textField
	 */
	@Override
	public void replaceText(int start, int end, String text) {
		if (numericOnly.getValue() && !text.equals("")) {
			if (!text.matches("[0-9]")) {
				return;
			}
		}
		if (getText().length() < getMaxSize() || text.equals("")) {
			super.replaceText(start, end, text);
		}
	}

	/**
	 * this method is called when user pastes text into the textField
	 */
	@Override
	public void replaceSelection(String text) {
		if (numericOnly.getValue() && !text.equals("")) {
			if (!text.matches("[0-9]+")) {
				return;
			}
		}
		super.replaceSelection(text);
		if (getText().length() > getMaxSize()) {
			String maxSubString = getText().substring(0, getMaxSize());
			setText(maxSubString);
			positionCaret(getMaxSize());
		}
	}

}
