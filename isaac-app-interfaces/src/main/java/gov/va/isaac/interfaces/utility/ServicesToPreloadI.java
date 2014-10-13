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
package gov.va.isaac.interfaces.utility;

import org.jvnet.hk2.annotations.Contract;



/**
 * {@link ServicesToPreloadI}
 *
 * An interface that allows implementers to easily mark their Service as something that should be 
 * populated ASAP, when the application starts.  The main application controller will iterate over 
 * these, requesting each to be constructed after initial startup.
 * 
 * Typically, this would only be used on Singleton implementations... 
 * 
 * All implementations should background thread any real work that happens as a consequence of being
 * constructed.  No expensive operations should be done either in the constructor, or in the loadRequested() 
 * method on the calling thread.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Contract
public interface ServicesToPreloadI
{
	/**
	 * Implementers don't actually have to do anything when called here... they can do the init in the constructor if 
	 * the prefer. This is just to prevent the compiler from doing something silly and dropping the for-loop over the 
	 * ServiceToPreloadI objects, when it looks like nothing is actually being done.  Implementors may do something here,
	 *  however, if they prefer to have the constructor do nothing, and instead, handle the loading when loadRequested 
	 *  is called.  This will be called during the startup sequence, after the database init has completed.
	 * 
	 * If any expensive works needs to be done as a result of loadRequested - it should be done in a background thread - loadRequested
	 * should return immediately.
	 */
	public void loadRequested();
	
	/**
	 * Called when the application is shutting down
	 */
	public void shutdown();
}
