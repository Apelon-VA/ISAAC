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
package gov.va.isaac.workflow.impl;

import gov.va.isaac.workflow.LocalTaskServiceBI;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.ProcessInstanceCreationRequest;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.services.client.api.RemoteRestRuntimeFactory;

/**
 *
 * @author alo
 */
public class LocalWfEngine implements LocalWorkflowRuntimeEngineBI {

    private URL url;
    private String userId;
    private String password;
    private String deploymentId;
    private RuntimeEngine remoteEngine;

    public LocalWfEngine(URL url, String userId, String password, String deploymentId) {
        this.url = url;
        this.userId = userId;
        this.password = password;
        this.deploymentId = deploymentId;
    }

    public LocalWfEngine() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public URL getUrl() {
        return url;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    @Override
    public void setRemoteData(URL url, String userId, String password, String deploymentId) {
        this.url = url;
        this.userId = userId;
        this.password = password;
        this.deploymentId = deploymentId;
    }

    @Override
    public void synchronizeWithRemote() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ProcessInstanceCreationRequest requestProcessInstanceCreation(String processName, Map<String, Object> params) {
        KieSession session = getRemoteEngine().getKieSession();
        session.startProcess(processName, params);
        return null;
    }

    @Override
    public List<ProcessInstanceCreationRequest> getPendingProcessInstanceRequests() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ProcessInstanceCreationRequest> getCompletedProcessInstanceRequests() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalTaskServiceBI getLocalTaskService() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TaskService getRemoteTaskService() {
        return getRemoteEngine().getTaskService();
    }

    private RuntimeEngine getRemoteEngine() {
        if (remoteEngine != null) {
            return remoteEngine;
        } else {
            RemoteRestRuntimeFactory restSessionFactory = new RemoteRestRuntimeFactory(deploymentId, url, userId, password);
            remoteEngine = restSessionFactory.newRuntimeEngine();
            return remoteEngine;
        }
    }

    @Override
    public Map<String, Object> getVariablesMapForTaskId(Long taskId) {
        Task task = getRemoteEngine().getTaskService().getTaskById(taskId);
        Content contentById = getRemoteEngine().getTaskService().getContentById(task.getTaskData().getDocumentContentId());
        JaxbContent jaxbTaskContent = (JaxbContent) contentById;
        return jaxbTaskContent.getContentMap();
        //return new HashMap<>();
    }

}
