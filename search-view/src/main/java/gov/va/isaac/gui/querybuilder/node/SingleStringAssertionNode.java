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
 * SingleConceptAssertionNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder.node;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * SingleStringAssertionNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class SingleStringAssertionNode extends AssertionNode {
	protected final StringProperty stringProperty = new SimpleStringProperty();

	protected void setValidationChangeListeners() {
		stringProperty.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if (newValue != null) {
					isValidProperty.set(true);
				} else {
					isValidProperty.set(false);
				}
			}
		});
	}
	protected void setDescriptionChangeListeners() {
		super.setDescriptionChangeListeners();
		stringProperty.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
	}

	{
		setValidationChangeListeners();
		setDescriptionChangeListeners();
	}
	/**
	 * 
	 */
	public SingleStringAssertionNode() {
	}
	public SingleStringAssertionNode(String str) {
		stringProperty.set(str);
	}

	public StringProperty getStringProperty() { return stringProperty; }
	
	public String getString() { return stringProperty != null ? stringProperty.get() : null; }
	public void setString(String str) { stringProperty.set(str); }
	
	@Override
	public String getDescription() {
		if (getString() != null) {
			return (invertProperty.get() ? "NOT " : "") + getNodeTypeName() + " \"" + getString() + "\"";
		} else {
			return (invertProperty.get() ? "NOT " : "") + getNodeTypeName();
		}
	}
	
	@Override
	public String toString() {
		return "SingleStringAssertionNode [stringProperty=" + stringProperty
				+ ", invertProperty=" + invertProperty + ", isValidProperty="
				+ isValidProperty + "]";
	}
}
