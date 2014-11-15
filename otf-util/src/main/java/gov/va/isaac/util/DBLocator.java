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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.util;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DBLocator}
 * 
 * A utility to help ease the transition from the old paths used for the BDB, to the new paths.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class DBLocator
{
	private static final Logger LOG = LoggerFactory.getLogger(DBLocator.class);
	
	public static File findDBFolder(File inputFolder)
	{
		inputFolder = inputFolder.getAbsoluteFile();

		//If they pass this in - and it exists - just use it.
		if (inputFolder.getName().equals("berkeley-db") && inputFolder.isDirectory())
		{
			LOG.info("BDB Location set to " + inputFolder.getAbsolutePath());
			return inputFolder;
		}
		
		//If it is a folder with a '.bdb' at the end of the name, then berkeley-db will be in a sub-folder.
		if (inputFolder.getName().endsWith(".bdb") && inputFolder.isDirectory())
		{
			LOG.info("BDB Location set to " + inputFolder.getAbsolutePath());
			return new File(inputFolder, "berkeley-db");
		}
		//Otherwise see if we can find a .bdb folder as a direct child
		if (inputFolder.isDirectory())
		{
			for (File f : inputFolder.listFiles())
			{
				//If it is a folder with a '.bdb' at the end of the name, then berkeley-db will be in a sub-folder.
				if (f.getName().endsWith(".bdb") && f.isDirectory())
				{
					LOG.info("BDB Location set to " + inputFolder.getAbsolutePath());
					return new File(f, "berkeley-db");
				}
			}
		}
		
		//Or as a sibling
		if (inputFolder.getParentFile().isDirectory())
		{
			for (File f : inputFolder.getParentFile().listFiles())
			{
				//If it is a folder with a '.bdb' at the end of the name, then berkeley-db will be in a sub-folder.
				if (f.getName().endsWith(".bdb") && f.isDirectory())
				{
					File berkeley = new File(f, "berkeley-db"); 
					LOG.info("BDB Location set to " + berkeley.getAbsolutePath());
					return berkeley;
				}
			}
		}

		//can't match an expected pattern... just return the input.
		LOG.info("BDB Location set to " + inputFolder.getAbsolutePath());
		return inputFolder;
	}
	
	public static File findLuceneIndexFolder(File dbLocation)
	{
		dbLocation = dbLocation.getAbsoluteFile();
		//old style - we had the db inside the berkeley-db folder - so if it already exists - use it.
		File lucene = new File(dbLocation, "lucene");
		if (lucene.isDirectory())
		{
			//But, we have to return the parent folder, because the lucene code adds on its own "lucene" subdirectory.... sigh.
			lucene = dbLocation;
			LOG.info("Lucene index location set to " + lucene.getAbsolutePath());
			return lucene;
		}
		//If it doesn't yet exist (we are setting up a new DB) then put it as a sibling to the dbLocation folder (and no 'lucene' 
		//subfolder -  it will make that on its own)
		lucene = dbLocation.getParentFile();
		LOG.info("Lucene index location set to " + lucene.getAbsolutePath());
		return lucene;
	}
}
