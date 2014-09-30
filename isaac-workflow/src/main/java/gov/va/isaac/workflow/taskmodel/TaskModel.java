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

import java.util.HashMap;
import java.util.Map;

import gov.va.isaac.workflow.Action;
import gov.va.isaac.workflow.LocalTask;
import gov.va.isaac.workflow.taskmodel.EditContentTaskModel.OutputVariable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

	protected final LocalTask task;
	
	protected ObjectProperty<Action> actionProperty = new SimpleObjectProperty<>();
	
	protected Map<String, StringProperty> outputVariables = new HashMap<>();
	
	protected BooleanProperty isSavableProperty = new SimpleBooleanProperty(false);
	
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

	/**
	 * 
	 */
	protected TaskModel(LocalTask inputTask) {
		task = inputTask;
		
		getActionProperty().addListener(getDefaultListenerToSetIsSavableProperty());
	}

	public BooleanProperty getIsSavableProperty() { return isSavableProperty; }
	
	public ObjectProperty<Action> getActionProperty() { return actionProperty; }
	public Action getAction() { return getActionProperty().get(); }
	public void setAction(Action action) { getActionProperty().set(action); }

	public Map<String, String> getInputVariables() { return task.getInputVariables(); }
	public Map<String, StringProperty> getOutputVariables() { return outputVariables; }
	
	public boolean isSavable() {
		if (actionProperty.get() != null && actionProperty.get() == Action.RELEASE) {
			return true;
		} else {
			if (actionProperty.get() == null || actionProperty.get() == Action.NONE) {
				return false;
			}
			
			for (Map.Entry<String, StringProperty> entry : this.outputVariables.entrySet()) {
				if (entry.getValue().get() == null || entry.getValue().get().length() < 1) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public abstract String getLabelName(String variableName);
	
	public abstract Node createOutputNode(String variableName);
}
