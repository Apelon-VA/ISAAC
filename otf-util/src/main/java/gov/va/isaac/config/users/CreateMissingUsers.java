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
package gov.va.isaac.config.users;

import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.util.Utility;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CreateMissingUsers}
 * 
 * A class to tie into the startup hook of the app - so that we can make sure all users specified
 * in the users.xml file actually exist.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
public class CreateMissingUsers implements ServicesToPreloadI
{
	private Logger logger = LoggerFactory.getLogger(CreateMissingUsers.class);

	/**
	 * @see gov.va.isaac.interfaces.utility.ServicesToPreloadI#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		Utility.execute(() -> 
		{
			logger.debug("Processing user.xml file from classpath");
			try
			{
				GenerateUsers.generateUsers();
			}
			catch (Exception e)
			{
				logger.error("Unexpected error processing users", e);
			}
		});
	}
}
