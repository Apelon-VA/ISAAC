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
import java.util.Map;
import java.util.Objects;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * @author alo
 */
public class LocalTask {

    private Long id;
    private String name;
    private String componentId;
    private String componentName;
    private String status;
    private String owner;
    private String action;
    private String actionStatus;

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

}
