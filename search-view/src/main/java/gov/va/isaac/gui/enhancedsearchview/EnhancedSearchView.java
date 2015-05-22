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
package gov.va.isaac.gui.enhancedsearchview;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchView
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a> 
 */

@Service @Named(value=SharedServiceNames.DOCKED)
@Singleton
public class EnhancedSearchView implements DockedViewI
{
	private EnhancedSearchViewController svc_;
	private final Logger LOG = LoggerFactory.getLogger(EnhancedSearchView.class);
	
	protected EnhancedSearchView() throws IOException
	{
		//created by HK2
		LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
	}
	
	//TODO Dans big list of Enhanced Search View cleanup things.....
	//1) (artf231406) EnhancedSearchView is too wide and not shrinkable
	//2) (artf231407) Search and Search-criteria-save buttons disableProperty are properly bound, but buttons should have Tooltips describing why buttons are disabled
	//3) (artf231408) Styling into css files
	//4) (artf231410) Refactor to replace use of statics with proper use of HK2 in order to allow multiple instances - stop passing around references to components here and there and using static variables all over.  What if I want an enhanced search in a pop
	// up window to put on my second monitor?  The current use of static makes that impossible
	//5) (artf231412) fix the look - buttons aren't aligned, buttons jump around in size randomly, standard icons we are using elsewhere for things like add/remove haven't been used
	//6.1) (artf231412) Add filter button is in a bizarre place
	//6.2) (artf231412) Search button is in a bizarre place.
	//6.3) (artf231412) Typing enter doesn't perform a search.
	//7) (artf231412) Fix the default widths on the columns on the result table
	//8) Why would save search even be an option before you perform the search?  move it to the bottom (along with many other fixes for the bottom)
	//9) (artf231412) turn the bottom into a toolbar.  Handle results should be one 16x16 icon.  Clicking, presents a menu of options.  Same with restore searches.
	//10) (artf231412) Tooltips... where are they?  Features and functions should be self documenting if not obvious.
	//11) Reset Display Table?  How about "Clear Results"
	//12) (artf231413) Fix the SCTID column.  We need "Native ID", and proper handling for SCTID, Loinc and RxNorm
	//13) (artf231412) FSN / Term / Text.  I still can't figure out what on earth Text is supposed to be.  Is is supposed to be the matching description?
	//14) (artf231412) Refset Specification Search is a terrible name.  We aren't searching the content of a refset.  This should be called OTF Query Search, 
	//with a prominent option to store the search 
	//15) did we have the feature to turn an OTF Query result into a refset?  I can't find it.
	
	//TODO bug (artf231414) - Find in taxonomy is broken from the right-click context menu:
	//Exception in thread "JavaFX Application Thread" java.lang.IllegalArgumentException: Children: duplicate children added: parent = StackPane@5a7dd09f
	//at javafx.scene.Parent$2.onProposedChange(Parent.java:450)
	//at com.sun.javafx.collections.VetoableListDecorator.add(VetoableListDecorator.java:206)
	// gov.va.isaac.gui.treeview.SctTreeView.finishTreeSetup(SctTreeView.java:268)
	//TODO (artf231416) bug context menu Content Request menus aren't working in component/description display view mode - if you right click on FSN - and click content request, it send the wrong nid
	//sending the nid of the description, rather than the concept.
	//TODO (artf231416?) bug content request options need to better handle getting a nid that isn't a concept... currently, they just present a silly error to the users.
	//
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView() {
		//init had to be delayed, because the current init runs way to slow, and hits the DB in the JavaFX thread.
		if (svc_ == null)
		{
			synchronized (this)
			{
				if (svc_ == null)
				{
					try
					{
						svc_ = EnhancedSearchViewController.init();
					}
					catch (IOException e)
					{
						LOG.error("Unexpected error initializing the Search View", e);
						return new Label("oops - check logs");
					}
				}
			}
			
		}
		return svc_.getRoot();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.IsaacViewI#getMenuBarMenus()
	 */
	@Override
	public List<MenuItemI> getMenuBarMenus()
	{
		//We don't currently have any custom menus with this view
		return new ArrayList<MenuItemI>();
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getMenuBarMenuToShowView()
	 */
	@Override
	public MenuItemI getMenuBarMenuToShowView()
	{
		MenuItemI menuItem = new MenuItemI()
		{
			@Override
			public void handleMenuSelection(Window parent)
			{
				//noop
			}
			
			@Override
			public int getSortOrder()
			{
				return 5;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.PANELS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Enhanced Search";
			}
			
			@Override
			public String getMenuId()
			{
				return "enhancedSearchPanelMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
			
			@Override
			public Image getImage()
			{
				return Images.SEARCH.getImage();
			}
		};
		return menuItem;
	}

	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getViewTitle()
	 */
	@Override
	public String getViewTitle()
	{
		return "Enhanced Search";
	}
}
