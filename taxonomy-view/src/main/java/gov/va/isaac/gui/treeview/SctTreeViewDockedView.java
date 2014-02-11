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
package gov.va.isaac.gui.treeview;

import gov.va.isaac.gui.interfaces.DockedViewI;
import gov.va.isaac.gui.interfaces.MenuItemI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.jvnet.hk2.annotations.Service;

/**
 * SctTreeViewDockedView
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
@Singleton
public class SctTreeViewDockedView  implements DockedViewI 
{
	private SctTreeView sctTreeView_;
	
	private SctTreeViewDockedView()
	{
		sctTreeView_ = new SctTreeView();
		sctTreeView_.init(Taxonomies.SNOMED.getUuids()[0]);
	}
	
	public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) 
	{
		sctTreeView_.showConcept(conceptUUID, workingIndicator);
	}
	
	/**
	 * @see gov.va.isaac.gui.interfaces.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		return new ArrayList<MenuItemI>();
	}
	/**
	 * @see gov.va.isaac.gui.interfaces.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
		return sctTreeView_.getWrapperWindow();
	}
	/**
	 * @see gov.va.isaac.gui.interfaces.DockedViewI#getMenuBarMenuToShowView()
	 */
	@Override
	public MenuItemI getMenuBarMenuToShowView()
	{
		return new MenuItemI()
		{
			
			@Override
			public void handleMenuSelection(Window parent)
			{
				//noop
			}
			
			@Override
			public int getSortOrder()
			{
				return 6;
			}
			
			@Override
			public String getParentMenuId()
			{
				return "panelsMenu";
			}
			
			@Override
			public String getMenuName()
			{
				return "TAXONOMY VIEWER";
			}
			
			@Override
			public String getMenuId()
			{
				return "taxonomyViewerMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
		};
	}
	/**
	 * @see gov.va.isaac.gui.interfaces.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle()
	{
		return "Snomed Browser";
	}
}
