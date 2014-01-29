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
package gov.va.legoEdit.formats;

import gov.va.legoEdit.model.userPrefs.UserPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * UserPrefsXMLUtils
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class UserPrefsXMLUtils
{
	protected static File userPrefsPath = new File("UserPreferences.xml");

	static Logger logger = LoggerFactory.getLogger(UserPrefsXMLUtils.class);
	static JAXBContext jc;

	static
	{
		try
		{
			jc = JAXBContext.newInstance(UserPreferences.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException("Build Error", e);
		}
	}

	public static UserPreferences readUserPreferences() throws JAXBException, FileNotFoundException
	{
		Unmarshaller um = jc.createUnmarshaller();
		return (UserPreferences) um.unmarshal(new FileReader(userPrefsPath));
	}

	public static void writeUserPreferences(UserPreferences userPreferences) throws PropertyException, JAXBException
	{
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(userPreferences, userPrefsPath);
	}
}
