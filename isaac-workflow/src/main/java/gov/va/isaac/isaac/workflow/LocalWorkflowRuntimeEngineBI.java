/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.isaac.isaac.workflow;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kie.api.task.TaskService;

/**
 *
 * @author alo
 */
public interface LocalWorkflowRuntimeEngineBI {
    
    public void setRemoteData(URL url, String userId, String password, String deploymentId);
    
    public void synchronizeWithRemote();
    
    public ProcessInstanceCreationRequest requestProcessInstanceCreation(String processName, Map<String, Object> params);
    
    public List<ProcessInstanceCreationRequest> getPendingProcessInstanceRequests();
    
    public List<ProcessInstanceCreationRequest> getCompletedProcessInstanceRequests();

    public LocalTaskServiceBI getLocalTaskService();
    
    public TaskService getRemoteTaskService();
    
    public Map<String,Object> getVariablesMapForTaskId(Long taskId);
    
}
