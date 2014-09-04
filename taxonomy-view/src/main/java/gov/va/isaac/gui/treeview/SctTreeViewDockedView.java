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

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import gov.va.isaac.interfaces.treeview.SctTreeItemDisplayPolicies;
import gov.va.isaac.util.WBUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

/**
 * SctTreeViewDockedView
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Service
@Singleton
public class SctTreeViewDockedView  implements DockedViewI, TaxonomyViewI 
{
	private SctTreeView sctTreeView_;
	private boolean hasBeenInited_ = false;
	
	private SctTreeViewDockedView()
	{
		sctTreeView_ = new SctTreeView();
	}
	
	public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) 
	{
		sctTreeView_.showConcept(conceptUUID, workingIndicator);
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		return new ArrayList<MenuItemI>();
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
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getMenuBarMenuToShowView()
	 */
	@Override
	public MenuItemI getMenuBarMenuToShowView()
	{
		return new MenuItemI()
		{
			
			@Override
			public void handleMenuSelection(Window parent)
			{
				if (!hasBeenInited_)
				{
					//delay init till first display
					sctTreeView_.init();
					hasBeenInited_ = true;
				}
			}
			
			@Override
			public int getSortOrder()
			{
				return 6;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.PANELS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Taxonomy Viewer";
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

			/**
			 * @see gov.va.isaac.interfaces.gui.MenuItemI#getImage()
			 */
			@Override
			public Image getImage()
			{
				return Images.ROOT.getImage();
			}

		};
	}
	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle()
	{
		return "Snomed Browser";
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.TaxonomyViewI#locateConcept(java.util.UUID, javafx.beans.property.BooleanProperty)
	 */
	@Override
	public void locateConcept(UUID uuid, BooleanProperty busyIndicator)
	{
		//TODO add a visible progress indicator while this happens
		showConcept(uuid, busyIndicator);
		AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(this);
	}

	/* 
	 * @see gov.va.isaac.interfaces.gui.TaxonomyViewI#locateConcept(int, javafx.beans.property.BooleanProperty)
	 */
	@Override
	public void locateConcept(int nid, BooleanProperty busyIndicator) {
		locateConcept(WBUtility.getConceptVersion(nid).getPrimordialUuid(), busyIndicator);
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.TaxonomyViewI#setDisplayPolicies(gov.va.isaac.interfaces.treeview.SctTreeItemDisplayPolicies)
	 */
	@Override
	public void setDisplayPolicies(SctTreeItemDisplayPolicies policies) {
		sctTreeView_.setDisplayPolicies(policies);
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.TaxonomyViewI#refresh()
	 */
	@Override
	public void refresh() {
		sctTreeView_.refresh();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.TaxonomyViewI#getDefaultDisplayPolicies()
	 */
	@Override
	public SctTreeItemDisplayPolicies getDefaultDisplayPolicies() {
		return SctTreeView.getDefaultDisplayPolicies();
	}
}
