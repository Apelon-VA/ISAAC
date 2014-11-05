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

import gov.va.isaac.interfaces.sync.MergeFailOption;
import gov.va.isaac.interfaces.sync.ProfileSyncI;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which pushes the local changes to the server for the profiles SCM.
 * 
 * This executes {@link ProfileSyncI#addUntrackedFiles(File)} followed by 
 * 
 * {@link ProfileSyncI#updateCommitAndPush(File, String, String, String, gov.va.isaac.interfaces.sync.MergeFailOption, String...)} 
 * 
 * 
 * See the above references for specific details on the behavior of this commit process
 * Keep this in a phase later than GenerateUsersMojo
 * 
 * @goal add-commit-and-push-profile-scm
 * @phase process-resources
 */
public class AddCommitAndPushProfilesToSCMMojo extends ProfilesMojoBase
{
	/**
	 * @throws MojoExecutionException
	 */
	public AddCommitAndPushProfilesToSCMMojo() throws MojoExecutionException
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
			getLog().info("Committing " +userProfileFolderLocation.getAbsolutePath() + " for SCM management");
			
			getProfileSyncImpl().addUntrackedFiles();
			getProfileSyncImpl().updateCommitAndPush("Adding profiles after executing GenerateUsersMojo", getUsername(), getPassword(), MergeFailOption.KEEP_REMOTE, 
					(String[])null);

			getLog().info("Done Committing SCM for profiles");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error committing SCM for the profiles", e);
		}
	}	
}
