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
package gov.va.isaac.workflow.demo;

import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWorkflowRuntimeEngineFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.JaxbSerializationProvider;

/**
 *
 * @author alo
 */
public class RemoteServiceDemo {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        LocalWorkflowRuntimeEngineBI wfEngine = LocalWorkflowRuntimeEngineFactory.getRuntimeEngine();

        Map<String, Object> params = new HashMap<String, Object>();
        String rndId = UUID.randomUUID().toString();
        params.put("componentId", rndId);
        params.put("componentName", "Component with Id:" + rndId);
        //wfEngine.requestProcessInstanceCreation("terminology-authoring.test1", params);
        System.out.println("Created remote instance: " + rndId);

//        List<TaskSummary> myTasks =  wfEngine.getRemoteTaskService().getTasksOwned("alejandro", "en-UK");
//        
//        Task firstTask =  wfEngine.getRemoteTaskService().getTaskById(myTasks.get(0).getId());
//        
//        Map<String, Object> vmap = wfEngine.getVariablesMapForTaskId(firstTask.getId());
//        
//        String componentUuidStr = (String) vmap.get("componentId");
        List<TaskSummary> possibleTasks = wfEngine.getRemoteTaskService().getTasksAssignedAsPotentialOwner("alejandro", "en-UK");

        for (TaskSummary loopTask : possibleTasks) {
            System.out.println("Task: " + loopTask.getName() + " - " + loopTask.getId());
            Map<String, Object> vmap2 = wfEngine.getVariablesMapForTaskId(loopTask.getId());
            for (String loopKey : vmap2.keySet()) {
                System.out.println("   Variable: " + loopKey + " - " + vmap2.get(loopKey));
                if (vmap2.get(loopKey).equals(rndId)) {
                    System.out.println("   ˆˆˆˆˆ  Match with created instance - SUCCESS");
                    System.out.println("");
                }
            }
        }

//        Long taskToClaim = null;
//        System.out.println("possibleTasks count: " + possibleTasks.size());
//        for (TaskSummary tsum : possibleTasks) {
//            System.out.println(tsum.getName() + " " + tsum.getId());
//            if (tsum.getActualOwner() == null) {
//                taskToClaim = tsum.getId();
//                break;
//            }
//        }
//        System.out.println(taskToClaim);
//        wfEngine.getRemoteTaskService().claim(taskToClaim, "alejandro");
//        Task myTask = wfEngine.getRemoteTaskService().getTaskById(13);
//        Content contentById = wfEngine.getRemoteTaskService().getContentById(myTask.getTaskData().getDocumentContentId());
//        JaxbContent jj = (JaxbContent) contentById;
//        wfEngine.getRemoteTaskService().start(firstTask.getId(), "alejandro");
//        Map<String, Object> cparams = new HashMap<String, Object>();
//        cparams.put("out_evaluation", "From ISAAC!");
//        
//        wfEngine.getRemoteTaskService().complete(firstTask.getId(), "alejandro", cparams);
//        System.out.println("Looking for attachments...");
//        List<Attachment> attachments = myTask.getTaskData().getAttachments();
//        for (Attachment loopAttachment : attachments) {
//            System.out.println("loopAttachment: " + loopAttachment.getName());
//        }
//        
//        System.out.println("Looking for comments...");
//        List<Comment> comments = myTask.getTaskData().getComments();
//        for (Comment loopComment : comments) {
//            System.out.println("loopComment: " + loopComment.getText());
//        }
//        
//        System.out.println("Looking for instance data...");
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

}
