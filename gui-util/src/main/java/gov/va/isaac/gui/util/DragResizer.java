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
package gov.va.isaac.gui.util;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * {@link DragResizer} can be used to add mouse listeners to a {@link Region} and make it resizable by the user by clicking 
 * and dragging the border in the same way as a window.
 * <p>
 * Only height resizing is currently implemented. Usage:
 * 
 * <pre>
 * DragResizer.makeResizable(myAnchorPane);
 * </pre>
 * 
 * @author atill
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> borrowed this from 
 * http://andrewtill.blogspot.com/2012/12/dragging-to-resize-javafx-region.html  which is sourcing github:
 * https://gist.github.com/andytill/4369729#file-dragresizer-java
 */
public class DragResizer
{
	/**
	 * The margin around the control that a user can click in to start resizing
	 * the region.
	 */
	private static final int RESIZE_MARGIN = 5;

	private final Region region;

	private double y;
	private boolean initMinHeight;
	private boolean dragging;

	private DragResizer(Region aRegion)
	{
		region = aRegion;
	}

	public static void makeResizable(Region region)
	{
		final DragResizer resizer = new DragResizer(region);
		region.setOnMousePressed(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				resizer.mousePressed(event);
			}
		});
		region.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				resizer.mouseDragged(event);
			}
		});
		region.setOnMouseMoved(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				resizer.mouseOver(event);
			}
		});
		region.setOnMouseReleased(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				resizer.mouseReleased(event);
			}
		});
	}

	protected void mouseReleased(MouseEvent event)
	{
		dragging = false;
		region.setCursor(Cursor.DEFAULT);
	}

	protected void mouseOver(MouseEvent event)
	{
		if (isInDraggableZone(event) || dragging)
		{
			region.setCursor(Cursor.S_RESIZE);
		}
		else
		{
			region.setCursor(Cursor.DEFAULT);
		}
	}

	protected boolean isInDraggableZone(MouseEvent event)
	{
		return event.getY() > (region.getHeight() - RESIZE_MARGIN);
	}

	protected void mouseDragged(MouseEvent event)
	{
		if (!dragging)
		{
			return;
		}
		double mousey = event.getY();
		double newHeight = region.getMinHeight() + (mousey - y);

		region.setMinHeight(newHeight);
		y = mousey;
	}

	protected void mousePressed(MouseEvent event)
	{
		// ignore clicks outside of the draggable margin
		if (!isInDraggableZone(event))
		{
			return;
		}
		dragging = true;
		// make sure that the minimum height is set to the current height once,
		// setting a min height that is smaller than the current height will
		// have no effect
		if (!initMinHeight)
		{
			region.setMinHeight(region.getHeight());
			initMinHeight = true;
		}
		y = event.getY();
	}
}
