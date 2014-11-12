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
package gov.va.isaac.mojos.changesets;

import gov.va.isaac.config.changesets.ChangesetProcessor;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which recursively processes any changeset (*.eccs) files found in the specified folder.
 * 
 * @goal process-changesets
 * @phase compile
 */

public class ProcessChangesets extends AbstractMojo
{

	/**
	 * The location of the (already existing) profiles folder which contains changesets to process
	 * 
	 * @parameter
	 * @required
	 */
	File userProfileFolderLocation = null;

	/**
	 * @throws MojoExecutionException
	 */
	public ProcessChangesets() throws MojoExecutionException
	{
		super();
	}

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			getLog().info("Processing " + userProfileFolderLocation.getAbsolutePath() + " for changesets");
			ChangesetProcessor.processChangeSets(userProfileFolderLocation);
			getLog().info("Done Processing changesets");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error configuring SCM for the profiles", e);
		}
	}
}
