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
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentChangeSetUrl()
	 */
	@Override
	public String getCurrentChangeSetUrl()
	{
		return userRepoPath_;
	}

	/**
	 * Returns the workflow server url as url.
	 *
	 * @return the workflow server url as url
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowServerUrlAsURL()
	 */
	@Override
	public URL getCurrentWorkflowServerUrlAsURL()
	{
		return workflowServerURL_;
	}

	/**
	 * Returns the workflow server deployment id.
	 *
	 * @return the workflow server deployment id
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowServerDeploymentId()
	 */
	@Override
	public String getCurrentWorkflowServerDeploymentId()
	{
		return workflowServerDeploymentID_;
	}

	/**
	 * Returns the promotion path as uuid.
	 *
	 * @return the promotion path as uuid
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowPromotionPathUuidAsUUID()
	 */
	@Override
	public UUID getCurrentWorkflowPromotionPathUuidAsUUID()
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
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentReleaseVersion()
	 */
	@Override
	public String getCurrentReleaseVersion() {
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentExtensionNamespace()
	 */
	@Override
	public String getCurrentExtensionNamespace() {
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
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowServerUrl()
	 */
	@Override
	public String getCurrentWorkflowServerUrl() {
		return null;
	}

	@Override
	public String getCurrentWorkflowPromotionPathName() {
		return null;
	}

	@Override
	public String getCurrentWorkflowPromotionPathUuid() {
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

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultReleaseVersion()
	 */
	@Override
	public String getDefaultReleaseVersion()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultExtensionNamespace()
	 */
	@Override
	public String getDefaultExtensionNamespace()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultChangeSetUrl()
	 */
	@Override
	public String getDefaultChangeSetUrl()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentEditPathName()
	 */
	@Override
	public String getCurrentEditPathName()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentEditPathUuid()
	 */
	@Override
	public String getCurrentEditPathUuid()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentViewPathName()
	 */
	@Override
	public String getCurrentViewPathName()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentViewPathUuid()
	 */
	@Override
	public String getCurrentViewPathUuid()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowServerUrl()
	 */
	@Override
	public String getDefaultWorkflowServerUrl()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowServerUrlAsURL()
	 */
	@Override
	public URL getDefaultWorkflowServerUrlAsURL()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowServerDeploymentId()
	 */
	@Override
	public String getDefaultWorkflowServerDeploymentId()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowPromotionPathName()
	 */
	@Override
	public String getDefaultWorkflowPromotionPathName()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowPromotionPathUuid()
	 */
	@Override
	public String getDefaultWorkflowPromotionPathUuid()
	{
		return null;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowPromotionPathUuidAsUUID()
	 */
	@Override
	public UUID getDefaultWorkflowPromotionPathUuidAsUUID()
	{
		return null;
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getVersion()
	 */
	@Override
	public String getVersion()
	{
		return null;
	}

}
