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
package gov.va.isaac.mojos.profileSync;

import gov.va.isaac.config.IsaacAppConfigWrapper;
import gov.va.isaac.config.generated.IsaacAppConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXException;

/**
 * {@link ProfilesMojoBase}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class ProfilesMojoBase extends AbstractMojo
{
	
	/**
	 * The location of the (already existing) profiles folder which should be shared via SCM.
	 * @parameter
	 * @required
	 */
	File profilesFolder = null;
	
	/**
	 * The location of the (already existing) app.xml file which contains the SCM connection information.
	 * @parameter
	 * @required
	 */
	File appXMLFile = null;
	
	private IsaacAppConfig config_;

	public ProfilesMojoBase() throws MojoExecutionException
	{
		super();
		
		try
		{
			config_ = IsaacAppConfigWrapper.unmarshallStream(new FileInputStream(appXMLFile));
		}
		catch (IOException | JAXBException | SAXException e)
		{
			throw new MojoExecutionException("Failure reading " + appXMLFile, e);
		}
	}
	
	protected String getURL()
	{
		return config_.getChangeSetUrl();
	}
	
	protected String getURLType()
	{
		return config_.getChangeSetUrlType().name();
	}
}