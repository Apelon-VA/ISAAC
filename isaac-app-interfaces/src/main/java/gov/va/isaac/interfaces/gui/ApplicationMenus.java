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
package gov.va.isaac.interfaces.gui;

/**
 * {@link ApplicationMenus}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum ApplicationMenus
{
	PANELS("Panels", "panelsMenu"),
	IMPORT_EXPORT("Import/Export", "importExportMenu"),
	ACTIONS("Actions", "actionsMenus"),
	HELP("Help", "helpMenus", true);
	
	private String name_;
	private String id_;
	private boolean alwaysAvailable_ = false;
	
	private ApplicationMenus(String menuName, String id)
	{
		name_ = menuName;
		id_ = id;
	}
	
	private ApplicationMenus(String menuName, String id, boolean alwaysAvailable)
	{
		name_ = menuName;
		id_ = id;
		alwaysAvailable_ = alwaysAvailable;
	}

	/**
	 * @return the menu name
	 */
	public String getMenuName()
	{
		return name_;
	}

	/**
	 * @return the id
	 */
	public String getMenuId()
	{
		return id_;
	}
	
	public boolean getAlwaysAvailable()
	{
		return alwaysAvailable_;
	}
}
