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
package gov.va.isaac;

import gov.va.isaac.interfaces.RuntimeGlobalsI;
import gov.va.isaac.interfaces.config.IsaacAppConfigI;
import gov.va.isaac.interfaces.gui.ApplicationWindowI;
import gov.va.isaac.interfaces.gui.CommonDialogsI;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PopupConceptViewI;
import gov.va.oia.HK2Utilities.HK2RuntimeInitializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppContext
 *
 * Provides convenience methods for retrieving implementations of various interfaces
 * from the HK2 dependency management system.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AppContext
{
	public static final String READ_INHABITANT_FILES = "gov.va.isaac.AppContext.READ_INHABITANT_FILES";
	public static final String EXTRA_PACKAGES_TO_SEARCH = "gov.va.isaac.AppContext.EXTRA_PACKAGES_TO_SEARCH";

	private static ServiceLocator serviceLocator_;

	private static Logger log_ = LoggerFactory.getLogger(AppContext.class);

	/**
	 * Call this once (and only once) to initialize the ISAAC HK2 service Locator.
	 * After this is called, you can access the service locator via convenience methods here,
	 * or via a call directly to HK2: 
	 * {@code
	 *     ServiceLocatorFactory.getInstance().create("ISAAC");
	 * }
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 *
	 */
	public synchronized static void setup() throws ClassNotFoundException, IOException
	{
		if (serviceLocator_ != null)
		{
			throw new RuntimeException("Only one service locator should be set");
		}
		ArrayList<String> packagesToSearch = new ArrayList<String>(Arrays.asList("gov.va", "org.ihtsdo"));

		boolean readInhabitantFiles = Boolean.getBoolean(System.getProperty(READ_INHABITANT_FILES, "false"));
		if (System.getProperty(EXTRA_PACKAGES_TO_SEARCH) != null) {
			String[] extraPackagesToSearch = System.getProperty(EXTRA_PACKAGES_TO_SEARCH).split(";");
			for (String packageToSearch: extraPackagesToSearch) {
				packagesToSearch.add(packageToSearch);
			}
		}
		serviceLocator_ = HK2RuntimeInitializer.init("ISAAC", readInhabitantFiles, packagesToSearch.toArray(new String[]{}));
	}

	/**
	 * @return
	 */
	public static IsaacAppConfigI getAppConfiguration()
	{
		return getService(IsaacAppConfigI.class);
	}

	public static ServiceLocator getServiceLocator()
	{
		return serviceLocator_;
	}

	public static <T> T getService(Class<T> contractOrService)
	{
		List<ServiceHandle<T>> handles = serviceLocator_.getAllServiceHandles(contractOrService);
		
		for (ServiceHandle<T> handle : handles)
		{
			if (handle.getActiveDescriptor().getName() == null || handle.getActiveDescriptor().getName().length() == 0)
			{
				//prefer this un-named one
				return handle.getService();
			}
		}
		
		//couldn't find an un-named one - just return the default from HK2 (which still may be null)
		return serviceLocator_.getService(contractOrService);
	}

	/**
	 * Find a service by name, and automatically fall back to any service which implements the contract if the named service was not available.
	 * 
	 * @param contractOrService May not be null, and is the contract or concrete implementation to get the best instance of
	 * @param name May be null (to indicate any name is ok), and is the name of the implementation to be returned
	 * @param qualifiers The set of qualifiers that must match this service definition
	 * @return
	 */
	public static <T> T getService(Class<T> contractOrService, String name)
	{
		T service = serviceLocator_.getService(contractOrService, name);
		if (service == null && name != null)
		{
			log_.info("Requested service '" + name + "' was not available, returning arbitrary service " + "which matches the contract (if any)");
			return serviceLocator_.getService(contractOrService);
		}
		return service;
	}

	public static CommonDialogsI getCommonDialogs()
	{
		return getService(CommonDialogsI.class);
	}

	public static ApplicationWindowI getMainApplicationWindow()
	{
		return getService(ApplicationWindowI.class);
	}
	
	public static RuntimeGlobalsI getRuntimeGlobals()
	{
		return getService(RuntimeGlobalsI.class);
	}

	public static PopupConceptViewI createConceptViewWindow()
	{
		return getService(PopupConceptViewI.class, SharedServiceNames.LEGACY_STYLE);
	}
}
