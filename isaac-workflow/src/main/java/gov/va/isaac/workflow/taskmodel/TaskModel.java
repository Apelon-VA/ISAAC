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
import javafx.scene.control.Label;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class TaskModel {
	private final static Logger LOGGER = LoggerFactory.getLogger(TaskModel.class);
	
	private static class ComponentsForOutputVariable {
		private final String variableName;
		private final Label label;
		private Node inputNode = null;
		private final StringProperty valueProperty = new SimpleStringProperty();
		private final BooleanProperty statusProperty = new SimpleBooleanProperty(false);

		String getVariableName() {
			return variableName;
		}
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

		private ComponentsForOutputVariable(String variableName, Label label) {
			super();
			this.variableName = variableName;
			this.label = label;
			this.inputNode = null;
		}
		private ComponentsForOutputVariable(String variableName, Label label, Node inputNode) {
			super();
			this.variableName = variableName;
			this.label = label;
			this.inputNode = inputNode;
		}
	}

	private final LocalTask task;

	private final ObjectProperty<Action> actionProperty = new SimpleObjectProperty<>();

	private final Map<String, ComponentsForOutputVariable> componentsForOutputVariables = new HashMap<>();

	private final BooleanProperty isSavableProperty = new SimpleBooleanProperty(false);

	/**
	 * 
	 */
	protected TaskModel(LocalTask inputTask) {
		task = inputTask;

		getActionProperty().addListener(getDefaultListenerToSetIsSavableProperty());
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

	protected void addOutputVariable(String variableName) {
		Label newLabel = createOutputVariableInputNodeLabel(variableName);
		ComponentsForOutputVariable newLabelAndNode = new ComponentsForOutputVariable(variableName, newLabel);
		componentsForOutputVariables.put(variableName, newLabelAndNode);
		
		Node newNode = createOutputVariableInputNode(variableName);
		newLabelAndNode.setInputNode(newNode);
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

			for (Map.Entry<String, ComponentsForOutputVariable> entry : componentsForOutputVariables.entrySet()) {
				if (! entry.getValue().getStatusProperty().get()) {
					LOGGER.debug("Validation failed on variable {} with value {}", entry.getKey(), entry.getValue().getValueProperty().get());
					return false;
				}
			}

			return true;
		}
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

	// Default implementation
	protected String getOutputVariableInputNodeLabelName(String variableName) { return variableName; }
	
	// Default implementation
	protected Label createOutputVariableInputNodeLabel(String variableName) {
		Label newLabel = new Label(getOutputVariableInputNodeLabelName(variableName));
		
		return newLabel;
	}

	protected abstract Node createOutputVariableInputNode(String variableName);
}
