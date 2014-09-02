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
 * SctTreeItemSearchResultsDisplayPolicies
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview;

import java.util.HashSet;
import java.util.Set;

import javafx.scene.Node;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.treeview.SctTreeItemDisplayPolicies;
import gov.va.isaac.interfaces.treeview.SctTreeItemI;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.HBox;

/**
 * SctTreeItemSearchResultsDisplayPolicies
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SctTreeItemSearchResultsDisplayPolicies implements SctTreeItemDisplayPolicies {
	
	private final SctTreeItemDisplayPolicies simplePolicies;
	
	private BooleanProperty shouldFilter = new SimpleBooleanProperty(false);
	
	Set<Integer> searchResultAncestors = new HashSet<>();
	Set<Integer> searchResults = new HashSet<>();
	
	public SctTreeItemSearchResultsDisplayPolicies(SctTreeItemDisplayPolicies simplePolicies) {
		this.simplePolicies = simplePolicies;
	}
	
	public BooleanProperty getFilterMode() {
		return shouldFilter;
	}
	public void setFilterMode(BooleanProperty filterMode) {
		this.shouldFilter = filterMode;
	}

	public Set<Integer> getSearchResultAncestors() {
		return searchResultAncestors;
	}

	public Set<Integer> getSearchResults() {
		return searchResults;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.treeview.SctTreeItemDisplayPolicies#computeGraphic(gov.va.isaac.interfaces.treeview.SctTreeItemI)
	 */
	@Override
	public Node computeGraphic(SctTreeItemI item) {
		HBox hbox = new HBox();
		
		hbox.getChildren().add(simplePolicies.computeGraphic(item));
		
		if (item.getConceptNid() != null) {
			if (getSearchResultAncestors().contains(item.getConceptNid())) {
				hbox.getChildren().add(Images.TAXONOMY_SEARCH_RESULT_ANCESTOR.createImageView());
			}
			if (getSearchResults().contains(item.getConceptNid())) {
				hbox.getChildren().add(Images.TAXONOMY_SEARCH_RESULT.createImageView());
			}
		}
		
		return hbox;
	}

	@Override
	public boolean shouldDisplay(SctTreeItemI treeItem) {
		if (! shouldFilter.get()) {
			// if the shouldFilter boolean property is set to false then display
			return true;
		} 
		else if (treeItem.getConceptNid() == null) {
			// if the treeItem nid is null, it may be a special node (i.e. hidden, etc), so display
			return true;
		}
		else {
			// do filtering
			if (getSearchResultAncestors().contains(treeItem.getConceptNid()) || getSearchResults().contains(treeItem.getConceptNid())) {
				// if treeItem is in search result ancestor list or is a search result then display
				return true;
			} else {
				// else do not
				return false;
			}
		}
	}
}
