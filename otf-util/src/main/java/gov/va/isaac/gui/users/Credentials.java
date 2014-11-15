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
package gov.va.isaac.gui.users;

/**
 * {@link Credentials}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Credentials
{
	String username_, password_;

	public Credentials(String username, String password)
	{
		username_ = username;
		password_ = password;
	}

	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return username_;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username)
	{
		this.username_ = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password_;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password_ = password;
	}
}
