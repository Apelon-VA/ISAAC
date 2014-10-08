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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.workflow.persistence;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI;
import gov.va.isaac.util.Utility;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * {@link DatastoreManager}
 *
 * @author alo
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public final class DatastoreManager implements ServicesToPreloadI, ShutdownBroadcastListenerI
{
	private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String protocol = "jdbc:derby:";
	private static final String dbName = "workflowDB";

	private final Logger log = LoggerFactory.getLogger(DatastoreManager.class);
	private final CountDownLatch cdl = new CountDownLatch(1);
	
	private ComboPooledDataSource dataSource_;

	private DatastoreManager()
	{
		//For HK2 to construct
		//AppContext.getMainApplicationWindow().registerShutdownListener(this);
	}
	
	public DataSource getDataSource()
	{
		try
		{
			cdl.await();
		}
		catch (InterruptedException e)
		{
			log.error("Unexpected interrupt during wait in getDataSource", e);
		}
		return dataSource_;
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		//start up Derby in a background thread
		Utility.execute(() -> {
			try
			{
				//Configure up a c3p0 connection pool.  It has reasonable defaults for most everything.
				//It will also handle automatically closing result sets, statements, etc, when connections are returned
				//to the pool via connection.close()
				ComboPooledDataSource cpds = new ComboPooledDataSource();
				cpds.setDriverClass(driver); //loads the jdbc driver
				cpds.setJdbcUrl(protocol + dbName + ";create=true");
				cpds.setUser("workflow");
				cpds.setPassword("workflow");

				Connection c = cpds.getConnection();
				if (!c.createStatement().execute("values 1"))
				{
					log.warn("derby test statement failed!");
				}
				c.close();

				dataSource_ = cpds;
			}
			catch (Exception e)
			{
				log.error("Unexpected error configuring Workflow Database", e);
			}
			cdl.countDown();
		});
	}

	/**
	 * @see gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		dataSource_.close();
		try
		{
			DriverManager.getConnection(protocol + ";shutdown=true");
		}
		catch (SQLException e)
		{
			if (e.getErrorCode() == 50000)
			{
				log.info("Workflow database shutdown");
			}
			else
			{
				log.error("Unexpected error shutting down Workflow DB", e);
			}
		}
		
	}
}
