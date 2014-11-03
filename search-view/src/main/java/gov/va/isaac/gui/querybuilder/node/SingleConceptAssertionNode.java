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

import gov.va.isaac.util.WBUtility;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * SingleConceptAssertionNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class SingleConceptAssertionNode extends AssertionNode {
	protected final IntegerProperty nidProperty = new SimpleIntegerProperty();

	protected void setValidationChangeListeners() {
		nidProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() != 0 && WBUtility.getConceptVersion(newValue.intValue()) != null) {
					isValidProperty.set(true);
				} else {
					isValidProperty.set(false);
				}
			}
		});
	}
	protected void setDescriptionChangeListeners() {
		super.setDescriptionChangeListeners();
		nidProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
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
	public SingleConceptAssertionNode() {
	}
	public SingleConceptAssertionNode(int nid) {
		nidProperty.set(nid);
	}

	public IntegerProperty getNidProperty() { return nidProperty; }
	
	public Integer getNid() { return nidProperty != null ? nidProperty.get() : null; }
	public void setNid(int nid) { nidProperty.set(nid); }
	
	@Override
	public String getDescription() {
		String conceptDescription = null;
		if (getNid() != null && getNid() != 0) {
			conceptDescription = WBUtility.getDescriptionIfConceptExists(getNid());
		}
		if (conceptDescription != null) {
			return (invertProperty.get() ? "NOT " : "") + getNodeTypeName() + " " + conceptDescription;
		} else {
			return (invertProperty.get() ? "NOT " : "") + getNodeTypeName();
		}
	}
	
	@Override
	public String toString() {
		return getNodeTypeName() + " [nidProperty=" + nidProperty
				+ ", invertProperty=" + invertProperty + ", isValidProperty="
				+ isValidProperty + "]";
	}
}
