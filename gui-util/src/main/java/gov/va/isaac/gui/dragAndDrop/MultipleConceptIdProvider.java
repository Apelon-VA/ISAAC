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
 * MultipleConceptIdProvider
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.dragAndDrop;

import java.util.UUID;

/**
 * MultipleConceptIdProvider
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public interface MultipleConceptIdProvider extends ConceptIdProvider {
	/**
	 * Get methods only use bulk methods if unambiguous (only 1 entry avail)
	 */
	@Override
	public default String getSctId() {
		if (getSctIds() != null && getSctIds().length == 1) {
			return getSctIds()[0];
		} else {
			return null;
		}
	}
	public default String[] getSctIds() {
		if (getSctId() != null) {
			return new String[] { getSctId() };
		} else {
			return null;
		}
	}

	@Override
	public default UUID getUUID() {
		if (getUUIDs() != null && getUUIDs().length == 1) {
			return getUUIDs()[0];
		} else {
			return null;
		}
	}
	public default UUID[] getUUIDs() {
		if (getUUID() != null) {
			return new UUID[] { getUUID() };
		} else {
			return null;
		}
	}

	@Override
	public default Integer getNid() {
		if (getNids() != null && getNids().length == 1) {
			return getNids()[0];
		} else {
			return null;
		}
	}
	public default Integer[] getNids() {
		if (getNid() != null) {
			return new Integer[] { getNid() };
		} else {
			return null;
		}
	}
}
