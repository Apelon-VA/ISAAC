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
 * SearchResultsIntersectionFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mahout.math.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchResultsIntersectionFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchResultsIntersectionFilter implements SearchResultsFilter {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsIntersectionFilter.class);

	List<SearchResultsFilter> filters = new ArrayList<>();
	
	public SearchResultsIntersectionFilter(List<SearchResultsFilter> passedFilters) {
		filters.addAll(passedFilters);
	}
		
	public SearchResultsIntersectionFilter(SearchResultsFilter...passedFilters) {
		if (passedFilters != null) {
			for (SearchResultsFilter filter : passedFilters) {
				filters.add(filter);
			}
		}
	}

	public Collection<SearchResultsFilter> getFilters() { return filters; }
	
	/* (non-Javadoc)
	 * @see gov.va.isaac.search.SearchResultsFilter#filter(java.util.Collection)
	 */
	@Override
	public List<CompositeSearchResult> filter(List<CompositeSearchResult> results)
			throws SearchResultsFilterException {

		List<CompositeSearchResult> filteredResults = results;
		for (SearchResultsFilter filter : filters) {
			int numResultsToFilter = filteredResults.size();
			LOG.debug("Applying SearchResultsFilter " + filter + " to " + numResultsToFilter + " search results");

			filteredResults = filter.filter(filteredResults);
			
			LOG.debug(filteredResults.size() + " results remained after filtering a total of " + numResultsToFilter + " search results");
		}

		return filteredResults;
	}

	@Override
	public String toString() {
		return "SearchResultsIntersectionFilter [filters=" + Arrays.toString(filters.toArray()) + "]";
	}
}
