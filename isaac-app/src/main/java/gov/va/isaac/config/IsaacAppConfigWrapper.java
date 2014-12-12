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
 *		 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.config;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.generated.IsaacAppConfig;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.interfaces.config.IsaacAppConfigI;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import javax.inject.Singleton;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * {@link IsaacAppConfigWrapper}
 * 
 * An ugly hack to make the generated (@link IsaacAppConfig} class implement the {@link IsaacAppConfigI}
 * But it also gives us a place to set up some defaults, if the user provided data was unreadable / missing.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
@Singleton
public class IsaacAppConfigWrapper extends IsaacAppConfig implements IsaacAppConfigI
{
	private static Logger log_ = LoggerFactory.getLogger(IsaacAppConfigWrapper.class);
	
	private IsaacAppConfigWrapper()
	{
		//This is contructed by HK2
		//Default values
		setApplicationTitle("Default (unbranded) ISAAC Application");
		
		try
		{
			InputStream in = AppContext.class.getResourceAsStream("/app.xml");
			if (in != null)
			{
				IsaacAppConfig temp = unmarshallStream(in);
				copyHack(temp);
			}
			else
			{
				log_.warn("App configuration file not found, using defaults");
			}

		}
		catch (Exception ex)
		{
			log_.warn("Unexpected error reading app configuration file, using defaults", ex);
		}
	}
	
	/**
	 * Exposed publicly as a utility method, for reuse in MOJO classes that need to read the app.xml file when the 
	 * rest of the application is not running.
	 * @param in - A stream that will read app.xml - validating to AppConfigSchema.xsd.
	 * @return - The parsed java representation
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	public static IsaacAppConfig unmarshallStream(InputStream in) throws IOException, JAXBException, SAXException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(IsaacAppConfig.class);
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new StreamSource(AppContext.class.getResourceAsStream("/xsd/AppConfigSchema.xsd")));
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(schema);
		IsaacAppConfig temp = (IsaacAppConfig) jaxbUnmarshaller.unmarshal(in);
		in.close();
		return temp;
	}

	/**
	 * Copy all of the data from the deserialized data - which doesn't implement our interface - to this instance, which does.
	 *
	 * @param read the read
	 */
	private void copyHack(IsaacAppConfig read)
	{
		if (read.getApplicationTitle() != null && read.getApplicationTitle().length() > 0)
		{
			setApplicationTitle(read.getApplicationTitle());
		}
		setArchetypeGroupId(read.getArchetypeGroupId());
		setArchetypeArtifactId(read.getArchetypeArtifactId());
		setArchetypeVersion(read.getArchetypeVersion());
		setIsaacVersion(read.getIsaacVersion());
		setScmConnection(read.getScmConnection());
		setScmUrl(read.getScmUrl());
		setDbGroupId(read.getDbGroupId());
		setDbArtifactId(read.getDbArtifactId());
		setDbVersion(read.getDbVersion());
		setDbClassifier(read.getDbClassifier());
		setDbType(read.getDbType());
		setApplicationTitle(read.getApplicationTitle());
		setPreviousReleaseVersion(read.getPreviousReleaseVersion());
		setReleaseVersion(read.getReleaseVersion());
		setExtensionNamespace(read.getExtensionNamespace());
		setModuleId(read.getModuleId());
		setChangeSetUrl(read.getChangeSetUrl());
		setChangeSetUrlType(read.getChangeSetUrlType());
		setAppSchemaLocation(read.getAppSchemaLocation());
		setUserSchemaLocation(read.getUserSchemaLocation());
		setWorkflowServerUrl(read.getWorkflowServerUrl());
		setWorkflowServerDeploymentId(read.getWorkflowServerDeploymentId());
		setDefaultEditPathName(read.getDefaultEditPathName());
		setDefaultEditPathUuid(read.getDefaultEditPathUuid());
		setDefaultViewPathName(read.getDefaultViewPathName());
		setDefaultViewPathUuid(read.getDefaultViewPathUuid());
		setWorkflowPromotionPathName(read.getWorkflowPromotionPathName());
		setWorkflowPromotionPathUuid(read.getWorkflowPromotionPathUuid());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowServerUrlAsURL()
	 */
	@Override
	public URL getDefaultWorkflowServerUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getDefaultWorkflowServerUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getScmUrlAsURL()
	 */
	@Override
	public URL getScmUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getScmUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getPromotionPathAsUUID()
	 */
	@Override
	public UUID getDefaultWorkflowPromotionPathUuidAsUUID() {
		return IsaacAppConfigI.getUuidForString(getDefaultWorkflowPromotionPathUuid());
	}

	/*
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getChangeSetUrlTypeName()
	 */
	@Override
	public String getChangeSetUrlTypeName()
	{
		return getChangeSetUrlType().name();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultReleaseVersion()
	 */
	@Override
	public String getDefaultReleaseVersion()
	{
		return getReleaseVersion();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentReleaseVersion()
	 */
	@Override
	public String getCurrentReleaseVersion()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getReleaseVersion();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultExtensionNamespace()
	 */
	@Override
	public String getDefaultExtensionNamespace()
	{
		return getExtensionNamespace();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentExtensionNamespace()
	 */
	@Override
	public String getCurrentExtensionNamespace()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getExtensionNamespace();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultChangeSetUrl()
	 */
	@Override
	public String getDefaultChangeSetUrl()
	{
		return getChangeSetUrl();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentChangeSetUrl()
	 */
	@Override
	public String getCurrentChangeSetUrl()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getChangeSetUrl();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentEditPathName()
	 */
	@Override
	public String getCurrentEditPathName()
	{
		return WBUtility.getDescriptionIfConceptExists(IsaacAppConfigI.getUuidForString(getCurrentEditPathUuid()));
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentEditPathUuid()
	 */
	@Override
	public String getCurrentEditPathUuid()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getEditCoordinatePath().toString();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentViewPathName()
	 */
	@Override
	public String getCurrentViewPathName()
	{
		return WBUtility.getDescriptionIfConceptExists(IsaacAppConfigI.getUuidForString(getCurrentViewPathUuid()));
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentViewPathUuid()
	 */
	@Override
	public String getCurrentViewPathUuid()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getViewCoordinatePath().toString();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowServerUrl()
	 */
	@Override
	public String getDefaultWorkflowServerUrl()
	{
		return getWorkflowServerUrl();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowServerUrl()
	 */
	@Override
	public String getCurrentWorkflowServerUrl()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getWorkflowServerUrl();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowServerUrlAsURL()
	 */
	@Override
	public URL getCurrentWorkflowServerUrlAsURL()
	{
		return IsaacAppConfigI.getUrlForString(getCurrentWorkflowServerUrl());
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowServerDeploymentId()
	 */
	@Override
	public String getDefaultWorkflowServerDeploymentId()
	{
		return getWorkflowServerDeploymentId();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowServerDeploymentId()
	 */
	@Override
	public String getCurrentWorkflowServerDeploymentId()
	{
		return AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getWorkflowServerDeploymentId();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowPromotionPathName()
	 */
	@Override
	public String getDefaultWorkflowPromotionPathName()
	{
		return getWorkflowPromotionPathName();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowPromotionPathName()
	 */
	@Override
	public String getCurrentWorkflowPromotionPathName()
	{
		return WBUtility.getDescriptionIfConceptExists(getCurrentWorkflowPromotionPathUuidAsUUID());
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDefaultWorkflowPromotionPathUuid()
	 */
	@Override
	public String getDefaultWorkflowPromotionPathUuid()
	{
		return getWorkflowPromotionPathUuid();
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowPromotionPathUuid()
	 */
	@Override
	public String getCurrentWorkflowPromotionPathUuid()
	{
		UUID current = AppContext.getService(UserProfileManager.class).getCurrentlyLoggedInUserProfile().getWorkflowPromotionPath();
		return current != null ? current.toString() : null;
	}

	/**
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getCurrentWorkflowPromotionPathUuidAsUUID()
	 */
	@Override
	public UUID getCurrentWorkflowPromotionPathUuidAsUUID()
	{
		return IsaacAppConfigI.getUuidForString(getCurrentWorkflowPromotionPathUuid());
	}
}
