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
package gov.va.isaac.interfaces.gui;

import gov.va.isaac.interfaces.gui.views.DockedViewI;
import javafx.stage.Stage;
import org.jvnet.hk2.annotations.Contract;

/**
 * ApplicationWindowI
 * 
 * Hooks for modules to get references to components of the main application that they may need.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface ApplicationWindowI
{
	/**
	 * Return a reference to the Primary Stage of the application.
	 */
	public Stage getPrimaryStage();
	
	/**
	 * Display the requested docked view, if it is currently hidden / disabled.
	 */
	public void ensureDockedViewIsVisble(DockedViewI view);
	
	/**
	 * A utility call to enable access to the built-in ability of JavaFX to launch a (system) web browser
	 * @param url
	 */
	public void browseURL(String url);
}
