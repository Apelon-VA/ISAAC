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

/**
 * EditContentTaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow.taskmodel;

import gov.va.isaac.workflow.LocalTask;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

/**
 * DualReviewReviewContentTaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DualReviewReviewContentTaskModel extends TaskModel {
	public enum InputVariable {
		component_id("Component Id"),
		component_name("Component Name"),
		instructions("Instructions"),
		
		// Only one of the following two should ever exist in the same task
		editor_comment_1("Editor Comment"),
		editor_comment_2("Editor Comment"),
		
		// Only one of the following two should ever exist in the same task
		editor_coordinate_1("Edit Coordinate"),
		editor_coordinate_2("Edit Coordinate");
		
		private final String labelName;
		private InputVariable(String labelName) {
			this.labelName = labelName;
		}
		
		public String getLabelName() { return labelName; }
		
		public static InputVariable fromString(String str) {
			try {
				return valueOf(str);
			} catch (Throwable t) {
				return null;
			}
		}
	}
	
	public enum OutputVariable {
		out_response("Response"),
		out_comment("Comment");

		private final String labelName;
		private OutputVariable(String labelName) {
			this.labelName = labelName;
		}
		
		public String getLabelName() { return labelName; }

		public static OutputVariable fromString(String str) {
			try {
				return valueOf(str);
			} catch (Throwable t) {
				return null;
			}
		}
	}

	private TextArea commentTextArea = null;

	/**
	 * @param inputTask
	 */
	DualReviewReviewContentTaskModel(LocalTask inputTask, ComboBox<UserActionOutputResponse> userActionOutputResponseComboBox) {
		super(inputTask, userActionOutputResponseComboBox, OutputVariable.values());
	}

	/**
	 * @see gov.va.isaac.workflow.taskmodel.TaskModel#getLabelName(java.lang.String)
	 */
	@Override
	protected String getOutputVariableInputNodeLabelName(String variableName) {
		if (InputVariable.fromString(variableName) != null) {
			return InputVariable.fromString(variableName).getLabelName();
		}

		if (OutputVariable.fromString(variableName) != null) {
			return OutputVariable.fromString(variableName).getLabelName();
		}
		
		return null;
	}

	/**
	 * @see gov.va.isaac.workflow.taskmodel.TaskModel#createOutputNode(java.lang.String)
	 */
	@Override
	protected Node getOrCreateOutputVariableInputNode(String variableName) {
		OutputVariable outputVariable = OutputVariable.valueOf(variableName);
		
		switch (outputVariable) {
		case out_comment: {
			commentTextArea = new TextArea();
			
			StringProperty commentProperty = getOutputVariableValueProperty(variableName);

			commentProperty.bind(commentTextArea.textProperty());

			return commentTextArea;
		}
		case out_response: {
			ComboBox<UserActionOutputResponse> responseComboBox = getUserActionOutputResponseComboBox();
			responseComboBox.getItems().addAll(
					UserActionOutputResponse.sendToApprover,
					UserActionOutputResponse.rejectToEditor);

			StringProperty responseProperty = getOutputVariableValueProperty(OutputVariable.out_response.name());

			setOutputVariableValidator(OutputVariable.out_response.name(), new Validator() {
				@Override
				public boolean isValid() {
					if (responseProperty == null || responseProperty.get() == null) {
						return false;
					}

					for (UserActionOutputResponse configuredUserAction : responseComboBox.getItems()) {
						if (configuredUserAction.getUserActionOutputResponseValue().equals(responseProperty.get())) {
							return true;
						}
					}

					return false;
				}
			});

			responseComboBox.valueProperty().addListener(new ChangeListener<UserActionOutputResponse>() {
				@Override
				public void changed(
						ObservableValue<? extends UserActionOutputResponse> observable,
						UserActionOutputResponse oldValue,
						UserActionOutputResponse newValue) {
					responseProperty.set(newValue != null ? newValue.getUserActionOutputResponseValue() : null);
				}});

			return responseComboBox;
		}
		
		default: throw new IllegalArgumentException("Unsupported " + OutputVariable.class.getName() + " value: " + outputVariable);
		}
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.taskmodel.TaskModel#initializeOutputVariableInputNode(java.lang.String)
	 */
	@Override
	protected void initializeOutputVariableInputNode(String variableName) {
		OutputVariable outputVariable = OutputVariable.valueOf(variableName);
		
		switch (outputVariable) {
		case out_comment: {
			// Initialize state of input control, triggering handlers/listeners
			commentTextArea.setText("unset"); // hack to trigger change listener
			commentTextArea.setText("");
			
			break;
		}
		case out_response: {
			// Initialize state of input control, triggering handlers/listeners
			getUserActionOutputResponseComboBox().getSelectionModel().clearAndSelect(0); // hack to trigger change listener
			getUserActionOutputResponseComboBox().getSelectionModel().select(null);

			break;
		}
		
		default: throw new IllegalArgumentException("Unsupported " + OutputVariable.class.getName() + " value: " + outputVariable);
		}
	}
}
