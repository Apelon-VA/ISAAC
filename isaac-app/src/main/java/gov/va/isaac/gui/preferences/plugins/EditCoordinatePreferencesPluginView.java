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

/**
 * ViewCoordinatePreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences.plugins;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EditCoordinatePreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class EditCoordinatePreferencesPluginView extends CoordinatePreferencesPluginView {
	private Logger logger = LoggerFactory.getLogger(EditCoordinatePreferencesPluginView.class);

	/**
	 * 
	 */
	public EditCoordinatePreferencesPluginView() {
		super();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public String getName() {
		return "Edit Coordinate";
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		// TODO implement EditCoordinatePreferencesPluginView.save()
		logger.debug("Saving EditCoordinatePreferencesPluginView data");
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		logger.debug("Setting stored EC path (currently \"{}\") to {}", loggedIn.getEditCoordinatePath(), currentPathProperty().get()); 
		//TODO Joel - you will have to reconcile how to display a string name, but store the UUID
		loggedIn.setEditCoordinatePath(UUID.fromString(currentPathProperty().get()));
		//logger.debug("Setting stored EC StatedInferredPolicy (currently \"{}\") to {}", loggedIn.getStatedInferredPolicy(), currentStatedInferredOptionProperty().get()); 
		//loggedIn.setStatedInferredPolicy(currentStatedInferredOptionProperty().get());
		try {
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile for " + getName();
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getCoordinatePathOptions()
	 */
	@Override
	protected Collection<String> getPathOptions() {
		// TODO load EditCoordinate path options
		List<String> list = Arrays.asList("bogus ec 1", "bogus ec 2", "bogus ec 3", "bogus ec 4");

		// Add currently-stored value to list of options, if not already there
		String storedPath = getStoredPath();
		if (storedPath != null && ! list.contains(storedPath)) {
			list.add(storedPath);
		}

		Collections.sort(list);
		return list;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredPath()
	 */
	@Override
	protected String getStoredPath() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		//TODO Joel - you will have to reconcile how to display a string name, but store the UUID
		return loggedIn.getEditCoordinatePath().toString();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredStatedInferredOption()
	 */
	@Override
	protected StatedInferredOptions getStoredStatedInferredOption() {
		return null;
	}
}
