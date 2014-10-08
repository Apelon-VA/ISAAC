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
package gov.va.isaac.workflow.engine;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import java.rmi.RemoteException;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.RemoteRestRuntimeFactory;

/**
 * {@link RemoteWfEngine}
 *
 * @author alo
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class RemoteWfEngine
{
	private RuntimeEngine remoteEngine;

	private RemoteWfEngine()
	{
		//for HK2
	}

	public RuntimeEngine getRemoteEngine() throws RemoteException
	{
		if (remoteEngine != null)
		{
			return remoteEngine;
		}
		else
		{
			//TODO prompt the user, block, wait for their response - let them update credentials
			try
			{
				RemoteRestRuntimeFactory restSessionFactory = new RemoteRestRuntimeFactory(AppContext.getAppConfiguration().getWorkflowServerDeploymentID(), AppContext
						.getAppConfiguration().getWorkflowServerURLasURL(), ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername(),
						ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowPassword());
				remoteEngine = restSessionFactory.newRuntimeEngine();
				return remoteEngine;
			}
			catch (RuntimeException e)
			{
				throw new RemoteException("Error connecting to server", e);
			}
		}
	}

	public TaskService getRemoteTaskService() throws RemoteException
	{
		try
		{
			return getRemoteEngine().getTaskService();
		}
		catch (RuntimeException e)
		{
			throw new RemoteException("Server error", e);
		}
	}
}
