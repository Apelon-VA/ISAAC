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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.gui.util.Images;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StatusCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class StatusCell extends TreeTableCell<RefexDynamicGUI, RefexDynamicGUI>
{
	private static Logger logger_ = LoggerFactory.getLogger(StatusCell.class);
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(RefexDynamicGUI item, boolean empty)
	{
		super.updateItem(item, empty);
		
		if (empty || item == null)
		{
			setText("");
			setGraphic(null);
		}
		else if (item != null)
		{
			String tooltipText = "";
			StackPane sp = new StackPane();
			sp.setPrefSize(25, 25);

			try
			{
				if (item.getRefex().isActive())
				{
					sizeAndPosition(Images.BLACK_DOT, sp, Pos.TOP_LEFT);
					tooltipText += "Active";
				}
				else
				{
					sizeAndPosition(Images.GREY_DOT, sp, Pos.TOP_LEFT);
					tooltipText += "Inactive";
				}
				
				if (!item.isCurrent())
				{
					sizeAndPosition(Images.HISTORICAL, sp, Pos.BOTTOM_LEFT);
					tooltipText += " and Historical";
				}
				else
				{
					tooltipText += " and Current";
				}
				
				if (item.getRefex().getTime() == Long.MAX_VALUE)
				{
					sizeAndPosition(Images.YELLOW_DOT, sp, Pos.TOP_RIGHT);
					tooltipText += " - Uncommitted";
				}
			}
			catch (Exception e)
			{
				logger_.error("Unexpected", e);
			}
			setGraphic(sp);
			setTooltip(new Tooltip(tooltipText));
		}
	}
	
	private void sizeAndPosition(Images image, StackPane sp, Pos position)
	{
		ImageView iv = image.createImageView();
		iv.setFitHeight(12);
		iv.setFitWidth(12);
		if (position == Pos.TOP_LEFT)
		{
			StackPane.setMargin(iv, new Insets(0, 0, 0, 0));
		}
		else if (position == Pos.TOP_RIGHT)
		{
			StackPane.setMargin(iv, new Insets(0, 0, 0, 13));
		}
		else if (position == Pos.BOTTOM_LEFT)
		{
			StackPane.setMargin(iv, new Insets(13, 0, 0, 0));
		}
		else if (position == Pos.BOTTOM_RIGHT)
		{
			StackPane.setMargin(iv, new Insets(13, 0, 0, 13));
		}
		else
		{
			throw new RuntimeException("Unsupported Position!");
		}
		sp.getChildren().add(iv);
		StackPane.setAlignment(iv, Pos.TOP_LEFT);
	}
}
