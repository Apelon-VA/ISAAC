/* 
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.workflow;

import java.util.Map;

/**
 *
 * @author alo
 */
public class ProcessInstanceCreationRequest {

    public enum RequestStatus {
        REQUESTED, REJECTED, CREATED
    }

    private int id;
    private String processName;
    private String componentId;
    private String componentName;
    private Map<String, Object> params;
    private String userId;
    private Long requestTime;
    private Long syncTime;
    private RequestStatus status;
    private String syncMessage;
    private long wfId;

    public ProcessInstanceCreationRequest() {
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    public Long getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Long syncTime) {
        this.syncTime = syncTime;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getSyncMessage() {
        return syncMessage;
    }

    public void setSyncMessage(String syncMessage) {
        this.syncMessage = syncMessage;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getWfId() {
        return wfId;
    }

    public void setWfId(long wfId) {
        this.wfId = wfId;
    }

	@Override
	public String toString() {
		return "ProcessInstanceCreationRequest [id=" + id + ", processName="
				+ processName + ", componentId=" + componentId
				+ ", componentName=" + componentName + ", params=" + params
				+ ", userId=" + userId + ", requestTime=" + requestTime
				+ ", syncTime=" + syncTime + ", status=" + status
				+ ", syncMessage=" + syncMessage + ", wfId=" + wfId + "]";
	}
    
}
