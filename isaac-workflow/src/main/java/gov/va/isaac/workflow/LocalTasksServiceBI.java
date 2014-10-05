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

import java.util.List;
import java.util.Map;

import org.kie.api.task.model.Status;

/**
 *
 * @author alo
 */
public interface LocalTasksServiceBI {
    
    List<LocalTask> getOpenOwnedTasks();
    List<LocalTask> getOpenOwnedTasksByComponentId(String componentId);
    List<LocalTask> getOwnedTasksByStatus(Status status);
    List<LocalTask> getOwnedTasksByActionStatus(TaskActionStatus actionStatus);
    LocalTask getTask(Long id);
    List<LocalTask> getTasks();
    List<LocalTask> getTasksByComponentId(String componentId);
    void saveTask(LocalTask task);

    void completeTask(Long taskId, Map<String, String> outputVariables);
    void releaseTask(Long taskId);

    void setAction(Long taskId, Action action, Map<String, String> outputVariables);
    void setAction(Long taskId, Action action, TaskActionStatus status, Map<String, String> outputVariables);

    void commit();
    void createSchema();
    void dropSchema();
    void closeConnection();
    
}
