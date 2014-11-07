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
package gov.va.isaac.mojos.profileSync;

import gov.va.isaac.interfaces.sync.ProfileSyncI;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which reads a app.xml file to find the specified SCM URL and type for the profiles / changeset
 * syncing service - and pulls down any existing profiles - if present.
 * 
 * See {@link ProfileSyncI#linkAndFetchFromRemote(String, String, String)} for specific details on the
 * behavior of this linking process.
 * 
 * See {@link ProfilesMojoBase} for details on how credentials are handled.
 * Keep this in a phase earlier than GenerateUsersMojo
 * 
 * @goal get-and-link-profile-scm
 * @phase generate-sources
 */
//TODO figure out phase
public class LinkProfilesToSCMMojo extends ProfilesMojoBase
{
	/**
	 * @throws MojoExecutionException
	 */
	public LinkProfilesToSCMMojo() throws MojoExecutionException
	{
		super();
	}

	@Override
	public void execute() throws MojoExecutionException
	{
		super.execute();
		if (skipRun())
		{
			return;
		}
		
		try
		{
			getLog().info("Configuring " +userProfileFolderLocation.getAbsolutePath() + " for SCM management");
			
			userProfileFolderLocation.mkdirs();
			getProfileSyncImpl().linkAndFetchFromRemote(getURL(), getUsername(), getPassword());

			getLog().info("Done Configuring SCM for profiles");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error configuring SCM for the profiles", e);
		}
	}	
}
