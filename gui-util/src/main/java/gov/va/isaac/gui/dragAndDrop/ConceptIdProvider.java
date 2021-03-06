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
 * ConceptIdProvider
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.dragAndDrop;

import java.util.UUID;

/**
 * ConceptIdProvider
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public interface ConceptIdProvider {
	/**
	 * Convenience method that will fail if it isn't overridden
	 */
	public default String getSctId() { return null; }

	/**
	 * Convenience method that will fail if it isn't overridden
	 */
	public default UUID getUUID() { return null; }

	/**
	 * Convenience method that will fail if it isn't overridden
	 */
	public default Integer getNid() { return null; }
}