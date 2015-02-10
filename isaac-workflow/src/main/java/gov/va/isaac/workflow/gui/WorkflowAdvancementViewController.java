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
import gov.va.isaac.util.ComponentDescriptionHelper;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.LocalTasksServiceBI;
import gov.va.isaac.workflow.exceptions.DatastoreException;
import gov.va.isaac.workflow.taskmodel.TaskModel;
import gov.va.isaac.workflow.taskmodel.TaskModel.UserActionOutputResponse;
import gov.va.isaac.workflow.taskmodel.TaskModelFactory;

import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
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
	
	// Underlying concept for loading detail pane
	private ConceptVersionBI conceptVersion;

	@FXML private BorderPane borderPane;
	@FXML private Button closeButton;
	@FXML private Button advanceButton;
	@FXML private Label generatedComponentSummary;
	@FXML private ComboBox<UserActionOutputResponse> actionComboBox;	
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

		closeButton.setOnAction((action) -> {
			stage.close();
		});
		
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
		final String errorDialogTitle = "Failed Setting Workflow Task";
		final String errorDialogMsg = "Failed setting initial workflow task by id #" + taskId;
		if (taskModel != null) {
			String details = "Cannot reset initialTask from #" + taskModel.getTask().getId() + " to #" + taskId;
			logger.error(details);
			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			stage.close();
			
			return;
		}

		if (conceptVersion != null) {
			String details = "Cannot set initialTask to #" + taskId + " when conceptVersion is already set to " + new SimpleDisplayConcept(conceptVersion);

			logger.error(details);
			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			stage.close();

			return;
		}
		
		initialTask = null;
		try
		{
			initialTask = taskService_.getTask(taskId);
		}
		catch (DatastoreException e1)
		{
			String details = "Failed loading initial workflow task #" + taskId + ". Caught " + e1.getClass().getName() + " " + e1.getLocalizedMessage();
			logger.error(details, e1);

			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			stage.close();

			return;
		}

		if (initialTask == null) {
			String details = "Failed loading initial workflow task #" + initialTask.getId() + " (got null)";
			
			logger.error(details);
			
			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			stage.close();

			return;
		}
		if (initialTask.getComponentId() == null) {
			String details = "Component ID for task #" + initialTask.getId() + " is null";

			logger.error(details);

			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			//stage.close();

			return;
		}
		UUID componentUuid = null;
		try {
			componentUuid = UUID.fromString(initialTask.getComponentId());
		} catch (IllegalArgumentException e) {
			String details = "Component ID for task #" + initialTask.getId() + " is not a valid UUID";
			logger.error(details);

			AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

			//stage.close();

			return;
		}

		ConceptVersionBI containingConcept = null;
		ComponentChronicleBI<? extends ComponentVersionBI> componentChronicle = OTFUtility.getComponentChronicle(componentUuid);
		if (componentChronicle == null) {
			logger.warn("Component ID for task " + initialTask.getId() + " retrieved a null componentChronicle");

			containingConcept = OTFUtility.getConceptVersion(componentUuid);
			if (containingConcept == null) {
				String details = "Component ID for task #" + initialTask.getId() + " retrieved a null concept";

				logger.error(details);
				
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

				return;
			}
		} else {
			try {
				containingConcept = componentChronicle.getEnclosingConcept().getVersion(OTFUtility.getViewCoordinate());
			} catch (Exception e) {
				String details = "Failed getting version from ComponentChronicleBI task " + initialTask.getId() + ".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage();

				logger.error(details, e);
				
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);
				
				return;
			}

			if (containingConcept == null) {
				String details = "ComponentChronicleBI task " + initialTask.getId() + " contained a null enclosing concept";

				logger.error(details);
				
				AppContext.getCommonDialogs().showErrorDialog(errorDialogTitle, errorDialogMsg, details, stage);

				return;
			}
		}

		if (componentChronicle == null) {
			logger.warn("Component id " + componentUuid + " for task " + initialTask.getId() + " is a concept, not just a component.");
		}
		
		conceptVersion = containingConcept;
		
		taskModel = TaskModelFactory.newTaskModel(initialTask, actionComboBox);

		taskModel.getOutputVariablesSavableProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue,
					Boolean newValue) {
				
			}
		});
		
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

	// Load data into GUI components not already loaded by TaskModel
	protected void loadContent()
	{
		componentId = UUID.fromString(initialTask.getComponentId());
		generatedComponentSummary.setText(ComponentDescriptionHelper.getComponentDescription(componentId));

		loadTaskModel();
		
		refreshSaveActionButtonBinding();
	}
	
	private void loadTaskModel() {
		int rowIndex = 1;
		for (String outputVariable : taskModel.getOutputVariableNames()) {
			if (taskModel.getOutputVariableInputNode(outputVariable) != this.actionComboBox) {
				advanceWfGridPane.addRow(rowIndex++, taskModel.getOutputVariableInputNodeLabel(outputVariable), taskModel.getOutputVariableInputNode(outputVariable));
			}
		}
		
//		for (Node child : inputTabGridPane.getChildren()) {
//			VariableGridPaneNodeConfigurationHelper.configureNode(child);
//		}
	}

	private void refreshSaveActionButtonBinding()
	{
		unbindSaveActionButtonFromModelIsSavableProperty();
		bindSaveActionButtonToModelIsSavableProperty();
	}
	
	public Region getRootNode() {
		return borderPane;
	}
}
