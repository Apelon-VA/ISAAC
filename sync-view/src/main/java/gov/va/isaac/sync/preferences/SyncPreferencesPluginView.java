package gov.va.isaac.sync.preferences;
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
 * ViewCoordinatePreferencesPlugin
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileDefaults;
import gov.va.isaac.gui.preferences.plugins.AbstractPreferencesPluginView;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginLabelProperty;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginProperty;
import gov.va.isaac.gui.preferences.plugins.properties.PreferencesPluginTextFieldProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.control.Control;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 * ExampleAbstractPreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

@Service
@Singleton
public class SyncPreferencesPluginView extends AbstractPreferencesPluginView {
	//private static Logger logger = LoggerFactory.getLogger(SyncPreferencesPluginView.class);
	
	private static Collection<PreferencesPluginProperty<?, ? extends Control>> createProperties() {
		List<PreferencesPluginProperty<?, ? extends Control>> properties = new ArrayList<>();

		PreferencesPluginLabelProperty syncUserProperty = new PreferencesPluginLabelProperty("Sync User") {
			@Override
			public String readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getSyncUsername();
			}

			@Override
			public String readFromDefaults() {
				return null;
			}
		};
		properties.add(syncUserProperty);

		PreferencesPluginTextFieldProperty changeSetUrlProperty = 
				new PreferencesPluginTextFieldProperty("ChangeSet URL") {
			@Override
			public String readFromPersistedPreferences() {
				UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
				return loggedIn.getChangeSetUrl();
			}

			@Override
			public String readFromDefaults() {
				return UserProfileDefaults.getDefaultChangeSetUrl();
			}

			@Override
			public void writeToUnpersistedPreferences(UserProfile userProfile) {
				userProfile.setChangeSetUrl(getProperty().getValue());
			}
		};
		properties.add(changeSetUrlProperty);

		return properties;
	}
	
	/**
	 * @param name
	 * @param properties
	 */
	protected SyncPreferencesPluginView() {
		super("Sync", createProperties());
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getTabOrder()
	 */
	@Override
	public int getTabOrder()
	{
		return 50;
	}
}