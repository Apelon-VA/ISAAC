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
package gov.va.isaac.workflow.persistence;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.workflow.ProcessInstanceCreationRequestI;
import gov.va.isaac.workflow.ProcessInstanceCreationRequest;
import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.exceptions.DatastoreException;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.jfree.util.Log;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link ProcessInstanceCreationRequestsAPI}
 *
 * @author alo
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class ProcessInstanceCreationRequestsAPI implements ProcessInstanceServiceBI {

    private final Logger log = LoggerFactory.getLogger(ProcessInstanceCreationRequestsAPI.class);
    private DataSource ds;
    
    private ProcessInstanceCreationRequestsAPI() {
        //For HK2 to construct
        ds = AppContext.getService(DatastoreManager.class).getDataSource();
        try
        {
            createSchema();
        }
        catch (DatastoreException e)
        {
            log.error("Create schema failed during init", e);
        }
    }

    @Override
    public ProcessInstanceCreationRequestI createRequest(String processName, UUID componentId, String componentName, String author, Map<String, String> variables) 
            throws DatastoreException {
        try (Connection conn = ds.getConnection()) {
            PreparedStatement psInsert = conn.prepareStatement("insert into PINST_REQUESTS(component_id, component_name, process_name, user_id, status, sync_message,"
                + " request_time, sync_time, wf_id, variables) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",PreparedStatement.RETURN_GENERATED_KEYS);
            if (componentId == null) {
            	log.error("Setting null componentId for ProcessInstanceCreationRequestsAPI.createRequest(): user_id={}, process_name={}, component={}, map={}", author, processName, componentName, variables);
            }
            psInsert.setString(1, componentId != null ? componentId.toString() : null);
            psInsert.setString(2, componentName);
            psInsert.setString(3, processName);
            psInsert.setString(4, author);
            psInsert.setString(5, ProcessInstanceCreationRequestI.RequestStatus.REQUESTED.name());
            psInsert.setString(6, "");
            psInsert.setLong(7, System.currentTimeMillis());
            psInsert.setLong(8, 0L);
            psInsert.setLong(9, 0L);
            psInsert.setString(10, serializeMap(variables));
            psInsert.executeUpdate();
            
            ProcessInstanceCreationRequestI result = new ProcessInstanceCreationRequest();
            result.setComponentId(componentId != null ? componentId.toString() : null);
            result.setComponentName(componentName);
            ResultSet generatedKeys = psInsert.getGeneratedKeys();
            if (generatedKeys.next()) {
                result.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating instance failed, no generated key obtained.");
            }
            result.setRequestTime(Long.MIN_VALUE);
            result.setProcessName(processName);
            result.setStatus(ProcessInstanceCreationRequestI.RequestStatus.REQUESTED);
            result.setUserId(author);
            result.setVariables(variables);
            conn.commit();
            return result;
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    @Override
    public void updateRequestStatus(int id, ProcessInstanceCreationRequestI.RequestStatus status, String syncMessage, Long wfId) throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            PreparedStatement psUpdateRequest = conn.prepareStatement("update PINST_REQUESTS set sync_message = ?, status = ?, wf_id = ? where id = ?");
            psUpdateRequest.setString(1, syncMessage);
            psUpdateRequest.setString(2, status.name());
            psUpdateRequest.setLong(3, (wfId == null ? 0 : wfId));
            psUpdateRequest.setInt(4, id);
            int rowCount = psUpdateRequest.executeUpdate();
            if (rowCount != 1)
            {
                throw new DatastoreException("updateRequestStatus failed to update any rows!");
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    @Override
    public List<ProcessInstanceCreationRequestI> getOpenOwnedRequests(String owner) throws DatastoreException {
        List<ProcessInstanceCreationRequestI> requests = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where user_id = '" + owner + "' and status = '"
                    + ProcessInstanceCreationRequestI.RequestStatus.REQUESTED.name() + "'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return requests;
    }

    @Override
    public List<ProcessInstanceCreationRequestI> getOwnedRequestsByStatus(String owner, ProcessInstanceCreationRequestI.RequestStatus status) throws DatastoreException {
        List<ProcessInstanceCreationRequestI> requests = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where user_id = '" + owner + "' and status = '" + status.name() + "'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return requests;
    }
    
    @Override
    public List<ProcessInstanceCreationRequestI> getOpenOwnedRequestsByComponentId(String owner, UUID componentId) throws DatastoreException {
        List<ProcessInstanceCreationRequestI> requests = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where user_id = '" + owner + "' and component_id = '" + componentId + "' and status = '"
                    + ProcessInstanceCreationRequestI.RequestStatus.REQUESTED.name() + "'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return requests;
    }

    @Override
    public List<ProcessInstanceCreationRequestI> getRequestsByComponentId(UUID componentId) throws DatastoreException {
        List<ProcessInstanceCreationRequestI> requests = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where component_id = '" + componentId + "'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return requests;
    }

    @Override
    public ProcessInstanceCreationRequestI getRequestByWfId(Long wfId) throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            ProcessInstanceCreationRequestI request = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where wf_id = " + wfId);
            if (!rs.next()) {
                // no results
            } else {
                request = readRequest(rs);
                return request;
            }

            return request;
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    @Override
    public List<ProcessInstanceCreationRequestI> getRequests() throws DatastoreException {
        List<ProcessInstanceCreationRequestI> requests = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return requests;
    }

    @Override
    public ProcessInstanceCreationRequestI getRequest(int id) throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            ProcessInstanceCreationRequestI request = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where id = " + id);
            if (!rs.next()) {
                // no results
            } else {
                request = readRequest(rs);
                return request;
            }

            return request;
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    private ProcessInstanceCreationRequestI readRequest(ResultSet rs) throws SQLException, DatastoreException {
        try
        {
            ProcessInstanceCreationRequestI request = new ProcessInstanceCreationRequest();
            request.setId(rs.getInt(1));
            request.setWfId(rs.getLong(2));
            request.setComponentId(rs.getString(3));
            request.setComponentName(rs.getString(4));
            request.setProcessName(rs.getString(5));
            request.setUserId(rs.getString(6));
            String status = rs.getString(7);
            switch (status) {
                case "CREATED":
                    request.setStatus(ProcessInstanceCreationRequestI.RequestStatus.CREATED);
                    break;
                case "REQUESTED":
                    request.setStatus(ProcessInstanceCreationRequestI.RequestStatus.REQUESTED);
                    break;
                case "REJECTED":
                    request.setStatus(ProcessInstanceCreationRequestI.RequestStatus.REJECTED);
                    break;
                default :
                    throw new DatastoreException("Unexpected 'status' found in DB: " + status);
            }
            request.setSyncMessage(rs.getString(8));
            request.setRequestTime(rs.getLong(9));
            request.setSyncTime(rs.getLong(10));
            request.setVariables(rs.getString(11).isEmpty() ? new HashMap<String, String>() : deserializeMap(rs.getString(11)));

            return request;
        }
        catch (NumberFormatException e)
        {
            throw new DatastoreException("Encountered an unparseable number in a field that should have held a number: " + e.getMessage());
        }
    }

    @Override
    public void createSchema() throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            log.info("Creating Workflow Process Instance Schema");
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "WORKFLOW", "PINST_REQUESTS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table PINST_REQUESTS "
                        + "(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, "
                        + "wf_id bigint, "
                        + "component_id varchar(40), "
                        + "component_name varchar(255), "
                        + "process_name varchar(255), "
                        + "user_id varchar(40), "
                        + "status varchar(40), "
                        + "sync_message varchar(255), "
                        + "request_time bigint, "
                        + "sync_time bigint, "
                        + "variables long varchar)");
                s.execute("create index WORKFLOW_status_idx on PINST_REQUESTS(status)");
                s.execute("create index WORKFLOW_component_id_idx on PINST_REQUESTS(component_id)");
                s.execute("create index WORKFLOW_wf_id_idx on PINST_REQUESTS(wf_id)");
                log.debug("Created table PINST_REQUESTS");
            } else {
                log.debug("PINST_REQUESTS already exists!");
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    @Override
    public void dropSchema() throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            log.info("Dropping PINST_REQUESTS");
            Statement s = conn.createStatement();
            s.execute("drop table PINST_REQUESTS");
        } catch (SQLException ex) {
            if (ex.getMessage().contains("does not exist")) {
                log.info("Table did not exist");
            }
            else {
                throw new DatastoreException(ex);
            }
        }
    }

    private String serializeMap(Map<String, String> map ) {
        if (map == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder xmlEncoder = new XMLEncoder(bos);
        xmlEncoder.writeObject(map);
        xmlEncoder.close();

        String serializedMap = bos.toString();
        return serializedMap;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> deserializeMap(String serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return new HashMap<String, String>();
        } else {
            try (XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(serializedMap.getBytes())))
            {
                return (Map<String, String>) xmlDecoder.readObject();
            }
            catch (Exception e)
            {
                log.error("Unexpected error while deserializing map", e);
                throw e;
            }
            
        }
    }

}
