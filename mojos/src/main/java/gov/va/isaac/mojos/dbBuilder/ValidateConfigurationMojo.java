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
package gov.va.isaac.mojos.dbBuilder;

import gov.va.isaac.config.IsaacAppConfigWrapper;
import gov.va.isaac.config.users.GenerateUsers;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXException;

/**
 * {@link ValidateConfigurationMojo}
 * Validate that all of the required user specified configuration information is present, and schema valid.
 * 
 * @goal validate-configuration
 * 
 * @phase process-sources
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class ValidateConfigurationMojo extends AbstractMojo
{

	/**
	 * The location of the resources folder that will be included on the classpath during build -
	 * this folder should contain the things we are looking to validate.
	 * 
	 * @parameter
	 * @required
	 */
	File resourcesFolderPath = null;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			getLog().info("Checking the resources folder " + resourcesFolderPath.getAbsolutePath());

			/*
			 * We require the resources folder to contain the following:
			 * 
			 * resources/
			 * users.xml
			 * app.xml
			 * icons/application-icon.png
			 * 
			 * The two XML files must validate to their specified schemas.
			 */

			File users = new File(resourcesFolderPath, "users.xml");
			if (users.exists() && users.isFile())
			{
				validate(users, GenerateUsers.class.getClassLoader().getResourceAsStream("xsd/UserGenerationSchema.xsd"));
			}
			else
			{
				throw new MojoExecutionException("The folder " + resourcesFolderPath.getAbsolutePath() + " must contain a 'users.xml' file.");
			}

			File app = new File(resourcesFolderPath, "app.xml");
			if (app.exists() && app.isFile())
			{
				validate(app, IsaacAppConfigWrapper.class.getClassLoader().getResourceAsStream("xsd/AppConfigSchema.xsd"));
			}
			else
			{
				throw new MojoExecutionException("The folder " + resourcesFolderPath.getAbsolutePath() + " must contain a 'app.xml' file.");
			}

			File icons = new File(resourcesFolderPath, "icons");
			if (icons.exists() && icons.isDirectory())
			{
				if (!new File(icons, "application-icon.png").isFile())
				{
					throw new MojoExecutionException("The folder " + icons.getAbsolutePath() + " must contain an 'application-icon.png' file.");
				}
			}
			else
			{
				throw new MojoExecutionException("The folder " + resourcesFolderPath.getAbsolutePath() + " must contain a 'icons' folder.");
			}

			getLog().info("Done Processing the resources folder - all valid.");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected error validating the resources folder", e);
		}
	}

	private void validate(File xmlFilePath, InputStream schemaSource) throws MojoExecutionException
	{
		try
		{
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new StreamSource(schemaSource));
			Validator validator = schema.newValidator();
			try
			{
				validator.validate(new StreamSource(xmlFilePath));
			}
			catch (SAXException e)
			{
				throw new MojoExecutionException("The file " + xmlFilePath.getAbsolutePath() + " is not valid against the required schema.  " + e.getLocalizedMessage());
			}
		}
		catch (SAXException | IOException e)
		{
			throw new MojoExecutionException("Unexpected error schema validating " + xmlFilePath.getAbsolutePath() + e.getLocalizedMessage());
		}
	}
}
