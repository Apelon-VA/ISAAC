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
import gov.va.isaac.workflow.gui.WorkflowAdvancementViewController;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

/**
 * TaskModel
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class TaskModel {
	private final static Logger LOGGER = LoggerFactory.getLogger(TaskModel.class);

	private final LocalTask task;

	private final ObjectProperty<Action> actionProperty = new SimpleObjectProperty<>();

	private final Map<String, StringProperty> outputVariables = new HashMap<>();
	
	private final Map<String, BooleanProperty> outputVariableStatuses = new HashMap<>();

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

	public Map<String, String> getInputVariables() { return task.getInputVariables(); }
	public Map<String, StringProperty> getOutputVariables() { return outputVariables; }
	public Map<String, BooleanProperty> getOutputVariableStatuses() { return outputVariableStatuses; }

	public void addOutputVariable(String variableName) {
		getOutputVariables().put(variableName, new SimpleStringProperty());

		getOutputVariableStatuses().put(variableName, new SimpleBooleanProperty(false));
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

			// Validation should be performed by adding handliers/listeners
			// to individual properties in createOutputNode()
//			for (Map.Entry<String, StringProperty> entry : this.outputVariables.entrySet()) {
//				if (entry.getValue().get() == null || entry.getValue().get().length() < 1) {
//					return false;
//				}
//			}

			for (Map.Entry<String, BooleanProperty> entry : this.outputVariableStatuses.entrySet()) {
				if (! entry.getValue().get()) {
					LOGGER.debug("Validation failed on variable {} with value {}", entry.getKey(), getOutputVariables().get(entry.getKey()).get());
					return false;
				}
			}

			return true;
		}
	}

	// Default implementation
	public String getLabelName(String variableName) { return variableName; }

	public abstract Node createOutputNode(String variableName);
}
