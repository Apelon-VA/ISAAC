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
package gov.va.isaac.gui.util;

import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * {@link WrappedLabeled}
 * 
 * Force a Label to wrap properly in situations where it doesn't (like when embedded in a grid pane, or table)
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class WrappedLabeled
{
	public static HBox wrap(Labeled labeled)
	{
		HBox result = new HBox();
		result.setMaxWidth(Double.MAX_VALUE);
		result.getChildren().add(labeled);
		labeled.setWrapText(true);
		labeled.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(labeled, Priority.ALWAYS);
		labeled.prefWidthProperty().bind(result.widthProperty().subtract(20));
		result.minHeightProperty().bind(labeled.heightProperty());
		return result;
	}
	
	/**
	 * Useful when taking a node already placed by a fxml file, for example, so it wraps properly.
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * GridPane properties, but it takes everything.
	 */
	public static HBox wrapInstalledLabel(Labeled placedNode, GridPane gp)
	{
		int index = gp.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the grid pane");
		}

		gp.getChildren().remove(index);
		HBox replacementNode = wrap(placedNode);
		gp.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}
}
