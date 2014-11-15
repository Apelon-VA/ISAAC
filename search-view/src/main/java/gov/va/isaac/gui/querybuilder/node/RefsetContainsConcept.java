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
 * RelType
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
 * RefsetContainsConcept
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 * RelType(
 * 		Query enclosingQuery,
 * 		String refsetConceptSpecKey,
 * 		String conceptSpecKey,
 * 		String viewCoordinateKey)
 */
public class RefsetContainsConcept extends AssertionNode {
	private IntegerProperty refsetConceptNidIntegerProperty = new SimpleIntegerProperty();
	private IntegerProperty conceptNidIntegerProperty = new SimpleIntegerProperty();

	/**
	 * 
	 */
	public RefsetContainsConcept() {
		super();
		getDescriptionProperty().set(getDescription());
		
		setValidationChangeListeners();
		setDescriptionChangeListeners();
	}
	
	private boolean isNodeValid() {
		if (refsetConceptNidIntegerProperty == null || WBUtility.getConceptVersion(refsetConceptNidIntegerProperty.get()) == null) {
			return false;
		} else if (conceptNidIntegerProperty == null || WBUtility.getConceptVersion(conceptNidIntegerProperty.get()) == null) {
			return false;
		} else {
			return true;
		}
	}

	public IntegerProperty getRefsetConceptNidIntegerProperty() { return refsetConceptNidIntegerProperty; }
	public Integer getRefsetConceptNid() { return refsetConceptNidIntegerProperty != null ? refsetConceptNidIntegerProperty.get() : null; }
	public void setRefsetConceptNid(int nid) { refsetConceptNidIntegerProperty.set(nid); }

	public IntegerProperty getConceptNidIntegerProperty() { return conceptNidIntegerProperty; }
	public Integer getConceptNid() { return conceptNidIntegerProperty != null ? conceptNidIntegerProperty.get() : null; }
	public void setConceptNid(int nid) { conceptNidIntegerProperty.set(nid); }
	
	protected void setValidationChangeListeners() {
		refsetConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() != 0 && WBUtility.getConceptVersion(newValue.intValue()) != null) {
					isValidProperty.set(isNodeValid());
				} else {
					isValidProperty.set(false);
				}
			}
		});
		conceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() != 0 && WBUtility.getConceptVersion(newValue.intValue()) != null) {
					isValidProperty.set(isNodeValid());
				} else {
					isValidProperty.set(false);
				}
			}
		});
	}
	protected void setDescriptionChangeListeners() {
		super.setDescriptionChangeListeners();
		invertProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		refsetConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		conceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
	}

	@Override
	public String getDescription() {
		String refsetConceptDescription = null;
		if (getRefsetConceptNid() != null && getRefsetConceptNid() != 0) {
			refsetConceptDescription = WBUtility.getDescriptionIfConceptExists(getRefsetConceptNid());
		}

		String conceptDescription = null;
		if (getConceptNid() != null && getConceptNid() != 0) {
			conceptDescription = WBUtility.getDescriptionIfConceptExists(getConceptNid());
		}
		
		return (invertProperty.get() ? "NOT " : "") + getNodeTypeName() + " refset=" + refsetConceptDescription + ", concept=" + conceptDescription;
	}
}
