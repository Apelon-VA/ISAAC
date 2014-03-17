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
package gov.va.isaac.gui;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.dragAndDrop.ConceptIdProvider;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.WBUtility;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * {@link ComboBoxSetupTool}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ComboBoxSetupTool
{
	/**
	 * Make the combobox draggable, add menus
	 * 
	 * Returns a BooleanProperty that indicates true when the combobox contains items
	 * @param comboBox
	 */
	public static BooleanProperty setupComboBox(final ComboBox<SimpleDisplayConcept> comboBox)
	{
		ContextMenu cm = new ContextMenu();

		final BooleanProperty isComboBoxPopulated = new SimpleBooleanProperty(comboBox.getItems().size() > 0);

		comboBox.getItems().addListener(new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(ListChangeListener.Change<? extends SimpleDisplayConcept> c)
			{
				isComboBoxPopulated.set(comboBox.getItems().size() > 0);
			}
		});

		CommonMenus.addCommonMenus(cm, isComboBoxPopulated, new ConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				return comboBox.getValue().getNid() + "";
			}

			/**
			 * @see gov.va.isaac.gui.dragAndDrop.ConceptIdProvider#getConceptUUID()
			 */
			@Override
			public UUID getConceptUUID()
			{
				ConceptVersionBI c = WBUtility.getConceptVersion(getNid());
				if (c == null)
				{
					return null;
				}
				return c.getPrimordialUuid();
			}

			/**
			 * @see gov.va.isaac.gui.dragAndDrop.ConceptIdProvider#getNid()
			 */
			@Override
			public int getNid()
			{
				return comboBox.getValue().getNid();
			}
		});

		comboBox.setContextMenu(cm);

		AppContext.getService(DragRegistry.class).setupDragAndDrop(comboBox, new ConceptIdProvider()
		{
			@Override
			public String getConceptId()
			{
				return comboBox.getValue().getNid() + "";
			}
		}, false);
		return isComboBoxPopulated;
	}

}
