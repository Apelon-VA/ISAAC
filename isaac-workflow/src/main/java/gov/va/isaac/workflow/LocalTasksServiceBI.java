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

import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;
import org.kie.api.task.model.Status;

/**
 *
 * @author alo
 */
@Contract
public interface LocalTasksServiceBI {
	public static interface ActionEvent {
		Long getTaskId();
		Action getAction();
		TaskActionStatus getActionStatus();
		Map<String, String> getOutputVariables();
	}
	@FunctionalInterface
    public static interface ActionEventListener {
    	void handle(ActionEvent actionEvent);
    }

    List<LocalTask> getOpenOwnedTasks() throws DatastoreException;
    List<LocalTask> getOpenOwnedTasksByComponentId(UUID componentId) throws DatastoreException;
    List<LocalTask> getOwnedTasksByStatus(Status status) throws DatastoreException;
    List<LocalTask> getOwnedTasksByActionStatus(TaskActionStatus actionStatus) throws DatastoreException;
    LocalTask getTask(Long id) throws DatastoreException;
    List<LocalTask> getTasks() throws DatastoreException;
    List<LocalTask> getTasksByComponentId(UUID componentId) throws DatastoreException;
    void saveTask(LocalTask task) throws DatastoreException;

    void completeTask(Long taskId, Map<String, String> outputVariables) throws DatastoreException;
    void releaseTask(Long taskId) throws DatastoreException;

    void setAction(Long taskId, Action action, Map<String, String> outputVariables) throws DatastoreException;
    void setAction(Long taskId, Action action, TaskActionStatus status, Map<String, String> outputVariables) throws DatastoreException;

    void addActionEventListener(ActionEventListener listener);
    void removeActionEventListener(ActionEventListener listener);
    
    void createSchema() throws DatastoreException;
    void dropSchema() throws DatastoreException;
}
