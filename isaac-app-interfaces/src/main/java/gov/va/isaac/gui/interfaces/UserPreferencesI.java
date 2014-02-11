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
package gov.va.isaac.gui.interfaces;

import org.jvnet.hk2.annotations.Contract;

/**
 * UserPreferencesI
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public abstract class UserPreferencesI
{
	/**
	 * Store the user preference. Return the previous value (if any)
	 */
	public abstract String set(String prefName, String prefValue);

	/**
	 * Return a user preference. Returns null if no preference is found with the requested name.
	 */
	public abstract String get(String prefName);

	/**
	 * Store the user preference. Return the previous value (if any)
	 */
	public Boolean set(String prefName, Boolean prefValue)
	{
		String old = set(prefName, prefValue.toString());
		if (old != null)
		{
			return Boolean.parseBoolean(old);
		}
		return null;
	}

	/**
	 * Return a user preference. Returns null if no preference is found with the requested name.
	 */
	public Boolean getBoolean(String prefName)
	{
		String s = get(prefName);
		if (s != null)
		{
			return Boolean.parseBoolean(s);
		}
		return null;
	}

	/**
	 * Return a user preference. Returns the default value if no preference found with the specified name.
	 */
	public String get(String prefName, String defaultValue)
	{
		String s = get(prefName);
		return (s == null ? defaultValue : s);
	}

	/**
	 * Return a user preference. Returns null if no preference is found with the requested name.
	 */
	public boolean getBoolean(String prefName, boolean defaultValue)
	{
		Boolean b = getBoolean(prefName);
		return (b == null ? defaultValue : b.booleanValue());
	}
}
