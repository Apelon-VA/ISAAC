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
package gov.va.legoEdit.gui;

import gov.va.isaac.AppContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import javafx.application.Application;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * {@link LegoGUILauncher}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class LegoGUILauncher
{

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, IOException
	{
		System.out.println("Launching");
		AppContext.setup();
		// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
		Field f = Hk2Looker.class.getDeclaredField("looker");
		f.setAccessible(true);
		f.set(null, AppContext.getServiceLocator());
		System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, new File("../isaac-app/berkeley-db").getAbsolutePath());
		Application.launch(LegoGUI.class, args);
	}

}
