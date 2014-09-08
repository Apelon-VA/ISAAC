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
 * SearchResultsFilterHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.searchresultsfilters;

import java.util.ArrayList;
import java.util.Collection;

import gov.va.isaac.gui.enhancedsearchview.filters.IsAFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.search.SearchResultsFilter;
import gov.va.isaac.search.SearchResultsFilterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchResultsFilterHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchResultsFilterHelper {
	static final Logger LOG = LoggerFactory.getLogger(SearchResultsFilterHelper.class);

	/**
	 * 
	 */
	private SearchResultsFilterHelper() {
	}

	/**
	 * @param filters
	 * @return SearchResultsFilter in form of SearchResultsIntersectionFilter
	 * @throws SearchResultsFilterException
	 */
	public static SearchResultsFilter createSearchResultsFilter(NonSearchTypeFilter<?>...filters) throws SearchResultsFilterException {
		validateFilters(filters);

		return new QueryBasedSearchResultsIntersectionFilter(filters);

//		SearchResultsIntersectionFilter intersectionFilter = new SearchResultsIntersectionFilter();
//		for (NonSearchTypeFilter<?> filter : filters) {
//			if (filter instanceof IsDescendantOfFilter) {
//				//intersectionFilter.getFilters().add(new QueryBasedIsDescendantOfSearchResultsIntersectionFilter((IsDescendantOfFilter)filter));
//
//				intersectionFilter.getFilters().add(new IsDescendantOfSearchResultsFilter((IsDescendantOfFilter)filter));
//			} else if (filter instanceof IsAFilter) {
//				//intersectionFilter.getFilters().add(new QueryBasedIsDescendantOfSearchResultsIntersectionFilter((IsDescendantOfFilter)filter));
//
//				intersectionFilter.getFilters().add(new IsASearchResultsFilter((IsAFilter)filter));
//			}
//		}
//		
//        return intersectionFilter;
		}
		
	static void validateFilters(NonSearchTypeFilter<?>...passedFilters) throws SearchResultsFilterException {
		if (passedFilters != null) {
			if (passedFilters.length == 0) {
				LOG.warn("No filters in list/array.  All results will pass.");
			} else {
				for (NonSearchTypeFilter<?> filter : passedFilters) {
					// Ensure only supported NonSearchTypeFilter types handled
					if (filter instanceof IsDescendantOfFilter) {
						// ok
					} else if (filter instanceof IsAFilter) {
						// ok
					}
//					else if (filter instanceof IsRefsetMemberFilter) {
//						// ok
//					}
					else {
						String msg = "Unsupported NonSearchTypeFilter " + filter.getClass().getName() + ". Curently only IsDescendantOfFilter supported";
						LOG.error(msg);
						throw new SearchResultsFilterException(null, msg);
					}

					// Ensure NonSearchTypeFilter, itself, knows it is valid
					if (! filter.isValid()) {
						String msg = "NonSearchTypeFilter is invalid: " + filter;
						LOG.error(msg);
						throw new SearchResultsFilterException(null, msg);
					}
				}
			}
		} else {
			String msg = "Passed null NonSearchTypeFilter...filters";
			LOG.error(msg);
			throw new SearchResultsFilterException(null, msg);
		}
	}
	static void validateFilters(Collection<NonSearchTypeFilter<?>> passedFilters) throws SearchResultsFilterException {
		validateFilters(passedFilters == null ? (NonSearchTypeFilter[])null : new ArrayList<>(passedFilters).toArray(new NonSearchTypeFilter[passedFilters.size()]));
	}
}