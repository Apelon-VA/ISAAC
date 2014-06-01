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
package gov.va.isaac.interfaces.gui;

import gov.va.isaac.interfaces.utility.DialogResponse;
import java.util.UUID;
import javafx.stage.Window;
import org.jvnet.hk2.annotations.Contract;

/**
 * CommonDialogsI
 * 
 * Provide access to commonly used dialogs that an ISAAC application module may need.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface CommonDialogsI
{
	/**
	 * Present an information dialog to the user, above the application main window.
	 * 
	 * @param title
	 * @param message
	 */
	public void showInformationDialog(String title, String message);
	
	/**
	 * Present an information dialog to the user, above the application main window.
	 * 
	 * @param title
	 * @param message
	 * @param parentWindow
	 */
	public void showInformationDialog(String title, String message, Window parentWindow);

	/**
	 * Present an error dialog to the user, above the application main window.
	 * Equivalent to Helper calling {@link #showErrorDialog(String, String, String)} with {@code title} set to the exception class name, and
	 * {@code details} set to the exception message.
	 * 
	 * @param message
	 * @param throwable
	 */
	public void showErrorDialog(String message, Throwable throwable);

	/**
	 * Present an error dialog to the user, above the application main window.
	 * 
	 * @param title
	 * @param message
	 * @param details
	 */
	public void showErrorDialog(final String title, final String message, final String details);
	
	/**
	 * Present an error dialog to the user, above the specified window.
	 * 
	 * @param title
	 * @param message
	 * @param details
	 * @param parentWindow
	 */
	public void showErrorDialog(final String title, final String message, final String details, final Window parentWindow);

	/**
	 * Present a non-modal pop-up window which displays the details of a concept.  Uses the default 
	 * implementation of a concept viewer dialog within the application.
	 * @param conceptUUID
	 */
	public void showConceptDialog(UUID conceptUUID);
	
	/**
	 * Present a non-modal pop-up window which displays the details of a concept.  Uses the default 
	 * implementation of a concept viewer dialog within the application.
	 * @param conceptUUID
	 */
	public void showConceptDialog(int conceptNID);
	
	/**
	 * Present a modal yes/no dialog to the user.  Returns the users answer (YES or NO) to the question.
	 */
	public DialogResponse showYesNoDialog(String title, String question);
}
