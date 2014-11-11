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
 * LuceneSearchTypeFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.filters;

import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ComponentSearchType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class LuceneSearchTypeFilter extends SearchTypeFilter<LuceneSearchTypeFilter> {
	private StringProperty searchParameter = new SimpleStringProperty();
	private BooleanProperty isValid = new SimpleBooleanProperty(false);

	public LuceneSearchTypeFilter() {
		searchParameter.addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue,
					String newValue) {
				if (newValue != null && newValue.trim().length() > 1) {
					isValid.set(true);
				} else {
					isValid.set(false);
				}
			}
		});
	}

	@Override
	public String toString() {
		return "LuceneSearchTypeFilter [searchParameter=" + searchParameter.get()
				+ ", isValid=" + isValid.get() + "]";
	}
	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.enhancedsearchview.filters.Filter#isValidProperty()
	 */
	@Override
	public BooleanProperty isValidProperty() {
		return isValid;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.enhancedsearchview.filters.Filter#copy(gov.va.isaac.gui.enhancedsearchview.filters.Filter)
	 */
	@Override
	public void copy(LuceneSearchTypeFilter toCopy) {
		searchParameter.set(toCopy.getSearchParameter());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter#getSearchType()
	 */
	@Override
	public ComponentSearchType getSearchType() {
		return ComponentSearchType.LUCENE;
	}
}