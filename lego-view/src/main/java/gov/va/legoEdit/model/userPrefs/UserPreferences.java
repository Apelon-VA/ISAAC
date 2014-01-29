/**
 * Copyright 2013
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
package gov.va.legoEdit.model.userPrefs;

import gov.va.legoEdit.formats.UserPrefsXMLUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * UserPreferences
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "userPreferences")
public class UserPreferences
{
	private static volatile UserPreferences userPreferences_;
	
	@XmlAttribute(name = "defaultStatus") protected String defaultStatus;
	@XmlAttribute(name = "author") protected String author;
	@XmlAttribute(name = "module") protected String module;
	@XmlAttribute(name = "path") protected String path;
	@XmlAttribute(name = "showSummary") protected boolean showSummary = true;
	@XmlAttribute(name = "useFSN") protected boolean useFSN = true;
	
	@SuppressWarnings("unused")
	private void UserPreference()
	{
		//Noop
	}
	
	public static UserPreferences getInstance()
	{
		if (userPreferences_ == null)
		{
			synchronized (UserPreferences.class)
			{
				if (userPreferences_ == null)
				{
					try
					{
						userPreferences_ = UserPrefsXMLUtils.readUserPreferences();
					}
					catch (Exception e)
					{
						//TODO this should be a bit smarter... some errors are ok, others, not so much...
						userPreferences_ = new UserPreferences();
						userPreferences_.setDefaultStatus("Active");
						userPreferences_.setAuthor(System.getProperty("user.name"));
						userPreferences_.setModule("default module");
						userPreferences_.setPath("default path");
					}
				}
			}
		}
		return userPreferences_;
	}
	
	/**
	 * Gets the value of the defaultStatus property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getDefaultStatus()
	{
		return defaultStatus;
	}

	/**
	 * Sets the value of the defaultStatus property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDefaultStatus(String value)
	{
		this.defaultStatus = value;
	}

	/**
	 * Gets the value of the author property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Sets the value of the author property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAuthor(String value)
	{
		this.author = value;
	}

	/**
	 * Gets the value of the module property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getModule()
	{
		return module;
	}

	/**
	 * Sets the value of the module property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setModule(String value)
	{
		this.module = value;
	}

	/**
	 * Gets the value of the path property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Sets the value of the path property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPath(String value)
	{
		this.path = value;
	}
	

	public boolean getShowSummary()
	{
		return showSummary;
	}

	public void setShowSummary(boolean showSummary)
	{
		boolean old = this.showSummary;
		this.showSummary = showSummary;
//TODO cleanup
//		if (old != this.showSummary)
//		{
//			LegoGUI.getInstance().getLegoGUIController().showLegoSummaryPrefChanged();
//		}
	}
	
	public boolean getUseFSN()
	{
		return useFSN;
	}

	public void setUseFSN(boolean useFSN)
	{
		boolean old = this.useFSN;
		this.useFSN = useFSN;
//TODO cleanup
//		if (old !=  this.useFSN)
//		{
//			LegoGUI.getInstance().getLegoGUIController().rebuildSCTTree();
//		}
	}
}
