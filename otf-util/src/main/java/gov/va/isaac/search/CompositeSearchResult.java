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

import gov.va.isaac.util.OTFUtility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;

/**
 * Encapsulates a data store search result.
 * <p>
 * Logic has been mostly copied from LEGO {@code SnomedSearchResult}.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CompositeSearchResult {

	private ConceptVersionBI containingConcept = null;
	private final Set<ComponentVersionBI> matchingComponents = new HashSet<>();
	private int matchingComponentNid;
	private float bestScore; // best score, rather than score, as multiple matches may go into a SearchResult

	public CompositeSearchResult(ComponentVersionBI matchingComponent, float score) {
		this.matchingComponents.add(matchingComponent);
		this.bestScore = score;
		//matchingComponent may be null, if the match is not on our view path...
		if (matchingComponent == null)
		{
			throw new RuntimeException("Please call the constructor that takes a nid, if matchingComponent is null...");
		}
		this.containingConcept = OTFUtility.getConceptVersion(matchingComponent.getConceptNid());
	}
	public CompositeSearchResult(int matchingComponentNid, float score) {
		this.bestScore = score;
		//matchingComponent may be null, if the match is not on our view path...
		this.containingConcept = null;
		this.matchingComponentNid = matchingComponentNid;
		
	}
	
	protected void adjustScore(float newScore) {
		bestScore = newScore;
	}

	public float getBestScore() {
		return bestScore;
	}
	
	/**
	 * This may return null, if the concept and/or matching component was not on the path
	 */
	public ConceptVersionBI getContainingConcept() {
		return containingConcept;
	}

	/**
	 * A convenience method to get string values from the matching Components
	 */
	public List<String> getMatchingStrings() {
		ArrayList<String> strings = new ArrayList<>();
		if (matchingComponents.size() == 0)
		{
			if (containingConcept == null)
			{
				strings.add("Match to NID (not on path):" + matchingComponentNid);
			}
			else
			{
				throw new RuntimeException("Unexpected");
			}
		}
		for (ComponentVersionBI cc : matchingComponents)
		{
			if (cc instanceof DescriptionAnalogBI)
			{
				strings.add(((DescriptionAnalogBI<?>) cc).getText());
			}
			else if (cc instanceof ConceptVersionBI)
			{
				//This means they matched on a UUID or other ID lookup.
				//Return UUID for now - matches on other ID types will be handled differently 
				//in the near future - so ignore the SCTID case for now.
				strings.add(cc.getPrimordialUuid().toString());
			}
			else
			{
				strings.add("ERROR: No string extractor available for " + cc.getClass().getName());
			}
		}
		return strings;
	}

	public Set<ComponentVersionBI> getMatchingComponents() {
		return matchingComponents;
	}
	
	/**
	 * Convenience method to return a filtered list of matchingComponents such that it only returns
	 * Description type components
	 */
	public Set<DescriptionAnalogBI<?>> getMatchingDescriptionComponents() {
		Set<DescriptionAnalogBI<?>> setToReturn = new HashSet<>();
		for (ComponentVersionBI comp : matchingComponents) {
			if (comp instanceof DescriptionAnalogBI) {
				setToReturn.add((DescriptionAnalogBI<?>)comp);
			}
		}
		
		return Collections.unmodifiableSet(setToReturn);
	}
	
	protected void merge(CompositeSearchResult other)
	{
		if (containingConcept.getNid() !=  other.containingConcept.getNid())
		{
			throw new RuntimeException("Unmergeable!");
		}
		if (other.bestScore > bestScore)
		{
			bestScore = other.bestScore;
		}
		matchingComponents.addAll(other.getMatchingComponents());
	}
}
