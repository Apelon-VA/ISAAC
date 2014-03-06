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

import javafx.beans.binding.BooleanExpression;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 * {@link Operation}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class Operation
{
	protected ObservableList<String> conceptList_;
	protected GridPane root_;
	
	public Operation(ObservableList<String> conceptList)
	{
		this.conceptList_ = conceptList;
		root_ = new GridPane();
		root_.setMaxWidth(Double.MAX_VALUE);
		root_.setHgap(5.0);
		root_.setVgap(3.0);
		
		conceptList_.addListener(new ListChangeListener<String>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
			{
				conceptListChanged();
			}
		});
	}
	
	public Node getNode()
	{
		return root_;
	}
	
	
	public abstract String getTitle();
	
	protected abstract void conceptListChanged();
	
	public abstract BooleanExpression isValid();
}
