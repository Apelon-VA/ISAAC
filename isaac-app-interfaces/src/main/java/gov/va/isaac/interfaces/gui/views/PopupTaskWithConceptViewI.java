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
 * {@link PopupTaskWithConceptViewI}
 * 
 * An interface that allows the creation of an PopupTaskWithConceptViewI implementation,
 * which will be a JavaFX component that extends/implements both {@link TaskWithConceptViewI}
 * and {@link PopupViewI}.  This popup panel is intended to allow display and manipulation of
 * a specified existing workflow task, which itself contains a displayable concept
 *
 * @author <a href="jkniaz@apelon.com">Joel Kniaz</a>
 */
@Contract
public interface PopupTaskWithConceptViewI extends TaskWithConceptViewI, PopupViewI
{   
	/**
	 * Get the specified workflow task's concept as UUID
	 * 
	 * @return UUID the UUID of the displayed concept
	 */
	public UUID getConceptUuid();
	
	/**
	 * Get the specified workflow task's concept as int NID
	 * 
	 * @return int the int NID of the displayed concept
	 */
	public int getConceptNid();

	/**
	 * Update the view to show the selected concept.
	 * @param ConceptViewMode the desired {@link ConceptViewMode} to display concept
	 */
	public void setViewMode(ConceptViewMode mode);

	/**
	 * Get the concept view mode used to show the selected concept.
	 * 
	 * @return ConceptViewMode the {@link ConceptViewMode} for displayed concept
	 */
	public ConceptViewMode getViewMode();
}
