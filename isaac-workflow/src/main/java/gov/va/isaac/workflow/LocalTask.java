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

import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * @author alo
 */
public class LocalTask {
	
	public static final Comparator<LocalTask> ID_COMPARATOR = (LocalTask o1, LocalTask o2) -> (Long.valueOf(o1.id).compareTo(Long.valueOf(o2.id)));
	public static final Comparator<LocalTask> NAME_COMPARATOR = (LocalTask o1, LocalTask o2) -> o1.name.compareTo(o2.name);

	// Task ID in KIE
    private Long id;

    // Name of the step in the workflow (i.e. Review, Approve, etc)
    private String name;

    // Id of the associated component, usually UUID
    private String componentId;

    // Name of the component
    private String componentName;

    // Current status of the task, in the task lifecycle
    private String status;

    // User owner of the task, name that matches KIE name
    private String owner;

    // Proposed action, added by the user in the client
    private String action;

    // Status of the action execution, will be completed when is synchronized and executed in the server
    private String actionStatus;

    // Input variables for the user, come from the KIE Task
    private Map<String, String> inputVariables;

    // Output variables from the user, created by the user and sent to KIE to influence on the workflow flow
    private Map<String, String> outputVariables;

    public LocalTask() {
    }

    public LocalTask(TaskSummary summary, boolean fetchAttachments) {
        this.id = summary.getId();
        this.name = summary.getName();
        this.status = summary.getStatus().name();
        if (summary.getActualOwner() != null) {
            this.owner = summary.getActualOwner().getId();
        } else {
            this.owner = "";
        }
        if (fetchAttachments) {
            LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
            Map<String, Object> vmap = wfEngine.getVariablesMapForTaskId(summary.getId());
            this.componentId = (String) vmap.get("in_componentId");
            this.componentName = (String) vmap.get("in_componentName");
            this.setInputVariables(new HashMap<String, String>());
            for (String key : vmap.keySet()) {
                this.getInputVariables().put(key, vmap.get(key).toString());
            }
        }
    }

    public LocalTask(Task task, boolean fetchAttachments) {
        this.id = task.getId();
        this.name = task.getNames().iterator().next().getText();
        this.status = task.getTaskData().getStatus().name();
        if (task.getTaskData().getActualOwner() != null) {
            this.owner = task.getTaskData().getActualOwner().getId();
        } else {
            this.owner = "";
        }

        if (fetchAttachments) {
            LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();
            Map<String, Object> vmap = wfEngine.getVariablesMapForTaskId(task.getId());
            this.componentId = (String) vmap.get("in_componentId");
            this.componentName = (String) vmap.get("in_componentName");
            this.setInputVariables(new HashMap<String, String>());
            for (String key : vmap.keySet()) {
                this.getInputVariables().put(key, vmap.get(key).toString());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    public Map<String, String> getInputVariables() {
        return inputVariables;
    }

    public void setInputVariables(Map<String, String> inputVariables) {
        this.inputVariables = inputVariables;
    }

    public Map<String, String> getOutputVariables() {
        return outputVariables;
    }

    public void setOutputVariables(Map<String, String> outputVariables) {
        this.outputVariables = outputVariables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocalTask other = (LocalTask) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.componentId, other.componentId)) {
            return false;
        }
        if (!Objects.equals(this.componentName, other.componentName)) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.owner, other.owner)) {
            return false;
        }
        return true;
    }

	@Override
	public String toString() {
		return "LocalTask [id=" + id + ", name=" + name + ", componentId="
				+ componentId + ", componentName=" + componentName
				+ ", status=" + status + ", owner=" + owner + ", action="
				+ action + ", actionStatus=" + actionStatus + "]";
	}
}
