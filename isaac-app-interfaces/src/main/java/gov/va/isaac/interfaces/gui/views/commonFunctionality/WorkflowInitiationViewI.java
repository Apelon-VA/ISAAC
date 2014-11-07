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
 * WorkflowInitiationViewI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.interfaces.gui.views.commonFunctionality;

import gov.va.isaac.interfaces.gui.views.PopupViewI;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;

/**
 * WorkflowInitiationViewI
 * 
 * An interface that allows the creation of an WorkflowInitiationViewI implementation,
 * which will be a JavaFX component that extends/implements {@link PopupViewI}.
 * This popup panel is intended to allow initiation of a new workflow instance
 * associated with a specified existing component
 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface WorkflowInitiationViewI extends PopupViewI {
	/**
	 * @param uuid the UUID of the existing component or concept
	 * associated with the new task to be created
	 */
	public void setComponent(UUID uuid);
	/**
	 * @return UUID the uuid of the existing component or concept
	 * associated with the new task created or to be created
	 */
	public UUID getComponentUuid();

	/**
	 * @param nid the int NID of the existing component or concept
	 * associated with the new task to be created
	 */
	public void setComponent(int nid);

	/**
	 * @return the int NID of the existing component or concept
	 * associated with the new task created or to be created
	 */
	public int getComponentNid();
}
