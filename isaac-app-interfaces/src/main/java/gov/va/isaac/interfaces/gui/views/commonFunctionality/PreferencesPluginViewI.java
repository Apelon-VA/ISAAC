package gov.va.isaac.interfaces.gui.views.commonFunctionality;
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

import gov.va.isaac.interfaces.gui.views.PopupViewI;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.layout.Region;

import org.jvnet.hk2.annotations.Contract;

/**
 * PreferencesPluginViewI
 * 
 * An interface that allows the creation of a PreferencesPluginViewI implementation, which 
 * will be a POJO component that contains a data model representing a set of UserProfile preference
 * values, the GUI Region required to view and (optionally) modify them, and the ability to
 * load and save them.  It is intended to populate a tab in the PreferencesView window.
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface PreferencesPluginViewI {
	/**
	 * @return String name of PreferencesView plugin tab
	 * 
	 * Serves as Tab label and default sort within getTabOrder()
	 */
	String getName();
	
	/**
	 * @return ReadOnlyStringProperty validationFailureMessageProperty text of validation failure message
	 * 
	 * The validationFailureMessageProperty is non-empty if-and-only-if the plugin is in an invalid, non-savable state.
	 * In such case, it will usually contain the validation failure message of one of its constituent properties,
	 * but may also be coded to detect invalid value combinations of its constituent properties.
	 * 
	 * A binding to the validationFailureMessageProperty is used by the PreferencesView to detect transition
	 * of the plugin into and out of an invalid, non-savable state.
	 */
	ReadOnlyStringProperty validationFailureMessageProperty();
	
	
	/**
	 * @return Region JavaFX displayable Region required to view and (optionally) modify the plugin's constituent properties
	 * 
	 * The getContent() method preferably loads and populates the Region, lazily initializing it
	 */
	Region getContent();
	
	
	/**
	 * @throws IOException Appropriately persists all constituent properties (usually to UserProfile)
	 * 
	 *  If the save() method persists to UserProfile then it should always first load the most recent version
	 *  then write to it this plugins values in order to avoid saving stale values of properties not handled by this plugin.
	 */
	void save() throws IOException;

	/**
	 * @return int Sort order of tab within the PreferencesView TabPane
	 * 
	 * Plugins with getTabOrder() values will be displayed in tabs to the left
	 * of plugins with higher getTabOrder() values. Plugins with identical getTabOrder()
	 * will be sorted alphabetically by tab name (getName())
	 */
	int getTabOrder() ;
}
