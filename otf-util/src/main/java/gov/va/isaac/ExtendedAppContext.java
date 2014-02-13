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

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * 
 * Note - this just contains convenience methods at this point... callers can go
 * directly to HK2 if they prefer.
 * 
 * This 'extended' class provides access to methods that require APIs that aren't available (due to dependencies)
 * in the parent 'AppContext' which is purposefully packaged in a project with the bare minimum of external dependencies.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExtendedAppContext extends AppContext
{
	public static BdbTerminologyStore getDataStore()
	{
		return getService(BdbTerminologyStore.class);
	}
}
