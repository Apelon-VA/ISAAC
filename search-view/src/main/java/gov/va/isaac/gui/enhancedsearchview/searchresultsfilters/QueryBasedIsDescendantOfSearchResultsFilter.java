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
import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.nid.IntSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

class QueryBasedIsDescendantOfSearchResultsFilter implements SearchResultsFilter {
	private final IsDescendantOfFilter filter;
	
	public QueryBasedIsDescendantOfSearchResultsFilter(IsDescendantOfFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public Collection<CompositeSearchResult> filter(Collection<CompositeSearchResult> results) throws SearchResultsFilterException {
		NativeIdSetBI inputNids = new IntSet();

		for (CompositeSearchResult result : results) {
			inputNids.add(result.getConceptNid());
		}

		final NativeIdSetBI finalInputNids = inputNids;
		final ConceptVersionBI concept = WBUtility.getConceptVersion(filter.getNid());

		Query q = new Query() {
			@Override
			protected NativeIdSetBI For() throws IOException {
				return finalInputNids;
			}

			@Override
			public void Let() throws IOException {
				let(concept.getPrimordialUuid().toString(), new ConceptSpec(WBUtility.getDescription(concept), concept.getPrimordialUuid()));
			}

			@Override
			public Clause Where() {
				//		                return And(ConceptIsKindOf("Physical force"),
				//		                            Xor(ConceptIsKindOf("Motion"),
				//		                                ConceptIsDescendentOf("Motion")));
				if (filter.getInvert()) {
					return Not(ConceptIsDescendentOf(concept.getPrimordialUuid().toString()));
				} else {
					return ConceptIsDescendentOf(concept.getPrimordialUuid().toString());
				}
			}
		};

        NativeIdSetBI outputNids = null;
        try {
			SearchResultsFilterHelper.LOG.debug("Applying " + (filter.getInvert() ? "Not(ConceptIsDescendentOf())" : "ConceptIsDescendentOf()") + filter + " to " + finalInputNids.size() + " nids");

			outputNids = q.compute();
			
			SearchResultsFilterHelper.LOG.debug(outputNids.size() + " nids remained after filtering a total of " + finalInputNids + " nids");
		} catch (Exception e) {
			throw new SearchResultsFilterException(this, "Failed calling Query.compute()", e);
		}
        
        List<CompositeSearchResult> filteredResults = new ArrayList<>(results.size());
		for (CompositeSearchResult result : results) {
			if (outputNids.contains(result.getConceptNid())) {
				filteredResults.add(result);
			}
		}
		
		return filteredResults;
	}

	@Override
	public String toString() {
		return "QueryBasedIsDescendantOfSearchResultsFilter [filter=" + filter + "]";
	}
}