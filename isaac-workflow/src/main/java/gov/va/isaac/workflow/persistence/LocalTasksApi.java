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

import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
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
public class LocalTasksApi implements LocalTasksServiceBI {

    private Connection conn;

    public LocalTasksApi() {
        conn = ConnectionManager.getConn();
    }

    @Override
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void saveTask(LocalTask task) {
        try {
            PreparedStatement psInsert = conn.prepareStatement("insert into local_tasks values (?, ?, ?, ?, ?, ?)");
            psInsert.setLong(1, task.getId());
            psInsert.setString(2, task.getName());
            psInsert.setString(3, task.getComponentId());
            psInsert.setString(4, task.getComponentName());
            psInsert.setString(5, task.getStatus());
            psInsert.setString(6, task.getOwner());
            psInsert.executeUpdate();
            psInsert.closeOnCompletion();
            System.out.println("Task " + task.getId() + " saved");
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23505")) {
                System.err.print("Task " + task.getId() + " already exists!");
                LocalTask taskInDb = getTask(task.getId());
                if (task.equals(taskInDb)) {
                    System.err.println(" No changes.");
                } else {
                    if (!task.getOwner().equals(taskInDb.getOwner())) {
                        System.err.print(" User has changed from " + taskInDb.getOwner() + " to " + task.getOwner() + ".");
                        try {
                            PreparedStatement psUpdateUser = conn.prepareStatement("update local_tasks set owner = ? where id = ?");
                            psUpdateUser.setString(1, task.getOwner());
                            psUpdateUser.setInt(2, Integer.parseInt(task.getId().toString()));
                            psUpdateUser.executeUpdate();
                            psUpdateUser.closeOnCompletion();
                        } catch (SQLException ex1) {
                            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    if (!task.getStatus().equals(taskInDb.getStatus())) {
                        System.err.print(" Status has changed from " + taskInDb.getStatus() + " to " + task.getStatus() + ".");
                        try {
                            PreparedStatement psUpdateStatus = conn.prepareStatement("update local_tasks set status = ? where id = ?");
                            psUpdateStatus.setString(1, task.getStatus());
                            psUpdateStatus.setInt(2, Integer.parseInt(task.getId().toString()));
                            psUpdateStatus.executeUpdate();
                            psUpdateStatus.closeOnCompletion();
                        } catch (SQLException ex1) {
                            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    System.err.println("-");
                };
            } else {
                Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public List<LocalTask> getOpenOwnedTasks(String owner) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where owner = '" + owner + "' and (status = 'Reserved' or status = 'InProgress')");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getOwnedTasksByStatus(String owner, String status) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where owner = '" + owner + "' and status = '" + status + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getOpenOwnedTasksByComponentId(String owner, String componentId) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where owner = '" + owner + "' and componentId = '" + componentId + "' and (status = 'Reserved' or status = 'InProgress')");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }
    
    @Override
    public List<LocalTask> getTasksByComponentId(String componentId) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where componentId = '" + componentId + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getTasks() {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }

    @Override
    public LocalTask getTask(Long id) {
        try {
            LocalTask task = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where id = " + id);
            if (!rs.next()) {
                // no results
            } else {
                task = readTask(rs);
                s.closeOnCompletion();
                return task;
            }

            return task;
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
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
        return task;
    }

    @Override
    public void createSchema() {
        try {
            System.out.println("Creating LOCAL_TASKS");
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "WORKFLOW", "LOCAL_TASKS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table LOCAL_TASKS(id int PRIMARY KEY, name varchar(40), componentId varchar(40), componentName varchar(255), status varchar(40), owner varchar(40))");
                s.closeOnCompletion();
                System.out.println("Created table LOCAL_TASKS");
            } else {
                System.err.println("LOCAL_TASKS already exists!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void dropSchema() {
        try {
            System.out.println("Dropping schema");
            Statement s = conn.createStatement();
            s.execute("drop table LOCAL_TASKS");
            s.closeOnCompletion();
        } catch (SQLException ex) {
            System.err.println("Schema already deleted...");
        }
    }
}
