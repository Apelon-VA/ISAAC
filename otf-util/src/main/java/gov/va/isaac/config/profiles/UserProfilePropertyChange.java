/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.config.profiles;

import gov.va.isaac.interfaces.config.UserProfileProperty;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * UserProfilePropertyChange
 * 
 * Standard PropertyChangeListener interface helper for use by UserProfileManager to handle UserProfile changes
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
class UserProfilePropertyChange {
	static {
		new UserProfilePropertyChange();
	}

	private static PropertyChangeSupport gPcs;

	private UserProfilePropertyChange() {
		gPcs = new PropertyChangeSupport(this);
	}
	
	static void addPropertyChangeListener(UserProfileProperty eventType, PropertyChangeListener listener) {
		gPcs.addPropertyChangeListener(eventType.toString(), listener);
	}

	static void removePropertyChangeListener(PropertyChangeListener listener) {
		gPcs.removePropertyChangeListener(listener);
	}

	static void firePropertyChange(UserProfileProperty pce, Object oldValue, Object newValue) {
		gPcs.firePropertyChange(pce.toString(), oldValue, newValue);
	}
}
