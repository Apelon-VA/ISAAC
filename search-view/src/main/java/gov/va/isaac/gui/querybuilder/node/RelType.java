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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * RelType
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 * RelType(
 * 		Query enclosingQuery,
 * 		String relTypeSpecKey,
 * 		String targetSpecKey,
 * 		String viewCoordinateKey,
 * 		Boolean relTypeSubsumption)
 */
public class RelType extends AssertionNode {
	private IntegerProperty relTypeConceptNidIntegerProperty = new SimpleIntegerProperty();
	private IntegerProperty targetConceptNidIntegerProperty = new SimpleIntegerProperty();
	
	private BooleanProperty useSubsumptionBooleanProperty = new SimpleBooleanProperty(false);

	/**
	 * 
	 */
	public RelType() {
		super();
		getDescriptionProperty().set(getDescription());
		
		setValidationChangeListeners();
		setDescriptionChangeListeners();
	}
	
	private boolean isNodeValid() {
		if (relTypeConceptNidIntegerProperty == null || WBUtility.getConceptVersion(relTypeConceptNidIntegerProperty.get()) == null) {
			return false;
		} else if (targetConceptNidIntegerProperty == null || WBUtility.getConceptVersion(targetConceptNidIntegerProperty.get()) == null) {
			return false;
		} else {
			return true;
		}
	}

	public IntegerProperty getRelTypeConceptNidIntegerProperty() { return relTypeConceptNidIntegerProperty; }
	public Integer getRelTypeConceptNid() { return relTypeConceptNidIntegerProperty != null ? relTypeConceptNidIntegerProperty.get() : null; }
	public void setRelTypeConceptNid(int nid) { relTypeConceptNidIntegerProperty.set(nid); }

	public IntegerProperty getTargetConceptNidIntegerProperty() { return targetConceptNidIntegerProperty; }
	public Integer getTargetConceptNid() { return targetConceptNidIntegerProperty != null ? targetConceptNidIntegerProperty.get() : null; }
	public void setTargetConceptNid(int nid) { targetConceptNidIntegerProperty.set(nid); }

	public BooleanProperty getUseSubsumptionBooleanProperty() { return useSubsumptionBooleanProperty; }
	public Boolean getUseSubsumption() { return useSubsumptionBooleanProperty != null ? useSubsumptionBooleanProperty.get() : null; }
	public void setUseSubsumption(boolean bool) { useSubsumptionBooleanProperty.set(bool); }
	
	protected void setValidationChangeListeners() {
		relTypeConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
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
		targetConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
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
		//super.setDescriptionChangeListeners();
		invertProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		relTypeConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		targetConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		useSubsumptionBooleanProperty.addListener(new ChangeListener<Boolean>() {
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
	public String getDescription() {
		String relTypeConceptDescription = null;
		if (getRelTypeConceptNid() != null && getRelTypeConceptNid() != 0) {
			relTypeConceptDescription = WBUtility.getDescriptionIfConceptExists(getRelTypeConceptNid());
		}

		String targetConceptDescription = null;
		if (getTargetConceptNid() != null && getTargetConceptNid() != 0) {
			targetConceptDescription = WBUtility.getDescriptionIfConceptExists(getTargetConceptNid());
		}
		
		String useSubsumptionDescription = null;
		
		if (getUseSubsumption() != null) {
			useSubsumptionDescription = getUseSubsumption() ? "use subsumption" : "do not use subsumption";
		}

		return (invertProperty.get() ? "NOT " : "") + getNodeTypeName() + " relType=" + relTypeConceptDescription + ", target=" + targetConceptDescription + " (" + useSubsumptionDescription + ")";
	}
}
