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
package gov.va.isaac.config.profiles;

import gov.va.isaac.config.generated.RoleOption;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.util.PasswordHasher;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UserProfile}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserProfile
{
	private static Logger logger = LoggerFactory.getLogger(UserProfile.class);

	//This is a cache of what they typed when the logged in - so it can be used later for logging into workflow, or sync.
	private transient char[] clearTextPassword;
	
	//What we actually perform login comparisons against - what is stored in the user prefs file.
	@XmlElement 
	private String hashedPassword;

	@XmlElement 
	private String userLogonName;
	
	@XmlElement
	private UUID conceptUUID;

	@XmlElement 
	private StatedInferredOptions statedInferredPolicy = StatedInferredOptions.INFERRED_THEN_STATED;
	
	@XmlElement 
	private boolean displayFSN = true;
	
	@XmlElement 
	private String workflowUsername = null;
	
	//This field will be encrypted using the clearTextPassword
	@XmlElement 
	private String workflowPasswordEncrypted = null;
	
	@XmlElement 
	private String syncUsername = null;
	
	//This field will be encrypted using the clearTextPassword
	@XmlElement 
	private String syncPasswordEncrypted = null;
	
	@XmlElement
	private boolean launchWorkflowForEachCommit = true;
	/*
	 *  !!!! UPDATE THE CLONE METHOD IF YOU ADD NEW PARAMETERS !!!!!
	 */

	/**
	 * do not use - only for jaxb
	 */
	private UserProfile()
	{
		//for jaxb and clone
	}

	/**
	 * Do not call - use {@link UserProfileManager#createUserProfile(gov.va.isaac.config.generated.User, gov.va.isaac.config.generated.NewUserDefaults)}
	 * 
	 * Only public due to a testing quirk  - BaseTest - in workflow needs this, as may some JUnit tests, eventually.
	 */
	public UserProfile(String userLogonName, String password)
	{
		this.userLogonName = userLogonName;
		try
		{
			this.hashedPassword = PasswordHasher.getSaltedHash(password);
			this.clearTextPassword = password.toCharArray();
		}
		catch (Exception e)
		{
			logger.error("Unexpected error hashing password", e);
			this.hashedPassword = "foo";
		}
	}

	public boolean isCorrectPassword(String password)
	{
		try
		{
			boolean result = PasswordHasher.check(password, hashedPassword);
			if (result)
			{
				clearTextPassword = password.toCharArray();
			}
			return result;
		}
		catch (Exception e)
		{
			logger.error("Unexpected error validating password", e);
			return false;
		}
	}

	/**
	 * This call sets both the clearTextPassword field, and the hashedPassword field - hashing as appropriate.
	 */
	public void setPassword(String currentPassword, String newPassword) throws InvalidPasswordException
	{
		if (!isCorrectPassword(currentPassword))
		{
			throw new InvalidPasswordException("Incorrect current password");
		}
		if (newPassword == null || newPassword.length() == 0)
		{
			throw new InvalidPasswordException("The password must be provided");
		}
		try
		{
			//Need to decrypt and reencrypt the workflow and sync passwords, since these are encrypted with the clearTextPassword.
			String wfPass = getWorkflowPassword();
			String syncPass = getSyncPassword();
			
			this.clearTextPassword = newPassword.toCharArray();
			this.hashedPassword = PasswordHasher.getSaltedHash(newPassword);
			
			if (wfPass != null)
			{
				setWorkflowPassword(wfPass);
			}
			if (syncPass != null)
			{
				setSyncPassword(syncPass);
			}
		}
		catch (Exception e)
		{
			logger.error("Unexpected error hashing password", e);
			throw new RuntimeException(e);
		}
	}

	public String getUserLogonName()
	{
		return userLogonName;
	}

	public StatedInferredOptions getStatedInferredPolicy()
	{
		return statedInferredPolicy;
	}

	public void setStatedInferredPolicy(StatedInferredOptions statedInferredPolicy)
	{
		this.statedInferredPolicy = statedInferredPolicy;
	}
	
	public void setDisplayFSN(boolean displayFSN)
	{
		this.displayFSN = displayFSN;
	}
	
	public boolean getDisplayFSN()
	{
		return displayFSN;
	}
	
	
	public String getWorkflowUsername()
	{
		return workflowUsername;
	}

	public void setWorkflowUsername(String workflowUsername)
	{
		this.workflowUsername = workflowUsername;
	}

	public String getSyncUsername()
	{
		return syncUsername;
	}

	public void setSyncUsername(String syncUsername)
	{
		this.syncUsername = syncUsername;
	}
	
	public void setWorkflowPassword(String workflowPassword)
	{
		if (clearTextPassword == null)
		{
			throw new RuntimeException("Cannot encrypt a workflow password until successfully logged in");
		}
		try
		{
			this.workflowPasswordEncrypted = PasswordHasher.encrypt(new String(clearTextPassword), workflowPassword);
		}
		catch (Exception e)
		{
			logger.error("Unexpected error encrypting password", e);
			throw new RuntimeException("Unexpected error encrypting workflow password");
		}
	}

	public String getWorkflowPassword() throws InvalidPasswordException
	{
		if (clearTextPassword == null)
		{
			throw new RuntimeException("Cannot decrypt a workflow password until successfully logged in");
		}
		if (workflowPasswordEncrypted == null)
		{
			return null;
		}
		try
		{
			return PasswordHasher.decryptToString(new String(clearTextPassword), this.workflowPasswordEncrypted);
		}
		catch (Exception e)
		{
			throw new InvalidPasswordException("Invalid password for decrypting the workflow password");
		}
	}

	public void setSyncPassword(String syncPassword)
	{
		if (clearTextPassword == null)
		{
			throw new RuntimeException("Cannot encrypt a sync password until successfully logged in");
		}
		try
		{
			this.syncPasswordEncrypted = PasswordHasher.encrypt(new String(clearTextPassword), syncPassword);
		}
		catch (Exception e)
		{
			logger.error("Unexpected error encrypting password", e);
			throw new RuntimeException("Unexpected error encrypting sync password");
		}
	}
	
	public String getSyncPassword()
	{
		if (clearTextPassword == null)
		{
			throw new RuntimeException("Cannot decrypt a sync password until successfully logged in");
		}
		if (syncPasswordEncrypted == null)
		{
			return null;
		}
		try
		{
			return PasswordHasher.decryptToString(new String(clearTextPassword), this.syncPasswordEncrypted);
		}
		catch (Exception e)
		{
			throw new InvalidPasswordException("Invalid password for decrypting the sync password");
		}
	}
	
	/**
	 * The UUID of the concept in the DB that represents this user.
	 */
	public UUID getConceptUUID()
	{
		return conceptUUID;
	}

	/**
	 * The UUID of the concept in the DB that represents this user.
	 */
	protected void setConceptUUID(UUID conceptUUID)
	{
		this.conceptUUID = conceptUUID;
	}
	
	public boolean hasRole(RoleOption role)
	{
		if (role == null)
		{
			return false;
		}
		//TODO implement role checking - probably store these on the user concept in a refex?
		return true;
	}
	

	/**
	 * @return the launchWorkflowForEachCommit
	 */
	public boolean isLaunchWorkflowForEachCommit()
	{
		return launchWorkflowForEachCommit;
	}

	/**
	 * @param launchWorkflowForEachCommit the launchWorkflowForEachCommit to set
	 */
	public void setLaunchWorkflowForEachCommit(boolean launchWorkflowForEachCommit)
	{
		this.launchWorkflowForEachCommit = launchWorkflowForEachCommit;
	}

	protected void store(File fileToWrite) throws IOException
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(UserProfile.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, fileToWrite);
		}
		catch (Exception e)
		{
			throw new IOException("Problem storings UserProfile to " + fileToWrite.getAbsolutePath());
		}
	}

	protected static UserProfile read(File path) throws IOException
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(UserProfile.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			return (UserProfile) jaxbUnmarshaller.unmarshal(path);
		}
		catch (Exception e)
		{
			logger.error("Problem reading user profile from " + path.getAbsolutePath(), e);
			throw new IOException("Problem reading user profile", e);
		}
	}

	@Override
	protected UserProfile clone()
	{
		UserProfile clone = new UserProfile();
		clone.userLogonName = this.userLogonName;
		clone.clearTextPassword = this.clearTextPassword;
		clone.hashedPassword = this.hashedPassword;
		clone.statedInferredPolicy = this.statedInferredPolicy;
		clone.displayFSN = this.displayFSN;
		clone.syncPasswordEncrypted = this.syncPasswordEncrypted;
		clone.syncUsername = this.syncUsername;
		clone.workflowPasswordEncrypted = this.workflowPasswordEncrypted;
		clone.workflowUsername = this.workflowUsername;
		clone.conceptUUID = this.conceptUUID;
		clone.launchWorkflowForEachCommit = this.launchWorkflowForEachCommit;
		
		return clone;
	}
}
