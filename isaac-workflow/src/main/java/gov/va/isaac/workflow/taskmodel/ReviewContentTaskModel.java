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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputEvent;

/**
 * EditContentTaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class ReviewContentTaskModel extends TaskModel {
	public enum Response {
		approve("Send for approval"),
		reject("Reject"),
		cancel("Cancel");
		
		private final String displayText;
		private Response(String displayTxt) {
			displayText = displayTxt;
		}
		
		public String getDisplayText() { return displayText; }
		
		public static Response valueOfIfExists(String str) {
			try {
				return valueOf(str);
			} catch (Throwable t) {
				return null;
			}
		}
	}
	public enum InputVariable {
		component_id("Component Id"),
		component_name("Component Name"),
		instructions("Instructions");
		
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

	/**
	 * @param inputTask
	 */
	ReviewContentTaskModel(LocalTask inputTask) {
		super(inputTask, OutputVariable.values());
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see gov.va.isaac.workflow.taskmodel.TaskModel#createOutputNode(java.lang.String)
	 */
	@Override
	protected Node createOutputVariableInputNode(String variableName) {
		OutputVariable outputVariable = OutputVariable.valueOf(variableName);
		
		switch (outputVariable) {
		case out_comment: {
			TextArea commentTextArea = new TextArea();
			
			StringProperty commentProperty = getOutputVariableValueProperty(OutputVariable.out_comment.name());
			BooleanProperty commentPropertyStatus = getOutputVariableStatusProperty(OutputVariable.out_comment.name());
			
			commentProperty.bind(commentTextArea.textProperty());
			commentTextArea.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
				@Override
				public void handle(InputEvent event) {
					commentPropertyStatus.set(commentTextArea.getText() != null && commentTextArea.getText().length() > 0);
					getIsSavableProperty().set(isSavable());
				}
			});
			
			// Initialize state of input control, triggering handlers/listeners
			commentTextArea.setText(null);
			
			return commentTextArea;
		}
		case out_response: {
			ComboBox<Response> responseComboBox = new ComboBox<>();
			
			StringProperty responseProperty = getOutputVariableValueProperty(OutputVariable.out_response.name());
			BooleanProperty responsePropertyStatus = getOutputVariableStatusProperty(OutputVariable.out_response.name());

			responseComboBox.valueProperty().addListener(new ChangeListener<Response>() {
			@Override
			public void changed(
					ObservableValue<? extends Response> observable,
					Response oldValue,
					Response newValue) {
				responsePropertyStatus.set(Response.valueOfIfExists(responseProperty.get()) != null);
			}});
			responseComboBox.setButtonCell(new ListCell<Response>() {
				@Override
				protected void updateItem(Response t, boolean bln) {
					super.updateItem(t, bln); 
					if (bln) {
						setText("");
						responseProperty.set("");
					} else {
						setText(t.displayText);
						responseProperty.set(t.name());
					}

					responsePropertyStatus.set(Response.valueOfIfExists(responseProperty.get()) != null);
				
					getIsSavableProperty().set(isSavable());
				}
			});

			responseComboBox.setCellFactory((p) -> {
				final ListCell<Response> cell = new ListCell<Response>() {
					@Override
					protected void updateItem(Response c, boolean emptyRow) {
						super.updateItem(c, emptyRow);

						if(c == null || emptyRow) {
							setText(null);
						} else {
							setText(c.displayText);
						}
					}
				};

				return cell;
			});

			// Initialize state of input control, triggering handlers/listeners
			responseComboBox.getItems().addAll(Response.values());
			responseComboBox.getSelectionModel().select(null);
			
			return responseComboBox;
		}
		
		default: throw new IllegalArgumentException("Unsupported " + OutputVariable.class.getName() + " value: " + outputVariable);
		}
	}
}
