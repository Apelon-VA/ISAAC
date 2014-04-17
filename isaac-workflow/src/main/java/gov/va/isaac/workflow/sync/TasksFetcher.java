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

import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.persistence.LocalTasksApi;
import java.util.ArrayList;
import java.util.List;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * @author alo
 */
public class TasksFetcher {

    private final String locale = "en-UK";

    private TaskService remoteTaskService;
    private LocalTasksApi persistenceApi;
    private List<Status> availableStatuses;
    private List<Status> reservedStatuses;

    public TasksFetcher(TaskService remoteTaskService, LocalTasksApi persistenceApi) {
        this.remoteTaskService = remoteTaskService;
        this.persistenceApi = persistenceApi;
        availableStatuses = new ArrayList<Status>();
        availableStatuses.add(Status.Ready);
        availableStatuses.add(Status.Created);
        reservedStatuses = new ArrayList<Status>();
        reservedStatuses.add(Status.Reserved);
        reservedStatuses.add(Status.InProgress);
    }

    public void fetchTasks(String userId) throws Exception {
        List<TaskSummary> tasksSummaries = remoteTaskService.getTasksOwnedByStatus(userId, reservedStatuses, locale);
        for (TaskSummary loopTask : tasksSummaries) {
            //System.out.println("Owned: " + loopTask.getId() + " - " + loopTask.getName() + " - " + loopTask.getStatus().name() + " - " + loopTask.getActualOwner());
            LocalTask dbTask = persistenceApi.getTask(loopTask.getId());
            if (dbTask == null) {
                System.out.println("Task is new: " + loopTask.getId());
                LocalTask loopLocal = new LocalTask(loopTask, true);
                persistenceApi.saveTask(loopLocal);
            } else if (!dbTask.getOwner().equals(loopTask.getActualOwner().getId()) || !dbTask.getStatus().equals(loopTask.getStatus().name())) {
                System.out.println("Task has changed: " + loopTask.getId());
                LocalTask loopLocal = new LocalTask(loopTask, true);
                persistenceApi.saveTask(loopLocal);
            } else {
                System.out.println("Task: " + loopTask.getId() + " No changes");
            }
        }
        List<LocalTask> openOwnedTasks = persistenceApi.getOpenOwnedTasks(userId);
        System.out.println("Looking for missing tasks. LocalCount = " + openOwnedTasks.size() + ", FetchCount = " + tasksSummaries.size());
        for (LocalTask loopLocalTask : openOwnedTasks) {
            boolean isInFetchCursor = false;
            for (TaskSummary loopCursorTask : tasksSummaries) {
                if (loopCursorTask.getId() == loopLocalTask.getId()) {
                    isInFetchCursor = true;
                }
            }
            if (!isInFetchCursor) {
                System.out.println("Missing task: " + loopLocalTask.getId());
                Task missingTask = remoteTaskService.getTaskById(loopLocalTask.getId());
                LocalTask missingTaskLocal = new LocalTask(missingTask, true);
                persistenceApi.saveTask(missingTaskLocal);
            }
        }
        persistenceApi.commit();
    }

    public void claimBatch(String userId, Integer limit) throws Exception {
        int count = 0;
        List<TaskSummary> tasksSummaries = remoteTaskService.getTasksAssignedAsPotentialOwnerByStatus(userId, availableStatuses, locale);
        for (TaskSummary loopTask : tasksSummaries) {
            //System.out.println("Claiming: " + loopTask.getId() + " - " + loopTask.getName() + " - " + loopTask.getStatus().name() + " - " + loopTask.getActualOwner());
            remoteTaskService.claim(loopTask.getId(), userId);
            count++;
            if (count >= limit) {
                break;
            }
        }
    }

}
