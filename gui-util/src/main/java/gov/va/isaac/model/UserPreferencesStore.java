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
package gov.va.isaac.model;

import gov.va.isaac.gui.interfaces.UserPreferencesI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Hashtable;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder for user preferences.
 * 
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
public class UserPreferencesStore extends UserPreferencesI
{
	private UserPrefs userPreferences_;
	private boolean disableSave = false;

	protected static File userPrefsPath = new File("UserPreferences.xml");

	static Logger logger = LoggerFactory.getLogger(UserPreferencesStore.class);
	static JAXBContext jc;

	static
	{
		try
		{
			jc = JAXBContext.newInstance(UserPrefs.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException("Build Error", e);
		}
	}

	private UserPreferencesStore()
	{
		readFromDisk();
	}

	/**
	 * @see gov.va.isaac.gui.interfaces.UserPreferencesI#set(java.lang.String, java.lang.String)
	 */
	@Override
	public String set(String prefName, String prefValue)
	{
		String old = userPreferences_.dataToWrite_.put(prefName, prefValue);
		writeToDisk();
		return old;
	}

	/**
	 * @see gov.va.isaac.gui.interfaces.UserPreferencesI#get(java.lang.String)
	 */
	@Override
	public String get(String prefName)
	{
		return userPreferences_.dataToWrite_.get(prefName);
	}

	private synchronized void writeToDisk()
	{
		if (disableSave)
		{
			return;
		}
		try
		{
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(userPreferences_, userPrefsPath);
		}
		catch (JAXBException e)
		{
			logger.error("Unexpected error storing user preferences", e);
		}
	}

	private synchronized void readFromDisk()
	{
		if (userPrefsPath.exists() && userPrefsPath.isFile())
		{
			try
			{
				Unmarshaller um = jc.createUnmarshaller();
				userPreferences_ = (UserPrefs) um.unmarshal(new FileReader(userPrefsPath));
			}
			catch (JAXBException | FileNotFoundException e)
			{
				logger.error("Error reading prefs file - loading defaults-  save disabled to prevent loss of existing file", e);
				disableSave = true;
				userPreferences_ = new UserPrefs();
			}
		}
		else
		{
			userPreferences_ = new UserPrefs();
		}
		if (userPreferences_.dataToWrite_ == null)
		{
			userPreferences_.dataToWrite_ = new Hashtable<String, String>();
		}
	}

	// Hack for easy XML store
	@XmlRootElement
	protected static class UserPrefs
	{
		private Hashtable<String, String> dataToWrite_;

		// jaxb doesn't work without the getters and setters...

		/**
		 * @return the dataToWrite_
		 */
		public Hashtable<String, String> getDataToWrite_()
		{
			return dataToWrite_;
		}

		/**
		 * @param dataToWrite_ the dataToWrite_ to set
		 */
		public void setDataToWrite_(Hashtable<String, String> dataToWrite_)
		{
			this.dataToWrite_ = dataToWrite_;
		}
	}

	public static void main(String[] args)
	{
		// TODO convert to junit test
		UserPreferencesStore ups = new UserPreferencesStore();
		ups.set("foo", "bar");
		ups.set("me", false);

		System.out.println(new UserPreferencesStore().get("foo"));
		System.out.println(new UserPreferencesStore().get("me"));
	}
}
