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


/**
 * WorkflowTaskViewI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 * An interface that allows that specifies task IDs for reuse in sub interfaces.
 * 
 */
// No contract annotation here - this should never be requested by itself - only sub implementations
public abstract interface WorkflowTaskI {
	/**
	 * @param taskId the long workflow task id of the task, the details of which are to be displayed
	 */
	public void setTask(long taskId);
	
	/**
	 * @return workflow task id of the task, which is being displayed
	 */
	public Long getTask();
}
