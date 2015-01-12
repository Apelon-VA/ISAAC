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
 * TaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow.taskmodel;

import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class TaskModel {
	// TODO (artf231903): change TaskModel to use ValidBooleanBinding

	private final static Logger LOGGER = LoggerFactory.getLogger(TaskModel.class);

	public enum UserActionOutputResponse {
		sendToReviewer("Send To Reviewer", null), // used only by Edit content but does not correspond to any output variable
		sendToApprover("Send To Approver", "approve"), // used by ReviewContentTaskModel
		approveForPublication("Approve For Publication", "approve"), // ?
		cancelWorkflow("Cancel Workflow", "cancel"),
		rejectToEditor("Reject and Send to Editor", "reject"),
		rejectToReviewer("Reject and Send to Reviewer", "reject-review"),
		rejectToReviewer1("Reject and Send to Reviewer 1", "reject-review-1"),
		rejectToReviewer2("Reject and Send to Reviewer 2", "reject-review-2"),;
		
		private final String displayValue;
		private final String userActionOutputResponseValue;
		
		private UserActionOutputResponse() {
			this(null, null);
		}

		private UserActionOutputResponse(String displayName, String serverAction) {
			this.displayValue = displayName;
			this.userActionOutputResponseValue = serverAction;
		}
		
		public String getDisplayValue() {
			return displayValue;
		}
		
		public String getUserActionOutputResponseValue() {
			return userActionOutputResponseValue;
		}
	}

	interface Validator {
		boolean isValid();
	}
	
	private static class ComponentsForOutputVariable {
		// TODO (artf231903): change ComponentsForOutputVariable to use ValidBooleanBinding
//		private final String variableName;
		private final Label label;
		private Node inputNode = null;
		private final StringProperty valueProperty = new SimpleStringProperty();
		private final BooleanProperty statusProperty = new SimpleBooleanProperty(false);
		private Validator validator;
		private ChangeListener<String> valuePropertyListener;
		
//		String getVariableName() {
//			return variableName;
//		}
		Label getLabel() {
			return label;
		}
		Node getInputNode() {
			return inputNode;
		}
		void setInputNode(Node node) {
			inputNode = node;
		}
		StringProperty getValueProperty() {
			return valueProperty;
		}
		BooleanProperty getStatusProperty() {
			return statusProperty;
		}
//		Validator getValidator() { return validator; }
		void setValidator(Validator v) {
			if (valuePropertyListener != null) {
				valueProperty.removeListener(valuePropertyListener);
			}
			validator = v;
			
			if (validator != null) {
				valueProperty.addListener(new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue,
							String newValue) {
						statusProperty.set(validator.isValid());
					}});
				statusProperty.set(validator.isValid());
			}
		}

		private ComponentsForOutputVariable(String variableName, Label label) {
			this(variableName, label, (Node)null, (Validator)null);
		}
		private ComponentsForOutputVariable(String variableName, Label label, Node inputNode, Validator validator) {
			super();
//			this.variableName = variableName;
			this.label = label;
			this.inputNode = inputNode;
			this.validator = validator;

			// Add default validator
			if (validator == null) {
				setValidator(new Validator() {
					@Override
					public boolean isValid() {
						return valueProperty != null && valueProperty.get() != null && valueProperty.get().length() > 0;
					}	
				});
			}
		}
	}

	private final LocalTask task;

	private final ComboBox<UserActionOutputResponse> userActionOutputResponseComboBox;

	private final ObjectProperty<Action> actionProperty = new SimpleObjectProperty<>(Action.COMPLETE);

	private final Map<String, ComponentsForOutputVariable> componentsForOutputVariables = new HashMap<>();

	private final BooleanProperty isSavableProperty = new SimpleBooleanProperty(false);
	
	private final BooleanProperty outputVariablesSavableProperty = new SimpleBooleanProperty(false);

	protected final ComboBox<UserActionOutputResponse> getUserActionOutputResponseComboBox() {
		return userActionOutputResponseComboBox;
	}

	protected void initializeUserActionOutputResponseComboBox() {
		getUserActionOutputResponseComboBox().setButtonCell(new ListCell<UserActionOutputResponse>() {
			@Override
			protected void updateItem(UserActionOutputResponse t, boolean bln) {
				super.updateItem(t, bln); 
				if (bln) {
					setText("");
				} else {
					setText(t.getDisplayValue());
				}
			}
		});

		getUserActionOutputResponseComboBox().setCellFactory((p) -> {
			final ListCell<UserActionOutputResponse> cell = new ListCell<UserActionOutputResponse>() {
				@Override
				protected void updateItem(UserActionOutputResponse c, boolean emptyRow) {
					super.updateItem(c, emptyRow);

					if(c == null || emptyRow) {
						setText(null);
					} else {
						setText(c.getDisplayValue());
					}
				}
			};

			return cell;
		});
	}

	/**
	 * 
	 */
	private TaskModel(LocalTask inputTask, ComboBox<UserActionOutputResponse> userActionOutputResponseComboBox) {
		this.task = inputTask;
		this.userActionOutputResponseComboBox = userActionOutputResponseComboBox;

		getActionProperty().addListener(getDefaultListenerToSetIsSavableProperty());
		getOutputVariablesSavableProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue,
					Boolean newValue) {
				isSavableProperty.set(isSavable());
			}});

		initializeUserActionOutputResponseComboBox();
	}
	protected TaskModel(LocalTask inputTask, ComboBox<UserActionOutputResponse> userActionOutputResponseComboBox, Enum<? extends Enum<?>>[] enumValues) {
		this(inputTask, userActionOutputResponseComboBox);
		
		for (Enum<?> en : enumValues) {
			addOutputVariable(en.name());
			
			initializeOutputVariableInputNode(en.name());
		}
	}

	protected ChangeListener<? super Action> getDefaultListenerToSetIsSavableProperty() {
		return new ChangeListener<Action>() {
			@Override
			public void changed(
					ObservableValue<? extends Action> observable,
					Action oldValue,
					Action newValue) {
				isSavableProperty.set(isSavable());
			}};
	}

	public LocalTask getTask() { return task; }

	public BooleanProperty getOutputVariablesSavableProperty() { return outputVariablesSavableProperty; }
	
	public BooleanProperty getIsSavableProperty() { return isSavableProperty; }
	public boolean getIsSavable() { return getIsSavableProperty().get(); }
	public void setIsSavable(boolean isSavable) { getIsSavableProperty().set(isSavable); }

	public ObjectProperty<Action> getActionProperty() { return actionProperty; }
	public Action getAction() { return getActionProperty().get(); }
	public void setAction(Action action) { getActionProperty().set(action); }

	public Map<String, String> getInputVariables() { return Collections.unmodifiableMap(task.getInputVariables()); }
	public Map<String, String> getCurrentOutputVariables() {
		Map<String, String> currentOutputVariables = new HashMap<>();
		
		for (String variableName : getOutputVariableNames()) {
			currentOutputVariables.put(variableName, getOutputVariableValueProperty(variableName).get());
		}
		
		return currentOutputVariables;
	}

	protected void setOutputVariableValidator(String variableName, Validator validator) {
		componentsForOutputVariables.get(variableName).setValidator(validator);
	}

	/**
	 * @param variableName
	 * 
	 * Creates ComponentsForOutputVariable entry for specified variableName
	 * by invoking overridable methods and overridden abstract method createOutputVariableInputNode().
	 * 
	 * Note that while implementations of createOutputVariableInputNode() may include references to
	 * the Label and other properties, createOutputVariableInputNodeLabel() may not refer to
	 * any of these, as they will not exist until the Label has already been created
	 * and the respective ComponentsForOutputVariable has been put into the componentsForOutputVariables map
	 * 
	 * This method is final because the order of its operations is significant
	 * 
	 */
	protected final void addOutputVariable(String variableName) {
		Label newLabel = createOutputVariableInputNodeLabel(variableName);
		ComponentsForOutputVariable componentsForOutputVariable = new ComponentsForOutputVariable(variableName, newLabel);
		componentsForOutputVariables.put(variableName, componentsForOutputVariable);

		BooleanProperty variableStatusProperty = getOutputVariableStatusProperty(variableName);
		variableStatusProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue,
					Boolean newValue) {
				if (newValue != null && newValue) {
					newLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
					if (areOutputVariablesSavable()) {
						getOutputVariablesSavableProperty().set(true);
					}
					if (isSavable()) {
						getIsSavableProperty().set(true);
					}
				} else {
					newLabel.setStyle("-fx-text-fill: red;");
					getOutputVariablesSavableProperty().set(false);
					getIsSavableProperty().set(false);
				}
			}});

		
		Node newNode = getOrCreateOutputVariableInputNode(variableName);
		componentsForOutputVariable.setInputNode(newNode);
	}
	
	/**
	 * @return boolean indicating whether or not this TaskModel is savable
	 * 
	 * For this default implementation to work it is important that all validations
	 * other than string size > 0 be applied on the respective input nodes before
	 * setting the validated value in the outputVariables map
	 * 
	 */
	public boolean isSavable() {
		if (actionProperty.get() != null && actionProperty.get() == Action.RELEASE) {
			return true;
		} else {
			if (actionProperty.get() == null || actionProperty.get() == Action.NONE) {
				return false;
			}

			// Validation should be performed by adding handlers/listeners
			// to individual properties in createOutputNode()
//			for (Map.Entry<String, StringProperty> entry : this.outputVariables.entrySet()) {
//				if (entry.getValue().get() == null || entry.getValue().get().length() < 1) {
//					return false;
//				}
//			}

			return areOutputVariablesSavable();
		}
	}

	public boolean areOutputVariablesSavable() {
		for (Map.Entry<String, ComponentsForOutputVariable> entry : componentsForOutputVariables.entrySet()) {
			if (! entry.getValue().getStatusProperty().get()) {
				LOGGER.debug("Validation failed on variable {} with value {}", entry.getKey(), entry.getValue().getValueProperty().get());
				return false;
			}
		}
		
		return true;
	}
	
	public Set<String> getOutputVariableNames() {
		return Collections.unmodifiableSet(componentsForOutputVariables.keySet());
	}
	public Label getOutputVariableInputNodeLabel(String variableName) {
		return componentsForOutputVariables.get(variableName).getLabel();
	}
	public Node getOutputVariableInputNode(String variableName) {
		return componentsForOutputVariables.get(variableName).getInputNode();
	}
	public BooleanProperty getOutputVariableStatusProperty(String variableName) {
		return componentsForOutputVariables.get(variableName).getStatusProperty();
	}
	public StringProperty getOutputVariableValueProperty(String variableName) {
		return componentsForOutputVariables.get(variableName).getValueProperty();
	}

	/**
	 * @param variableName
	 * @return the displayable Label name for the specified variableName
	 * 
	 * This default implementation is overridable
	 */
	protected String getOutputVariableInputNodeLabelName(String variableName) { return variableName; }

	/**
	 * @param variableName
	 * @return the Label corresponding to the variableName and its respective properties and input node
	 * 
	 * Note that while implementations of createOutputVariableInputNode() may include references to
	 * the Label and other properties, createOutputVariableInputNodeLabel() may not refer to
	 * any of these, as they will not exist until the Label has already been created
	 * and the respective ComponentsForOutputVariable has been put into the componentsForOutputVariables map
	 * 
	 */
	protected Label createOutputVariableInputNodeLabel(String variableName) {
		Label newLabel = new Label(getOutputVariableInputNodeLabelName(variableName));
		
		return newLabel;
	}

	/**
	 * @param variableName
	 * @return the input node associated with the specified variableName
	 * 
	 * Note that while implementations of createOutputVariableInputNode() may include references to
	 * the Label and other properties, createOutputVariableInputNodeLabel() may not refer to
	 * any of these, as they will not exist until the Label has already been created
	 * and the respective ComponentsForOutputVariable has been put into the componentsForOutputVariables map
	 * 
	 */
	protected abstract Node getOrCreateOutputVariableInputNode(String variableName);

	/**
	 * @param variableName
	 * 
	 * Initialize the input control value.
	 * 
	 * TODO Note that this method, in order to trigger appropriate change listeners,
	 * should first set the control to some value other than its initial value then immediately set it
	 * to its initial value.  This is a hack that should be addressed.
	 */
	protected abstract void initializeOutputVariableInputNode(String variableName);
}
