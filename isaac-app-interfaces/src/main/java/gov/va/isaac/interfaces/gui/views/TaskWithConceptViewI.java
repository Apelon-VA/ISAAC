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
package gov.va.isaac.interfaces.gui.views;

import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link TaskWithConceptViewI}
 * 
 * An interface that requests a pop up window that displays the details of a concept.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface TaskWithConceptViewI extends ViewI
{   
	public void setTask(long taskId);
	public Long getTask();

	/**
	 * Update the view to get the selected concept as UUID.
	 * @param conceptUuid
	 */
	public UUID getConceptUuid();
	
	/**
	 * Update the view to get the selected concept as int.
	 * @param conceptUuid
	 */
	public int getConceptNid();

	/**
	 * Update the view to show the selected concept.
	 * @param conceptUuid
	 */
	public void setViewMode(ConceptViewMode mode);

	/**
	 * Update the view to show the selected concept.
	 * @param conceptUuid
	 */
	public ConceptViewMode getViewMode();
}
