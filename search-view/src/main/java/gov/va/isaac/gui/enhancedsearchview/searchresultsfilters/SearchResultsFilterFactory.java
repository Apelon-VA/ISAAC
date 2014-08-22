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
 * SearchResultsFilterFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.searchresultsfilters;

import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.gui.enhancedsearchview.filters.NonSearchTypeFilter;
import gov.va.isaac.search.SearchResultsFilter;
import gov.va.isaac.search.SearchResultsFilterException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchResultsFilterFactory
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchResultsFilterFactory {
	static final Logger LOG = LoggerFactory.getLogger(SearchResultsFilterFactory.class);

	/**
	 * 
	 */
	private SearchResultsFilterFactory() {
	}

	public static SearchResultsFilter createSearchResultsFilter(IsDescendantOfFilter filter) {
		SearchResultsFilter newFilter = new IsDescendantOfSearchResultsFilter(filter);
		
        return newFilter;
	}
	public static SearchResultsFilter createNonSearchTypeFilterSearchResultsIntersectionFilter(NonSearchTypeFilter...filters) throws SearchResultsFilterException {
		SearchResultsFilter newFilter = new NonSearchTypeFilterSearchResultsIntersectionFilter(filters);
		
        return newFilter;
	}
	public static SearchResultsFilter createNonSearchTypeFilterSearchResultsIntersectionFilter(Collection<NonSearchTypeFilter> filters) throws SearchResultsFilterException {
		return createNonSearchTypeFilterSearchResultsIntersectionFilter(filters.toArray(new NonSearchTypeFilter[filters.size()]));
	}

}
