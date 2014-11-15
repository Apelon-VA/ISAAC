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
 * SearchTypeFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.filters;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.ComponentSearchType;
import gov.va.isaac.gui.enhancedsearchview.SearchTypeEnums.SearchType;

/**
 * SearchTypeFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class SearchTypeFilter<T extends SearchTypeFilter<T>> implements Filter<T> {
	final protected StringProperty searchParameter;
	final protected BooleanProperty isValid;

	public SearchTypeFilter() {
		this(null, null);
	}
	public SearchTypeFilter(StringProperty searchParamProperty) {
		this(searchParamProperty, null);
	}
	public SearchTypeFilter(StringProperty searchParamProperty, BooleanProperty isValidProperty) {
		searchParameter = searchParamProperty != null ? searchParamProperty : new SimpleStringProperty();
		isValid = isValidProperty != null ? isValidProperty : new SimpleBooleanProperty(false);
	}

	public abstract SearchType getSearchType();
	public abstract ComponentSearchType getComponentSearchType();

	public StringProperty getSearchParameterProperty() {
		return searchParameter;
	}
	public String getSearchParameter() {
		return searchParameter.get();
	}

	public void setSearchParameter(String searchParameter) {
		this.searchParameter.set(searchParameter);
	}

}
