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

import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.util.Utility;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
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
public final class DatastoreManager implements ServicesToPreloadI
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
	}
	
	public DataSource getDataSource()
	{
		loadRequested();
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
		if (cdl.getCount() == 0)
		{
			return;
		}
		//start up Derby in a background thread
		Utility.execute(() -> {
			try
			{
				File derbyFolder = new File(dbName); 
				if (!derbyFolder.exists())
				{
					log.info("No Derby DB folder found at {}, creating a new DB", derbyFolder.getAbsolutePath());
					//So, derby is silly, and doesn't have any way you can configure it for a pool, while at the same time, allowing
					//it to create a database, if necessary.  The only way I've found to keep it quiet during startup is to see if the DB
					//exists, if not, connect to it once with the 'special' URL, then shut that down - then start up the proper pool with the 
					//normal URL.  (facepalm)
					DriverManager.getConnection(protocol + dbName + ";create=true", "workflow", "workflow");
					try
					{
						DriverManager.getConnection(protocol + ";shutdown=true;deregister=false");
					}
					catch (SQLException e)
					{
						//Yes... this is really how derby signals a proper DB shutdown.  Sigh.
						if (e.getErrorCode() == 50000)
						{
							log.debug("Initial Workflow database shutdown");
						}
						else
						{
							log.error("Unexpected error shutting down Workflow DB", e);
						}
					}
					//Derby requires this nasty hack to be allowed to restart properly...
					Class.forName(driver).newInstance();
				}

				//c3p0 isn't configuring right with the logger... don't know why.  force it to slf4j
				System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.slf4j.Slf4jMLog");
				
				//Configure up a c3p0 connection pool.  It has reasonable defaults for most everything.
				//It will also handle automatically closing result sets, statements, etc, when connections are returned
				//to the pool via connection.close()
				ComboPooledDataSource cpds = new ComboPooledDataSource();
				cpds.setDriverClass(driver); //loads the jdbc driver
				cpds.setJdbcUrl(protocol + dbName);
				cpds.setUser("workflow");
				cpds.setPassword("workflow");

				log.info("Opening the connection pool on the DB folder {}", derbyFolder.getAbsolutePath());
				
				Connection c = cpds.getConnection();
				if (!c.createStatement().execute("values 1"))
				{
					log.warn("derby test statement failed!");
				}
				else
				{
					log.info("Workflow database ready");
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
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#shutdown()
	 */
	@Override
	public void shutdown()
	{
		if (dataSource_ != null)
		{
			dataSource_.close();
			try
			{
				((EmbeddedDriver)Class.forName(driver).newInstance()).connect(protocol + ";shutdown=true", null);
			}
			catch (SQLException e)
			{
				//Yes... this is really how derby signals a proper DB shutdown.  Sigh.
				if (e.getErrorCode() == 50000)
				{
					log.info("Workflow database shutdown");
				}
				else
				{
					log.error("Unexpected error shutting down Workflow DB", e);
				}
			}
			catch (Exception e)
			{
				log.error("Unexpected error shutting down Workflow DB", e);
			}
		}
	}
}
