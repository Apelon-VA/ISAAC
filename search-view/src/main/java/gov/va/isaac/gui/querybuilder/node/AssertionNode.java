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
 * AssertionNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.querybuilder.node;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * AssertionNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class AssertionNode extends DraggableNode {
	protected final BooleanProperty invertProperty = new SimpleBooleanProperty(false);
	protected final BooleanProperty isValidProperty = new SimpleBooleanProperty(false);

	protected final StringProperty descriptionProperty = new SimpleStringProperty(getDescription());

	/**
	 * 
	 */
	public AssertionNode() {
		super();
	}
	
	public BooleanProperty getInvertProperty() { return invertProperty; }
	
	public boolean getInvert() { return invertProperty.get(); }
	public void setInvert(boolean invert) { invertProperty.set(invert); }

	public BooleanProperty getIsValidProperty() { return isValidProperty; }
	
	public boolean getIsValid() { return isValidProperty.get(); }
	public void setIsValid(boolean isValid) { isValidProperty.set(isValid); }
	
	public StringProperty getDescriptionProperty() { return descriptionProperty; }
	
	@Override
	public String getDescription() {
		return (invertProperty.get() ? "NOT " : "") + getNodeTypeName();
	}
	
	protected void setDescriptionChangeListeners() {
		invertProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
	}

	@Override
	public String toString() {
		return "AssertionNode [invertProperty=" + invertProperty
				+ ", isValidProperty=" + isValidProperty + "]";
	}
}
