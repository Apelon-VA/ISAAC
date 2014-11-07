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
 * ComponentWorkflowServiceI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.interfaces.workflow;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;

/**
 * ComponentWorkflowServiceI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface ComponentWorkflowServiceI {
	
	/**
	 * A non-blocking call to perform a sync.  Sync happens in the background, no errors are reported back (even if they happen)
	 * This call returns immediately.
	 */
	public abstract void synchronizeWithRemote();

	public abstract ProcessInstanceCreationRequestI createNewComponentWorkflowRequest(
			String preferredDescription,
			UUID UUID,
			String processName,
			Map<String,String> variables) throws IOException;
	
	public abstract void releaseTask(Long taskId) throws IOException;
}