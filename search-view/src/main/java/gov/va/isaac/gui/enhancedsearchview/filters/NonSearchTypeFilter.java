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
 * NonSearchTypeFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.filters;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.WBUtility;

import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.IntegerProperty;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.jfree.util.Log;

/**
 * NonSearchTypeFilter
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class NonSearchTypeFilter<T extends NonSearchTypeFilter<T>> implements Filter<T> {
	public abstract Set<Integer> gatherNoSearchTermCaseList(Set<Integer> startList);
	abstract IntegerProperty getSingleNid();

	protected Set<Integer> getSingleNidNoSearchTermCaseList(Set<Integer> startList) {
		Set<Integer> mergedSet = new HashSet<Integer>();

		try {
			ConceptVersionBI con = WBUtility.getConceptVersion(getSingleNid().get());
			ConceptVersionBI rootCon = WBUtility.getRootConcept(con);

			NativeIdSetBI allConcepts = ExtendedAppContext.getDataStore().getAllConceptNids();
			NoSearchTermConcurrentSearcher searcher = new NoSearchTermConcurrentSearcher(allConcepts, rootCon.getConceptNid());
			ExtendedAppContext.getDataStore().iterateConceptDataInParallel(searcher);

			if (!startList.isEmpty()) {
				for (Integer examCon : startList) {
					if (searcher.getResults().contains(examCon)) {
						mergedSet.add(examCon);
					}
				}
			} else {
				mergedSet.addAll(searcher.getResults());
			}
		} catch (Exception e) {
			Log.error("Cannot find calculate the NoSearchTermCaseList", e);
		}

		return mergedSet;
	}
}
