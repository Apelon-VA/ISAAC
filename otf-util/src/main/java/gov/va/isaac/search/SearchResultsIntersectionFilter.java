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
import java.util.function.Function;
import org.apache.mahout.math.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchResultsIntersectionFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchResultsIntersectionFilter implements Function<List<CompositeSearchResult>, List<CompositeSearchResult>> {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsIntersectionFilter.class);

	List<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> filters = new ArrayList<>();
	
	public SearchResultsIntersectionFilter(List<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> passedFilters) {
		filters.addAll(passedFilters);
	}
		
	@SafeVarargs
	public SearchResultsIntersectionFilter(Function<List<CompositeSearchResult>, List<CompositeSearchResult>>...passedFilters) {
		if (passedFilters != null) {
			for (Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter : passedFilters) {
				filters.add(filter);
			}
		}
	}

	public Collection<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> getFilters() { return filters; }

	/**
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public List<CompositeSearchResult> apply(List<CompositeSearchResult> results)
	{
		List<CompositeSearchResult> filteredResults = results;
		for (Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter : filters) {
			int numResultsToFilter = filteredResults.size();
			LOG.debug("Applying SearchResultsFilter " + filter + " to " + numResultsToFilter + " search results");

			filteredResults = filter.apply(filteredResults);
			
			LOG.debug(filteredResults.size() + " results remained after filtering a total of " + numResultsToFilter + " search results");
		}

		return filteredResults;
	}

	@Override
	public String toString() {
		return "SearchResultsIntersectionFilter [filters=" + Arrays.toString(filters.toArray()) + "]";
	}
}
