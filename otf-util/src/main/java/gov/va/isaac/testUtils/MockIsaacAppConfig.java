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
package gov.va.isaac.testUtils;

import java.net.URL;
import java.util.UUID;
import org.jvnet.hk2.annotations.Service;
import gov.va.isaac.interfaces.config.IsaacAppConfigI;

/**
 * {@link MockIsaacAppConfig}
 * 
 * A mock service to simplify testing of things outside of a full ISAAC env.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service (name = "mock")
public class MockIsaacAppConfig implements IsaacAppConfigI
{
	String appTitle_;
	String userRepoPath_;
	String workflowServerDeploymentID_;
	URL workflowServerURL_;
	UUID workflowPromotionPath_;
	
	private MockIsaacAppConfig()
	{
		//For HK2
	}
	
	public void configure(String appTitle, String userRepoPath, String workflowServerDeploymentID, URL workflowServerURL, UUID workflowPromotionPath)
	{
		appTitle_ = appTitle;
		userRepoPath_ = userRepoPath;
		workflowServerDeploymentID_ = workflowServerDeploymentID;
		workflowServerURL_ = workflowServerURL;
		workflowPromotionPath_ = workflowPromotionPath;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getApplicationTitle()
	 */
	@Override
	public String getApplicationTitle()
	{
		return appTitle_;
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getUserRepositoryPath()
	 */
	@Override
	public String getUserRepositoryPath()
	{
		return userRepoPath_;
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowServerURLasURL()
	 */
	@Override
	public URL getWorkflowServerURLasURL()
	{
		return workflowServerURL_;
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowServerDeploymentID()
	 */
	@Override
	public String getWorkflowServerDeploymentID()
	{
		return workflowServerDeploymentID_;
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getPromotionPathUUID()
	 */
	@Override
	public UUID getPromotionPathUUID()
	{
		return workflowPromotionPath_;
	}

}
