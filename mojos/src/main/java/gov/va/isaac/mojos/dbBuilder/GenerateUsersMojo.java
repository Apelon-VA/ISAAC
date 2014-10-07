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
package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.config.users.GenerateUsers;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which processes a users.xml file which is formatted according to the UserGenerationSchema.xsd
 * stored in otf-util.  See {@link GenerateUsers} for more details.
 * 
 * @goal create-users
 * 
 * @phase process-sources
 */
public class GenerateUsersMojo extends AbstractMojo
{

	/**
	 * The location of the users.xml file to process. If not provided,
	 * will read "/users.xml" from the classpath.
	 * 
	 * @parameter
	 * @optional
	 */
	File usersFileLocation = null;

	/**
	 * To execute this mojo, you need to first have run the "Setup" mojo against
	 * the same database. Here, we assume the data store is ready to go and we can
	 * acquire it simply as shown in the createPath method below.
	 * 
	 * If not yet initialized, this will fail.
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			getLog().info("Processing the users file " + (usersFileLocation == null ? "(classpath) /users.xml" : usersFileLocation.getAbsolutePath()));
			if (usersFileLocation != null)
			{
				GenerateUsers.generateUsers(usersFileLocation);
			}
			else
			{
				GenerateUsers.generateUsers();
			}
			getLog().info("Done Processing the users file.");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error processing the users file", e);
		}
	}	
}
