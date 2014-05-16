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
import gov.va.isaac.interfaces.gui.views.ConceptViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import com.sun.javafx.tk.Toolkit;
import java.io.IOException;
import java.util.UUID;
import javafx.application.Platform;
import javafx.stage.Window;
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
	private YesNoDialog yesNoDialog_;

	@Inject
	private CommonDialogs(ApplicationWindowI mainAppWindow) throws IOException
	{
		// hidden - constructed by HK2
		this.errorDialog_ = new ErrorDialog(mainAppWindow.getPrimaryStage());
		this.informationDialog_ = new InformationDialog(mainAppWindow.getPrimaryStage());
		this.yesNoDialog_ = new YesNoDialog(mainAppWindow.getPrimaryStage());
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
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showConceptDialog(java.util.UUID)
	 */
	@Override
	public void showConceptDialog(UUID uuid)
	{
		try
		{
			ConceptViewI dialog = AppContext.createConceptViewWindow();
			dialog.showConcept(uuid);
		}
		catch (Exception ex)
		{
			String message = "Unexpected error displaying concept view";
			LOG.warn(message, ex);
			showErrorDialog("Unexpected Error", message, ex.getMessage());
		}
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showConceptDialog(int)
	 */
	@Override
	public void showConceptDialog(int conceptNID)
	{
		try
		{
			ConceptViewI dialog = AppContext.createConceptViewWindow();
			dialog.showConcept(conceptNID);
		}
		catch (Exception ex)
		{
			String message = "Unexpected error displaying concept view";
			LOG.warn(message, ex);
			showErrorDialog("Unexpected Error", message, ex.getMessage());
		}
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showYesNoDialog(java.lang.String, java.lang.String)
	 */
	@Override
	public DialogResponse showYesNoDialog(String title, String question)
	{
		return yesNoDialog_.showYesNoDialog(title, question);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.CommonDialogsI#showErrorDialog(java.lang.String, java.lang.String, java.lang.String, javafx.stage.Window)
	 */
	@Override
	public void showErrorDialog(String title, String message, String details, Window parentWindow)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ErrorDialog ed = new ErrorDialog(parentWindow);
					ed.setVariables(title, message, details);
					ed.showAndWait();
				}
				catch (IOException e)
				{
					LOG.error("Unexpected error creating an error dialog!", e);
				}
			}
		};
		if (Toolkit.getToolkit().isFxUserThread())
		{
			r.run();
		}
		else
		{
			Platform.runLater(r);  //TODO not sure what the showAndWait will do inside a runLater... need to test.
		}
	}
}
