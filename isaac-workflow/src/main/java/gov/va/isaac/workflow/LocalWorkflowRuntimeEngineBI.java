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

import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.rmi.RemoteException;
import java.util.Map;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author alo
 */
@Contract
public interface LocalWorkflowRuntimeEngineBI {

    public void requestProcessInstanceCreationToServer(ProcessInstanceCreationRequestI instanceRequest) throws RemoteException, DatastoreException;
    
    public Map<String,Object> getVariablesMapForTaskId(Long taskId) throws RemoteException;
    
    public void claim(Integer count) throws RemoteException;

    public void release(Long taskId) throws DatastoreException;
    
}
