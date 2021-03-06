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
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.util.DBLocator;
import java.io.File;
import java.lang.reflect.Field;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.query.lucene.LuceneIndexer;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Goal which opens (and creates if necessary) an OTF Versioning Store DB.
 * 
 * @goal setup-terminology-store
 * 
 * @phase process-sources
 */
public class Setup extends AbstractMojo
{

	/**
	 * Location of the folder to look for the BDB database. May be a direct link to a "berkeley-db" folder,
	 * or it may be a link to a "foo.bdb" folder which contains a "berkeley-db" folder, or it may be a link
	 * to the parent of a "foo.bdb" folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String bdbFolderLocation; //TODO: Dan - change this from a String to a File type variable
	
	/**
	 * Location of the folder that contains the user profiles
	 * 
	 * @parameter
	 * @optional
	 */
	private File userProfileFolderLocation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		getLog().info("Setup terminology store");
		try
		{
			File bdbFolderFile = new File(bdbFolderLocation);

			bdbFolderFile = DBLocator.findDBFolder(bdbFolderFile);
			
			System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, bdbFolderFile.getCanonicalPath());
			System.setProperty(LuceneIndexer.LUCENE_ROOT_LOCATION_PROPERTY, DBLocator.findLuceneIndexFolder(bdbFolderFile).getCanonicalPath());

			getLog().info("  Setup AppContext, bdb location = " + bdbFolderFile.getCanonicalPath());
			AppContext.setup();

			// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
			Field f = Hk2Looker.class.getDeclaredField("looker");
			f.setAccessible(true);
			f.set(null, AppContext.getServiceLocator());

			getLog().info("Loading terminology store");
			AppContext.getService(TerminologyStoreDI.class);
			
			getLog().info("Done setting up terminology store");
			
			getLog().info("Set Automation User");
			AppContext.getService(UserProfileManager.class).configureAutomationMode(userProfileFolderLocation);
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database build failure", e);
		}
	}
	
	
	public void setBdbFolderLocation(String inputBdbFolderlocation) {
		bdbFolderLocation = inputBdbFolderlocation;
	}
	
	public void setUserProfileFolderLocation(File inputUserProfileLocation) {
		userProfileFolderLocation = inputUserProfileLocation;
	}
	
}
