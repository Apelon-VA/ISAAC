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

import java.net.URL;
import java.util.Map;

import org.kie.api.task.TaskService;

/**
 *
 * @author alo
 */
public interface LocalWorkflowRuntimeEngineBI {
    
    public void setRemoteData(URL url, String userId, String password, String deploymentId);
    
    //TODO this API needs to throw errors, not silently eat them - also, a cancel mechanism would be nice
    public void synchronizeWithRemote();
    
    public void requestProcessInstanceCreationToServer(ProcessInstanceCreationRequestI instanceRequest);
    
    //TODO these APIs need to throw errors, not silently eat them
    public ProcessInstanceServiceBI getProcessInstanceService();
    
    public LocalTasksServiceBI getLocalTaskService();
    
    public TaskService getRemoteTaskService();
    
    public Map<String,Object> getVariablesMapForTaskId(Long taskId);
    
    //TODO this API needs to throw errors, not silently eat them - also, a cancel mechanism would be nice
    public void claim(Integer count, String userId);

    //TODO this API needs to throw errors, not silently eat them - also, a cancel mechanism would be nice
    public void release(Long taskId);
    
}
