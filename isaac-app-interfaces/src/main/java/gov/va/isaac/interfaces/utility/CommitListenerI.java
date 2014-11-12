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
 * {@link CommitListenerI}
 *
 * An interface that allows implementers to easily mark their Service as something that will register 
 * itself with {@link BdbTerminologyStore#addPropertyChangeListener} as a class that does things on 
 * preCommit / Commit / postCommit.
 * 
 * The only intent of this interface is to provide a global handle to all modules which are registered to do 
 * things - so that then can be enabled or disabled individually, or across the board prior to performing 
 * programmatic operations where you don't want the listeners firing.
 * 
 * An example of this - would be disabling the Workflow Initiation listener prior to importing batch data.
 * 
 * =====================================
 * Implementers of this class MUST use a @Named annotation:
 * <code>@Named (value="the name")</code>
 * =====================================
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Contract
public interface CommitListenerI
{
	/**
	 * Returns the HK2 service name of this listener.  This should return the same string as the 
	 * <code>@Named (value="the name")</code>
	 * annotation on the implementation of the class.
	 */
	public String getListenerName();
	
	/**
	 * Tell the listener that it MAY enable itself (it does not have to).  A listener may be primarily controlled
	 * by a user preference, for example.  While {@link #disable()} overrides the user preferences, and orders the 
	 * listener disabled - this call only informs the user that it is no longer being overridden - and may return to 
	 * the enabled state if that was the users preference.
	 */
	public void enable();
	
	/**
	 * Tell the listener to disable itself - either by unregistering upstream, or ignoring all commit related events 
	 * until {@link #enable()} is called.
	 */
	public void disable();
}
