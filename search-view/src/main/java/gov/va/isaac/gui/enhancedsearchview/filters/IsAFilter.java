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
 * IsDescendantOfFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.filters;

import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * IsDescendantOfFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class IsAFilter extends NonSearchTypeFilter<IsAFilter> implements Invertable, SingleNidFilter {
	private BooleanProperty isValid = new SimpleBooleanProperty(false);
	private BooleanProperty invert = new SimpleBooleanProperty(false);
	private IntegerProperty nid = new SimpleIntegerProperty(0);
	
	{
		nid.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				if (newValue.intValue() != 0) {
					isValid.set(true);
				} else {
					isValid.set(false);
				}
			}});
	}

	public IsAFilter() {}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.enhancedsearchview.filters.Filter#isValidProperty()
	 */
	@Override
	public BooleanProperty isValidProperty() {
		return isValid;
	}
	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.enhancedsearchview.filters.Invertable#getInvertProperty()
	 */
	@Override
	public BooleanProperty getInvertProperty() {
		return invert;
	}
	
	public IntegerProperty getNidProperty() {
		return nid;
	}
	
	@Override
	public int getNid() {
		return nid.get();
	}
	@Override
	public void setNid(int nid) {
		this.nid.set(nid);
	}
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.enhancedsearchview.filters.Filter#copy(gov.va.isaac.gui.enhancedsearchview.filters.Filter)
	 */
	@Override
	public void copy(IsAFilter toCopy) {
		invert.set(toCopy.getInvert());
		nid.set(toCopy.getNid());
	}
	/* (non-Javadoc)
	 * @see gov.va.isaac.search.IndividualSearchResultFilter#getInvert()
	 */
	@Override
	public boolean getInvert() {
		return invert.get();
	}
	@Override
	public String toString() {
		return "IsAFilter [isValid=" + isValid.get() + ", invert="
				+ invert.get() + ", nid=" + nid.get() + "]";
	}

	@Override
	public Set<Integer> gatherNoSearchTermCaseList(Set<Integer> startList) {
		return getSingleNidNoSearchTermCaseList(startList);
	}

	@Override
	IntegerProperty getSingleNid() {
		return nid;
	}
}
