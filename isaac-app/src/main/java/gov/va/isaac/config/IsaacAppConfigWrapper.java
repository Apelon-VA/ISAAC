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
import gov.va.isaac.interfaces.config.IsaacAppConfigI;
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
		//TODO we will need a way for the user to change some of these parameters later (and a place to save them) like the two 
		//workflow ones.	where and how?
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
		setDistReposId(read.getDistReposId());
		setDistReposName(read.getDistReposName());
		setDistReposUrl(read.getDistReposUrl());
		setDistReposSnapId(read.getDistReposSnapId());
		setDistReposSnapName(read.getDistReposSnapName());
		setDistReposSnapUrl(read.getDistReposSnapUrl());
		setDbGroupId(read.getDbGroupId());
		setDbArtifactId(read.getDbArtifactId());
		setDbVersion(read.getDbVersion());
		setDbClassifier(read.getDbClassifier());
		setDbType(read.getDbType());
		setApplicationTitle(read.getApplicationTitle());
		setPreviousReleaseVersion(read.getPreviousReleaseVersion());
		setReleaseVersion(read.getReleaseVersion());
		setExtensionNamespace(read.getExtensionNamespace());
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
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getWorkflowServerUrlAsURL()
	 */
	@Override
	public URL getWorkflowServerUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getWorkflowServerUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getScmUrlAsURL()
	 */
	@Override
	public URL getScmUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getScmUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDistReposUrlAsURL()
	 */
	@Override
	public URL getDistReposUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getDistReposUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getDistReposSnapUrlAsURL()
	 */
	@Override
	public URL getDistReposSnapUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getDistReposSnapUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getChangeSetUrlAsURL()
	 */
	@Override
	public URL getChangeSetUrlAsURL() {
		return IsaacAppConfigI.getUrlForString(getChangeSetUrl());
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getPromotionPathAsUUID()
	 */
	@Override
	public UUID getWorkflowPromotionPathUuidAsUUID() {
		return IsaacAppConfigI.getUuidForString(getWorkflowPromotionPathUuid());
	}

	/*
	 * @see gov.va.isaac.interfaces.config.IsaacAppConfigI#getChangeSetUrlTypeName()
	 */
	@Override
	public String getChangeSetUrlTypeName()
	{
		return getChangeSetUrlType().name();
	}
}
