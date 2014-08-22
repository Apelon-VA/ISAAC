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
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.util.CommonMenuBuilderI;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;

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
		
		CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider() {
			@Override
			public Set<Integer> getNIds() {
				Set<Integer> nids = new HashSet<>();
				if (comboBox.getValue() != null) {
					nids.add(comboBox.getValue().getNid());
				}
				return nids;
			}
		};
		CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
		menuBuilder.setInvisibleWhenFalse(isComboBoxPopulated);
		CommonMenus.addCommonMenus(cm, menuBuilder, nidProvider);

		comboBox.setContextMenu(cm);

		AppContext.getService(DragRegistry.class).setupDragAndDrop(comboBox, new SingleConceptIdProvider()
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
