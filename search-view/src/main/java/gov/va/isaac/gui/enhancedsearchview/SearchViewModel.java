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
 * SearchViewModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.gui.enhancedsearchview.filters.Filter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.SearchTypeFilter;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public class SearchViewModel {
	private BooleanProperty isValid = new SimpleBooleanProperty();
	private StringProperty name = new SimpleStringProperty();
	private StringProperty description = new SimpleStringProperty();

	private SearchTypeFilter searchTypeFilter;
	private final ObservableList<NonSearchTypeFilter<? extends NonSearchTypeFilter<?>>> filters = FXCollections.observableArrayList();
	private ViewCoordinate viewCoordinate = WBUtility.getViewCoordinate();
	private IntegerProperty maxResults = new SimpleIntegerProperty(0);
	private StringProperty droolsExpr = new SimpleStringProperty();
	
	public SearchViewModel() {
	}
	
	public void copy(SearchViewModel other) {
		name.set(other.getName());
		description.set(other.getDescription());
		if (other.getSearchType() == null || searchTypeFilter == null || searchTypeFilter.getClass() != other.getSearchType().getClass()) {
			searchTypeFilter = other.getSearchType();
		} else {
			searchTypeFilter.copy(other.getSearchType());
		}
		filters.clear();
		filters.addAll(other.getFilters());
		viewCoordinate = other.viewCoordinate;
		maxResults.set(other.getMaxResults());
		droolsExpr.set(other.getDroolsExpr());
	}
	
	public boolean isValid() {
		if (getInvalidFilters().size() > 0) {
			return false;
		}

		if (viewCoordinate == null) {
			return false;
		}
		
		if (searchTypeFilter == null || ! searchTypeFilter.isValid()) {
			return false;
		}
		
		return true;
	}
	
	public SearchTypeFilter<?> getSearchType() {
		return searchTypeFilter;
	}
	public void setSearchType(SearchTypeFilter<?> searchTypeFilter) {
		this.searchTypeFilter = searchTypeFilter;
	}


	public StringProperty getNameProperty() {
		return name;
	}
	public String getName() {
		return name.getValue();
	}
	public void setName(String name) {
		this.name.set(name);
	}

	public StringProperty getDescriptionProperty() {
		return description;
	}
	public String getDescription() {
		return description.getValue();
	}
	public void setDescription(String description) {
		this.description.set(description);
	}

	public ObservableList<NonSearchTypeFilter<? extends NonSearchTypeFilter<?>>> getFilters() {
		return filters;
	}
	
	public ViewCoordinate getViewCoordinate() {
		return viewCoordinate;
	}
	public void setViewCoordinate(ViewCoordinate viewCoordinate) {
		this.viewCoordinate = viewCoordinate;
	}

	public IntegerProperty getMaxResultsProperty() {
		return maxResults;
	}
	public int getMaxResults() {
		return maxResults.get();
	}
	public void setMaxResults(int maxResults) {
		this.maxResults.set(maxResults);
	}

	public StringProperty getDroolsExprProperty() {
		return droolsExpr;
	}
	public String getDroolsExpr() {
		return droolsExpr.get();
	}
	public void setDroolsExpr(String droolsExpr) {
		this.droolsExpr.set(droolsExpr);
	}

	public Collection<Filter<?>> getValidFilters() {
		List<Filter<?>> validFilters = new ArrayList<>();
		
		for (Filter<?> filter : filters) {
			if (filter.isValid()) {
				validFilters.add(filter);
			}
		}
		
		return Collections.unmodifiableCollection(validFilters);
	}

	public Collection<Filter<?>> getInvalidFilters() {
		List<Filter<?>> invalidFilters = new ArrayList<>();
		
		for (Filter<?> filter : filters) {
			if (! filter.isValid()) {
				invalidFilters.add(filter);
			}
		}
		
		return Collections.unmodifiableCollection(invalidFilters);
	}

	@Override
	public String toString() {
		return "SearchViewModel [isValid=" + isValid + ", name=" + name
				+ ", description=" + description + ", searchTypeFilter="
				+ searchTypeFilter + ", maxResults=" + maxResults
				+ ", droolsExpr=" + droolsExpr + ", filter=" + Arrays.toString(filters.toArray())
				+ ", viewCoordinate=" + (viewCoordinate != null ? viewCoordinate.getViewPosition() : null) + "]";
	}
}