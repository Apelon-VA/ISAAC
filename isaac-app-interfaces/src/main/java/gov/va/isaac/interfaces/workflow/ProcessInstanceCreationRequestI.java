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
 * ProcessInstanceCreationRequestI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.interfaces.workflow;

import java.util.Map;

/**
 * ProcessInstanceCreationRequestI
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public interface ProcessInstanceCreationRequestI {
	public enum RequestStatus {
		REQUESTED, REJECTED, CREATED
	}

	public abstract String getProcessName();

	public abstract void setProcessName(String processName);

	public abstract Map<String, Object> getParams();

	public abstract void setParams(Map<String, Object> params);

	public abstract String getUserId();

	public abstract void setUserId(String userId);

	public abstract Long getRequestTime();

	public abstract void setRequestTime(Long requestTime);

	public abstract Long getSyncTime();

	public abstract void setSyncTime(Long syncTime);

	public abstract RequestStatus getStatus();

	public abstract void setStatus(RequestStatus status);

	public abstract String getSyncMessage();

	public abstract void setSyncMessage(String syncMessage);

	public abstract String getComponentId();

	public abstract void setComponentId(String componentId);

	public abstract String getComponentName();

	public abstract void setComponentName(String componentName);

	public abstract int getId();

	public abstract void setId(int id);

	public abstract long getWfId();

	public abstract void setWfId(long wfId);

	public abstract Map<String, String> getVariables();

	public abstract void setVariables(Map<String, String> variables);

}