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

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowHistoryViewI;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

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
 * {@link WorkflowHistoryView}
 * 
 * @author <a href="mailto:jeforn@apelon.com">Jesse Efron</a>
 */

@Service
@PerLookup
public class WorkflowHistoryView extends Stage implements WorkflowHistoryViewI
{
	private final Logger logger = LoggerFactory.getLogger(WorkflowHistoryView.class);

	private WorkflowHistoryViewController controller_;

	private boolean shown = false;
	
	private WorkflowHistoryView() throws IOException
	{
		super();

		URL resource = this.getClass().getResource("WorkflowHistoryView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root = (Parent) loader.load();
		setScene(new Scene(root));
		getScene().getStylesheets().add(WorkflowHistoryView.class.getResource("/isaac-shared-styles.css").toString());
		getIcons().add(Images.INBOX.getImage());

		controller_ = loader.getController();
		controller_.setStage(this);
		
		setTitle("Workflow History");
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
			initModality(Modality.NONE);
			initStyle(StageStyle.DECORATED);
		}

		logger.debug("showing Workflow History View");
		show();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowTaskWithConceptI#getConceptUuid()
	 */
	@Override
	public UUID getConceptUuid() {
		return controller_.getConcept().getPrimordialUuid();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowTaskWithConceptI#getConceptNid()
	 */
	@Override
	public int getConceptNid() {
		return controller_.getConcept().getConceptNid();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowHistoryViewI#setInitialTask(long)
	 */
	@Override
	public void setTask(long taskId) {
		FxUtils.checkFxUserThread();

		controller_.setTask(taskId);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.WorkflowHistoryViewI#getInitialTask()
	 */
	@Override
	public Long getTask() {
		return controller_.getTask() != null ? controller_.getTask().getId() : null;
	}
}
