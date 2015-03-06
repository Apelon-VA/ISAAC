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
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.TaskActionStatus;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.jvnet.hk2.annotations.Service;
import org.kie.api.task.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link LocalTasksApi}
 *
 * @author alo
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class LocalTasksApi implements LocalTasksServiceBI {
    private static final Logger log = LoggerFactory.getLogger(LocalTasksApi.class);
    
    private final Set<ActionEventListener> actionEventListeners = new HashSet<>();
    
    private DataSource ds;
    
    private LocalTasksApi()
    {
        //For HK2 to construct
        ds = AppContext.getService(DatastoreManager.class).getDataSource();
        try
        {
        	//TODO need to get this in a background thread
            createSchema();
        }
        catch (DatastoreException e)
        {
            log.error("Create schema failed during init", e);
        }
    }
    
    public void changeUserName(String oldWFUsername, String newWFUsername)
    {
        //TODO (artf231902) DAN THIS MUST BE IMPLEMENTED before changing a WF username will work properly!!!!!
        log.error("The change username functionality is not yet complete - WF state is now corrupt!!!!!!!");
    }
    

    @Override
    public void saveTask(LocalTask task) throws DatastoreException {
    	final Action action = Action.NONE;
    	final TaskActionStatus actionStatus = TaskActionStatus.None;

    	WorkflowHistoryHelper.createAndAddNewEntry(task, action, task.getInputVariables());

        try (Connection conn = ds.getConnection()) {
            try {
                PreparedStatement psInsert = conn.prepareStatement("insert into local_tasks values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                psInsert.setLong(1, task.getId());
                psInsert.setString(2, task.getName());
                psInsert.setString(3, task.getComponentId());
                psInsert.setString(4, task.getComponentName());
                psInsert.setString(5, task.getStatus().name());
                psInsert.setString(6, task.getOwner());
                psInsert.setString(7, action.name());
                psInsert.setString(8, actionStatus.name());
                psInsert.setString(9, serializeMap(task.getInputVariables()));
                psInsert.setString(10, "");
                psInsert.executeUpdate();
                conn.commit();
                log.debug("Task {} saved", task.getId());

                if (actionEventListeners.size() > 0) {
                	final ActionEvent event = new ActionEvent() {
            			@Override
            			public Long getTaskId() {
            				return task.getId();
            			}

            			@Override
            			public Action getAction() {
            				return action;
            			}

            			@Override
            			public TaskActionStatus getActionStatus() {
            				return actionStatus;
            			}

            			@Override
            			public Map<String, String> getOutputVariables() {
            				return Collections.unmodifiableMap(new HashMap<>());
            			}
            		};

                	for (ActionEventListener listener : actionEventListeners) {
                		//Utility.execute(() -> listener.handle(event));
                		listener.handle(event);
                	}
                }
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("23505")) {
                    log.info("Task {} already exists", task.getId());
                    LocalTask taskInDb = getTask(task.getId());
                    if (task.equals(taskInDb)) {
                        log.debug(" No changes.");
                    } else {
                        if (!task.getOwner().equals(taskInDb.getOwner())) {
                            log.info(" User has changed from {} to {}.", taskInDb.getOwner(), task.getOwner());
                            PreparedStatement psUpdateUser = conn.prepareStatement("update local_tasks set owner = ? where id = ?");
                            psUpdateUser.setString(1, task.getOwner());
                            psUpdateUser.setInt(2, Integer.parseInt(task.getId().toString()));
                            int updatedRowCount = psUpdateUser.executeUpdate();
                            if (updatedRowCount != 1)
                            {
                                throw new DatastoreException("update owner on existing task failed!");
                            }
                            conn.commit();
                        }
                        if (!task.getStatus().equals(taskInDb.getStatus())) {
                            log.info(" Status has changed from {} to {}.", taskInDb.getStatus(), task.getStatus());
                            PreparedStatement psUpdateStatus = conn.prepareStatement("update local_tasks set status = ? where id = ?");
                            psUpdateStatus.setString(1, task.getStatus().name());
                            psUpdateStatus.setInt(2, Integer.parseInt(task.getId().toString()));
                            int updatedRowCount = psUpdateStatus.executeUpdate();
                            if (updatedRowCount != 1)
                            {
                                throw new DatastoreException("update status on existing task failed!");
                            }
                            conn.commit();
                        }
                        //log.error("-Unknown?-");
                    };
                } else {
                    throw new DatastoreException(ex);
                }
            }
        } catch (RuntimeException re) {
            log.error("Caught {} \"{}\" saving task #{}: {}", re.getClass().getName(), re.getLocalizedMessage(), task.getId(), task);
            throw re;
        }
        catch (SQLException e)
        {
            throw new DatastoreException(e);
        }
    }

    @Override
    public void setAction(Long taskId, Action action, Map<String,String> outputVariables) throws DatastoreException {
        setAction(taskId, action, TaskActionStatus.Pending, outputVariables);
    }

    @Override
    public void setAction(Long taskId, Action action, TaskActionStatus actionStatus, Map<String,String> outputVariables) throws DatastoreException {
        LocalTask task = this.getTask(taskId);
        WorkflowHistoryHelper.createAndAddNewEntry(task, action, outputVariables);
        
        try (Connection conn = ds.getConnection()){
            PreparedStatement psUpdateStatus = conn.prepareStatement("update local_tasks set action = ?, actionStatus = ?, outputVariables = ? where id = ?");
            psUpdateStatus.setString(1, action.name());
            psUpdateStatus.setString(2, actionStatus.name());
            psUpdateStatus.setString(3, serializeMap(outputVariables));
            psUpdateStatus.setInt(4, Integer.parseInt(taskId.toString()));
            int updatedRowCount = psUpdateStatus.executeUpdate();
            if (updatedRowCount != 1)
            {
                throw new DatastoreException("update action on existing task failed!");
            }
            conn.commit();

            if (actionEventListeners.size() > 0) {
            	final ActionEvent event = new ActionEvent() {
        			@Override
        			public Long getTaskId() {
        				return taskId;
        			}

        			@Override
        			public Action getAction() {
        				return action;
        			}

        			@Override
        			public TaskActionStatus getActionStatus() {
        				return actionStatus;
        			}

        			@Override
        			public Map<String, String> getOutputVariables() {
        				return Collections.unmodifiableMap(outputVariables);
        			}
        		};

            	for (ActionEventListener listener : actionEventListeners) {
            		//Utility.execute(() -> listener.handle(event));
            		listener.handle(event);
            	}
            }
        } catch (RuntimeException re) {
            log.error("Caught {} \"{}\" setting Action {} with TaskActionStatus {} on task {}: {}", re.getClass().getName(), re.getLocalizedMessage(), action, actionStatus, taskId, outputVariables);
            throw re;
        } catch (SQLException ex1) {
            throw new DatastoreException(ex1);
        }
        if ((task.getName().equals("Approve content") || task.getName().equals("Adjudicate content"))
                && action == Action.COMPLETE
                && actionStatus == TaskActionStatus.Pending
                && outputVariables.get("out_response") != null && outputVariables.get("out_response").equals("approve")) {
            
            populateReleaseCandidatePath(task);
        }
    }
    
    private void populateReleaseCandidatePath(LocalTask task) throws DatastoreException {
        try {
            OTFUtility.addToPromotionPath(UUID.fromString(task.getComponentId()));
        } catch (Exception e) {
            log.error("Error promoting concept to promotion path for task: " + task, e);
            throw new DatastoreException("Unexpected error adding a new version of the concept onto the promotion path for task " + task, e);
        }
    }

    @Override
    public void completeTask(Long taskId, Map<String, String> outputVariablesMap) throws DatastoreException {
        setAction(taskId, Action.COMPLETE, outputVariablesMap);
    }

    @Override
    public void releaseTask(Long taskId) throws DatastoreException {
        setAction(taskId, Action.RELEASE, new HashMap<String, String>());
    }

    @Override
    public List<LocalTask> getOpenOwnedTasks() throws DatastoreException {
        List<LocalTask> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername() 
                    + "' and (status = 'Reserved' or status = 'InProgress')");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getOwnedTasksByStatus(Status status) throws DatastoreException {
        List<LocalTask> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername() 
                    + "' and status = '" + status + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return tasks;
    }
    
    @Override
    public List<LocalTask> getOwnedTasksByActionStatus(TaskActionStatus actionStatus) throws DatastoreException {
        List<LocalTask> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            //we will need to change the userId in the DB - if the user enters a different workflow username
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername() 
                    + "' and actionStatus = '" + actionStatus + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getOpenOwnedTasksByComponentId(UUID componentId) throws DatastoreException {
        List<LocalTask> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where owner = '" + ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername() 
                    + "' and componentId = '" + componentId.toString() + "' and (status = 'Reserved' or status = 'InProgress')");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return tasks;
    }
    
    @Override
    public List<LocalTask> getTasksByComponentId(UUID componentId) throws DatastoreException {
        List<LocalTask> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where componentId = '" + componentId.toString() + "'");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return tasks;
    }

    @Override
    public List<LocalTask> getTasks() throws DatastoreException {
        List<LocalTask> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection()){
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks");
            while (rs.next()) {
                tasks.add(readTask(rs));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
        return tasks;
    }

    @Override
    public LocalTask getTask(Long id) throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            LocalTask task = null;
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM local_tasks where id = " + id);
            if (!rs.next()) {
                // no results
            } else {
                task = readTask(rs);
                return task;
            }

            return task;
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    private LocalTask readTask(ResultSet rs) throws SQLException {
        LocalTask task = new LocalTask();
        
        try {
            task.setId(rs.getLong(1));
            task.setName(rs.getString(2));
            task.setComponentId(rs.getString(3));
            task.setComponentName(rs.getString(4));
            task.setStatus(Status.valueOf(rs.getString(5)));
            task.setOwner(rs.getString(6));
            task.setAction(Action.valueOf(rs.getString(7)));
            task.setActionStatus(TaskActionStatus.valueOf(rs.getString(8)));
            task.setInputVariables(deserializeMap(rs.getString(9)));
            task.setOutputVariables(deserializeMap(rs.getString(10)));
        } catch (RuntimeException e) {
            log.error("Caught {} \"{}\" reading task #{}", e.getClass().getName(), e.getLocalizedMessage(), rs.getLong(1));
            throw e;
        }

        return task;
    }

    private static String serializeMap(Map<String, String> map) {
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

    private static Map<String, String> deserializeMap(String serializedMap) {
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
    public void createSchema() throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            log.info("Creating Workflow LOCAL_TASKS schema");
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "WORKFLOW", "LOCAL_TASKS", null);
            if (!rs.next()) {
                Statement s = conn.createStatement();
                s.execute("create table LOCAL_TASKS("
                        + "id bigint PRIMARY KEY, "
                        + "name varchar(40), "
                        + "componentId varchar(40), "
                        + "componentName varchar(255), "
                        + "status varchar(40), "
                        + "owner varchar(40), "
                        + "action varchar(40), "
                        + "actionStatus varchar(40), "
                        + "inputVariables long varchar, "
                        + "outputVariables long varchar)");
                
                s.execute("create index LOCAL_TASKS_status_idx on LOCAL_TASKS(status)");
                s.execute("create index LOCAL_TASKS_actionStatus_idx on LOCAL_TASKS(actionStatus)");
                s.execute("create index LOCAL_TASKS_componentId_idx on LOCAL_TASKS(componentId)");
                
                conn.commit();
                log.info("Created table LOCAL_TASKS");
            } else {
                log.info("LOCAL_TASKS already exists!");
            }
        } catch (SQLException ex) {
            throw new DatastoreException(ex);
        }
    }

    @Override
    public void dropSchema() throws DatastoreException {
        try (Connection conn = ds.getConnection()){
            log.info("Dropping table local_tasks");
            Statement s = conn.createStatement();
            s.execute("drop table LOCAL_TASKS");
            conn.commit();
        } catch (SQLException ex) {
            if (ex.getMessage().contains("does not exist"))
            {
                log.info("Table did not exist");
            }
            else
            {
                throw new DatastoreException(ex);
            }
        }
    }

	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.LocalTasksServiceBI#addActionEventListener(gov.va.isaac.workflow.LocalTasksServiceBI.ActionListener)
	 */
	@Override
	public void addActionEventListener(ActionEventListener listener) {
		this.actionEventListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.LocalTasksServiceBI#removeActionEventListener(gov.va.isaac.workflow.LocalTasksServiceBI.ActionEventListener)
	 */
	@Override
	public void removeActionEventListener(ActionEventListener listener) {
		this.actionEventListeners.remove(listener);
	}
}
