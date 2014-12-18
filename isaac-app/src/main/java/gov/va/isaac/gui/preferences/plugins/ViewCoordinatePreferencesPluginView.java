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
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ViewCoordinatePreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class ViewCoordinatePreferencesPluginView extends CoordinatePreferencesPluginView {
	private Logger logger = LoggerFactory.getLogger(ViewCoordinatePreferencesPluginView.class);
	
	/**
	 * 
	 */
	public ViewCoordinatePreferencesPluginView() {
		super();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public String getName() {
		return "View Coordinate";
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		logger.debug("Saving ViewCoordinatePreferencesPluginView data");
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		logger.debug("Setting stored VC path (currently \"{}\") to {}", loggedIn.getViewCoordinatePath(), currentPathProperty().get()); 
		loggedIn.setViewCoordinatePath(currentPathProperty().get());
		logger.debug("Setting stored VC StatedInferredPolicy (currently \"{}\") to {}", loggedIn.getStatedInferredPolicy(), currentStatedInferredOptionProperty().get()); 
		loggedIn.setStatedInferredPolicy(currentStatedInferredOptionProperty().get());
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
	protected Collection<UUID> getPathOptions() {
		List<UUID> list = new ArrayList<>();

		try {
			List<ConceptChronicleBI> pathConcepts = WBUtility.getPathConcepts();
			for (ConceptChronicleBI cc : pathConcepts) {
				list.add(cc.getPrimordialUuid());
			}
		} catch (IOException | ContradictionException e) {
			logger.error("Failed loading path concepts. Caught {} {}", e.getClass().getName(), e.getLocalizedMessage());
			e.printStackTrace();
		}
		// Add currently-stored value to list of options, if not already there
		UUID storedPath = getStoredPath();
		if (storedPath != null && ! list.contains(storedPath)) {
			list.add(storedPath);
		}

		//Collections.sort(list);
		return list;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredPath()
	 */
	@Override
	protected UUID getStoredPath() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		return loggedIn.getViewCoordinatePath();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getStoredStatedInferredOption()
	 */
	@Override
	protected StatedInferredOptions getStoredStatedInferredOption() {
		UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
		return loggedIn.getStatedInferredPolicy();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getDefaultPath()
	 */
	@Override
	protected UUID getDefaultPath() {
		return UserProfileDefaults.getDefaultViewCoordinatePath();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getDefaultStatedInferredOption()
	 */
	@Override
	protected StatedInferredOptions getDefaultStatedInferredOption() {
		return UserProfileDefaults.getDefaultStatedInferredPolicy();
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getTabOrder()
	 */
	@Override
	public int getTabOrder()
	{
		return 10;
	}
}
