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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;

/**
 * Encapsulates a data store search result.
 * <p>
 * Logic has been mostly copied from LEGO {@code SnomedSearchResult}.
 * Original author comments are in "quotes".
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CompositeSearchResult {

    private final HashSet<String> matchingStrings = new HashSet<>();
    private final int conceptNid;
    private final ConceptVersionBI concept;
    private final Set<ComponentVersionBI> components = new HashSet<>();

    private float bestScore; // "best score, rather than score, as multiple matches may go into a SearchResult"

    public CompositeSearchResult(int conceptNid, float score, ConceptVersionBI concept) {
        this.conceptNid = conceptNid;
        this.bestScore = score;
        this.concept = concept;
    }

    public void addMatchingString(String matchingString) {
        matchingStrings.add(matchingString);
    }

    public void adjustScore(float newScore) {
        bestScore = newScore;
    }

    public float getBestScore() {
        return bestScore;
    }

    public int getConceptNid() {
        return conceptNid;
    }

    public Set<String> getMatchStrings() {
        return matchingStrings;
    }

    public ConceptVersionBI getConcept() {
        return concept;
    }

	public Set<ComponentVersionBI> getComponents() {
		return components;
	}
	
	// Convenience method only
	public Set<DescriptionAnalogBI<?>> getMatchingDescriptionComponents() {
		Set<DescriptionAnalogBI<?>> setToReturn = new HashSet<>();
		for (ComponentVersionBI comp : components) {
			if (comp instanceof DescriptionAnalogBI) {
				setToReturn.add((DescriptionAnalogBI<?>)comp);
			}
		}
		
		return Collections.unmodifiableSet(setToReturn);
	}
}
