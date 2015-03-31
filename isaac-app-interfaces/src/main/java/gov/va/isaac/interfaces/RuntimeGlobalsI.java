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
package gov.va.isaac.interfaces;

import gov.va.isaac.interfaces.utility.CommitListenerI;
import gov.va.isaac.interfaces.utility.ServicesToPreloadI;
import gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI;
import java.util.List;
import javax.naming.InvalidNameException;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link RuntimeGlobalsI}
 * 
 * An interface that defines methods that may be useful to any ISAAC module, but do 
 * not specifically pertain to GUI, nor to they specifically fit the HK2 Service 
 * paradigm.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface RuntimeGlobalsI
{
	/**
	 * Register a for a callback to be notified when an app shutdown is requested.
	 * Note that this is only required for classes that are not an HK2 Service - HK2 
	 * service classes can get shutdown notification by implementing {@link ServicesToPreloadI}
	 * @param listener
	 */
	public void registerShutdownListener(ShutdownBroadcastListenerI listener);
	
	/**
	 * Returns the names of all registered {@link CommitListenerI} implementations.
	 */
	public List<String> getAllCommitListenerNames();
	
	/**
	 * Disable the specified commit listener - overriding any user-specific setting for 
	 * enable/disable.
	 */
	public void disableCommitListener(String commitListenerName) throws InvalidNameException;
	
	/**
	 * Notify the specified commit listener that it may enable itself, if it desires (the 
	 * disable override is no longer in force)
	 */
	public void enableCommitListener(String commitListenerName) throws InvalidNameException;
	
	
	/**
	 * Notify all commit listeners that they may enable themselves, if they desire (the 
	 * disable override is no longer in force)
	 */
	public void enableAllCommitListeners();
	
	/**
	 * Disable all commit listeners - overriding any user-specific setting for enable/disable.
	 */
	public void disableAllCommitListeners();
}
