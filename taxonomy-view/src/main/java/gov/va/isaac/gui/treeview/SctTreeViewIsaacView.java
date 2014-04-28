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

import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.ViewI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.Region;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * SctTreeViewDockedView
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
@PerLookup
public class SctTreeViewIsaacView  implements ViewI 
{
	private SctTreeView sctTreeView_;
	
	private SctTreeViewIsaacView()
	{
		sctTreeView_ = new SctTreeView();
	}
	
	public void init(UUID[] rootConcepts) 
	{
		sctTreeView_.init(rootConcepts);
	}
	
	public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) 
	{
		sctTreeView_.showConcept(conceptUUID, workingIndicator);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{
		return sctTreeView_.getView();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		return new ArrayList<MenuItemI>();
	}
}
