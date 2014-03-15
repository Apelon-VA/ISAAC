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
package gov.va.isaac.util;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dragAndDrop.ConceptIdProvider;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * {@link CommonMenus}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CommonMenus
{
	public static void addCommonMenus(ContextMenu cm, BooleanProperty invisibleWhenFalse, final ConceptIdProvider idProvider)
	{
		// Menu item to copy UUID.
		MenuItem mi0 = new MenuItem("Copy UUID");
		mi0.setGraphic(Images.COPY.createImageView());
		mi0.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				UUID id = idProvider.getConceptUUID();
				if (id != null)
				{
					CustomClipboard.set(id.toString());
				}
			}
		});
		if (invisibleWhenFalse != null)
		{
			mi0.visibleProperty().bind(invisibleWhenFalse);
		}
		cm.getItems().add(mi0);
		
		MenuItem mi1 = new MenuItem("Copy NID");
		mi1.setGraphic(Images.COPY.createImageView());
		mi1.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				String id = idProvider.getNid() + "";
				if (id != null)
				{
					CustomClipboard.set(id);
				}
			}
		});
		if (invisibleWhenFalse != null)
		{
			mi1.visibleProperty().bind(invisibleWhenFalse);
		}
		cm.getItems().add(mi1);

		// Menu item to show concept details.
		MenuItem mi2 = new MenuItem("View Concept");
		mi2.setGraphic(Images.CONCEPT_VIEW.createImageView());
		mi2.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				UUID id = idProvider.getConceptUUID();
				if (id != null)
				{
					AppContext.getCommonDialogs().showConceptDialog(id);
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				}
			}
		});
		if (invisibleWhenFalse != null)
		{
			mi2.visibleProperty().bind(invisibleWhenFalse);
		}
		cm.getItems().add(mi2);

		// Menu item to find concept in tree.
		MenuItem mi3 = new MenuItem("Find Concept in Taxonomy View");
		mi3.setGraphic(Images.ROOT.createImageView());
		mi3.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				UUID id = idProvider.getConceptUUID();
				if (id != null)
				{
					AppContext.getService(TaxonomyViewI.class).locateConcept(id, null);
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't locate an invalid concept");
				}
			}
		});
		if (invisibleWhenFalse != null)
		{
			mi3.visibleProperty().bind(invisibleWhenFalse);
		}
		cm.getItems().add(mi3);
	}
}
