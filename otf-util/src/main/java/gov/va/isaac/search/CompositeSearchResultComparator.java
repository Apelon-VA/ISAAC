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
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.search;

import gov.va.isaac.util.WBUtility;

import java.io.IOException;
import java.util.Comparator;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
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
        
        // else same score
        String o1FSN = null;
        try {
			o1FSN = o1.getConcept().getFullySpecifiedDescription().getText().trim();
		} catch (IOException | ContradictionException e) {
			LOG.error("Failed calling getFullySpecifiedDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o1, e);
			e.printStackTrace();
		}
        String o2FSN = null;
        try {
        	o2FSN = o2.getConcept().getFullySpecifiedDescription().getText().trim();
		} catch (IOException | ContradictionException e) {
			LOG.error("Failed calling getFullySpecifiedDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o2, e);
			e.printStackTrace();
		}
        int fsnComparison = o1FSN.compareTo(o2FSN);
        if (fsnComparison != 0) {
            return fsnComparison;
        }
        
        // else same score and FSN
        String o1PreferredDescription = null;
		try {
			o1PreferredDescription = o1.getConcept().getPreferredDescription().getText().trim();
		} catch (IOException | ContradictionException e) {
			LOG.error("Failed calling getPreferredDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o1, e);
			e.printStackTrace();
		}

        String o2PreferredDescription = null;
		try {
			o2PreferredDescription = o2.getConcept().getPreferredDescription().getText().trim();
		} catch (IOException | ContradictionException e) {
			LOG.error("Failed calling getPreferredDescription() (" + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\") on concept " + o2, e);
			e.printStackTrace();
		}
        
        int prefDescComparison = o1PreferredDescription.compareTo(o2PreferredDescription);
        if (prefDescComparison != 0) {
            return prefDescComparison;
        }
        
        // else same score and FSN and preferred description
        String o1matchingComponentType = WBUtility.getConPrefTerm(o1.getMatchingDescriptionComponents().iterator().next().getTypeNid()).trim();
        String o2matchingComponentType = WBUtility.getConPrefTerm(o2.getMatchingDescriptionComponents().iterator().next().getTypeNid()).trim();

        final String fullySpecifiedNameStr = "Fully specified name";
        if (o1matchingComponentType.equalsIgnoreCase(fullySpecifiedNameStr)) {
        	return -1;
        } else if (o2matchingComponentType.equalsIgnoreCase(fullySpecifiedNameStr)) {
        	return 1;
        }
        
        // else same score and FSN and preferred description and both FSNs
        final String synonymStr = "Synonym";
        if (o1matchingComponentType.equalsIgnoreCase(synonymStr)) {
        	return -1;
        } else if (o2matchingComponentType.equalsIgnoreCase(synonymStr)) {
        	return 1;
        }
        
        // else same score and FSN and preferred description and both Synonyms
        return 0;
    }
}
