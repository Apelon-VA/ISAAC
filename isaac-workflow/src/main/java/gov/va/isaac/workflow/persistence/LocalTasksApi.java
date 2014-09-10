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

import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alo
 */
public class LocalTasksApi implements LocalTasksServiceBI {

    private Connection conn;
    private static final Logger log = LoggerFactory.getLogger(LocalTasksApi.class);

    public LocalTasksApi() {
        conn = ConnectionManager.getConn();
    }

    @Override
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException ex) {
            log.error("Problem committing", ex);
        }
    }

    @Override
    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            log.error("Problem closing connection", ex);
        }
    }

    @Override
    public void saveTask(LocalTask task) {
        try {
            PreparedStatement psInsert = conn.prepareStatement("insert into local_tasks values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psInsert.setLong(1, task.getId());
            psInsert.setString(2, task.getName());
            psInsert.setString(3, task.getComponentId());
            psInsert.setString(4, task.getComponentName());
            psInsert.setString(5, task.getStatus());
            psInsert.setString(6, task.getOwner());
            psInsert.setString(7, "NONE");
            psInsert.setString(8, "");
            psInsert.setString(9, serializeMap(task.getInputVariables()));
            psInsert.setString(10, "");
            psInsert.executeUpdate();
            psInsert.closeOnCompletion();
            conn.commit();
            log.debug("Task {} saved", task.getId());
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23505")) {
                log.error("Task " + task.getId() + " already exists!");
                LocalTask taskInDb = getTask(task.getId());
                if (task.equals(taskInDb)) {
                    log.error(" No changes.");
                } else {
                    if (!task.getOwner().equals(taskInDb.getOwner())) {
                        log.error(" User has changed from " + taskInDb.getOwner() + " to " + task.getOwner() + ".");
                        try {
                            PreparedStatement psUpdateUser = conn.prepareStatement("update local_tasks set owner = ? where id = ?");
                            psUpdateUser.setString(1, task.getOwner());
                            psUpdateUser.setInt(2, Integer.parseInt(task.getId().toString()));
                            psUpdateUser.executeUpdate();
                            psUpdateUser.closeOnCompletion();
                            conn.commit();
                        } catch (SQLException ex1) {
                            log.error("Unexpected SQL Error", ex1);
                        }
                    }
                    if (!task.getStatus().equals(taskInDb.getStatus())) {
                        log.error(" Status has changed from " + taskInDb.getStatus() + " to " + task.getStatus() + ".");
                        try {
                            PreparedStatement psUpdateStatus = conn.prepareStatement("update local_tasks set status = ? where id = ?");
                            psUpdateStatus.setString(1, task.getStatus());
                            psUpdateStatus.setInt(2, Integer.parseInt(task.getId().toString()));
                            psUpdateStatus.executeUpdate();
                            psUpdateStatus.closeOnCompletion();
                            conn.commit();
                        } catch (SQLException ex1) {
                            log.error("Unexpected SQL Error", ex1);
                        }
                    }
                    log.error("-Unknown?-");
                };
            } else {
                log.error("Unexpected SQL Error", ex);
            }
        }
    }

    @Override
    public void setAction(Long taskId, Action action, Map<String,String> outputVariables) {
        try {
            PreparedStatement psUpdateStatus = conn.prepareStatement("update local_tasks set action = ?, actionStatus = ?, outputVariables = ? where id = ?");
            psUpdateStatus.setString(1, action.name());
            psUpdateStatus.setString(2, "pending");
            psUpdateStatus.setString(3, serializeMap(outputVariables));
            psUpdateStatus.setInt(4, Integer.parseInt(taskId.toString()));
            psUpdateStatus.executeUpdate();
            psUpdateStatus.closeOnCompletion();
            conn.commit();
        } catch (SQLException ex1) {
            log.error("Unexpected SQL Error", ex1);
        }
    }

    @Override
    public void setAction(Long taskId, Action action, String actionStatus, Map<String,String> outputVariables) {
        try {
            PreparedStatement psUpdateStatus = conn.prepareStatement("update local_tasks set action = ?, actionStatus = ?, outputVariables = ? where id = ?");
            psUpdateStatus.setString(1, action.name());
            psUpdateStatus.setString(2, actionStatus);
            psUpdateStatus.setString(3, serializeMap(outputVariables));
            psUpdateStatus.setInt(4, Integer.parseInt(taskId.toString()));
            psUpdateStatus.executeUpdate();
            psUpdateStatus.closeOnCompletion();
            conn.commit();
        } catch (SQLException ex1) {
            log.error("Unexpected SQL Error", ex1);
        }
    }

    @Override
    public List<LocalTask> getOpenOwnedTasks(String owner) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + owner + "' and (status = 'Reserved' or status = 'InProgress')");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getOwnedTasksByStatus(String owner, String status) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + owner + "' and status = '" + status + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return tasks;
    }
    
    @Override
    public List<LocalTask> getOwnedTasksByActionStatus(String owner, String actionStatus) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + owner + "' and actionStatus = '" + actionStatus + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getOpenOwnedTasksByComponentId(String owner, String componentId) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + owner + "' and componentId = '" + componentId + "' and (status = 'Reserved' or status = 'InProgress')");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return tasks;
    }
    
    @Override
    public List<LocalTask> getTasksByComponentId(String componentId) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where componentId = '" + componentId + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getTasks() {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return tasks;
    }

    @Override
    public LocalTask getTask(Long id) {
        try {
            LocalTask task = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where id = " + id);
            if (!rs.next()) {
                // no results
            } else {
                task = readTask(rs);
                s.closeOnCompletion();
                return task;
            }

            return task;
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
        return null;
    }

    private LocalTask readTask(ResultSet rs) throws SQLException {
        LocalTask task = new LocalTask();
        task.setId(rs.getLong(1));
        task.setName(rs.getString(2));
        task.setComponentId(rs.getString(3));
        task.setComponentName(rs.getString(4));
        task.setStatus(rs.getString(5));
        task.setOwner(rs.getString(6));
        task.setAction(Action.valueOf(rs.getString(7)));
        task.setActionStatus(rs.getString(8));
        task.setInputVariables(deserializeMap(rs.getString(9)));
        task.setOutputVariables(deserializeMap(rs.getString(10)));
        return task;
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

    private Map<String, String> deserializeMap(String serializedMap) {
        if (serializedMap == null || serializedMap.isEmpty()) {
            return new HashMap<String, String>();
        } else {
            XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(serializedMap.getBytes()));
            @SuppressWarnings("unchecked")
            Map<String, String> parsedMap = (Map<String, String>) xmlDecoder.readObject();
            xmlDecoder.close();
            return parsedMap;
        }
    }

    @Override
    public void createSchema() {
        try {
            log.info("Creating LOCAL_TASKS");
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "WORKFLOW", "LOCAL_TASKS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table LOCAL_TASKS(id int PRIMARY KEY, name varchar(40), componentId varchar(40), componentName varchar(255), status varchar(40), owner varchar(40), action varchar(40), actionStatus varchar(40), inputVariables long varchar, outputVariables long varchar)");
                s.closeOnCompletion();
                conn.commit();
                log.info("Created table LOCAL_TASKS");
            } else {
                log.info("LOCAL_TASKS already exists!");
            }
        } catch (SQLException ex) {
            log.error("Unexpected SQL Error", ex);
        }
    }

    @Override
    public void dropSchema() {
        try {
            log.info("Dropping schema");
            Statement s = conn.createStatement();
            s.execute("drop table LOCAL_TASKS");
            s.closeOnCompletion();
            conn.commit();
        } catch (SQLException ex) {
            log.error("Schema already deleted...");
        }
    }
}
