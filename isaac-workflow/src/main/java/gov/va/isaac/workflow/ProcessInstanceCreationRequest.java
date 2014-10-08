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

import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;

import java.util.Map;

/**
 *
 * @author alo
 */
public class ProcessInstanceCreationRequest implements ProcessInstanceCreationRequestI {

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
    private Map<String, String> variables;

    public ProcessInstanceCreationRequest() {
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getProcessName()
     */
    @Override
    public String getProcessName() {
        return processName;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setProcessName(java.lang.String)
     */
    @Override
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getParams()
     */
    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setParams(java.util.Map)
     */
    @Override
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getUserId()
     */
    @Override
    public String getUserId() {
        return userId;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setUserId(java.lang.String)
     */
    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getRequestTime()
     */
    @Override
    public Long getRequestTime() {
        return requestTime;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setRequestTime(java.lang.Long)
     */
    @Override
    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getSyncTime()
     */
    @Override
    public Long getSyncTime() {
        return syncTime;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setSyncTime(java.lang.Long)
     */
    @Override
    public void setSyncTime(Long syncTime) {
        this.syncTime = syncTime;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getStatus()
     */
    @Override
    public RequestStatus getStatus() {
        return status;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setStatus(gov.va.isaac.workflow.ProcessInstanceCreationRequest.RequestStatus)
     */
    @Override
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getSyncMessage()
     */
    @Override
    public String getSyncMessage() {
        return syncMessage;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setSyncMessage(java.lang.String)
     */
    @Override
    public void setSyncMessage(String syncMessage) {
        this.syncMessage = syncMessage;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getComponentId()
     */
    @Override
    public String getComponentId() {
        return componentId;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setComponentId(java.lang.String)
     */
    @Override
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getComponentName()
     */
    @Override
    public String getComponentName() {
        return componentName;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setComponentName(java.lang.String)
     */
    @Override
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getId()
     */
    @Override
    public int getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setId(int)
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#getWfId()
     */
    @Override
    public long getWfId() {
        return wfId;
    }

    /* (non-Javadoc)
     * @see gov.va.isaac.workflow.ProcessInstanceCreationRequestI#setWfId(long)
     */
    @Override
    public void setWfId(long wfId) {
        this.wfId = wfId;
    }

    @Override
    public Map<String, String> getVariables() {
        return variables;
    }

    @Override
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
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
