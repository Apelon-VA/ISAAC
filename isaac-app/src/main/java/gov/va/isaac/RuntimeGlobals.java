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
import gov.va.isaac.interfaces.utility.CommitListenerI;
import gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InvalidNameException;
import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link RuntimeGlobals}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class RuntimeGlobals implements RuntimeGlobalsI
{
	private ArrayList<ShutdownBroadcastListenerI> shutdownListeners_ = new ArrayList<>();
	@Inject private IterableProvider<CommitListenerI> commitListeners_;

	private RuntimeGlobals()
	{
		//For HK2 to construct
	}
	
	/**
	 * Do not call this - only for internal use.
	 */
	public void shutdown()
	{
		for (ShutdownBroadcastListenerI s : shutdownListeners_)
		{
			if (s != null)
			{
				s.shutdown();
			}
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.RuntimeGlobalsI#registerShutdownListener(gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI)
	 */
	@Override
	public void registerShutdownListener(ShutdownBroadcastListenerI listener)
	{
		//TODO this needs to use weak references - but would rather just get rid of the interface, and allow HK2 to manage.
		shutdownListeners_.add(listener);
	}

	/**
	 * @see gov.va.isaac.interfaces.RuntimeGlobalsI#getAllCommitListenerNames()
	 */
	@Override
	public List<String> getAllCommitListenerNames()
	{
		ArrayList<String> result = new ArrayList<>();
		for (CommitListenerI cl : commitListeners_)
		{
			result.add(cl.getListenerName());
		}
		return result;
	}

	/**
	 * @see gov.va.isaac.interfaces.RuntimeGlobalsI#disableCommitListener(java.lang.String)
	 */
	@Override
	public void disableCommitListener(String commitListenerName) throws InvalidNameException
	{
		CommitListenerI item = AppContext.getService(CommitListenerI.class, commitListenerName);
		if (item == null)
		{
			throw new InvalidNameException("Couldn't find a commit listener with the name '" + commitListenerName + "'");
		}
		else
		{
			item.disable();
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.RuntimeGlobalsI#enableCommitListener(java.lang.String)
	 */
	@Override
	public void enableCommitListener(String commitListenerName) throws InvalidNameException
	{
		CommitListenerI item = AppContext.getService(CommitListenerI.class, commitListenerName);
		if (item == null)
		{
			throw new InvalidNameException("Couldn't find a commit listener with the name '" + commitListenerName + "'");
		}
		else
		{
			item.enable();
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.RuntimeGlobalsI#enableAllCommitListeners()
	 */
	@Override
	public void enableAllCommitListeners()
	{
		for (CommitListenerI cl : commitListeners_)
		{
			cl.enable();
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.RuntimeGlobalsI#disableAllCommitListeners()
	 */
	@Override
	public void disableAllCommitListeners()
	{
		for (CommitListenerI cl : commitListeners_)
		{
			cl.disable();
		}
	}
}
