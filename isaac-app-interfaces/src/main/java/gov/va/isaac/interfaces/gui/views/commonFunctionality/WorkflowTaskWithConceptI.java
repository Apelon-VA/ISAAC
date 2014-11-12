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
package gov.va.isaac.interfaces.gui.views.commonFunctionality;

import java.util.UUID;

/**
 * {@link WorkflowTaskWithConceptI}
 * 
 * An interface that adds concept API for reuse by subtasks - extends {@link WorkflowTaskI}.
 * 
 * @author <a href="mailto:jkniaz@apelon.com">Joel Kniaz</a> 
 */

//Note, this one is not Contract annotated, as it should never be requested by itself - only sub interfaces.
public abstract interface WorkflowTaskWithConceptI extends WorkflowTaskI
{
	/**
	 * Get the selected concept as UUID.
	 * @return conceptUuid the selected concept as UUID
	 */
	public UUID getConceptUuid();
	
	/**
	 * Get the selected concept as int.
	 * @return conceptUuid the selected concept as int
	 */
	public int getConceptNid();
}
