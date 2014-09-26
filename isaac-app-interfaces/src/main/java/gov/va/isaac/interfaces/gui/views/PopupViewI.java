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
package gov.va.isaac.interfaces.gui.views;

import javafx.stage.Window;
import org.jvnet.hk2.annotations.Contract;

/**
 * @{link PopupViewI}
 *
 * Extends the @{link IsaacViewI} to provide a View which will pop up a new window on top of the parent 
 * window when the showView method is called.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface PopupViewI extends IsaacViewI
{
	/**
	 * Display this popup view to the user in front of the specified parent window.
	 * Or pass null if you want the popup window to be unowned, having its own task bar entry.
	 * The PopupViewI implementation is responsible for showing itself when this method 
	 * is called.
	 * 
	 * @param parent the parent Window
	 */
	public void showView(Window parent);
}
