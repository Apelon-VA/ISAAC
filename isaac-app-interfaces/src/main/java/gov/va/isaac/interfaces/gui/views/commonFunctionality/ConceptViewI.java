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

import gov.va.isaac.interfaces.gui.constants.ConceptViewMode;
import gov.va.isaac.interfaces.gui.views.EmbeddableViewI;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link ConceptViewI}
 * 
 * An interface that requests a window that displays the details of a concept.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface ConceptViewI extends EmbeddableViewI
{
	/**
	 * Update the view to show the selected concept.
	 * @param conceptUuid the selected concept as UUID
	 */
	public void setConcept(UUID conceptUUID);
	
	/**
	 * Update the view to show the selected concept.
	 * @param conceptNid the selected concept as int NID
	 */
	public void setConcept(int conceptNid);
	
	/**
	 * Get the selected concept as UUID.
	 * @return conceptUuid the selected concept as UUID
	 */
	public UUID getConceptUuid();
	
	/**
	 * Get the selected concept as int.
	 * @return conceptNid the selected concept as int NID
	 */
	public int getConceptNid();

	/**
	 * Update the view to show the selected concept using the specified ConceptViewMode.
	 * @param conceptUuid the ConceptViewMode used to show the selected concept
	 */
	public void setViewMode(ConceptViewMode mode);

	/**
	 * Get the ConceptViewMode used to display the selected concept.
	 * @return conceptUuid the ConceptViewMode used to display the selected concept
	 */
	public ConceptViewMode getViewMode();
}
