/**
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
package gov.va.isaac.init;

import gov.va.isaac.AppContext;
import gov.va.isaac.util.DBLocator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * {@link SystemInit}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SystemInit
{
	private static final Logger LOG = LoggerFactory.getLogger(SystemInit.class);

	/**
	 * Performs the basic init of ISAAC related applications.  Configures SLF4J Logging, configures the HK2 looker from OTF properly, 
	 * configures the DB location paths of OTF properly.
	 * @return - null, if no issue configuring the data store paths - otherwise - the exception that happened while configuring the datastore paths.
	 * @throws Exception - if some other unexpected event happens
	 */
	public static IOException doBasicSystemInit() throws Exception
	{
		//Configure Java logging into logback
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		AppContext.setup();
		// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
		Field f = Hk2Looker.class.getDeclaredField("looker");
		f.setAccessible(true);
		f.set(null, AppContext.getServiceLocator());
		//This has to be done _very_ early, otherwise, any code that hits it via H2K will kick off the init process, on the wrong path
		//Which is made worse by the fact that the defaults in OTF are inconsistent between BDB and lucene...

		try
		{
			if (System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY) == null)
			{
				configDataStorePaths(new File(BdbTerminologyStore.DEFAULT_BDB_LOCATION));
			}
			else
			{
				configDataStorePaths(new File(System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY)));
			}
		}
		catch (IOException e)
		{
			System.err.println("Configuration of datastore path failed.  DB will not be able to start properly!  " + e);
			return e;
		}
		return null;
	}

	/**
	 * Read and Set the necessary system properties so that OTF configures itself properly, as we are still lacking
	 * proper APIs to communicate with OTF.
	 * 
	 * @param bdbFolder - The folder to look in for the database files. See {@link DBLocator#findDBFolder(File)}
	 * @throws IOException
	 */
	public static void configDataStorePaths(File bdbFolder) throws IOException
	{
		// Default value if null.
		if (bdbFolder == null)
		{
			//do nothing... let the store use its own default
			//TODO OTF fix: note - the BDB defaults are not in sync with the Lucene defaults...  lucene will init in the wrong place and fail....
			//https://jira.ihtsdotools.org/browse/OTFISSUE-12
		}
		else
		{
			File localBDBLocation = DBLocator.findDBFolder(bdbFolder);
			File localLuceneLocation = DBLocator.findLuceneIndexFolder(localBDBLocation);

			if (!localBDBLocation.exists())
			{
				throw new FileNotFoundException("Couldn't find a bdb data store in '" + localBDBLocation.getAbsoluteFile().getParentFile().getAbsolutePath() + "'");
			}
			if (!localBDBLocation.isDirectory())
			{
				throw new IOException("The specified bdb data store: '" + localBDBLocation.getAbsolutePath() + "' is not a folder");
			}

			if (System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY) == null)
			{
				System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, localBDBLocation.getCanonicalPath());
			}
			else
			{
				LOG.warn("The application specified '" + localBDBLocation.getCanonicalPath() + "' but the system property " + BdbTerminologyStore.BDB_LOCATION_PROPERTY
						+ "is set to " + System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY + " this will override the application path"));
			}
			if (System.getProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY) == null)
			{
				System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY, localLuceneLocation.getCanonicalPath());
			}
			else
			{
				LOG.warn("The application specified '" + localLuceneLocation.getCanonicalPath() + "' but the system property "
						+ LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY + "is set to " + System.getProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY)
						+ " this will override the application path");
			}
		}
	}
}
