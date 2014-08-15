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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alo
 */
public class ConnectionManager {

    //private static final String framework = "embedded";
    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String protocol = "jdbc:derby:";
    
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private static Connection conn = null;

    public static Connection getConn() {
        if (conn == null) {
            loadDriver();
            Properties props = new Properties(); // connection properties
            props.put("user", "workflow");
            props.put("password", "workflow");
            String dbName = "workflowDB"; // the name of the database
            try {
                conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);
                conn.setAutoCommit(false);
            } catch (SQLException ex) {
                log.error("Unexpected error getting DB connection", ex);
            }
        }
        return conn;
    }

    private static void loadDriver() {
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
            log.debug("Loaded the appropriate driver");
        } catch (ClassNotFoundException cnfe) {
            log.error("Unable to load the JDBC driver " + driver, cnfe);
        } catch (InstantiationException ie) {
            log.error("Unable to instantiate the JDBC driver " + driver, ie);
        } catch (IllegalAccessException iae) {
            log.error("Not allowed to access the JDBC driver " + driver, iae);
        }
    }
}
