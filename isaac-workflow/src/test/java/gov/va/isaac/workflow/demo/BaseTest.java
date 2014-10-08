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
package gov.va.isaac.workflow.demo;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.testUtils.MockIsaacAppConfig;
import gov.va.isaac.testUtils.MockUserProfileManager;
import gov.va.isaac.workflow.persistence.DatastoreManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * {@link BaseTest}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
public class BaseTest 
{
	protected static void setup() throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
			SecurityException
	{
		// Configure Java logging into logback
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		AppContext.setup();
		// TODO OTF fix: this needs to be fixed so I don't have to hack it with reflection.... https://jira.ihtsdotools.org/browse/OTFISSUE-11
		Field f = Hk2Looker.class.getDeclaredField("looker");
		f.setAccessible(true);
		f.set(null, AppContext.getServiceLocator());
		
		AppContext.getServiceLocator().getServiceHandle(UserProfileManager.class).destroy();
		//AppContext.getServiceLocator().getServiceHandle(IsaacAppConfigWrapper.class).destroy();  //This isn't on the classpath for local run
		
		AppContext.getService(MockIsaacAppConfig.class).configure("test", "test", "gov.va.isaac.demo:terminology-authoring:1.4",
				new URL("http://162.243.255.43:8080/kie-wb/"));
		
		
		UserProfile up = new UserProfile("test", "test");
		up.setWorkflowPassword("alejandro");
		up.setWorkflowUsername("alejandro");
		
		AppContext.getService(MockUserProfileManager.class).configure(up);
		AppContext.getService(DatastoreManager.class).loadRequested();

		//Hack the extended appcontext to return our mock UserProfileManager
		f = ExtendedAppContext.class.getDeclaredField("userProfileManagerClass");
		f.setAccessible(true);
		f.set(null, MockUserProfileManager.class);
	}
}
