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
 * SearchDisplayConcept
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview;

/**
 * SearchDisplayConcept
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchDisplayConcept {
	private String fullySpecifiedName;
	private String preferredTerm;
	private int nid;
	
	/**
	 * 
	 */
	public SearchDisplayConcept() {
	}

	public SearchDisplayConcept(String fullySpecifiedName, String preferredTerm, int nid) {
		super();
		this.fullySpecifiedName = fullySpecifiedName;
		this.preferredTerm = preferredTerm;
		this.nid = nid;
	}

	public String getFullySpecifiedName() {
		return fullySpecifiedName;
	}

	public void setFullySpecifiedName(String fullySpecifiedName) {
		this.fullySpecifiedName = fullySpecifiedName;
	}

	public String getPreferredTerm() {
		return preferredTerm;
	}

	public void setPreferredTerm(String preferredTerm) {
		this.preferredTerm = preferredTerm;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	
	public String getDetailedToString() {
		return "SearchDisplayConcept [fullySpecifiedName=" + fullySpecifiedName
				+ ", preferredTerm=" + preferredTerm + ", nid=" + nid + "]";
	}
	
	@Override
	public String toString() {
		return fullySpecifiedName;
	}
}
