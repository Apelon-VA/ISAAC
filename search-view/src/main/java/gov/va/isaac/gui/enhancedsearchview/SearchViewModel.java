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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.apache.mahout.math.Arrays;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public class SearchViewModel {
	private final BooleanProperty isSearchRunnableProperty = new SimpleBooleanProperty();
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty description = new SimpleStringProperty();

	private final ObjectProperty<SearchTypeFilter> searchTypeFilterProperty = new SimpleObjectProperty();
	private final ObservableList<NonSearchTypeFilter<? extends NonSearchTypeFilter<?>>> filters = FXCollections.observableArrayList();
	private final ObjectProperty<ViewCoordinate> viewCoordinateProperty = new SimpleObjectProperty(WBUtility.getViewCoordinate());
	private final IntegerProperty maxResults = new SimpleIntegerProperty(100);
	private final StringProperty droolsExpr = new SimpleStringProperty();
	
	public SearchViewModel() {
		filters.addListener(new ListChangeListener<NonSearchTypeFilter<?>>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends NonSearchTypeFilter<?>> c) {
				isSearchRunnableProperty.set(isValid());
			}
		});
		searchTypeFilterProperty.addListener(new ChangeListener<SearchTypeFilter>() {
			@Override
			public void changed(
					ObservableValue<? extends SearchTypeFilter> observable,
					SearchTypeFilter oldValue, SearchTypeFilter newValue) {
				isSearchRunnableProperty.set(isValid());
			}
		});
		viewCoordinateProperty.addListener(new ChangeListener<ViewCoordinate>() {
			@Override
			public void changed(
					ObservableValue<? extends ViewCoordinate> observable,
					ViewCoordinate oldValue, ViewCoordinate newValue) {
				isSearchRunnableProperty.set(isValid());
			}
		});
	}
	
	public void copy(SearchViewModel other) {
		name.set(other.getName());
		description.set(other.getDescription());
		//if (other.getSearchType() == null || searchTypeFilterProperty.get() == null || searchTypeFilterProperty.get().getClass() != other.getSearchType().getClass()) {
		searchTypeFilterProperty.set(other.getSearchType());
		//} else {
		//	searchTypeFilterProperty.get().copy(other.getSearchType());
		//}
		filters.clear();
		filters.addAll(other.getFilters());
		viewCoordinateProperty.set(other.viewCoordinateProperty.get());
		maxResults.set(other.getMaxResults());
		droolsExpr.set(other.getDroolsExpr());
	}
	
	public boolean isValid() {
		if (getInvalidFilters().size() > 0) {
			return false;
		}

		if (viewCoordinateProperty.get() == null) {
			return false;
		}
		
		if (searchTypeFilterProperty.get() == null || ! searchTypeFilterProperty.get().isValid()) {
			return false;
		}
		
		return true;
	}
	
	public SearchTypeFilter<?> getSearchType() {
		return searchTypeFilterProperty.get();
	}
	public void setSearchType(SearchTypeFilter<?> searchTypeFilter) {
		this.searchTypeFilterProperty.set(searchTypeFilter);
	}

	public BooleanProperty getIsSearchRunnableProperty() { return isSearchRunnableProperty; }
	
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
		return viewCoordinateProperty.get();
	}
	public ObjectProperty<ViewCoordinate> getViewCoordinateProperty() {
		return viewCoordinateProperty;
	}
	public void setViewCoordinate(ViewCoordinate viewCoordinate) {
		this.viewCoordinateProperty.set(viewCoordinate);
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
		return "SearchViewModel [isValid=" + isSearchRunnableProperty + ", name=" + name
				+ ", description=" + description + ", searchTypeFilter="
				+ searchTypeFilterProperty.get() + ", maxResults=" + maxResults
				+ ", droolsExpr=" + droolsExpr + ", filter=" + Arrays.toString(filters.toArray())
				+ ", viewCoordinate=" + (viewCoordinateProperty.get() != null ? viewCoordinateProperty.get().getViewPosition() : null) + "]";
	}
}