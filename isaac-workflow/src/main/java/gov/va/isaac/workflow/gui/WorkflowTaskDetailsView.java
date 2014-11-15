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
package gov.va.isaac.workflow.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowTaskDetailsViewI;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link WorkflowTaskDetailsViewI} which can be used to initiate a new workflow instance
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
@Service
@PerLookup
public class WorkflowTaskDetailsView extends Stage implements WorkflowTaskDetailsViewI
{
	private final Logger logger = LoggerFactory.getLogger(WorkflowTaskDetailsView.class);

	private WorkflowTaskDetailsViewController controller_;

	private boolean shown = false;
	
	private WorkflowTaskDetailsView() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("WorkflowTaskDetailsView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(WorkflowTaskDetailsView.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.INBOX.getImage());

		controller_ = loader.getController();
		
		controller_.setView(this);
		
		setTitle("Workflow Task Details");
		setResizable(true);

		setWidth(600);
		setHeight(400);
	}

	/**
	 * Call setReferencedComponent first
	 * 
	 * @see gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
	 */
	@Override
	public void showView(Window parent)
	{
		if (! shown) {
			shown = true;

			initOwner(parent);
			initModality(Modality.WINDOW_MODAL);
			initStyle(StageStyle.DECORATED);
		}

		show();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.WorkflowTaskViewI#setInitialTask(long)
	 */
	@Override
	public void setTask(long taskId) {
		// Make sure in application thread.
		FxUtils.checkFxUserThread();
		try {
			controller_.setTask(taskId);
		} catch (Exception e) {
			String title = "Unexpected error loading task " + taskId;
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage();
			logger.error(title + ". " + msg, e);
			AppContext.getCommonDialogs().showErrorDialog(title, msg, e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.WorkflowTaskViewI#getInitialTask()
	 */
	@Override
	public Long getTask() {
		return controller_.getTask() != null ? controller_.getTask().getId() : null;
	}

	@Override
	public void releaseTask(long taskId) throws IOException {
		try
		{
			controller_.doReleaseTask(taskId);
		}
		catch (DatastoreException e)
		{
			throw new IOException(e.getMessage());
		}
	}
}