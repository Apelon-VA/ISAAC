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
package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.AppContext;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Goal which shuts down an open data store.
 * 
 * @goal shutdown-terminology-store
 * 
 * @phase process-sources
 */
public class Shutdown extends AbstractMojo
{

	/**
	 * true if the mutable database should replace the read-only database after
	 * load is complete.
	 * 
	 * @parameter default-value=true
	 * @required
	 */
	private boolean moveToReadOnly = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			getLog().info("Shutdown terminology store");
			// ASSUMES setup has run already
			TerminologyStoreDI store = AppContext.getService(TerminologyStoreDI.class);

			getLog().info("  Shutting Down");

			store.shutdown();

			if (moveToReadOnly)
			{
				getLog().info("moving mutable to read-only");

				String bdbFolderLocation = System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY);

				File readOnlyDir = new File(bdbFolderLocation, "read-only");
				FileIO.recursiveDelete(readOnlyDir);
				File mutableDir = new File(bdbFolderLocation, "mutable");
				mutableDir.renameTo(readOnlyDir);
			}

			getLog().info("Done shutting down terminology store");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database build failure", e);
		}
	}
}
