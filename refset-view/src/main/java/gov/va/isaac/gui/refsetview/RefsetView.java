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
package gov.va.isaac.gui.refsetview;

import gov.va.isaac.gui.refsetview.RefsetViewController;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.views.RefsetViewI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.scene.layout.Region;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * Refset View
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@PerLookup
public class RefsetView implements RefsetViewI
{
	private RefsetViewController rvc_;
	
	private RefsetView() throws IOException
	{
		//created by HK2
		rvc_ = RefsetViewController.init();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView()
	{		
		return rvc_.getRoot();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		// We don't currently have any custom menus with this view
		return new ArrayList<MenuItemI>();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefsetViewI#setRefsetAndComponent(java.util.UUID, java.util.UUID)
	 */
	@Override
	public void setRefsetAndComponent(UUID refsetUUID, UUID componentId)
	{
		rvc_.setRefsetAndComponent(refsetUUID, componentId);
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.RefsetViewI#setViewActiveOnly(boolean)
	 */
	@Override
	public void setViewActiveOnly(boolean activeOnly)
	{
		rvc_.setViewActiveOnly(activeOnly);
		
	}
}
