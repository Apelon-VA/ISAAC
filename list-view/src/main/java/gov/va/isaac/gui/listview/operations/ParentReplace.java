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

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * {@link ParentReplace}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ParentReplace implements Operation
{
	private GridPane root_;
	private ComboBox<String> replaceOptions_;
	private TextField withConcept_;
	
	public ParentReplace()
	{
		root_ = new GridPane();
		root_.setMaxWidth(Double.MAX_VALUE);
		root_.setHgap(5.0);
		root_.setVgap(3.0);
		
		root_.add(new Label("Replace: "), 0, 0);
		
		replaceOptions_ = new ComboBox<>();
		replaceOptions_.setPromptText("Populate the Concepts List");
		replaceOptions_.setMaxWidth(Double.MAX_VALUE);
		root_.add(replaceOptions_, 1, 0);
		
		root_.add(new Label("With Parent: "), 0, 1);
		
		withConcept_ = new TextField();
		withConcept_.setPromptText("Concept Required");
		GridPane.setHgrow(withConcept_, Priority.ALWAYS);
		root_.add(withConcept_, 1, 1);
	}
	public Node getNode()
	{
		return root_;
	}
	/**
	 * @see gov.va.isaac.gui.listview.operations.Operation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "Parent, Replace";
	}
}

