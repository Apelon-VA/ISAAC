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

import gov.va.isaac.config.generated.NewUserDefaults;
import gov.va.isaac.config.generated.User;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import java.io.IOException;
import javafx.collections.ObservableList;
import org.jvnet.hk2.annotations.Service;

/**
 * {@link MockUserProfileManager}
 * 
 * Some mock testing code to allow easier testing.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service (name = "mock")
public class MockUserProfileManager extends UserProfileManager
{
	private UserProfile user;
	
	private MockUserProfileManager()
	{
		//for HK2
		super();
	}
	
	public void configure(UserProfile user)
	{
		this.user = user;
		this.loadRequested();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#getCurrentlyLoggedInUserProfile()
	 */
	@Override
	public UserProfile getCurrentlyLoggedInUserProfile()
	{
		return user;
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#getCurrentlyLoggedInUser()
	 */
	@Override
	public String getCurrentlyLoggedInUser()
	{
		return user.getUserLogonName();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#saveChanges(gov.va.isaac.config.profiles.UserProfile)
	 */
	@Override
	public void saveChanges(UserProfile userProfile) throws InvalidUserException, IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#authenticateBoolean(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticateBoolean(String userLogonName, String password)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public void authenticate(String userLogonName, String password) throws InvalidUserException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#createUserProfile(gov.va.isaac.config.generated.User, gov.va.isaac.config.generated.NewUserDefaults)
	 */
	@Override
	public void createUserProfile(User user, NewUserDefaults defaults) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#doesProfileExist(java.lang.String)
	 */
	@Override
	public boolean doesProfileExist(String userLogonName)
	{
		return userLogonName.equals(user.getUserLogonName());
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#loadRequested()
	 */
	@Override
	public void loadRequested()
	{
		cdl.countDown();
		cdl.countDown();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#getUsersWithProfiles()
	 */
	@Override
	public ObservableList<String> getUsersWithProfiles()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#getLastLoggedInUser()
	 */
	@Override
	public String getLastLoggedInUser()
	{
		return user.getUserLogonName();
	}

	/**
	 * @see gov.va.isaac.config.profiles.UserProfileManager#createNewUser(gov.va.isaac.config.generated.User)
	 */
	@Override
	public User createNewUser(User user) throws InvalidUserException, IOException
	{
		throw new UnsupportedOperationException();
	}
}
