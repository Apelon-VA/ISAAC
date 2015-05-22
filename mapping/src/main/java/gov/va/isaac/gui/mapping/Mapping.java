package gov.va.isaac.gui.mapping;

import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.ApplicationMenus;
import gov.va.isaac.interfaces.gui.MenuItemI;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.DockedViewI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Service @Named(value=SharedServiceNames.DOCKED)
@Singleton
public class Mapping implements DockedViewI
{
	private boolean hasBeenInited_ = false;
	private MappingController mappingController_;
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private Mapping() throws IOException
	{
		//created by HK2
		LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
	}
	
	private MappingController getMappingController()
	{
		if (mappingController_ == null)
		{
			try
			{
				mappingController_ = MappingController.init();
			}
			catch (IOException e)
			{
				LOG.error("Unexpected", e);
				throw new RuntimeException(e);
			}
		}
		return mappingController_;
	}
	
	/**
	 * @see gov.va.isaac.interfaces.gui.views.DockedViewI#getView()
	 */
	@Override
	public Region getView() {
		return getMappingController().getRoot();
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
				if (!hasBeenInited_)
				{
					//delay init till first display
					getMappingController().refreshMappingSets();
					hasBeenInited_ = true;
				}
			}
			
			@Override
			public int getSortOrder()
			{
				return 20;
			}
			
			@Override
			public String getParentMenuId()
			{
				return ApplicationMenus.PANELS.getMenuId();
			}
			
			@Override
			public String getMenuName()
			{
				return "Mapping";
			}
			
			@Override
			public String getMenuId()
			{
				return "mappingMenuItem";
			}
			
			@Override
			public boolean enableMnemonicParsing()
			{
				return false;
			}
			
			@Override
			public Image getImage()
			{
				return Images.MAPPING.getImage();
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
		return "Mapping";
	}
	/**
	 * 
	 */
	public void refreshMappingSets()
	{
		getMappingController().refreshMappingSets();
	}
	
	public void refreshMappingItems()
	{
		getMappingController().refreshMappingItems();
	}
		
}
