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

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
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
			try
			{
				if (item.isCurrent())
				{
					if (item.getRefex().isActive())
					{
						setText("\u2b24");  //big dot
						setTooltip(new Tooltip("Current and Active"));
					}
					else
					{
						setText("\u2d54");  //big circle
						setTooltip(new Tooltip("Current and Inactive"));
					}
				}
				else
				{
					if (item.getRefex().isActive())
					{
						setText("\u2022");  //small dot
						setTooltip(new Tooltip("Historical and Active"));
					}
					else
					{
						setText("\u25e6");  //small circle
						setTooltip(new Tooltip("Historical and Inactive"));
					}
				}
			}
			catch (Exception e)
			{
				logger_.error("Unexpected", e);
			}
		}
	}
}
