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
package gov.va.isaac.workflow.sync;

import gov.va.isaac.AppContext;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.TaskActionStatus;
import gov.va.isaac.workflow.engine.RemoteWfEngine;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alo
 */
public class TasksFetcher {

    private final String locale = "en-UK";

    private TaskService remoteTaskService;
    private LocalTasksServiceBI persistenceApi;
    private List<Status> availableStatuses;
    private List<Status> reservedStatuses;
    
    private static final Logger log = LoggerFactory.getLogger(TasksFetcher.class);

    public TasksFetcher() throws RemoteException {
        persistenceApi = AppContext.getService(LocalTasksServiceBI.class);
        remoteTaskService = AppContext.getService(RemoteWfEngine.class).getRemoteTaskService();
        availableStatuses = new ArrayList<Status>();
        availableStatuses.add(Status.Ready);
        availableStatuses.add(Status.Created);
        reservedStatuses = new ArrayList<Status>();
        reservedStatuses.add(Status.Reserved);
        reservedStatuses.add(Status.InProgress);
    }

    public String fetchTasks(String userId) throws DatastoreException, RemoteException {
        String result = "";
        int countNew = 0;
        int countUpdated = 0;
        int countRemoved = 0;
        List<TaskSummary> tasksSummaries = remoteTaskService.getTasksOwnedByStatus(userId, reservedStatuses, locale);
        for (TaskSummary loopTask : tasksSummaries) {
            log.debug("Owned: " + loopTask.getId() + " - " + loopTask.getName() + " - " + loopTask.getStatus().name() + " - " + loopTask.getActualOwner());
            LocalTask dbTask = persistenceApi.getTask(loopTask.getId());
            if (dbTask == null) {
                log.debug("Task is new: " + loopTask.getId());
                LocalTask loopLocal = new LocalTask(loopTask, true);
                persistenceApi.saveTask(loopLocal);
                countNew++;
            } else if (!dbTask.getOwner().equals(loopTask.getActualOwner().getId()) || !dbTask.getStatus().name().equals(loopTask.getStatus().name())) {
                log.debug("Task has changed: " + loopTask.getId());
                LocalTask loopLocal = new LocalTask(loopTask, true);
                persistenceApi.saveTask(loopLocal);
                countUpdated++;
            } else {
                log.debug("Task: " + loopTask.getId() + " No changes");
            }
        }
        List<LocalTask> openOwnedTasks = persistenceApi.getOpenOwnedTasks();
        log.debug("Looking for missing tasks. LocalCount = " + openOwnedTasks.size() + ", FetchCount = " + tasksSummaries.size());
        for (LocalTask loopLocalTask : openOwnedTasks) {
            boolean isInFetchCursor = false;
            for (TaskSummary loopCursorTask : tasksSummaries) {
                if (loopCursorTask.getId() == loopLocalTask.getId()) {
                    isInFetchCursor = true;
                }
            }
            if (!isInFetchCursor) {
                log.info("Missing task: " + loopLocalTask.getId());
                loopLocalTask.setStatus(Status.Obsolete);
                loopLocalTask.setActionStatus(TaskActionStatus.Canceled);
                persistenceApi.saveTask(loopLocalTask);
                countRemoved++;
            }
        }
        result = "Tasks -> New: " + countNew + " Updated: " + countUpdated + " Removed: " + countRemoved; 
        return result;
    }

    public void claimBatch(String userId, Integer limit) throws Exception {
        int count = 0;
        List<TaskSummary> tasksSummaries = remoteTaskService.getTasksAssignedAsPotentialOwnerByStatus(userId, availableStatuses, locale);
        for (TaskSummary loopTask : tasksSummaries) {
            log.debug("Claiming: " + loopTask.getId() + " - " + loopTask.getName() + " - " + loopTask.getStatus().name() + " - " + loopTask.getActualOwner());
            remoteTaskService.claim(loopTask.getId(), userId);
            count++;
            if (count >= limit) {
                break;
            }
        }
    }
}
