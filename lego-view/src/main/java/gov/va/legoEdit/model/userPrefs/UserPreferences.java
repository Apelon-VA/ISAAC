/**
 * Copyright 2013
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
package gov.va.legoEdit.model.userPrefs;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.utility.UserPreferencesI;

/**
 * 
 * UserPreferences
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */

public class UserPreferences
{
	private static UserPreferencesI ups_ = AppContext.getService(UserPreferencesI.class);

	/**
	 * Gets the value of the defaultStatus property.
	 * 
	 * @return
	 * possible object is {@link String }
	 * 
	 */
	public static String getDefaultStatus()
	{
		return ups_.get("defaultStatus", "Active");
	}

	/**
	 * Sets the value of the defaultStatus property.
	 * 
	 * @param value
	 * allowed object is {@link String }
	 * 
	 */
	public static void setDefaultStatus(String value)
	{
		ups_.set("defaultStatus", value);
	}

	/**
	 * Gets the value of the author property.
	 * 
	 * @return
	 * possible object is {@link String }
	 * 
	 */
	public static String getAuthor()
	{
		return ups_.get("author", System.getProperty("user.name"));
	}

	/**
	 * Sets the value of the author property.
	 * 
	 * @param value
	 * allowed object is {@link String }
	 * 
	 */
	public static void setAuthor(String value)
	{
		ups_.set("author", value);
	}

	/**
	 * Gets the value of the module property.
	 * 
	 * @return
	 * possible object is {@link String }
	 * 
	 */
	public static String getModule()
	{
		return ups_.get("module", "default module");
	}

	/**
	 * Sets the value of the module property.
	 * 
	 * @param value
	 * allowed object is {@link String }
	 * 
	 */
	public static void setModule(String value)
	{
		ups_.set("module", value);
	}

	/**
	 * Gets the value of the path property.
	 * 
	 * @return
	 * possible object is {@link String }
	 * 
	 */
	public static String getPath()
	{
		return ups_.get("path", "default path");
	}

	/**
	 * Sets the value of the path property.
	 * 
	 * @param value
	 * allowed object is {@link String }
	 * 
	 */
	public static void setPath(String value)
	{
		ups_.set("path", value);
	}

	public static boolean getShowSummary()
	{
		return ups_.getBoolean("showSummary", true);
	}

	public static void setShowSummary(boolean showSummary)
	{
		Boolean old = ups_.set("showSummary", showSummary);
		// TODO cleanup
		// if (old != showSummary)
		// {
		// LegoGUI.getInstance().getLegoGUIController().showLegoSummaryPrefChanged();
		// }
	}

	public static boolean getUseFSN()
	{
		return ups_.getBoolean("useFSN", true);
	}

	public static void setUseFSN(boolean useFSN)
	{
		boolean old = ups_.set("useFSN", useFSN);
		// TODO cleanup
		// if (old != useFSN)
		// {
		// LegoGUI.getInstance().getLegoGUIController().rebuildSCTTree();
		// }
	}
}
