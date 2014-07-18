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
package gov.va.isaac.gui.dragAndDrop;

import java.util.UUID;
import gov.va.isaac.util.Utility;

/**
 * {@link ConceptIdProvider}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@FunctionalInterface
public interface ConceptIdProvider
{
	/**
	 * Implementers of this should return a UUID, or a NID - which we would expect most drop targets to handle.
	 * @return
	 */
	public String getConceptId();
	
	/**
	 * Convenience method
	 * @return
	 */
	default public boolean isSctId()
	{
		return getSctId() != null;
	}
	
	/**
	 * Convenience method
	 * @return
	 */
	default public boolean isNid()
	{
		return Utility.isInt(getConceptId());
	}
	
	/**
	 * Convenience method
	 * @return
	 */
	default public boolean isUUID()
	{
		return Utility.isUUID(getConceptId());
	}

	/**
	 * Convenience method that simply parses the UUID from {@code #getConceptId}
	 * Will fail if it isn't overridden
	 */
	default public String getSctId()
	{
		return null;
	}
	
	/**
	 * Convenience method that simply parses the UUID from {@code #getConceptId}
	 * Will fail if it isn't a UUID (but this may be overridden to be smarter by implementations)
	 */
	default public UUID getConceptUUID()
	{
		return UUID.fromString(getConceptId());
	}
	
	/**
	 * Convenience method that simply parses the UUID from {@code #getConceptId}
	 * Will fail if it isn't an int (but this may be overridden to be smarter by implementations)
	 */
	default public int getNid()
	{
		return Integer.parseInt(getConceptId());
	}
}
