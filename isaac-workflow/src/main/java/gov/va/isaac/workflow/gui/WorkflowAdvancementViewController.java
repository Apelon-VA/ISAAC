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
import gov.va.isaac.gui.SimpleDisplayConcept;
import gov.va.isaac.gui.dialog.BusyPopover;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import gov.va.isaac.workflow.ComponentDescriptionHelper;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import gov.va.isaac.workflow.taskmodel.TaskModel;
import gov.va.isaac.workflow.taskmodel.TaskModelFactory;

import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.kie.api.task.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link WorkflowAdvancementViewController}
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class WorkflowAdvancementViewController
{	
	private final static Logger logger = LoggerFactory.getLogger(WorkflowAdvancementViewController.class);
	
	enum WorkflowActionsValueMap {
		sendToReviewer("Send To Reviewer", "aa"),
		sendToApprover("Send To Approver", "aa"),
		approveForPublication("Approve For Publication", "aa"),
		cancelWorkflow("Cancel Workflow", "aa"),
		rejectToEditor("Reject and Send to Editor", "aa"),
		rejectToReviewer("Reject and Send to Reviewer", "aa");
		
		private final String displayName;
		private final String serverAction;
		
		private WorkflowActionsValueMap() {
			this(null, null);
		}

		private WorkflowActionsValueMap(String displayName, String serverAction) {
			this.displayName = displayName;
			this.serverAction = serverAction;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		public String getServerAction() {
			return serverAction;
		}
	}
	
	// Underlying concept for loading detail pane
	private ConceptVersionBI conceptVersion;

	@FXML private BorderPane borderPane;
	@FXML private TextArea commentTextField;
	@FXML private Button closeButton;
	@FXML private Button advanceButton;
	@FXML private Label generatedComponentSummary;
	@FXML private ComboBox<WorkflowActionsValueMap> actionComboBox;	
	@FXML private GridPane advanceWfGridPane;
	
	private WorkflowAdvancementView stage;
	private TaskModel taskModel = null;
	
	@Inject
	private LocalTasksServiceBI taskService_;

	private LocalTask initialTask;

	private UUID componentId;

	// Initialize GUI (invoked by FXML)
	@FXML
	void initialize()
	{
		AppContext.getServiceLocator().inject(this);

		// Disabling saveActionButton until dependencies met 
		advanceButton.setDisable(true);

		// Activation of save depends on taskModel.isSavable()
		advanceButton.setOnAction((action) -> {
			if (taskModel.isSavable()) {
				Platform.runLater(() -> 
				{
					unbindSaveActionButtonFromModelIsSavableProperty();
					advanceButton.setDisable(true);
				});
				final BusyPopover saveActionBusyPopover = BusyPopover.createBusyPopover("Saving action...", advanceButton);

				Utility.execute(() -> {
					try
					{
						taskService_.completeTask(taskModel.getTask().getId(), taskModel.getCurrentOutputVariables());
						
						addToPromotionPath();
						
						Platform.runLater(() -> 
						{
							//refreshSaveActionButtonBinding();

							if (stage != null) {
								stage.close();
							}
						});
					}
					catch (Exception e)
					{
						logger.error("Error saving task: unexpected " + e.getClass().getName() + " \"" + e.getLocalizedMessage() + "\"", e);
					} finally {
						cleanupAfterSaveTaskAction(this, saveActionBusyPopover);
					}
				});
			} else { // ! this.taskModel.isSavable()
				// This should never happen, if saveActionButton.setDisable(true) used in proper places
				logger.error("Error completing task: fields not set: task={}", taskModel.getTask());
			}
		});
	}

	/*
	 * Need this method as workaround for compiler/JVM bug:
	 * { @link http://stackoverflow.com/questions/13219297/bad-type-on-operand-stack-using-jdk-8-lambdas-with-anonymous-inner-classes }
	 * { @link http://mail.openjdk.java.net/pipermail/lambda-dev/2012-September/005938.html }
	 */
	private static void cleanupAfterSaveTaskAction(WorkflowAdvancementViewController ctrlr, BusyPopover saveActionBusyPopover) {
		Platform.runLater(() ->  {
			saveActionBusyPopover.hide();

			ctrlr.refreshSaveActionButtonBinding();
		});
	}

	void setStage(WorkflowAdvancementView stage) {
		this.stage = stage;
	}

	public LocalTask getTask() {
		return taskModel.getTask();
	}

	public void setTask(long taskId) {
		//TODO Joel - what is a user supposed to do about any of these failures?  Shouldn't all of these be putting up a dialog informing them that the 
		//set task failed?  You can't just silently eat them - or can you eat some of these?
		if (taskModel != null) {
			String msg = "Cannot reset initialTask from " + taskModel.getTask().getId() + " to " + taskId;
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		if (conceptVersion != null) {
			String msg = "Cannot set initialTask to " + taskId + " when conceptVersion is already set to " + new SimpleDisplayConcept(conceptVersion);
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		initialTask = null;
		try
		{
			initialTask = taskService_.getTask(taskId);
		}
		catch (DatastoreException e1)
		{
			logger.error("error getting task", e1);
		}

		if (initialTask == null) {
			logger.error("Task retrieved by id {} is null", taskId);

			return;
		}
		if (initialTask.getComponentId() == null) {
			logger.error("Component ID for task {} is null", initialTask.getId());

			return;
		}
		UUID componentUuid = null;
		try {
			componentUuid = UUID.fromString(initialTask.getComponentId());
		} catch (IllegalArgumentException e) {
			logger.error("Component ID for task {} is not a valid UUID", initialTask.getId());

			return;
		}

		ConceptVersionBI containingConcept = null;
		ComponentChronicleBI<? extends ComponentVersionBI> componentChronicle = WBUtility.getComponentChronicle(componentUuid);
		if (componentChronicle == null) {
			logger.warn("Component ID for task " + initialTask.getId() + " retrieved a null componentChronicle");

			containingConcept = WBUtility.getConceptVersion(componentUuid);
			if (containingConcept == null) {
				logger.error("Component ID for task " + initialTask.getId() + " retrieved a null concept");

				return;
			}
		} else {
			try {
				containingConcept = componentChronicle.getEnclosingConcept().getVersion(WBUtility.getViewCoordinate());
			} catch (Exception e) {
				logger.error("Failed getting version from ComponentChronicleBI task " + initialTask.getId() + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
				e.printStackTrace();
			}

			if (containingConcept == null) {
				logger.error("ComponentChronicleBI task " + initialTask.getId() + " contained a null enclosing concept");

				return;
			}
		}

		if (componentChronicle == null) {
			logger.warn("Component id " + componentUuid + " for task " + initialTask.getId() + " is a concept, not just a component.");
		}
		
		conceptVersion = containingConcept;
		
		taskModel = TaskModelFactory.newTaskModel(initialTask);

		taskModel.getOutputVariablesSavableProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue,
					Boolean newValue) {
			}});
		
		loadContent();
	}

	ConceptVersionBI getConcept() {
		return conceptVersion;
	}
	
	private void bindSaveActionButtonToModelIsSavableProperty() {
		advanceButton.disableProperty().bind(taskModel.getIsSavableProperty().not());
	}
	private void unbindSaveActionButtonFromModelIsSavableProperty() {
		advanceButton.disableProperty().unbind();
	}

	// Load data into GUI components
	protected void loadContent()
	{
		componentId = UUID.fromString(initialTask.getComponentId());
		generatedComponentSummary.setText(ComponentDescriptionHelper.getComponentDescription(componentId));

		setComboBoxContents();
		
		refreshSaveActionButtonBinding();
	}

	private void refreshSaveActionButtonBinding()
	{
		unbindSaveActionButtonFromModelIsSavableProperty();
		bindSaveActionButtonToModelIsSavableProperty();
	}
	
	public Region getRootNode() {
		return borderPane;
	}

	private void setComboBoxContents() {

	}

	private void addToPromotionPath() {
		// TODO Auto-generated method stub
		
	}
}
