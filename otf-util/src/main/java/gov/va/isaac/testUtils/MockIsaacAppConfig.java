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
 * A mock service to simplify testing of things outside of a full ISAAC env.  Most things are not implemented.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service (name = "mock")
public class MockIsaacAppConfig implements IsaacAppConfigI
{
	
	/**  The app title_. */
	String appTitle_;
	
	/**  The user repo path_. */
	String userRepoPath_;
	
	/**  The workflow server deployment i d_. */
	String workflowServerDeploymentID_;
	
	/**  The workflow server ur l_. */
	URL workflowServerURL_;
	
	/**  The workflow promotion path_. */
	UUID workflowPromotionPath_;
	
	/**
	 * Instantiates an empty {@link MockIsaacAppConfig}.
	 */
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
	 * Returns the application title.
	 *
	 * @return the application title
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getApplicationTitle()
	 */
	@Override
	public String getApplicationTitle()
	{
		return appTitle_;
	}

	/**
	 * Returns the change set url.
	 *
	 * @return the change set url
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getChangeSetUrl()
	 */
	@Override
	public String getChangeSetUrl()
	{
		return userRepoPath_;
	}

	/**
	 * Returns the workflow server url as url.
	 *
	 * @return the workflow server url as url
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowServerUrlAsURL()
	 */
	@Override
	public URL getWorkflowServerUrlAsURL()
	{
		return workflowServerURL_;
	}

	/**
	 * Returns the workflow server deployment id.
	 *
	 * @return the workflow server deployment id
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowServerDeploymentId()
	 */
	@Override
	public String getWorkflowServerDeploymentId()
	{
		return workflowServerDeploymentID_;
	}

	/**
	 * Returns the promotion path as uuid.
	 *
	 * @return the promotion path as uuid
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowPromotionPathUuidAsUUID()
	 */
	@Override
	public UUID getWorkflowPromotionPathUuidAsUUID()
	{
		return workflowPromotionPath_;
	}

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getArchetypeGroupId()
   */
  @Override
  public String getArchetypeGroupId() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getArchetypeArtifactId()
   */
  @Override
  public String getArchetypeArtifactId() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getArchetypeVersion()
   */
  @Override
  public String getArchetypeVersion() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getIsaacVersion()
   */
  @Override
  public String getIsaacVersion() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getScmConnection()
   */
  @Override
  public String getScmConnection() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getScmUrl()
   */
  @Override
  public String getScmUrl() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getScmUrlAsURL()
   */
  @Override
  public URL getScmUrlAsURL() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDbGroupId()
   */
  @Override
  public String getDbGroupId() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDbArtifactId()
   */
  @Override
  public String getDbArtifactId() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDbVersion()
   */
  @Override
  public String getDbVersion() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDbClassifier()
   */
  @Override
  public String getDbClassifier() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getPreviousReleaseVersion()
   */
  @Override
  public String getPreviousReleaseVersion() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getReleaseVersion()
   */
  @Override
  public String getReleaseVersion() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getExtensionNamespace()
   */
  @Override
  public String getExtensionNamespace() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getModuleId()
   */
  @Override
  public String getModuleId() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getChangeSetUrlAsURL()
   */
  @Override
  public URL getChangeSetUrlAsURL() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultEditPathName()
   */
  @Override
  public String getDefaultEditPathName() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultEditPathUuid()
   */
  @Override
  public String getDefaultEditPathUuid() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultViewPathName()
   */
  @Override
  public String getDefaultViewPathName() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultViewPathUuid()
   */
  @Override
  public String getDefaultViewPathUuid() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getUserSchemaLocation()
   */
  @Override
  public String getUserSchemaLocation() {

    return null;
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowServerUrl()
   */
  @Override
  public String getWorkflowServerUrl() {

    return null;
  }

  @Override
  public String getWorkflowPromotionPathName() {

    return null;
  }

  @Override
  public String getWorkflowPromotionPathUuid() {

    return null;
  }

  @Override
  public String getAppSchemaLocation() {

    return null;
  }

  @Override
  public String getDbType() {

    return null;
  }


  @Override
  public String getChangeSetUrlTypeName()
  {

    return null;
  }

}
