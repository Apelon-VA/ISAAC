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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

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
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.gui.preferences.plugins.CoordinatePreferencesPluginView#getCoordinatePathOptions()
	 */
	@Override
	protected Collection<String> getCoordinatePathOptions() {
		// TODO load EditCoordinate path options
		
		return Arrays.asList("bogus ec 1", "bogus ec 2", "bogus ec 3", "bogus ec 4");
	}
}
