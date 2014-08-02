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
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
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
		SimpleIntegerProperty stringCount = new SimpleIntegerProperty(0);
		SimpleIntegerProperty numberCount = new SimpleIntegerProperty(0);
		SimpleIntegerProperty objectCount = new SimpleIntegerProperty(0);
		default public String[] getStrings() { return null; }

		default public Number[] getNumbers() { return null; }

		default public ObjectContainer[] getObjectContainers() { return null; }
		
		default public IntegerExpression getObservableStringCount()
		{
			return stringCount;
		}
		default public IntegerExpression getObservableObjectCount()
		{
			return objectCount;
		}
		default public IntegerExpression getObservableNumberCount()
		{
			return numberCount;
		}
		default public void invalidateAll()
		{
			String[] s = getStrings();
			stringCount.set(s == null ? 0 : s.length);
			
			Object[] o = getObjectContainers();
			objectCount.set(o == null ? 0 : o.length);
			
			Number[] n = getNumbers();
			numberCount.set(n == null ? 0 : n.length);
		}
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

	@FunctionalInterface
	public interface NIdProvider {
		SimpleIntegerProperty nidCount = new SimpleIntegerProperty(0);
		public Collection<Integer> getNIds();
		
		default public IntegerExpression getObservableNidCount()
		{
			return nidCount;
		}
		default public void invalidateAll()
		{
			Collection<Integer> nids = getNIds();
			nidCount.set(nids == null ? 0 : nids.size());
		}
	}

	public static CommonMenuBuilderI getDefaultMenuBuilder()
	{
		return new CommonMenuBuilder();
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
			DataProvider dataProvider, 
			NIdProvider nids)
	{
		CommonMenuBuilder builder = null;
		if (passedBuilder == null) {
			builder = new CommonMenuBuilder();
		} else if (! (passedBuilder instanceof CommonMenuBuilder)) {
			builder = new CommonMenuBuilder(passedBuilder);
		} else {
			builder = (CommonMenuBuilder)passedBuilder;
		}
		DataProvider dataProviderLocal = (dataProvider == null ? new DataProvider() {} : dataProvider);
		
		//Check the nid provider just before each display of the menu - and see if we have a nid or not.
		//If we don't have a nid, set the observable flag to false, so all of the menus that care, go invisible.
		//else, set to true, to menus that care about nids will be visible.
		existingMenu.setOnShowing((windowEvent) ->
		{
			nids.invalidateAll();
			dataProviderLocal.invalidateAll();
		});
		
		List<MenuItem> menuItems = getCommonMenus(builder, dataProviderLocal, nids);

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

	private static MenuItem createNewMenuItem(
			CommonMenuItem itemType, 
			CommonMenuBuilder builder, 
			BooleanSupplier canHandle, 
			BooleanExpression makeVisible,
			Runnable onHandlable) {
		return createNewMenuItem(itemType, builder, canHandle, makeVisible, onHandlable, null);
	}

	private static MenuItem createNewMenuItem(
			CommonMenuItem itemType, 
			CommonMenuBuilder builder, 
			BooleanSupplier canHandle, 
			BooleanExpression makeVisible,
			Runnable onHandlable,
			Runnable onNotHandleable) {
		
		if (builder.isCommonMenuItemExcluded(itemType))
		{
			return null;
		}
		MenuItem menuItem = new MenuItem(itemType.getText());
		menuItem.setGraphic(itemType.getImage().createImageView());
		menuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				if (canHandle.getAsBoolean())
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
		
		menuItem.visibleProperty().bind(builder.getInvisibleWhenfalse() == null ? makeVisible : builder.getInvisibleWhenfalse().not().and(makeVisible));
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

		// Menu item to show concept details.
		MenuItem enhancedConceptViewMenuItem = createNewMenuItem(
				CommonMenuItem.CONCEPT_VIEW,
				builder, 
				() -> {return nidProvider.getObservableNidCount().get() == 1;}, // canHandle
				nidProvider.getObservableNidCount().isEqualTo(1),				//make visible
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.CONCEPT_VIEW.getText() + "\" menu item to display concept with id \"" 
							+ nidProvider.getNIds().iterator().next() + "\"");

					PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, "ModernStyle");
					cv.setConcept(nidProvider.getNIds().iterator().next());
					cv.showView(null);
				},
				() -> { // onNotHandlable
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				});
		if (enhancedConceptViewMenuItem != null)
		{
			menuItems.add(enhancedConceptViewMenuItem);
		}

		// Menu item to show concept details. (legacy)
		MenuItem legacyConceptViewMenuItem = createNewMenuItem(
				CommonMenuItem.CONCEPT_VIEW_LEGACY,
				builder,
				() -> {return nidProvider.getObservableNidCount().get() == 1;}, // canHandle
				nidProvider.getObservableNidCount().isEqualTo(1),				//make visible
				() -> {
					LOG.debug("Using \"" + CommonMenuItem.CONCEPT_VIEW_LEGACY.getText() + "\" menu item to display concept with id \"" 
							+ nidProvider.getNIds().iterator().next() + "\"");

					PopupConceptViewI cv = AppContext.getService(PopupConceptViewI.class, "LegacyStyle");
					cv.setConcept(nidProvider.getNIds().iterator().next());

					cv.showView(null);
				},
				() -> {
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				}
				);
		if (legacyConceptViewMenuItem != null)
		{
			menuItems.add(legacyConceptViewMenuItem);
		}

		// Menu item to find concept in tree.
		MenuItem findInTaxonomyViewMenuItem = createNewMenuItem(
				CommonMenuItem.TAXONOMY_VIEW,
				builder,
				() -> {return nidProvider.getObservableNidCount().get() == 1;}, // canHandle
				nidProvider.getObservableNidCount().isEqualTo(1),				//make visible
				// onHandlable
				() -> { AppContext.getService(TaxonomyViewI.class).locateConcept(nidProvider.getNIds().iterator().next(), null); },
				// onNotHandlable
				() -> { AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't locate an invalid concept");});
		if (findInTaxonomyViewMenuItem != null)
		{
			menuItems.add(findInTaxonomyViewMenuItem);
		}

		// get Send-To menu items
		Menu sendToMenu = new Menu(CommonMenuItem.SEND_TO.getText());
		sendToMenu.setVisible(false);
		Task<List<MenuItem>> getSendToMenuItemsTask = new Task<List<MenuItem>>() {
			private List<MenuItem> items = null;
			
			@Override
			protected List<MenuItem> call() throws Exception {
				items = getSendToMenuItems(builder, dataProvider, nidProvider);

				return items;
			}

			@Override
			protected void succeeded() {
				super.succeeded();

				if (builder.isCommonMenuItemExcluded(CommonMenuItem.SEND_TO) || items == null || items.size() == 0) {
					sendToMenu.setVisible(false);
				} else {
					
					//Start at false
					BooleanBinding bb = new SimpleBooleanProperty(true).not();
					
					//bind to all visible properties
					for (MenuItem mi : items)
					{
						bb = bb.or(mi.visibleProperty());
					}
					sendToMenu.visibleProperty().bind(bb);
				}

				sendToMenu.getItems().addAll(items);
			}
		};
		Utility.execute(getSendToMenuItemsTask);

		menuItems.add(sendToMenu);

		// Get copy menu items
		// If no copyable data avail, then menu invisible
		Menu copyMenu = new Menu(CommonMenuItem.COPY.getText());
		copyMenu.setVisible(false);
		Task<List<MenuItem>> getCopyMenuItemsTask = new Task<List<MenuItem>>() {
			private List<MenuItem> items = null;

			@Override
			protected List<MenuItem> call() throws Exception {
				items = getCopyMenuItems(builder, dataProvider, nidProvider);
				
				return items;
			}

			@Override
			protected void succeeded() {
				super.succeeded();

				if (builder.isCommonMenuItemExcluded(CommonMenuItem.COPY) || items == null || items.size() == 0) {
					copyMenu.setVisible(false);
				} else {
					//Start at false
					BooleanBinding bb = new SimpleBooleanProperty(true).not();
					
					//bind to all visible properties
					for (MenuItem mi : items)
					{
						bb = bb.or(mi.visibleProperty());
					}
					copyMenu.visibleProperty().bind(bb);
				}

				copyMenu.getItems().addAll(items);
			}
		};
		Utility.execute(getCopyMenuItemsTask);
		
		menuItems.add(copyMenu);

		return menuItems;
	}

	private static List<MenuItem> getSendToMenuItems(CommonMenuBuilder builder, DataProvider dataProvider, NIdProvider nids) {
		// The following code is for the "Send To" submenu

		List<MenuItem> menuItems = new ArrayList<>();

		// Menu item to send Concept to ListView
		MenuItem listViewMenuItem = createNewMenuItem(
				CommonMenuItem.LIST_VIEW,
				builder,
				() -> {return nids.getObservableNidCount().get() > 0;}, // canHandle
				nids.getObservableNidCount().greaterThan(0),				//make visible
				() -> { // onHandlable
					ArrayList<Integer> nidList = new ArrayList<>();
					nidList.addAll(nids.getNIds());
					LOG.debug("Using \"" + CommonMenuItem.LIST_VIEW.getText() + "\" menu item to list concept(s) with id(s) \"" 
							+ Arrays.toString(nidList.toArray()) + "\"");

					ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);

					AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);

					lv.addConcepts(nidList);		
				},
				() -> {	 // onNotHandlable
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept or no concept selected", "Can only list valid concept(s)");
				}
				);
		if (listViewMenuItem != null)
		{
			menuItems.add(listViewMenuItem);
		}

		// Menu item to generate New Workflow Instance.
		MenuItem newWorkflowInstanceItem = createNewMenuItem(
				CommonMenuItem.WORKFLOW_VIEW,
				builder,
				() -> {return nids.getObservableNidCount().get() == 1;}, // canHandle
				nids.getObservableNidCount().isEqualTo(1),				//make visible
				() -> { // onHandlable
					ConceptWorkflowViewI view = AppContext.getService(ConceptWorkflowViewI.class);
					view.setConcept(nids.getNIds().iterator().next());
					view.showView(AppContext.getMainApplicationWindow().getPrimaryStage());
				},
				() -> { // onNotHandlable
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept or invalid number of Concepts selected", "Selection must be of exactly one valid Concept");
				}
				);
		if (newWorkflowInstanceItem != null)
		{
			menuItems.add(newWorkflowInstanceItem);
		}

		return menuItems;
	}

	private static List<MenuItem> getCopyMenuItems(CommonMenuBuilder builder, DataProvider dataProvider, NIdProvider nids) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();

		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		SeparatorMenuItem separator = new SeparatorMenuItem();

		MenuItem copyTextItem = createNewMenuItem(
				CommonMenuItem.COPY_TEXT,
				builder,
				() -> {	return dataProvider.getObservableStringCount().isEqualTo(1).get();},  // canHandle 
				dataProvider.getObservableStringCount().isEqualTo(1),	//make visible  
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.COPY_TEXT.getText() + "\" menu item to copy text \"" + dataProvider.getStrings()[0] + "\"");
					CustomClipboard.set(dataProvider.getStrings()[0]);
				}
				);
		if (copyTextItem != null)
		{
			menuItems.add(copyTextItem);
		}

		MenuItem copyContentItem = createNewMenuItem(
				CommonMenuItem.COPY_CONTENT,
				builder,
				() -> {return dataProvider.getObservableObjectCount().isEqualTo(1).get();},// canHandle
				dataProvider.getObservableObjectCount().isEqualTo(1), 	//make visible  
				() -> { // onHandlable
					LOG.debug("Using \"" + CommonMenuItem.COPY_CONTENT.getText() + "\" menu item to copy " + dataProvider.getObjectContainers()[0].getObject().getClass() + " object \"" + dataProvider.getObjectContainers()[0].getString() + "\"");
					CustomClipboard.set(dataProvider.getObjectContainers()[0].getObject(), dataProvider.getObjectContainers()[0].getString());
				}
				);
		if (copyContentItem != null)
		{
			menuItems.add(copyContentItem);
		}
		
		final boolean copyTextOrContentItemVisible = copyTextItem.isVisible() || copyContentItem.isVisible();
		
		// The following are ID-related and will be under a separator
		//TODO these UUID and SCTID lists are evaled at build time, not display time - tis a tricky one - as - we want to do this in a background
		//thread... we probably need to move this conversion into the dataProvider - and provide an observable over top of it... but the invalidate 
		//would need to cause it to recompute in a background thread, rather than foreground, like the other validators currently do...
		//Then, each of the following calls should be changed to pull data from the new observables, rather than this out-of-date cache / nid list
		
		ArrayList<UUID> uuids = new ArrayList<>();
		ArrayList<String> sctIds = new ArrayList<>();

		for (Integer i : nids.getNIds()) {
			ConceptVersionBI concept = null;
			concept = WBUtility.getConceptVersion(i);
			if (concept != null) {
				uuids.add(concept != null ? concept.getPrimordialUuid() : null);
				sctIds.add(concept != null ? ConceptViewerHelper.getSctId(ConceptViewerHelper.getConceptAttributes(concept)).trim() : null);
			}
		}

		// Menu item to copy SCT ID
		MenuItem copySctIdMenuItem = createNewMenuItem(
				CommonMenuItem.COPY_SCTID,
				builder,
				() -> {return sctIds != null && sctIds.size() == 1 && sctIds.get(0) != null;}, // canHandle
				nids.getObservableNidCount().isEqualTo(1),
				() -> { CustomClipboard.set(sctIds.get(0)); } // onHandlable
				);
		// Add menu separator IFF there were non-ID items AND this is the first ID item
		
		if (copySctIdMenuItem != null)
		{
			if (copyTextOrContentItemVisible && copySctIdMenuItem.isVisible() && ! menuItems.contains(separator)) {
				menuItems.add(separator);
			}
			menuItems.add(copySctIdMenuItem);
		}

		// Menu item to copy UUID
		MenuItem copyUuidMenuItem = createNewMenuItem(
				CommonMenuItem.COPY_UUID,
				builder,
				() -> {return uuids != null && uuids.size() == 1 && uuids.get(0) != null;}, // canHandle
				nids.getObservableNidCount().isEqualTo(1),
				() -> { CustomClipboard.set(uuids.get(0).toString()); } // onHandlable
				);
		if (copyUuidMenuItem != null)
		{
			// Add menu separator IFF there were non-ID items AND this is the first ID item
			if (copyTextOrContentItemVisible && copyUuidMenuItem.isVisible() && ! menuItems.contains(separator)) {
				menuItems.add(separator);
			}
			menuItems.add(copyUuidMenuItem);
		}
		
		// Menu item to copy NID
		MenuItem copyNidMenuItem = createNewMenuItem(
				CommonMenuItem.COPY_NID,
				builder,
				() -> {return nids.getObservableNidCount().get() == 1;}, // canHandle
				nids.getObservableNidCount().isEqualTo(1),				//make visible
				() -> { CustomClipboard.set(nids.getNIds().iterator().next(), new Integer(nids.getNIds().iterator().next()).toString()); } // onHandlable
				);
		if (copyNidMenuItem != null)
		{
			// Add menu separator IFF there were non-ID items AND this is the first ID item
			if (copyTextOrContentItemVisible && copyNidMenuItem.isVisible() && ! menuItems.contains(separator)) {
				menuItems.add(separator);
			}
			menuItems.add(copyNidMenuItem);
		}

		return menuItems;
	}
}