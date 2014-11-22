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
import gov.va.isaac.config.generated.ChangeSetSCMType;
import gov.va.isaac.config.generated.IsaacAppConfig;
import gov.va.isaac.interfaces.sync.ProfileSyncI;
import gov.va.isaac.sync.git.SyncServiceGIT;
import gov.va.isaac.sync.svn.SyncServiceSVN;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXException;

/**
 * {@link ProfilesMojoBase}
 * 
 * This allows authentication to be passed in via system property, parameter, or, will 
 * prompt for the username/password (if allowed by the system property 'profileSyncNoPrompt')
 * IN THAT ORDER.  System properties have the highest priority.
 * 
 * To prevent prompting during automated runs - set the system property 'profileSyncNoPrompt=true'
 * To set the username via system property - set 'profileSyncUsername=username'
 * To set the password via system property - set 'profileSyncPassword=password'
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class ProfilesMojoBase extends AbstractMojo
{
	// For disabling Profile Sync entirely
	public static final String PROFILE_SYNC_DISABLE = "profileSyncDisable";
	
	// For preventing command line prompts for credentials during automated runs - set this system property to true.
	public static final String PROFILE_SYNC_NO_PROMPTS = "profileSyncNoPrompt";
	//TODO autodetect running on maestrodev (have code elsewhere that does this)
	
	// Allow setting the username via a system property
	public static final String PROFILE_SYNC_USERNAME_PROPERTY = "profileSyncUsername";
	
	// Allow setting the password via a system property
	public static final String PROFILE_SYNC_PASSWORD_PROPERTY = "profileSyncPassword";
	
	private boolean disableHintGiven = false;
	
	/**
	 * The location of the (already existing) profiles folder which should be shared via SCM.
	 * @parameter
	 * @required
	 */
	File userProfileFolderLocation = null;
	
	/**
	 * The location of the (already existing) app.xml file which contains the SCM connection information.
	 * @parameter
	 * @required
	 */
	File appXMLFile = null;
	
	/**
	 * The username to use for remote operations
	 * @parameter
	 * @optional
	 */
	private String profileSyncUsername = null;
	
	/**
	 * The password to use for remote operations
	 * @parameter
	 * @optional
	 */
	private String profileSyncPassword = null;
	
	private IsaacAppConfig config_;
	private static String username = null;
	private static String password = null;

	public ProfilesMojoBase() throws MojoExecutionException
	{
		super();
	}
	
	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		if (appXMLFile == null || !appXMLFile.isFile())
		{
			throw new MojoExecutionException("The file specified in the appXMLFile parameter must exist, and the parameter must be specified.");
		}
		
		try
		{
			config_ = IsaacAppConfigWrapper.unmarshallStream(new FileInputStream(appXMLFile));
		}
		catch (IOException | JAXBException | SAXException e)
		{
			throw new MojoExecutionException("Failure reading " + appXMLFile, e);
		}
		if (StringUtils.isNotBlank(config_.getChangeSetUrl()))
		{
			if (config_.getChangeSetUrlType() == null)
			{
				throw new MojoExecutionException("If the 'changeSetUrl' parameter is provided, then you must provide the 'changeSetUrlType' parameter");
			}
		}
	}

	protected boolean skipRun()
	{
		if (Boolean.getBoolean(PROFILE_SYNC_DISABLE))
		{
			return true;
		}
		if (StringUtils.isBlank(config_.getChangeSetUrl()))
		{
			getLog().info("No SCM configuration will be done - no 'changeSetUrl' parameter was provided");
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected ProfileSyncI getProfileSyncImpl() throws MojoExecutionException
	{
		if (config_.getChangeSetUrlType() == ChangeSetSCMType.GIT)
		{
			return new SyncServiceGIT(userProfileFolderLocation);
		}
		else if (config_.getChangeSetUrlType() == ChangeSetSCMType.SVN)
		{
			return new SyncServiceSVN(userProfileFolderLocation);
		}
		else
		{
			throw new MojoExecutionException("Unsupported change set URL Type");
		}
	}
	
	/**
	 * Does the necessary substitution to put the contents of getUserName() into the URL, if a known pattern needing substitution is found.
	 *  ssh://someuser@csfe.aceworkspace.net:29418/... for example needs to become:
	 *  ssh://<getUsername()>@csfe.aceworkspace.net:29418/...
	 * @throws MojoExecutionException 
	 */
	protected String getURL() throws MojoExecutionException
	{
		return getProfileSyncImpl().substituteURL(config_.getChangeSetUrl(), getUsername());
	}
	
	protected String getUsername() throws MojoExecutionException
	{
		try
		{
			if (username == null)
			{
				username = System.getProperty(PROFILE_SYNC_USERNAME_PROPERTY);
				
				//still blank, try property
				if (StringUtils.isBlank(username))
				{
					username = profileSyncUsername;
				}
				
				//still no username, prompt if allowed
				if (StringUtils.isBlank(username) && !Boolean.getBoolean(PROFILE_SYNC_NO_PROMPTS))
				{
					if (!disableHintGiven)
					{
						System.out.println("To disable remote sync during build, add '-D" + PROFILE_SYNC_DISABLE + "=true' to your maven command");
						disableHintGiven = true;
					}
					
					System.out.println("Enter the " + config_.getChangeSetUrlType().name() + " username for the Profiles/Changset remote store (" +
							config_.getChangeSetUrl() + "):");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					username = br.readLine();
				}
			}
			return username;
		}
		catch (IOException e)
		{
			throw new MojoExecutionException("Error reading username from console");
		}
	}
	
	protected String getPassword() throws MojoExecutionException
	{
		try
		{
			if (password == null)
			{
				password = System.getProperty(PROFILE_SYNC_PASSWORD_PROPERTY);
				
				//still blank, try the passed in param
				if (StringUtils.isBlank(password))
				{
					password = profileSyncPassword;
				}
				
				//still no password, prompt if allowed
				if (StringUtils.isBlank(password) && !Boolean.getBoolean(PROFILE_SYNC_NO_PROMPTS))
				{
					if (!disableHintGiven)
					{
						System.out.println("To disable remote sync during build, add '-D" + PROFILE_SYNC_DISABLE + "=true' to your maven command");
						disableHintGiven = true;
					}
					System.out.println("Enter the " + config_.getChangeSetUrlType().name() + " password for the Profiles/Changset remote store: (" +
							config_.getChangeSetUrl() + "):");
					
					//Use console if available, for password masking
					Console console = System.console();
					if (console != null)
					{
						password = new String(console.readPassword());
					}
					else
					{
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						password = br.readLine();
					}
				}
			}
			return password;
		}
		catch (IOException e)
		{
			throw new MojoExecutionException("Error reading password from console");
		}
	}
}