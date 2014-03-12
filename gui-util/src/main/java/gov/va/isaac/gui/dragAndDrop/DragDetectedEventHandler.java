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
package gov.va.isaac.gui.dragAndDrop;

import gov.va.isaac.AppContext;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * {@link DragDetectedEventHandler}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DragDetectedEventHandler implements EventHandler<MouseEvent>
{
	private Node node_;
	private ConceptIdProvider idProvider_;
	public DragDetectedEventHandler(Node n, ConceptIdProvider idProvider)
	{
		node_ = n;
		idProvider_ = idProvider;
	}
	
	/**
	 * @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override
	public void handle(MouseEvent event)
	{
		/* drag was detected, start a drag-and-drop gesture */
		/* allow any transfer mode */
		if (node_ != null)
		{
			Dragboard db = node_.startDragAndDrop(TransferMode.COPY);

			/* Put a string on a dragboard */
			String drag = idProvider_.getConceptId();
			if (drag != null && drag.length() > 0)
			{
				ClipboardContent content = new ClipboardContent();
				content.putString(drag);
				db.setContent(content);
				AppContext.getService(DragRegistry.class).conceptDragStarted();
				event.consume();
			}
		}
	}
}
