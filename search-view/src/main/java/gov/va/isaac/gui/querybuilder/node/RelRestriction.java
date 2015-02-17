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

import gov.va.isaac.util.OTFUtility;
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
 *	 * RelRestriction(Query enclosingQuery, String relRestrictionSpecKey, String relTypeKey, String sourceSpecKey,
	 *		String viewCoordinateKey, String destinationSubsumptionKey, String relTypeSubsumptionKey)

 * RelRestriction(
 * 		Query enclosingQuery,
 * 		String relRestrictionSpecKey,
 * 		String relTypeSpecKey,
 * 		String sourceSpecKey,
 * 		String viewCoordinateKey,
 * 		String destinationSubsumptionKey,
 * 		String relTypeSubsumptionKey)
 */
public class RelRestriction extends AssertionNode {
	private IntegerProperty relRestrictionConceptNidIntegerProperty = new SimpleIntegerProperty();
	private IntegerProperty relTypeConceptNidIntegerProperty = new SimpleIntegerProperty();
	private IntegerProperty sourceConceptNidIntegerProperty = new SimpleIntegerProperty();
	
	private BooleanProperty useDestinationSubsumptionBooleanProperty = new SimpleBooleanProperty(false);
	private BooleanProperty useRelTypeSubsumptionBooleanProperty = new SimpleBooleanProperty(false);

	/**
	 * 
	 */
	public RelRestriction() {
		super();
		getDescriptionProperty().set(getDescription());
		
		setValidationChangeListeners();
		setDescriptionChangeListeners();
	}
	
	private boolean isNodeValid() {
		if (relRestrictionConceptNidIntegerProperty == null || OTFUtility.getConceptVersion(relRestrictionConceptNidIntegerProperty.get()) == null) {
			return false;
		} else if (relTypeConceptNidIntegerProperty == null || OTFUtility.getConceptVersion(relTypeConceptNidIntegerProperty.get()) == null) {
			return false;
		} else if (sourceConceptNidIntegerProperty == null || OTFUtility.getConceptVersion(sourceConceptNidIntegerProperty.get()) == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public IntegerProperty getRestrictionConceptNidIntegerProperty() { return relRestrictionConceptNidIntegerProperty; }
	public Integer getRelRestrictionConceptNid() { return relRestrictionConceptNidIntegerProperty != null ? relRestrictionConceptNidIntegerProperty.get() : null; }
	public void setRelRestrictionConceptNid(int nid) { relRestrictionConceptNidIntegerProperty.set(nid); }

	public IntegerProperty getRelTypeConceptNidIntegerProperty() { return relTypeConceptNidIntegerProperty; }
	public Integer getRelTypeConceptNid() { return relTypeConceptNidIntegerProperty != null ? relTypeConceptNidIntegerProperty.get() : null; }
	public void setRelTypeConceptNid(int nid) { relTypeConceptNidIntegerProperty.set(nid); }

	public IntegerProperty getSourceConceptNidIntegerProperty() { return sourceConceptNidIntegerProperty; }
	public Integer getSourceConceptNid() { return sourceConceptNidIntegerProperty != null ? sourceConceptNidIntegerProperty.get() : null; }
	public void setSourceConceptNid(int nid) { sourceConceptNidIntegerProperty.set(nid); }

	public BooleanProperty getUseDestinationSubsumptionBooleanProperty() { return useDestinationSubsumptionBooleanProperty; }
	public Boolean getUseDestinationSubsumption() { return useDestinationSubsumptionBooleanProperty != null ? useDestinationSubsumptionBooleanProperty.get() : null; }
	public void setUseDestinationSubsumption(boolean bool) { useDestinationSubsumptionBooleanProperty.set(bool); }

	public BooleanProperty getUseRelTypeSubsumptionBooleanProperty() { return useRelTypeSubsumptionBooleanProperty; }
	public Boolean getUseRelTypeSubsumption() { return useRelTypeSubsumptionBooleanProperty != null ? useRelTypeSubsumptionBooleanProperty.get() : null; }
	public void setUseRelTypeSubsumption(boolean bool) { useRelTypeSubsumptionBooleanProperty.set(bool); }
	
	protected void setValidationChangeListeners() {
		relRestrictionConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() != 0 && OTFUtility.getConceptVersion(newValue.intValue()) != null) {
					isValidProperty.set(isNodeValid());
				} else {
					isValidProperty.set(false);
				}
			}
		});
		relTypeConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() != 0 && OTFUtility.getConceptVersion(newValue.intValue()) != null) {
					isValidProperty.set(isNodeValid());
				} else {
					isValidProperty.set(false);
				}
			}
		});
		sourceConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() != 0 && OTFUtility.getConceptVersion(newValue.intValue()) != null) {
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
		relRestrictionConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
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
		sourceConceptNidIntegerProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		useDestinationSubsumptionBooleanProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					getDescriptionProperty().set(getDescription());
				}
			}
		});
		useRelTypeSubsumptionBooleanProperty.addListener(new ChangeListener<Boolean>() {
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
		String relRestrictionConceptDescription = null;
		if (getRelRestrictionConceptNid() != null && getRelRestrictionConceptNid() != 0) {
			relRestrictionConceptDescription = OTFUtility.getDescriptionIfConceptExists(getRelRestrictionConceptNid());
		}
		
		String relTypeConceptDescription = null;
		if (getRelTypeConceptNid() != null && getRelTypeConceptNid() != 0) {
			relTypeConceptDescription = OTFUtility.getDescriptionIfConceptExists(getRelTypeConceptNid());
		}

		String sourceConceptDescription = null;
		if (getSourceConceptNid() != null && getSourceConceptNid() != 0) {
			sourceConceptDescription = OTFUtility.getDescriptionIfConceptExists(getSourceConceptNid());
		}
		
		String useDestinationSubsumptionDescription = null;
		if (getUseDestinationSubsumption() != null) {
			useDestinationSubsumptionDescription = getUseDestinationSubsumption() ? "use destination subsumption" : "do not use destination subsumption";
		}
		
		String useRelTypeSubsumptionDescription = null;
		if (getUseRelTypeSubsumption() != null) {
			useRelTypeSubsumptionDescription = getUseRelTypeSubsumption() ? "use relType subsumption" : "do not use relType subsumption";
		}

		return (invertProperty.get() ? "NOT " : "") + getNodeTypeName() +" relRestrict=" + relRestrictionConceptDescription + ", relType=" + relTypeConceptDescription + ", target=" + sourceConceptDescription + " (" + useDestinationSubsumptionDescription + ", " + useRelTypeSubsumptionDescription + ")";
	}
}
