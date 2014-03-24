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
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * @author alo
 */
public interface LocalTaskServiceBI {
    
    public Task getTaskById(long l);
    
    public List<TaskSummary> getTasksOwned(String string, String string1);

    public List<TaskSummary> getTasksOwnedByStatus(String string, List<Status> list, String string1);
    
    public List<TaskSummary> getTasksByVariousFields(Map<String, List<?>> map, boolean bln);
    
    public void complete(long l, String string, Map<String, Object> map);

    public void delegate(long l, String string, String string1);

    public void exit(long l, String string);

    public void fail(long l, String string, Map<String, Object> map);

    public void forward(long l, String string, String string1);
    
    public void release(long l, String string);

    public void resume(long l, String string);

    public void skip(long l, String string);

    public void start(long l, String string);

    public void stop(long l, String string);

    public void suspend(long l, String string);

    public void nominate(long l, String string, List<OrganizationalEntity> list);

    public Content getContentById(long l);

    public Attachment getAttachmentById(long l);
    
}
