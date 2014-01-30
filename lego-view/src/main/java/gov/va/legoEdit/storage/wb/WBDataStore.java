/**
 * Copyright 2014
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
package gov.va.legoEdit.storage.wb;

import java.io.File;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * WBDataStore
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class WBDataStore
{
	private static BdbTerminologyStore dataStoreReference_ = null;
	
	public static void setStore(BdbTerminologyStore store)
	{
		dataStoreReference_ = store;
	}
	
	public static void openStore(File path)
	{
		//Yes, seriously, the API is so strange, you can't pass in a proper file you want to open, you have to set it as a string, in a system property.
		//god forbid we ever want to open to DBs at the same time....
		System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, path.getAbsolutePath());
		dataStoreReference_ = new BdbTerminologyStore();
	}
	
	public static void shutdown()
	{
		dataStoreReference_.shutdown();
	}
	
	public static BdbTerminologyStore dataStore()
	{
		if (dataStoreReference_ == null)
		{
			throw new RuntimeException("DataStore was not initialized!");
		}
		return dataStoreReference_;
	}
}
