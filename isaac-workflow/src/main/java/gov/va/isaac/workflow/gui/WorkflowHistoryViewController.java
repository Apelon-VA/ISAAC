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
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.persistence.WorkflowHistoryHelper;

import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link WorkflowHistoryViewController}
 *
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a>
 */
public class WorkflowHistoryViewController
{	
	private final static Logger logger = LoggerFactory.getLogger(WorkflowHistoryViewController.class);
	
	@FXML private BorderPane borderPane;
	@FXML private Button closeButton;
	@FXML private GridPane wfHxGridPane;
	
	private WorkflowHistoryView stage;
	
	@Inject
	private LocalTasksServiceBI taskService_;

	private LocalTask localTask;

	// Initialize GUI (invoked by FXML)
	@FXML
	void initialize()
	{
		AppContext.getServiceLocator().inject(this);

		closeButton.setOnAction((action) -> {
			stage.close();
		});
	}

	void setStage(WorkflowHistoryView stage) {
		this.stage = stage;
	}

	public LocalTask getTask() {
		return localTask;
	}

	public void setTask(long taskId) {
		final String errorDialogTitle = "Failed Setting Workflow Task";
		final String errorDialogMsg = "Failed setting initial workflow task by id #" + taskId;

		localTask = null;
		try
		{
			localTask = taskService_.getTask(taskId);
		}
		catch (Exception e1)
		{
			String details = "Failed loading initial workflow task #" + taskId + ". Caught " + e1.getClass().getName() + " " + e1.getLocalizedMessage();
			logger.error(details, e1);

			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			stage.close();

			return;
		}

		if (localTask == null) {
			String details = "Failed loading initial workflow task #" + localTask.getId() + " (got null)";
			
			logger.error(details);
			
			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			stage.close();

			return;
		}

		
		loadContent();
	}

	// Load data into GUI components not already loaded by TaskModel
	protected void loadContent()
	{
		WorkflowHistoryHelper.loadGridPane(wfHxGridPane, localTask);
	}
	

	public Region getRootNode() {
		return borderPane;
	}

	ConceptVersionBI getConcept() {
		UUID componentUuid = UUID.fromString(localTask.getComponentId());
		
		return OTFUtility.getConceptVersion(componentUuid);
	}
}
