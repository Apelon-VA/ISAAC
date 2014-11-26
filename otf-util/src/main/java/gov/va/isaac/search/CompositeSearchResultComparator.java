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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.search;

import java.util.Comparator;
import org.apache.commons.lang3.ObjectUtils;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Comparator} for {@link SearchResult} objects.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CompositeSearchResultComparator implements Comparator<CompositeSearchResult> {
	protected static final Logger LOG = LoggerFactory.getLogger(CompositeSearchResultComparator.class);

	/**
	 * Note, the primary getBestScore() sort is in reverse, so it goes highest to lowest
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(CompositeSearchResult o1, CompositeSearchResult o2) {
		if (o1.getBestScore() < o2.getBestScore()) {
			return 1;
		} else if (o1.getBestScore() > o2.getBestScore()) {
			return -1;
		}
		
		if (o1.getContainingConcept() == null || o2.getContainingConcept() == null)
		{
			if (o1.getContainingConcept() == null && o2.getContainingConcept() != null)
			{
				return 1;
			}
			else if (o1.getContainingConcept() != null && o2.getContainingConcept() == null)
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
		// else same score
		String o1FSN = null;
		try {
			o1FSN = o1.getContainingConcept().getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			LOG.warn("Failed calling getFullySpecifiedDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o1, e);
		}
		String o2FSN = null;
		try {
			o2FSN = o2.getContainingConcept().getFullySpecifiedDescription().getText().trim();
		} catch (Exception e) {
			LOG.warn("Failed calling getFullySpecifiedDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o2, e);
		}
		
		int fsnComparison = ObjectUtils.compare(o1FSN, o2FSN);
		if (fsnComparison != 0) {
			return fsnComparison;
		}
		
		// else same score and FSN
		String o1PreferredDescription = null;
		try {
			o1PreferredDescription = o1.getContainingConcept().getPreferredDescription().getText().trim();
		} catch (Exception e) {
			LOG.debug("Failed calling getPreferredDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o1, e);
		}

		String o2PreferredDescription = null;
		try {
			o2PreferredDescription = o2.getContainingConcept().getPreferredDescription().getText().trim();
		} catch (Exception e) {
			LOG.debug("Failed calling getPreferredDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o2, e);
		}
		
		int prefDescComparison = ObjectUtils.compare(o1PreferredDescription, o2PreferredDescription);
		if (prefDescComparison != 0) {
			return prefDescComparison;
		}
		
		// else same score and FSN and preferred description - sort on type
		String comp1String = o1.getMatchingComponents().iterator().next().toUserString();
		String comp2String = o2.getMatchingComponents().iterator().next().toUserString();

		return ObjectUtils.compare(comp1String, comp2String);
	}
}
