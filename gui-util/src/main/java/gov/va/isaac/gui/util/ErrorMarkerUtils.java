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

import gov.va.isaac.util.ValidBooleanBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
	 * @deprecated Use {@link #setupErrorMarker(Control, StackPane, ValidBooleanBinding)} instead
	 */
	public static Node setupErrorMarker(Control initialControl, ObservableStringValue reasonWhyControlInvalid)
	{
		return setupErrorMarker(initialControl, new StackPane(), reasonWhyControlInvalid);
	}
	
	/**
	 * @deprecated Use {@link #setupErrorMarker(Control, StackPane, ValidBooleanBinding)} instead
	 */
	public static Node setupErrorMarker(Control initialControl, StackPane stackPane, ObservableStringValue reasonWhyControlInvalid)
	{
		ValidBooleanBinding binding = new ValidBooleanBinding()
		{
			{
				bind(reasonWhyControlInvalid);
			}
			
			@Override
			protected boolean computeValue()
			{
				if (StringUtils.isNotBlank(reasonWhyControlInvalid.get()))
				{
					setInvalidReason(reasonWhyControlInvalid.get());
					return false;
				}
				else
				{
					clearInvalidReason();
					return true;
				}
			}
		};
		
		return setupErrorMarker(initialControl, stackPane, binding);
	}

	/**
	 * Setup an 'EXCLAMATION' error marker on the component. Automatically displays anytime that the reasonWhyControlInvalid value
	 * is false. Hides when the isControlCurrentlyValid is true.
	 * @param stackPane - optional - created if necessary
	 */
	public static StackPane setupErrorMarker(Node initialNode, StackPane stackPane, ValidBooleanBinding isNodeCurrentlyValid)
	{
		ImageView exclamation = Images.EXCLAMATION.createImageView();
		
		if (stackPane == null)
		{
			stackPane = new StackPane();
		}

		exclamation.visibleProperty().bind(isNodeCurrentlyValid.not());
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(isNodeCurrentlyValid.getReasonWhyInvalid());
		Tooltip.install(exclamation, tooltip);
		tooltip.setAutoHide(true);
		
		exclamation.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				tooltip.show(exclamation, event.getScreenX(), event.getScreenY());
				
			}
			
		});

		stackPane.setMaxWidth(Double.MAX_VALUE);
		stackPane.getChildren().add(initialNode);
		StackPane.setAlignment(initialNode, Pos.CENTER_LEFT);
		stackPane.getChildren().add(exclamation);
		StackPane.setAlignment(exclamation, Pos.CENTER_RIGHT);
		double insetFromRight;
		if (initialNode instanceof ComboBox)
		{
			insetFromRight = 30.0;
		}
		else if (initialNode instanceof ChoiceBox)
		{
			insetFromRight = 25.0;
		}
		else
		{
			insetFromRight = 5.0;
		}
		StackPane.setMargin(exclamation, new Insets(0.0, insetFromRight, 0.0, 0.0));
		return stackPane;
	}

	/**
	 * Setup an 'INFORMATION' info marker on the component. Automatically displays anytime that the initialControl is disabled.
	 */
	public static Node setupDisabledInfoMarker(Control initialControl, ObservableStringValue reasonWhyControlDisabled)
	{
		return setupDisabledInfoMarker(initialControl, new StackPane(), reasonWhyControlDisabled);
	}

	/**
	 * Setup an 'INFORMATION' info marker on the component. Automatically displays anytime that the initialControl is disabled.
	 * Put the initial control in the provided stack pane
	 */
	public static Node setupDisabledInfoMarker(Control initialControl, StackPane stackPane, ObservableStringValue reasonWhyControlDisabled)
	{
		ImageView information = Images.INFORMATION.createImageView();

		information.visibleProperty().bind(initialControl.disabledProperty());
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(reasonWhyControlDisabled);
		Tooltip.install(information, tooltip);
		tooltip.setAutoHide(true);
		
		information.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				tooltip.show(information, event.getScreenX(), event.getScreenY());
				
			}
			
		});

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
	 * A convenience method that sets up a new error marker using {@link #swapGridPaneComponents(Node, StackPane, GridPane)} and 
	 * {@link #setupErrorMarker(Node, StackPane, ValidBooleanBinding)} 
	 * @param prePlacedNode
	 * @param whereToPlace
	 * @param isNodeCurrentlyValid
	 */
	public static void setupErrorMarkerAndSwap(Node prePlacedNode, GridPane whereToPlace, ValidBooleanBinding isNodeCurrentlyValid)
	{
		StackPane newStack = new StackPane();
		swapGridPaneComponents(prePlacedNode, newStack, whereToPlace);
		setupErrorMarker(prePlacedNode, newStack, isNodeCurrentlyValid);
	}

	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * GridPane properties, but it takes everything.
	 * 
	 * @return the replacementNode
	 */
	public static Node swapGridPaneComponents(Node placedNode, Node replacementNode, GridPane gp)
	{
		int index = gp.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the grid pane");
		}

		gp.getChildren().remove(index);
		gp.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}

	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * VBox properties, but it takes everything.
	 * @return replacementNode
	 */
	public static StackPane swapVBoxComponents(Node placedNode, StackPane replacementNode, VBox vb)
	{
		int index = vb.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the vbox");
		}

		vb.getChildren().remove(index);
		vb.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}
	
	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * HBox properties, but it takes everything.
	 * @return replacementNode
	 */
	public static StackPane swapHBoxComponents(Node placedNode, StackPane replacementNode, HBox hb)
	{
		int index = hb.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the vbox");
		}

		hb.getChildren().remove(index);
		hb.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}
}
