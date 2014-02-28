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
package gov.va.isaac.gui.dialog;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.interfaces.gui.ApplicationWindowI;
import gov.va.isaac.interfaces.gui.CommonDialogsI;
import gov.va.isaac.interfaces.gui.views.SnomedConceptViewI;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommonDialogs
 * 
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
public class CommonDialogs implements CommonDialogsI
{
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private ErrorDialog errorDialog_;
	private InformationDialog informationDialog_;

	@Inject
	private CommonDialogs(ApplicationWindowI mainAppWindow) throws IOException
	{
		// hidden - constructed by HK2
		this.errorDialog_ = new ErrorDialog(mainAppWindow.getPrimaryStage());
		this.informationDialog_ = new InformationDialog(mainAppWindow.getPrimaryStage());
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showInformationDialog(java.lang.String, java.lang.String)
	 */
	@Override
	public void showInformationDialog(String title, String message)
	{
		// Make sure in application thread.
		FxUtils.checkFxUserThread();

		informationDialog_.setVariables(title, message);
		informationDialog_.showAndWait();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showErrorDialog(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void showErrorDialog(String message, Throwable throwable)
	{
		String title = throwable.getClass().getName();
		String details = throwable.getMessage();
		showErrorDialog(title, message, details);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showErrorDialog(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void showErrorDialog(String title, String message, String details)
	{
		// Make sure in application thread.
		FxUtils.checkFxUserThread();
		
		ErrorDialog ed;
		
		//If we already have our cached one up, create a new one.
		if (errorDialog_.isShowing())
		{
			try
			{
				ed = new ErrorDialog(errorDialog_.getOwner());
			}
			catch (IOException e)
			{
				LOG.error("Unexpected error creating an error dialog!", e);
				throw new RuntimeException("Can't display error dialog!");
			}
		}
		else
		{
			ed = errorDialog_;
		}

		ed.setVariables(title, message, details);
		ed.showAndWait();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showSnomedConceptDialog(java.util.UUID)
	 */
	@Override
	public void showSnomedConceptDialog(UUID conceptUUID)
	{
		try
		{
			SnomedConceptViewI dialog = AppContext.createSnomedConceptViewWindow();
			dialog.showConcept(conceptUUID);
		}
		catch (Exception ex)
		{
			String message = "Unexpected error displaying snomed concept view";
			LOG.warn(message, ex);
			showErrorDialog("Unexpected Error", message, ex.getMessage());
		}
	}
}
