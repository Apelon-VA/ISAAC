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
import javafx.scene.text.Font;
import com.sun.javafx.tk.Toolkit;

/**
 * {@link Operation}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class Operation
{
	protected ObservableList<SimpleDisplayConcept> conceptList_;
	protected GridPane root_;
	
	public Operation(ObservableList<SimpleDisplayConcept> conceptList)
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
	 * Call this after adding all label content to the first column, to prevent it from shrinking
	 */
	protected void preventColOneCollapse()
	{
		Font f = new Font("System", 13.0);
		float largestWidth = 0;
		for (Node n : root_.getChildrenUnmodifiable())
		{
			if (GridPane.getColumnIndex(n) == 0 && n instanceof Label)
			{
				float width = Toolkit.getToolkit().getFontLoader().computeStringWidth(((Label)n).getText(), f);
				if (width > largestWidth)
				{
					largestWidth = width;
				}
			}
		}
		//don't let the first column shrink less than the labels
		ColumnConstraints cc = new ColumnConstraints();
		cc.setMinWidth(largestWidth);
		root_.getColumnConstraints().add(cc);
	}
	
	public abstract String getTitle();
	
	protected abstract void conceptListChanged();
	
	public abstract BooleanExpression isValid();
}
