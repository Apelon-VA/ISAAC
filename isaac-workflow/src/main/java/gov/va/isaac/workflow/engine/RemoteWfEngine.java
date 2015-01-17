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
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.gui.users.Credentials;
import gov.va.isaac.gui.users.CredentialsPromptDialog;
import gov.va.isaac.workflow.persistence.LocalTasksApi;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import javafx.application.Platform;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.RemoteRestRuntimeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOG = LoggerFactory.getLogger(RemoteWfEngine.class);

	private RuntimeEngine remoteEngine;
	
	private transient Credentials promptResult;
	private CountDownLatch waitForPrompt = null;

	private RemoteWfEngine()
	{
		//for HK2
	}

	public RuntimeEngine getRemoteEngine() throws RemoteException
	{
		RuntimeEngine re = remoteEngine;
		if (re != null)
		{
			return re;
		}
		else
		{
			final String deploymentId = AppContext.getAppConfiguration().getCurrentWorkflowServerDeploymentId();
			final URL workflowServerUrlAsURL = AppContext.getAppConfiguration().getCurrentWorkflowServerUrlAsURL();
			final String workflowUserName = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowUsername();
			final String workflowPassword = ExtendedAppContext.getCurrentlyLoggedInUserProfile().getWorkflowPassword();
			try
			{
				LOG.debug("Getting remote WF engine with deploymentId={}, workflowServerUrlAsURL={} and workflowUserName={}", deploymentId, workflowServerUrlAsURL, workflowUserName);
				RemoteRestRuntimeFactory restSessionFactory = new RemoteRestRuntimeFactory(
						deploymentId, 
						workflowServerUrlAsURL, 
						workflowUserName,
						workflowPassword);
				re = restSessionFactory.newRuntimeEngine();
				re.getTaskService().getTaskById(-1);  //Something to validate the credentials are correct...
				remoteEngine = re;
				return re;
			}
			catch (RuntimeException e)
			{
				if (Platform.isFxApplicationThread())
				{
					throw new RuntimeException("API Misuse!  Do not call a  remote operation on the Platform thread!");
				}
				
				waitForPrompt = new CountDownLatch(1);
				promptResult = null;
				
				Platform.runLater(() ->
				{
					AppContext.getService(CredentialsPromptDialog.class).showView(workflowUserName, workflowPassword, 
							"Please provide your Workflow server (KIE) credentials", new Consumer<Credentials>()
							{
								@Override
								public void accept(Credentials t)
								{
									promptResult = t;
									waitForPrompt.countDown();
								}
							});
				});
				
				try
				{
					waitForPrompt.await();
					if (promptResult != null)
					{
						UserProfile up = AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile();
						if (!up.getWorkflowUsername().equals(promptResult.getUsername()))
						{
							AppContext.getService(LocalTasksApi.class).changeUserName(up.getWorkflowUsername(), promptResult.getUsername());
						}
						up.setWorkflowUsername(promptResult.getUsername());
						up.setWorkflowPassword(promptResult.getPassword());
						try
						{
							AppContext.getService(UserProfileManager.class).saveChanges(up);
						}
						catch (Exception e1)
						{
							throw new RuntimeException("Unexpected error saving credentials");
						}
						return getRemoteEngine();  //loop
					}
					//else - just let the remote exception go back
				}
				catch (InterruptedException e1)
				{
					LOG.info("Interrupted while waiting for credentials");
					// noop - just let the remote exception go back.
				}
				
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
	
	/**
	 * Calling this will force the next call that requires remote access to reconnect
	 */
	public void closeConnection()
	{
		remoteEngine = null;
	}
}
