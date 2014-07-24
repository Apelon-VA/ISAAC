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
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI;
import gov.va.isaac.interfaces.gui.views.ListBatchViewI;
import gov.va.isaac.interfaces.gui.views.PopupConceptViewI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CommonMenus}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CommonMenus
{
	public enum MergeMode {
		USE_EXISTING, // Will not replace existing menu items with matching text
		REPLACE_EXISTING, // Will replace existing menu items with matching text
		ADD_TO_EXISTING // Will add, ignoring any existing menu items with matching text
	}
	public enum CommonMenuItem {
		// These text values must be distinct
		// including across non-CommonMenu items that may exist on any passed ContextMenu
		CONCEPT_VIEW("View Concept"),
		CONCEPT_VIEW_LEGACY("View Concept 2"),
		TAXONOMY_VIEW("Find in Taxonomy View"),
		
		SEND_TO("Send To"),
			LIST_VIEW("List View"),
			WORKFLOW_VIEW("Workflow View"),
		
		COPY("Copy"),
			COPY_TEXT("Copy Text"),
			COPY_CONTENT("Copy Content"),
			
			COPY_SCTID("Copy SCTID"),
			COPY_UUID("Copy UUID"),
			COPY_NID("Copy NID");

		final String text;

		private CommonMenuItem(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(CommonMenus.class);

	public interface DataProvider {
		//default public String getString() { return null; }
		default public String[] getStrings() { return null; }

		//default public Number getNumber() { return null; }
		default public Number[] getNumbers() { return null; }

		//default public ObjectContainer getObjectContainer() { return null; }
		default public ObjectContainer[] getObjectContainers() { return null; }
	}

	public static class ObjectContainer {
		final Object object;
		final String string;

		public ObjectContainer(Object object, String string) {
			super();
			this.object = object;
			this.string = string;
		}
		public ObjectContainer(Object object) {
			super();
			this.object = object;
			this.string = object.toString();
		}

		/**
		 * @return the object
		 */
		public Object getObject() {
			return object;
		}
		/**
		 * @return the string
		 */
		public String getString() {
			return string;
		}
	}

	public interface NIdProvider {
		public default Set<Integer> getNIds() { return new HashSet<>(); }
	}

	private static int getNumVisibleMenuItems(List<MenuItem> items) {
		int count = 0;
		for (MenuItem item : items) {
			if (item.isVisible()) {
				++count;
			}
		}

		return count;
	}
	
	public static class CommonMenuBuilder implements CommonMenuBuilderI {
		CommonMenuItem[] menuItemsToExclude;
		MergeMode mergeMode = MergeMode.REPLACE_EXISTING;
		BooleanProperty invisibleWhenfalse;

		public static CommonMenuBuilderI newInstance() {
			return new CommonMenuBuilder();
		}
		
		private CommonMenuBuilder() {}
		
		private CommonMenuBuilder(CommonMenuBuilderI toCopy) {
			super();
			if (toCopy != null) {
				this.menuItemsToExclude = toCopy.getMenuItemsToExclude();
				this.mergeMode = toCopy.getMergeMode();
				this.invisibleWhenfalse = toCopy.getInvisibleWhenfalse();
			}
		}

		/* (non-Javadoc)
		 * @see gov.va.isaac.util.CommonMenuBuilderI#getMenuItemsToExclude()
		 */
		@Override
		public CommonMenuItem[] getMenuItemsToExclude() {
			return menuItemsToExclude;
		}
		/* (non-Javadoc)
		 * @see gov.va.isaac.util.CommonMenuBuilderI#setMenuItemsToExclude(gov.va.isaac.util.CommonMenus.CommonMenuItem)
		 */
		@Override
		public void setMenuItemsToExclude(CommonMenuItem...menuItemsToExclude) {
			this.menuItemsToExclude = menuItemsToExclude;
		}
		/* (non-Javadoc)
		 * @see gov.va.isaac.util.CommonMenuBuilderI#getMergeMode()
		 */
		@Override
		public MergeMode getMergeMode() {
			return mergeMode;
		}
		/* (non-Javadoc)
		 * @see gov.va.isaac.util.CommonMenuBuilderI#setMergeMode(gov.va.isaac.util.CommonMenus.MergeMode)
		 */
		@Override
		public void setMergeMode(MergeMode mergeMode) {
			this.mergeMode = mergeMode;
		}
		/* (non-Javadoc)
		 * @see gov.va.isaac.util.CommonMenuBuilderI#getInvisibleWhenfalse()
		 */
		@Override
		public BooleanProperty getInvisibleWhenfalse() {
			return invisibleWhenfalse;
		}
		/* (non-Javadoc)
		 * @see gov.va.isaac.util.CommonMenuBuilderI#setInvisibleWhenfalse(javafx.beans.property.BooleanProperty)
		 */
		@Override
		public void setInvisibleWhenfalse(BooleanProperty invisibleWhenfalse) {
			this.invisibleWhenfalse = invisibleWhenfalse;
		}
		
		private boolean isCommonMenuItemExcluded(CommonMenuItem item) {
			if (menuItemsToExclude != null) {
				for (CommonMenuItem itemToExclude : menuItemsToExclude) {
					if (item == itemToExclude) {
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
	public static void addCommonMenus(ContextMenu existingMenu, final DataProvider data) {
		addCommonMenus(existingMenu, null, data, null);
	}
	public static void addCommonMenus(ContextMenu existingMenu, CommonMenuBuilderI builder, final DataProvider data) {
		addCommonMenus(existingMenu, builder, data, null);
	}

	public static void addCommonMenus(ContextMenu existingMenu, final NIdProvider nids) {
		addCommonMenus(existingMenu, null, null, nids);
	}
	public static void addCommonMenus(ContextMenu existingMenu, CommonMenuBuilderI builder, final NIdProvider nids) {
		addCommonMenus(existingMenu, builder, null, nids);
	}

	public static void addCommonMenus(ContextMenu existingMenu, final DataProvider dataProvider, final NIdProvider nids) {
		addCommonMenus(existingMenu, null, dataProvider, nids);
	}
	public static void addCommonMenus(
			ContextMenu existingMenu, 
			CommonMenuBuilderI passedBuilder, 
			final DataProvider dataProvider, 
			final NIdProvider nids)
	{
		CommonMenuBuilder builder = null;
		if (passedBuilder == null) {
			builder = new CommonMenuBuilder();
		} else if (! (passedBuilder instanceof CommonMenuBuilder)) {
			builder = new CommonMenuBuilder(passedBuilder);
		} else {
			builder = (CommonMenuBuilder)passedBuilder;
		}
		
		List<MenuItem> menuItems = getCommonMenus(builder, dataProvider, nids);

		if (menuItems.size() > 0) {
			for (MenuItem newItem : menuItems) {
				MenuItem existingMatch = null;
				for (MenuItem existingItem : existingMenu.getItems()) {
					if (existingItem.getText().equals(newItem.getText())) {
						existingMatch = existingItem;

						break;
					}
				}

				switch (builder.getMergeMode()) {
				case ADD_TO_EXISTING:
					if (existingMatch != null) {
						Log.debug("Adding MenuItem with same name as existing MenuItem \"" + existingMatch.getText() + "\"");
					}
					existingMenu.getItems().add(newItem);
					break;
				case REPLACE_EXISTING:
					if (existingMatch != null) {
						Log.debug("Removing and replacing existing MenuItem \"" + existingMatch.getText() + "\"");
						existingMenu.getItems().remove(existingMatch);
					}
					existingMenu.getItems().add(newItem);
					break;
				case USE_EXISTING:
					if (existingMatch == null) {
						existingMenu.getItems().add(newItem);
					} else {
						Log.debug("Not adding MenuItem with same name as existing MenuItem \"" + existingMatch.getText() + "\"");
					}
					break;
				default:
					throw new RuntimeException("Unsupported enum value " + builder.getMergeMode() + " for " + MergeMode.class.getName());
				}
			}
		}
	}

	public static List<MenuItem> getCommonMenus(CommonMenuBuilderI passedBuilder, final DataProvider dataProvider, final NIdProvider nidProvider)
	{
		List<MenuItem> menuItems = new ArrayList<>();
		
		CommonMenuBuilder builder = null;
		if (passedBuilder == null) {
			builder = new CommonMenuBuilder();
		} else if (! (passedBuilder instanceof CommonMenuBuilder)) {
			builder = new CommonMenuBuilder(passedBuilder);
		} else {
			builder = (CommonMenuBuilder)passedBuilder;
		}
		
		Integer[] nids = nidProvider.getNIds().toArray(new Integer[nidProvider.getNIds().size()]);

		// Menu item to show concept details.
		MenuItem enhancedConceptViewMenuItem = new MenuItem(CommonMenuItem.CONCEPT_VIEW.getText());
		enhancedConceptViewMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
		enhancedConceptViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				Integer id = nids[0];
				if (id != null)
				{
					LOG.debug("Using \"" + CommonMenuItem.CONCEPT_VIEW.getText() + "\" menu item to display concept with id \"" + id + "\"");

					PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, "ModernStyle");
					cv.setConcept(id);

					cv.showView(AppContext.getMainApplicationWindow().getPrimaryStage().getScene().getWindow());
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			enhancedConceptViewMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.CONCEPT_VIEW) || nids == null || nids.length != 1) {
				enhancedConceptViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(enhancedConceptViewMenuItem);
		
		// Menu item to show concept details. (legacy)
		// TODO - bad dan, ugly copy-paste stuff.
		MenuItem legacyConceptViewMenuItem = new MenuItem(CommonMenuItem.CONCEPT_VIEW_LEGACY.getText());
		legacyConceptViewMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
		legacyConceptViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				Integer id = nids[0];
				if (id != null)
				{
					LOG.debug("Using \"" + CommonMenuItem.CONCEPT_VIEW_LEGACY.getText() + "\" menu item to display concept with id \"" + id + "\"");

					PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, "LegacyStyle");
					cv.setConcept(id);

					cv.showView(AppContext.getMainApplicationWindow().getPrimaryStage().getScene().getWindow());
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			legacyConceptViewMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.CONCEPT_VIEW) || nids == null || nids.length != 1) {
				legacyConceptViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(legacyConceptViewMenuItem);

		// Menu item to find concept in tree.
		MenuItem findInTaxonomyViewMenuItem = new MenuItem(CommonMenuItem.TAXONOMY_VIEW.getText());
		findInTaxonomyViewMenuItem.setGraphic(Images.ROOT.createImageView());
		findInTaxonomyViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0)
			{

				if (nids != null && nids.length == 1)
				{
					AppContext.getService(TaxonomyViewI.class).locateConcept(nids[0], null);
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't locate an invalid concept");
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			findInTaxonomyViewMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.TAXONOMY_VIEW) || nids == null || nids.length != 1) {
				findInTaxonomyViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(findInTaxonomyViewMenuItem);

		List<MenuItem> sendToMenuItems = getSendToMenuItems(builder, dataProvider, nids);
		// ContextMenu already has view actions, so possibly add submenu
			// Copy menu items exist, so add in submenu
		Menu sendToMenu = new Menu(CommonMenuItem.SEND_TO.getText());
		sendToMenu.getItems().addAll(sendToMenuItems);
		if (builder.isCommonMenuItemExcluded(CommonMenuItem.SEND_TO) || getNumVisibleMenuItems(sendToMenuItems) == 0) {
			sendToMenu.setVisible(false);
		}
		menuItems.add(sendToMenu);
		
		// Get copy menu items
		// If no copyable data avail, then don't add menu
		// If no other ContextMenu items, then add as items, not as submenu
		List<MenuItem> copyMenuItems = getCopyMenuItems(builder, dataProvider, nids);

		// ContextMenu already has view actions, so possibly add submenu
		// Copy menu items exist, so add in submenu
		Menu copyMenu = new Menu(CommonMenuItem.COPY.getText());
		copyMenu.getItems().addAll(copyMenuItems);
		if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY) || getNumVisibleMenuItems(copyMenuItems) == 0) {
			copyMenu.setVisible(false);
		}
		menuItems.add(copyMenu);


		return menuItems;
	}

	private static List<MenuItem> getSendToMenuItems(CommonMenuBuilder builder, DataProvider dataProvider, final Integer...nids) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();

		// Menu item to show concept details.
		MenuItem listViewMenuItem = new MenuItem(CommonMenuItem.LIST_VIEW.getText());
		listViewMenuItem.setGraphic(Images.LIST_VIEW.createImageView());
		listViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (nids != null && nids.length > 0)
				{
					LOG.debug("Using \"List View\" menu item to list concept(s) with id(s) \"" + Arrays.toString(nids) + "\"");

					ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);

					AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);

					List<Integer> nidList = new ArrayList<>();
					for (int nid : nids) {
						nidList.add(nid);
					}
					lv.addConcepts(nidList);		
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't list invalid concept(s)");
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			listViewMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.LIST_VIEW) || nids == null || nids.length == 0) {
				listViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(listViewMenuItem);


		// Menu item to generate New Workflow Instance.
		MenuItem newWorkflowInstanceItem = new MenuItem(CommonMenuItem.WORKFLOW_VIEW.getText());
		newWorkflowInstanceItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
		newWorkflowInstanceItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				Integer id = nids[0];
				if (id != null)
				{
					ConceptWorkflowViewI view = AppContext.getService(ConceptWorkflowViewI.class);
					view.setConcept(id);
					view.showView(null);
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't create workflow for invalid concept");
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			newWorkflowInstanceItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.WORKFLOW_VIEW) || nids == null || nids.length != 1) {
				newWorkflowInstanceItem.setVisible(false);
			}
		}
		menuItems.add(newWorkflowInstanceItem);


		return menuItems;
	}

	private static List<MenuItem> getCopyMenuItems(CommonMenuBuilder builder, DataProvider dataProvider, final Integer...nids) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();

		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		SeparatorMenuItem separator = new SeparatorMenuItem();

		MenuItem copyTextItem = new MenuItem(CommonMenuItem.COPY_TEXT.getText());
		copyTextItem.setGraphic(Images.COPY.createImageView());
		copyTextItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				LOG.debug("Using \"" + CommonMenuItem.COPY_TEXT.getText() + "\" menu item to copy text \"" + dataProvider.getStrings()[0] + "\"");
				CustomClipboard.set(dataProvider.getStrings()[0]);
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			copyTextItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY_TEXT) || dataProvider == null || dataProvider.getStrings() == null || dataProvider.getStrings().length != 1 || dataProvider.getStrings()[0] == null) {
				copyTextItem.setVisible(false);
			}
		}
		menuItems.add(copyTextItem);

		MenuItem copyContentItem = new MenuItem(CommonMenuItem.COPY_CONTENT.getText());
		copyContentItem.setGraphic(Images.COPY.createImageView());
		copyContentItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				LOG.debug("Using \"" + CommonMenuItem.COPY_CONTENT.getText() + "\" menu item to copy " + dataProvider.getObjectContainers()[0].getObject().getClass() + " object \"" + dataProvider.getObjectContainers()[0].getString() + "\"");
				CustomClipboard.set(dataProvider.getObjectContainers()[0].getObject(), dataProvider.getObjectContainers()[0].getString());
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			copyContentItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY_CONTENT) || dataProvider == null || dataProvider.getObjectContainers() == null || dataProvider.getObjectContainers().length != 1 || dataProvider.getObjectContainers()[0] == null) {
				copyContentItem.setVisible(false);
			}
		}
		menuItems.add(copyContentItem);
		
		boolean separatorNecessary = copyTextItem.isVisible() || copyContentItem.isVisible();
		
		// The following are ID-related and will be under a separator
		UUID[] uuids = new UUID[nids.length];
		String[] sctIds = new String[nids.length];

		for (int i = 0; i < nids.length; ++i) {
			ConceptVersionBI concept = null;
			concept = WBUtility.getConceptVersion(nids[i]);
			if (concept != null) {
				uuids[i] = concept != null ? concept.getPrimordialUuid() : null;
				sctIds[i] = concept != null ? ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(concept)).trim() : null;
			}
		}

		// Menu item to copy SCT ID
		MenuItem copySctIdMenuItem = new MenuItem(CommonMenuItem.COPY_SCTID.getText());
		copySctIdMenuItem.setGraphic(Images.COPY.createImageView());
		copySctIdMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (sctIds != null && sctIds.length == 1 && sctIds[0] != null)
				{
					CustomClipboard.set(sctIds[0]);
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			copySctIdMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY_SCTID) || sctIds == null || sctIds.length != 1 || sctIds[0] == null) {
				copySctIdMenuItem.setVisible(false);
			}
		}

		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (separatorNecessary && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copySctIdMenuItem);


		// Menu item to copy UUID.
		MenuItem copyUuidMenuItem = new MenuItem(CommonMenuItem.COPY_UUID.getText());
		copyUuidMenuItem.setGraphic(Images.COPY.createImageView());
		copyUuidMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (uuids != null && uuids.length == 1 && uuids[0] != null)
				{
					CustomClipboard.set(uuids[0].toString());
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			copyUuidMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY_UUID) || uuids == null || uuids.length != 1 || uuids[0] == null) {
				copyUuidMenuItem.setVisible(false);
			}
		}

		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (separatorNecessary && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copyUuidMenuItem);


		MenuItem copyNidMenuItem = new MenuItem(CommonMenuItem.COPY_NID.getText());
		copyNidMenuItem.setGraphic(Images.COPY.createImageView());
		copyNidMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (nids != null && nids.length == 1)
				{
					CustomClipboard.set(nids[0], new Integer(nids[0]).toString());
				}
			}
		});
		if (builder.getInvisibleWhenfalse() != null)
		{
			copyNidMenuItem.visibleProperty().bind(builder.getInvisibleWhenfalse());
		} else {
			if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY_NID) || nids == null || nids.length != 1) {
				copyNidMenuItem.setVisible(false);
			}
		}

		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (separatorNecessary && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copyNidMenuItem);


		return menuItems;
	}
}
