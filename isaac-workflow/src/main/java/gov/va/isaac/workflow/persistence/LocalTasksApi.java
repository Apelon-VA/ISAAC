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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alo
 */
public class LocalTasksApi {

    private String framework = "embedded";
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private String protocol = "jdbc:derby:";

    private Connection conn;

    public LocalTasksApi() {
        loadDriver();
        conn = null;
        Properties props = new Properties(); // connection properties
        props.put("user", "workflow");
        props.put("password", "workflow");
        String dbName = "workflowDB"; // the name of the database
        try {
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println("Connected to and created database " + dbName);
    }

    public void commit() {
        try {
            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
            System.out.println("Task " + task.getId() + " inserted");
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23505")) {
                System.err.print("Task " + task.getId() + " already exists!");
                LocalTask taskInDb = getTask(task.getId());
                if (task.equals(taskInDb)) {
                    System.err.println(" No changes.");
                } else {
                    if (!task.getOwner().equals(taskInDb.getOwner())) {
                        System.err.print(" User has changed from " + taskInDb.getOwner() + " to " + task.getOwner() + ".");
                    }
                    if (!task.getStatus().equals(taskInDb.getStatus())) {
                        System.err.print(" Status has changed from " + taskInDb.getStatus() + " to " + task.getStatus() + ".");
                    }
                    System.err.println("-");
                };
            } else {
                Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<LocalTask> getOpenOwnedTasks(String owner) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where owner = '" + owner + "' and status = 'reserved'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }
    
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
    
    public List<LocalTask> getOwnedTasksByComponentId(String owner, String componentId) {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks where owner = '" + owner + "' and componentId = '" + componentId + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }
    
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
    
    public List<LocalTask> getTasks() {
        List<LocalTask> tasks = new ArrayList<>();
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id, name, componentId, componentName, status, owner FROM local_tasks");
            while (rs.next()) {
                //System.out.println("Navigating rs:" + rs.getLong(1));
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tasks;
    }

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

    public void createSchema() {
        try {
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "WORKFLOW", "LOCAL_TASKS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table LOCAL_TASKS(id int PRIMARY KEY, name varchar(40), componentId varchar(40), componentName varchar(255), status varchar(40), owner varchar(40))");
                s.closeOnCompletion();
                System.out.println("Created table local_tasks");
            } else {
                System.err.println("Schema already exists!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalTasksApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dropSchema() {
        try {
            Statement s = conn.createStatement();
            s.execute("drop table LOCAL_TASKS");
            s.closeOnCompletion();
        } catch (SQLException ex) {
            System.err.println("Schema already deleted...");
        }
    }

    private void loadDriver() {
        /*
         *  The JDBC driver is loaded by loading its class.
         *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
         *  be automatically loaded, making this code optional.
         *
         *  In an embedded environment, this will also start up the Derby
         *  engine (though not any databases), since it is not already
         *  running. In a client environment, the Derby engine is being run
         *  by the network server framework.
         *
         *  In an embedded environment, any static Derby system properties
         *  must be set before loading the driver to take effect.
         */
        try {
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("\nUnable to load the JDBC driver " + driver);
            System.err.println("Please check your CLASSPATH.");
            cnfe.printStackTrace(System.err);
        } catch (InstantiationException ie) {
            System.err.println(
                    "\nUnable to instantiate the JDBC driver " + driver);
            ie.printStackTrace(System.err);
        } catch (IllegalAccessException iae) {
            System.err.println(
                    "\nNot allowed to access the JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }
    }

}
