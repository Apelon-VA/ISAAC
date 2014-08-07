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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public class SearchViewModel {
	public static interface Filter {
		public boolean isValid();
	}
	public static interface SingleStringParameterFilter extends Filter {
		public String getSearchParameter();
	}
	public static class LuceneFilter implements SingleStringParameterFilter {
		String searchParameter;
		
		public String getSearchParameter() {
			return searchParameter;
		}

		public void setSearchParameter(String searchParameter) {
			this.searchParameter = searchParameter;
		}

		/* (non-Javadoc)
		 * @see gov.va.isaac.gui.enhancedsearchview.SearchViewModel.Filter#isValid()
		 */
		@Override
		public boolean isValid() {
			return searchParameter != null && searchParameter.length() > 1;
		}

		@Override
		public String toString() {
			return "LuceneFilter [searchParameter=" + searchParameter + "]";
		}
	}
	public static class RegExpFilter implements SingleStringParameterFilter {
		String searchParameter;
		
		public String getSearchParameter() {
			return searchParameter;
		}

		public void setSearchParameter(String searchParameter) {
			this.searchParameter = searchParameter;
		}

		/* (non-Javadoc)
		 * @see gov.va.isaac.gui.enhancedsearchview.SearchViewModel.Filter#isValid()
		 */
		@Override
		public boolean isValid() {
			return searchParameter != null && searchParameter.length() > 1;
		}

		@Override
		public String toString() {
			return "RegExpFilter [searchParameter=" + searchParameter + "]";
		}
	}

	final List<Filter> filters = new ArrayList<>();
	ViewCoordinate viewCoordinate;
	
	public boolean isValid() {
		return getValidFilters().size() > 0 && getInvalidFilters().size() == 0;
	}
	
	public List<Filter> getFilters() {
		return filters;
	}
	public ViewCoordinate getViewCoordinate() {
		return viewCoordinate;
	}
	public void setViewCoordinate(ViewCoordinate viewCoordinate) {
		this.viewCoordinate = viewCoordinate;
	}
	
	public Collection<Filter> getValidFilters() {
		List<Filter> validFilters = new ArrayList<>();
		
		for (Filter filter : filters) {
			if (filter.isValid()) {
				validFilters.add(filter);
			}
		}
		
		return Collections.unmodifiableCollection(validFilters);
	}

	public Collection<Filter> getInvalidFilters() {
		List<Filter> invalidFilters = new ArrayList<>();
		
		for (Filter filter : filters) {
			if (! filter.isValid()) {
				invalidFilters.add(filter);
			}
		}
		
		return Collections.unmodifiableCollection(invalidFilters);
	}

	@Override
	public String toString() {
		return "SearchViewModel [viewCoordinate="
				+ viewCoordinate + ", filters=" + Arrays.toString(filters.toArray(new Filter[filters.size()])) + "]";
	}
}