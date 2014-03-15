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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link ErrorMarkerUtils}
 *
 * Convenience methods to wrap a control (textfield, combobox, etc) in a stack pane 
 * that has an error/info marker icon, and a tooltip that explains the reason why the marker is there.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ErrorMarkerUtils
{
	/**
	 * Setup an 'EXCLAMATION' error marker on the component.  Automatically displays anytime that the reasonWhyControlInvalid value
	 * is not empty.  Hides when the reasonWhyControlInvalid is empty.
	 */
	public static Node setupErrorMarker(Control initialControl, StringProperty reasonWhyControlInvalid)
	{
		return setupDisabledInfoMarker(initialControl, new StackPane(), reasonWhyControlInvalid);
	}
	
	/**
	 * Setup an 'EXCLAMATION' error marker on the component.  Automatically displays anytime that the reasonWhyControlInvalid value
	 * is not empty.  Hides when the reasonWhyControlInvalid is empty.
	 */
	public static Node setupErrorMarker(Control initialControl, StackPane stackPane, StringProperty reasonWhyControlInvalid)
	{
		ImageView exclamation = Images.EXCLAMATION.createImageView();
		
		final BooleanProperty showExclamation = new SimpleBooleanProperty(StringUtils.isNotBlank(reasonWhyControlInvalid.get()));
		reasonWhyControlInvalid.addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				showExclamation.set(StringUtils.isNotBlank(newValue));
			}
		});
		
		exclamation.visibleProperty().bind(showExclamation);
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(reasonWhyControlInvalid);
		Tooltip.install(exclamation, tooltip);
		
		stackPane.setMaxWidth(Double.MAX_VALUE);
		stackPane.getChildren().add(initialControl);
		StackPane.setAlignment(initialControl, Pos.CENTER_LEFT);
		stackPane.getChildren().add(exclamation);
		StackPane.setAlignment(exclamation, Pos.CENTER_RIGHT);
		double insetFromRight = (initialControl instanceof ComboBox ? 30.0 : 5.0);
		StackPane.setMargin(exclamation, new Insets(0.0, insetFromRight, 0.0, 0.0));
		return stackPane;
	}
	
	/**
	 * Setup an 'INFORMATION' info marker on the component.  Automatically displays anytime that the initialControl is disabled.
	 */
	public static Node setupDisabledInfoMarker(Control initialControl, StringProperty reasonWhyControlDisabled)
	{
		return setupDisabledInfoMarker(initialControl, new StackPane(), reasonWhyControlDisabled);
	}
	
	/**
	 * Setup an 'INFORMATION' info marker on the component.  Automatically displays anytime that the initialControl is disabled.
	 * Put the initial control in the provided stack pane
	 */
	public static Node setupDisabledInfoMarker(Control initialControl, StackPane stackPane, StringProperty reasonWhyControlDisabled)
	{
		ImageView information = Images.INFORMATION.createImageView();
		
		information.visibleProperty().bind(initialControl.disabledProperty());
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(reasonWhyControlDisabled);
		Tooltip.install(information, tooltip);
		
		stackPane.setMaxWidth(Double.MAX_VALUE);
		stackPane.getChildren().add(initialControl);
		StackPane.setAlignment(initialControl, Pos.CENTER_LEFT);
		stackPane.getChildren().add(information);
		if (initialControl instanceof Button)
		{
			StackPane.setAlignment(information, Pos.CENTER);
		}
		else if (initialControl instanceof CheckBox)
		{
			StackPane.setAlignment(information, Pos.CENTER_LEFT);
			StackPane.setMargin(information, new Insets(0, 0, 0, 1));
		}
		else
		{
			StackPane.setAlignment(information, Pos.CENTER_RIGHT);
			double insetFromRight = (initialControl instanceof ComboBox ? 30.0 : 5.0);
			StackPane.setMargin(information, new Insets(0.0, insetFromRight, 0.0, 0.0));
		}
		return stackPane;
	}
	
	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane 
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target 
	 * GridPane properties, but it takes everything.
	 */
	public static void swapComponents(Node placedNode, Node replacementNode, GridPane gp)
	{
		int index = gp.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("placed Node is not in the grid pane");
		}

		gp.getChildren().remove(index);
		gp.getChildren().add(index, replacementNode);
		
		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
	}
}
