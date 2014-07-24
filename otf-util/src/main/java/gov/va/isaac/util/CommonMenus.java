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
import javafx.concurrent.Task;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.isaac.interfaces.gui.views.PopupConceptViewI;

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
		CONCEPT_VIEW("View Concept", Images.CONCEPT_VIEW),
		CONCEPT_VIEW_LEGACY("View Concept 2", Images.CONCEPT_VIEW),
		TAXONOMY_VIEW("Find in Taxonomy View", Images.ROOT),
		
		SEND_TO("Send To", null),
			LIST_VIEW("List View", Images.LIST_VIEW),
			WORKFLOW_VIEW("Workflow View", Images.INBOX),
		
		COPY("Copy", null),
			COPY_TEXT("Copy Text", Images.COPY),
			COPY_CONTENT("Copy Content", Images.COPY),
			
			COPY_SCTID("Copy SCTID", Images.COPY),
			COPY_UUID("Copy UUID", Images.COPY),
			COPY_NID("Copy NID", Images.COPY);

		final String text;
		final Images image;

		private CommonMenuItem(String text, Images image) {
			this.text = text;
			this.image = image;
		}

		public String getText() {
			return text;
		}
		public Images getImage() {
			return image;
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

	private static void makeInvisibleIfNecessary(MenuItem item, BooleanProperty visibilityProperty, boolean shouldBeInvisible) {
		if (visibilityProperty != null)
		{
			item.visibleProperty().bind(visibilityProperty);
		} else {
			if (shouldBeInvisible) {
				item.setVisible(false);
			}
		}
	}
	
	private static MenuItem createNewMenuItem(
			CommonMenuItem itemType, 
			CommonMenuBuilder builder, 
			boolean isHandlable, 
			Runnable onHandlable) {
		return createNewMenuItem(itemType, builder, isHandlable, onHandlable, null);
	}
	private static MenuItem createNewMenuItem(
			CommonMenuItem itemType, 
			CommonMenuBuilder builder, 
			boolean isHandlable, 
			Runnable onHandlable,
			Runnable onNotHandleable) {
		MenuItem menuItem = new MenuItem(itemType.getText());
		menuItem.setGraphic(itemType.getImage().createImageView());
		menuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (isHandlable)
				{
					onHandlable.run();
				} else {
					if (onNotHandleable != null) {
						onNotHandleable.run();
					} else {
						AppContext.getCommonDialogs().showInformationDialog("Invalid data selected for menu item \"" + itemType.getText() + "\"", "Can't invoke " + itemType.getText() + " for invalid data");
					}
				}
			}
		});
		makeInvisibleIfNecessary(
				menuItem, 
				builder.getInvisibleWhenfalse(), 
				! isHandlable || builder.isCommonMenuItemExcluded(itemType));
		
		return menuItem;
	}
	
	public static List<MenuItem> getCommonMenus(CommonMenuBuilderI passedBuilder, final DataProvider dataProvider, final NIdProvider nidProvider)
	{
		List<MenuItem> menuItems = new ArrayList<>();
		
		CommonMenuBuilder tmpBuilder = null;
		if (passedBuilder == null) {
			tmpBuilder = new CommonMenuBuilder();
		} else if (! (passedBuilder instanceof CommonMenuBuilder)) {
			tmpBuilder = new CommonMenuBuilder(passedBuilder);
		} else {
			tmpBuilder = (CommonMenuBuilder)passedBuilder;
		}
		final CommonMenuBuilder builder = tmpBuilder;
		
		Integer[] nids = nidProvider.getNIds().toArray(new Integer[nidProvider.getNIds().size()]);

		// Menu item to show concept details.
		MenuItem enhancedConceptViewMenuItem = createNewMenuItem(
				CommonMenuItem.CONCEPT_VIEW, 
				builder, 
				nids != null && nids.length == 1 && nids[0] != null, // isHandlable
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.CONCEPT_VIEW.getText() + "\" menu item to display concept with id \"" + nids[0] + "\"");

					PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, "ModernStyle");

					//EnhancedConceptView cv = AppContext.getService(EnhancedConceptView.class);
					cv.setConcept(nids[0]);

					cv.showView(AppContext.getMainApplicationWindow().getPrimaryStage().getScene().getWindow());
				},
				() -> { // onNotHandlable
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				});
		menuItems.add(enhancedConceptViewMenuItem);

		// Menu item to show concept details. (legacy)
		// TODO - bad dan, ugly copy-paste stuff.
		MenuItem legacyConceptViewMenuItem = createNewMenuItem(
				CommonMenuItem.CONCEPT_VIEW_LEGACY,
				builder,
				nids != null && nids.length == 1 && nids[0] != null, // isHandlable
				() -> {
					LOG.debug("Using \"" + CommonMenuItem.CONCEPT_VIEW_LEGACY.getText() + "\" menu item to display concept with id \"" + nids[0] + "\"");

					PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, "LegacyStyle");
					cv.setConcept(nids[0]);

					cv.showView(AppContext.getMainApplicationWindow().getPrimaryStage().getScene().getWindow());
				},
				() -> {
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				}
				);
		menuItems.add(legacyConceptViewMenuItem);

		// Menu item to find concept in tree.
		MenuItem findInTaxonomyViewMenuItem = createNewMenuItem(
				CommonMenuItem.TAXONOMY_VIEW,
				builder,
				nids != null && nids.length == 1 && nids[0] != null, // isHandlable
				// onHandlable
				() -> { AppContext.getService(TaxonomyViewI.class).locateConcept(nids[0], null); },
				// onNotHandlable
				() -> { AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't locate an invalid concept");});
		menuItems.add(findInTaxonomyViewMenuItem);

		// get Send-To menu items
		Menu sendToMenu = new Menu(CommonMenuItem.SEND_TO.getText());
		sendToMenu.setVisible(false);
		Task<List<MenuItem>> getSendToMenuItemsTask = new Task<List<MenuItem>>() {
			@Override
			protected List<MenuItem> call() throws Exception {
				List<MenuItem> items = getSendToMenuItems(builder, dataProvider, nids);
				
				sendToMenu.getItems().addAll(items);

				if (builder.isCommonMenuItemExcluded(CommonMenuItem.SEND_TO) || getNumVisibleMenuItems(items) == 0) {
					sendToMenu.setVisible(false);
				} else {
					sendToMenu.setVisible(true);
				}

				return items;
			}
		};
		Utility.execute(getSendToMenuItemsTask);

		menuItems.add(sendToMenu);

		// Get copy menu items
		// If no copyable data avail, then menu invisible
		Menu copyMenu = new Menu(CommonMenuItem.COPY.getText());
		copyMenu.setVisible(false);
		Task<List<MenuItem>> getCopyMenuItemsTask = new Task<List<MenuItem>>() {
			@Override
			protected List<MenuItem> call() throws Exception {
				List<MenuItem> items = getCopyMenuItems(builder, dataProvider, nids);
				
				copyMenu.getItems().addAll(items);

				if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY) || getNumVisibleMenuItems(items) == 0) {
					copyMenu.setVisible(false);
				} else {
					copyMenu.setVisible(true);
				}

				return items;
			}
		};
		Utility.execute(getCopyMenuItemsTask);
		
		menuItems.add(copyMenu);

		return menuItems;
	}

	private static List<MenuItem> getSendToMenuItems(CommonMenuBuilder builder, DataProvider dataProvider, final Integer...nids) {
		// The following code is for the "Send To" submenu

		List<MenuItem> menuItems = new ArrayList<>();

		// Menu item to send Concept to ListView
		MenuItem listViewMenuItem = createNewMenuItem(
				CommonMenuItem.LIST_VIEW,
				builder,
				nids != null && nids.length > 0, // isHandlable
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.LIST_VIEW.getText() + "\" menu item to list concept(s) with id(s) \"" + Arrays.toString(nids) + "\"");

					ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);

					AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);

					List<Integer> nidList = new ArrayList<>();
					for (int nid : nids) {
						nidList.add(nid);
					}
					lv.addConcepts(nidList);		
				},
				() -> {	 // onNotHandlable
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept or no concept selected", "Can only list valid concept(s)");
				}
				);
		menuItems.add(listViewMenuItem);

		// Menu item to generate New Workflow Instance.
		MenuItem newWorkflowInstanceItem = createNewMenuItem(
				CommonMenuItem.WORKFLOW_VIEW,
				builder,
				nids != null && nids.length == 1 && nids[0] != null, // isHandlable
				() -> { // onHandlable
					ConceptWorkflowViewI view = AppContext.getService(ConceptWorkflowViewI.class);
					view.setConcept(nids[0]);
					view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
				},
				() -> { // onNotHandlable
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept or invalid number of Concepts selected", "Selection must be of exactly one valid Concept");
				}
				);
		menuItems.add(newWorkflowInstanceItem);

		return menuItems;
	}

	private static List<MenuItem> getCopyMenuItems(CommonMenuBuilder builder, DataProvider dataProvider, final Integer...nids) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();

		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		SeparatorMenuItem separator = new SeparatorMenuItem();

		MenuItem copyTextItem = createNewMenuItem(
				CommonMenuItem.COPY_TEXT,
				builder,
				dataProvider != null && dataProvider.getStrings() != null && dataProvider.getStrings().length == 1 && dataProvider.getStrings()[0] != null, // isHandlable
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.COPY_TEXT.getText() + "\" menu item to copy text \"" + dataProvider.getStrings()[0] + "\"");
					CustomClipboard.set(dataProvider.getStrings()[0]);
				}
				);
		menuItems.add(copyTextItem);

		MenuItem copyContentItem = createNewMenuItem(
				CommonMenuItem.COPY_CONTENT,
				builder,
				dataProvider != null && dataProvider.getObjectContainers() != null && dataProvider.getObjectContainers().length == 1 && dataProvider.getObjectContainers()[0] != null, // isHandlable
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.COPY_CONTENT.getText() + "\" menu item to copy " + dataProvider.getObjectContainers()[0].getObject().getClass() + " object \"" + dataProvider.getObjectContainers()[0].getString() + "\"");
					CustomClipboard.set(dataProvider.getObjectContainers()[0].getObject(), dataProvider.getObjectContainers()[0].getString());
				}
				);
		menuItems.add(copyContentItem);
		
		final boolean copyTextOrContentItemVisible = copyTextItem.isVisible() || copyContentItem.isVisible();
		
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
		MenuItem copySctIdMenuItem = createNewMenuItem(
				CommonMenuItem.COPY_SCTID,
				builder,
				sctIds != null && sctIds.length == 1 && sctIds[0] != null, // isHandlable
				() -> { CustomClipboard.set(sctIds[0]); } // onHandlable
				);
		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (copyTextOrContentItemVisible && copySctIdMenuItem.isVisible() && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copySctIdMenuItem);

		// Menu item to copy UUID
		MenuItem copyUuidMenuItem = createNewMenuItem(
				CommonMenuItem.COPY_UUID,
				builder,
				uuids != null && uuids.length == 1 && uuids[0] != null, // isHandlable
				() -> { CustomClipboard.set(uuids[0].toString()); } // onHandlable
				);
		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (copyTextOrContentItemVisible && copyUuidMenuItem.isVisible() && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copyUuidMenuItem);
		
		// Menu item to copy NID
		MenuItem copyNidMenuItem = createNewMenuItem(
				CommonMenuItem.COPY_NID,
				builder,
				nids != null && nids.length == 1 && nids[0] != null, // isHandlable
				() -> { CustomClipboard.set(nids[0], new Integer(nids[0]).toString()); } // onHandlable
				);
		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (copyTextOrContentItemVisible && copyNidMenuItem.isVisible() && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copyNidMenuItem);

		return menuItems;
	}
}