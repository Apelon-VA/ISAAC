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

import gov.va.isaac.workflow.ProcessInstanceServiceBI;
import gov.va.isaac.workflow.ProcessInstanceCreationRequest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alo
 */
public class ProcessInstanceCreationRequestsAPI implements ProcessInstanceServiceBI {

    private Connection conn;

    public ProcessInstanceCreationRequestsAPI() {
        conn = ConnectionManager.getConn();
    }

    @Override
    public ProcessInstanceCreationRequest createRequest(String processName, String componentId, String componentName, String author) {
        try {
            // PINST_REQUESTS (id int PRIMARY KEY, component_id varchar(40), component_name varchar(255), user_id varchar(40), status varchar(40), sync_message varchar(255), request_time varchar(40), sync_time varchar(40), wf_id Integer)");
            PreparedStatement psInsert = conn.prepareStatement("insert into PINST_REQUESTS(component_id, component_name, process_name, user_id, status, sync_message, request_time, sync_time, wf_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",PreparedStatement.RETURN_GENERATED_KEYS);
            psInsert.setString(1, componentId);
            psInsert.setString(2, componentName);
            psInsert.setString(3, processName);
            psInsert.setString(4, author);
            psInsert.setString(5, "REQUESTED");
            psInsert.setString(6, "");
            Long requestTime = System.currentTimeMillis();
            psInsert.setString(7, String.valueOf(requestTime));
            psInsert.setString(8, "0");
            psInsert.setInt(9, 0);
            psInsert.executeUpdate();
            
            ProcessInstanceCreationRequest result = new ProcessInstanceCreationRequest();
            result.setComponentId(componentId);
            result.setComponentName(componentName);
            ResultSet generatedKeys = psInsert.getGeneratedKeys();
            if (generatedKeys.next()) {
                result.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating instance failed, no generated key obtained.");
            }
            result.setRequestTime(Long.MIN_VALUE);
            result.setProcessName(processName);
            result.setStatus(ProcessInstanceCreationRequest.RequestStatus.REQUESTED);
            result.setUserId(author);
            psInsert.closeOnCompletion();
            conn.commit();
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return null;
    }

    @Override
    public void updateRequestStatus(int id, ProcessInstanceCreationRequest.RequestStatus status, String syncMessage, Long wfId) {
        try {
            PreparedStatement psUpdateRequest = conn.prepareStatement("update PINST_REQUESTS set sync_message = ?, status = ?, wf_id = ? where id = ?");
            psUpdateRequest.setString(1, syncMessage);
            psUpdateRequest.setString(2, status.name());
            psUpdateRequest.setInt(3, Integer.parseInt(wfId.toString()));
            psUpdateRequest.setInt(4, id);
            psUpdateRequest.executeUpdate();
            psUpdateRequest.closeOnCompletion();
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<ProcessInstanceCreationRequest> getOpenOwnedRequests(String owner) {
        List<ProcessInstanceCreationRequest> requests = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where user_id = '" + owner + "' and status = 'REQUESTED'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requests;
    }

    @Override
    public List<ProcessInstanceCreationRequest> getOwnedRequestsByStatus(String owner, ProcessInstanceCreationRequest.RequestStatus status) {
        List<ProcessInstanceCreationRequest> requests = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where user_id = '" + owner + "' and status = '" + status.name() + "'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requests;
    }
    
    @Override
    public List<ProcessInstanceCreationRequest> getOpenOwnedRequestsByComponentId(String owner, String componentId) {
        List<ProcessInstanceCreationRequest> requests = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where user_id = '" + owner + "' and component_id = '" + componentId + "' and status = 'REQUESTED'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requests;
    }

    @Override
    public List<ProcessInstanceCreationRequest> getRequestsByComponentId(String componentId) {
        List<ProcessInstanceCreationRequest> requests = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where component_id = '" + componentId + "'");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requests;
    }

    @Override
    public ProcessInstanceCreationRequest getRequestByWfId(Long wfId) {
        try {
            ProcessInstanceCreationRequest request = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where wf_id = " + wfId);
            if (!rs.next()) {
                // no results
            } else {
                request = readRequest(rs);
                s.closeOnCompletion();
                return request;
            }

            return request;
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<ProcessInstanceCreationRequest> getRequests() {
        List<ProcessInstanceCreationRequest> requests = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS");
            while (rs.next()) {
                requests.add(readRequest(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requests;
    }

    @Override
    public ProcessInstanceCreationRequest getRequest(int id) {
        try {
            ProcessInstanceCreationRequest request = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PINST_REQUESTS where id = " + id);
            if (!rs.next()) {
                // no results
            } else {
                request = readRequest(rs);
                s.closeOnCompletion();
                return request;
            }

            return request;
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private ProcessInstanceCreationRequest readRequest(ResultSet rs) throws SQLException {
        ProcessInstanceCreationRequest request = new ProcessInstanceCreationRequest();
        request.setId(rs.getInt(1));
        request.setWfId(Long.parseLong(rs.getString(2)));
        request.setComponentId(rs.getString(3));
        request.setComponentName(rs.getString(4));
        request.setProcessName(rs.getString(5));
        request.setUserId(rs.getString(6));
        String status = rs.getString(7);
        switch (status) {
            case "CREATED":
                request.setStatus(ProcessInstanceCreationRequest.RequestStatus.CREATED);
                break;
            case "REQUESTED":
                request.setStatus(ProcessInstanceCreationRequest.RequestStatus.REQUESTED);
                break;
            case "REJECTED":
                request.setStatus(ProcessInstanceCreationRequest.RequestStatus.REJECTED);
                break;
        }
        request.setSyncMessage(rs.getString(8));
        request.setRequestTime((rs.getString(9).isEmpty()) ? 0L : Long.parseLong(rs.getString(9)));
        request.setSyncTime((rs.getString(10).isEmpty()) ? 0L : Long.parseLong(rs.getString(10)));

        return request;
    }

    public void createSchema() {
        try {
            System.out.println("Creating schema");
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "WORKFLOW", "PINST_REQUESTS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table PINST_REQUESTS (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, wf_id INTEGER, component_id varchar(40), component_name varchar(255), process_name varchar(255), user_id varchar(40), status varchar(40), sync_message varchar(255), request_time varchar(40), sync_time varchar(40))");
                s.closeOnCompletion();
                System.out.println("Created table PINST_REQUESTS");
            } else {
                System.out.println("PINST_REQUESTS already exists!");
            }
            rs = dbmd.getTables(null, "WORKFLOW", "PINST_REQUESTS_PARAMS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table PINST_REQUESTS_PARAMS (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, pins_id int, param_name varchar(255), param_value varchar(255))");
                s.closeOnCompletion();
                System.out.println("Created table PINST_REQUESTS_PARAMS");
            } else {
                System.out.println("PINST_REQUESTS_PARAMS already exists!");
            }
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dropSchema() {
        try {
            System.out.println("Dropping PINST_REQUESTS");
            Statement s = conn.createStatement();
            s.execute("drop table PINST_REQUESTS");
            s.closeOnCompletion();
        } catch (SQLException ex) {
            System.err.println("PINST_REQUESTS already deleted...");
        }
        try {
            System.out.println("Dropping PINST_REQUESTS_PARAMS");
            Statement s = conn.createStatement();
            s.execute("drop table PINST_REQUESTS_PARAMS");
            s.closeOnCompletion();
        } catch (SQLException ex) {
            System.err.println("PINST_REQUESTS_PARAMS already deleted...");
        }
        try {
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(ProcessInstanceCreationRequestsAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
