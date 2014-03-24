/*
 * Copyright 2014 alo.
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
package gov.va.isaac.workflow.persistence;

/**
 *
 * @author alo
 */
public class LocalTasksApiDemo {
    
    public static void main(String[] args) {
        LocalTasksApi tapi = new LocalTasksApi();
        
        System.out.print("Creting Schema...  ");
        tapi.createSchema();
        System.out.println("OK");
        
        LocalTask task = new LocalTask();
        task.setId(2L);
        task.setName("task 2 name");
        task.setComponentId("componentId");
        task.setComponentName("componentName");
        
        System.out.print("Saving task 2...  ");
        tapi.saveLocalTask(task);
        tapi.commit();
        System.out.println("OK");
        
        System.out.print("Getting task 2...  ");
        LocalTask retrievedTask = tapi.getLocalTask(1L);
        System.out.println("Done, Name: " + retrievedTask.getName() + " cid: " + retrievedTask.getComponentId() + " cname: " + retrievedTask.getComponentName());
        //tapi.closeConnection();
        
    }
    
}
