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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.listview.operations;

import gov.va.isaac.gui.SimpleDisplayConcept;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.jvnet.hk2.annotations.Contract;
import com.sun.javafx.tk.Toolkit;

/**
 * {@link Operation}
 * 
 * The interface that serves as the basis for batch operations.  Extend this to create a new operation.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public abstract class Operation
{
	protected ObservableList<SimpleDisplayConcept> conceptList_;
	protected GridPane root_;
	
	protected Operation()
	{
		//For HK2 to create
	}
	
	public void init(ObservableList<SimpleDisplayConcept> conceptList)
	{
		this.conceptList_ = conceptList;
		root_ = new GridPane();
		root_.setMaxWidth(Double.MAX_VALUE);
		root_.setHgap(5.0);
		root_.setVgap(3.0);
		
		conceptList_.addListener(new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends SimpleDisplayConcept> c)
			{
				conceptListChanged();
			}
		});
	}
	
	public Node getNode()
	{
		return root_;
	}
	
	/**
	 * The title of the operation, which will be displayed to the user.
	 * @return
	 */
	public abstract String getTitle();
	
	/**
	 * The description of the operation, which will be displayed to the user
	 * @return
	 */
	public abstract String getOperationDescription();
	
	/**
	 * A convenience method which will be called when the conceptList changes.
	 */
	protected abstract void conceptListChanged();
	
	/**
	 * Implementers should return a BooleanExpression which evaluates to true when all necessary values
	 * for task execution have been provided.
	 */
	public abstract BooleanExpression isValid();
	
	/**
	 * Implementers should return a task - the task should update the progress value as appropriate, and 
	 * update the message value as appropriate as the task progresses.
	 * 
	 * If the execution fails during the task execution, an Exception should be thrown using the 
	 * Task API.  The returned String value should be a summary of what happened during the task execution
	 * which will be displayed to end users.
	 * 
	 * Implementers should also monitor the {@code CustomTask#cancelRequested_} variable, and respond appropriately 
	 * if the variable is set to true.
	 * 
	 * @return a Task implementation that performs the necessary work on the {@code #conceptList_}
	 */
	public abstract CustomTask<String> createTask();
}
