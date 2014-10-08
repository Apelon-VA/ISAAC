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

import gov.va.isaac.interfaces.config.IsaacAppConfigI;

import java.net.URL;
import java.util.UUID;

import org.jvnet.hk2.annotations.Service;

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
	
	/**
	 * Configure.
	 *
	 * @param appTitle the app title
	 * @param userRepoPath the user repo path
	 * @param workflowServerDeploymentID the workflow server deployment id
	 * @param workflowServerURL the workflow server url
	 * @param workflowPromotionPath the workflow promotion path
	 */
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
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getChangeSetUrl()
	 */
	@Override
	public String getChangeSetUrl()
	{
		return userRepoPath_;
	}

  @Override
  public String getArchetypeGroupId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getArchetypeArtifactId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getArchetypeVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getIsaacVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getScmConnection() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getScmUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDistReposId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDistReposName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDistReposUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDistReposSnapId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDistReposSnapName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDistReposSnapUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDbGroupId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDbArtifactId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDbVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDbClassifier() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDroolsGroupId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDroolsArtifactId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDroolsVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDroolsClassifier() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPreviousReleaseVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getReleaseVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getExtensionNamespace() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDefaultEditPathName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDefaultEditPathUuid() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDefaultViewPathName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDefaultViewPathUuid() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getUserSchemaLocation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getWorkflowServerUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getWorkflowServerDeploymentId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPromotionPath() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getWorkflowServerUrlAsURL() {
    return IsaacAppConfigI.getUrlForString(getWorkflowServerUrl());
  }

  @Override
  public URL getScmUrlAsURL() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getDistReposUrlAsURL() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getDistReposSnapUrlAsURL() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getChangeSetUrlAsURL() {
    // TODO Auto-generated method stub
    return null;
  }

}
