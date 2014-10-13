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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author alo
 */
@Contract
public interface ProcessInstanceServiceBI {

    ProcessInstanceCreationRequestI createRequest(String processName, UUID componentId, String componentName, String author, Map<String, String> variables) throws DatastoreException;

    List<ProcessInstanceCreationRequestI> getOpenOwnedRequests(String owner) throws DatastoreException;

    List<ProcessInstanceCreationRequestI> getOpenOwnedRequestsByComponentId(String owner, UUID componentId) throws DatastoreException;

    List<ProcessInstanceCreationRequestI> getOwnedRequestsByStatus(String owner, ProcessInstanceCreationRequestI.RequestStatus status) throws DatastoreException;

    ProcessInstanceCreationRequestI getRequest(int id) throws DatastoreException;

    ProcessInstanceCreationRequestI getRequestByWfId(Long wfId) throws DatastoreException;

    List<ProcessInstanceCreationRequestI> getRequests() throws DatastoreException;

    List<ProcessInstanceCreationRequestI> getRequestsByComponentId(UUID componentId) throws DatastoreException;

    void updateRequestStatus(int id, ProcessInstanceCreationRequestI.RequestStatus status, String syncMessage, Long wfId) throws DatastoreException;
    
    void createSchema() throws DatastoreException;
    
    void dropSchema() throws DatastoreException;
    
}
