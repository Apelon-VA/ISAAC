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

import gov.va.isaac.config.generated.User;

/**
 * {@link InvalidUserException}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class InvalidUserException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see Exception#Exception(String)
	 * Prepends a toString of the {@link User} object
	 */
	public InvalidUserException(String string, User user)
	{
		super("Invalid user specification: " + GenerateUsers.toString(user) + ":  " + string);
	}
	
	/**
	 * @see Exception#Exception(String)
	 */
	public InvalidUserException(String string)
	{
		super(string);
	}
}
