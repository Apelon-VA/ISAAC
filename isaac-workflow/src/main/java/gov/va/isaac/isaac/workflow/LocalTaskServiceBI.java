/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.isaac.isaac.workflow;

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
