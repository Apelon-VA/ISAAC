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
 * QueryBasedIsDescendantOfSearchResultsFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.searchresultsfilters;

import gov.va.isaac.gui.enhancedsearchview.filters.IsDescendantOfFilter;
import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchResultsFilter;
import gov.va.isaac.search.SearchResultsFilterException;
import gov.va.isaac.util.OTFUtility;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IsDescendantOfSearchResultsFilter implements SearchResultsFilter {
	static final Logger LOG = LoggerFactory.getLogger(IsDescendantOfSearchResultsFilter.class);

	private final IsDescendantOfFilter filter;
	
	public IsDescendantOfSearchResultsFilter(IsDescendantOfFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public List<CompositeSearchResult> filter(List<CompositeSearchResult> results) throws SearchResultsFilterException {
		final ConceptVersionBI possibleAscendantConcept = OTFUtility.getConceptVersion(filter.getNid());

		CompositeSearchResult currentResult = null;
		try {
			LOG.debug("Applying " + (filter.getInvert() ? "! isKindOf() " : "isKindOf() ") + filter + " to " + results.size() + " results");
			List<CompositeSearchResult> filteredResults = new ArrayList<>(results.size());
			
			for (CompositeSearchResult result : results) {
				currentResult = result;
				if (result.getContainingConcept().isKindOf(possibleAscendantConcept) && ! filter.getInvert()) {
					filteredResults.add(result);
				} else if (! result.getContainingConcept().isKindOf(possibleAscendantConcept) && filter.getInvert()) {
					filteredResults.add(result);
				}
			}
			
			LOG.debug(filteredResults.size() + " results remained after filtering a total of " + results.size() + " results");
			
			return filteredResults;
		} catch (Exception e) {
			throw new SearchResultsFilterException(this, "Failed calling (" + OTFUtility.getDescription(currentResult.getContainingConcept()) + " (nid=" + currentResult.getContainingConcept().getNid() + ")).isKindOf(" + OTFUtility.getDescription(possibleAscendantConcept) + " (nid=" + possibleAscendantConcept.getConceptNid() + "))", e);
		}
	}

	@Override
	public String toString() {
		return "IsDescendantOfSearchResultsFilter [filter=" + filter + "]";
	}
}