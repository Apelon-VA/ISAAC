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
import java.io.IOException;
import org.jvnet.hk2.annotations.Contract;

/**
 * WorkflowTaskViewI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 * An interface that allows the creation of an WorkflowTaskViewI implementation,
 * which will be a JavaFX component that extends/implements {@link PopupViewI}.
 * This popup panel is intended to allow display of characteristics and details
 * of a specified existing workflow task
 * 
 */
@Contract
public interface WorkflowTaskDetailsViewI extends PopupViewI, WorkflowTaskI {
	
	/**
	 * Release the specified task.
	 * @param taskId
	 * @throws IOException
	 */
	public void releaseTask(long taskId) throws IOException, UnsupportedOperationException;
}
