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
import gov.va.isaac.gui.conceptViews.EnhancedConceptView;
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
		USE_EXISTING,
		REPLACE_EXISTING,
		ADD_TO_EXISTING
	}
	enum CommonMenuItems {
		CONCEPT_VIEW("View Concept"),
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

		private CommonMenuItems(String text) {
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

	public static class CommonMenuBuilder {
		MergeMode mergeMode;
		BooleanProperty invisibleWhenfalse;
		
	}
	
	public static void addCommonMenus(ContextMenu cm, BooleanProperty invisibleWhenFalse, final NIdProvider nids)
	{
		addCommonMenus(cm, MergeMode.ADD_TO_EXISTING, invisibleWhenFalse, null, nids);
	}
	public static void addCommonMenus(ContextMenu cm, MergeMode mergeMode, BooleanProperty invisibleWhenFalse, final NIdProvider nids)
	{
		addCommonMenus(cm, mergeMode, invisibleWhenFalse, null, nids);
	}
	public static void addCommonMenus(
			ContextMenu cm, 
			BooleanProperty invisibleWhenFalse, 
			final DataProvider dataProvider, 
			final NIdProvider nids)
	{
		addCommonMenus(cm, MergeMode.ADD_TO_EXISTING, invisibleWhenFalse, dataProvider, nids);
	}
	
	public static void addCommonMenus(
			ContextMenu existingMenu, 
			MergeMode mergeMode, 
			BooleanProperty invisibleWhenFalse, 
			final DataProvider dataProvider, 
			final NIdProvider nids)
	{
		List<MenuItem> menuItems = getCommonMenus(invisibleWhenFalse, dataProvider, nids);

		if (menuItems.size() > 0) {
			for (MenuItem newItem : menuItems) {
				MenuItem existingMatch = null;
				for (MenuItem existingItem : existingMenu.getItems()) {
					if (existingItem.getText().equals(newItem.getText())) {
						existingMatch = existingItem;

						break;
					}
				}

				switch (mergeMode) {
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
					throw new RuntimeException("Unsupported enum value " + mergeMode + " for " + MergeMode.class.getName());
				}
			}
		}
	}
	public static List<MenuItem> getCommonMenus(BooleanProperty invisibleWhenFalse, final DataProvider dataProvider, final NIdProvider nidProvider)
	{
		List<MenuItem> menuItems = new ArrayList<>();

		Integer[] nids = nidProvider.getNIds().toArray(new Integer[nidProvider.getNIds().size()]);

		// Menu item to show concept details.
		MenuItem enhancedConceptViewMenuItem = new MenuItem(CommonMenuItems.CONCEPT_VIEW.getText());
		enhancedConceptViewMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
		enhancedConceptViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event)
			{
				Integer id = nids[0];
				if (id != null)
				{
					LOG.debug("Using \"" + CommonMenuItems.CONCEPT_VIEW.getText() + "\" menu item to display concept with id \"" + id + "\"");

					EnhancedConceptView cv = AppContext.getService(EnhancedConceptView.class);
					cv.setConcept(id);

					cv.showView(AppContext.getMainApplicationWindow().getPrimaryStage().getScene().getWindow());
				}
				else
				{
					AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
				}
			}
		});
		if (invisibleWhenFalse != null)
		{
			enhancedConceptViewMenuItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (nids == null || nids.length != 1) {
				enhancedConceptViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(enhancedConceptViewMenuItem);

		// Menu item to find concept in tree.
		MenuItem findInTaxonomyViewMenuItem = new MenuItem(CommonMenuItems.TAXONOMY_VIEW.getText());
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
		if (invisibleWhenFalse != null)
		{
			findInTaxonomyViewMenuItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (nids == null || nids.length != 1) {
				findInTaxonomyViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(findInTaxonomyViewMenuItem);

		List<MenuItem> sendToMenuItems = getSendToMenuItems(invisibleWhenFalse, dataProvider, nids);
		// ContextMenu already has view actions, so possibly add submenu
		if (getNumVisibleMenuItems(sendToMenuItems) > 0) {
			// Copy menu items exist, so add in submenu
			Menu sendToMenu = new Menu(CommonMenuItems.SEND_TO.getText());
			sendToMenu.getItems().addAll(sendToMenuItems);
			menuItems.add(sendToMenu);
		}

		// Get copy menu items
		// If no copyable data avail, then don't add menu
		// If no other ContextMenu items, then add as items, not as submenu
		List<MenuItem> copyMenuItems = getCopyMenuItems(invisibleWhenFalse, dataProvider, nids);

		// ContextMenu already has view actions, so possibly add submenu
		if (getNumVisibleMenuItems(copyMenuItems) > 0) {
			// Copy menu items exist, so add in submenu
			Menu copyMenu = new Menu(CommonMenuItems.COPY.getText());
			copyMenu.getItems().addAll(copyMenuItems);
			menuItems.add(copyMenu);
		}

		return menuItems;
	}

	private static List<MenuItem> getSendToMenuItems(BooleanProperty invisibleWhenFalse, DataProvider dataProvider, final Integer...nids) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();

		// Menu item to show concept details.
		MenuItem listViewMenuItem = new MenuItem(CommonMenuItems.LIST_VIEW.getText());
		listViewMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
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
		if (invisibleWhenFalse != null)
		{
			listViewMenuItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (nids == null || nids.length == 0) {
				listViewMenuItem.setVisible(false);
			}
		}
		menuItems.add(listViewMenuItem);


		// Menu item to generate New Workflow Instance.
		MenuItem newWorkflowInstanceItem = new MenuItem(CommonMenuItems.WORKFLOW_VIEW.getText());
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
		if (invisibleWhenFalse != null)
		{
			newWorkflowInstanceItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (nids == null || nids.length != 1) {
				newWorkflowInstanceItem.setVisible(false);
			}
		}
		menuItems.add(newWorkflowInstanceItem);


		return menuItems;
	}

	private static List<MenuItem> getCopyMenuItems(BooleanProperty invisibleWhenFalse, DataProvider dataProvider, final Integer...nids) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();

		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		SeparatorMenuItem separator = new SeparatorMenuItem();

		if (dataProvider != null) {

				MenuItem copyTextItem = new MenuItem(CommonMenuItems.COPY_TEXT.getText());
				copyTextItem.setGraphic(Images.COPY.createImageView());
				copyTextItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						LOG.debug("Using \"" + CommonMenuItems.CONCEPT_VIEW.getText() + "\" menu item to copy text \"" + dataProvider.getStrings()[0] + "\"");
						CustomClipboard.set(dataProvider.getStrings()[0]);
					}
				});
				if (invisibleWhenFalse != null)
				{
					copyTextItem.visibleProperty().bind(invisibleWhenFalse);
				} else {
					if (dataProvider == null || dataProvider.getStrings() == null || dataProvider.getStrings().length != 1 || dataProvider.getStrings()[0] == null) {
						copyTextItem.setVisible(false);
					}
				}

				menuItems.add(copyTextItem);
			

			if (dataProvider.getObjectContainers() != null) {
				MenuItem copyContentItem = new MenuItem(CommonMenuItems.COPY_CONTENT.getText());
				copyContentItem.setGraphic(Images.COPY.createImageView());
				copyContentItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						LOG.debug("Using \"" + CommonMenuItems.COPY_CONTENT.getText() + "\" menu item to copy " + dataProvider.getObjectContainers()[0].getObject().getClass() + " object \"" + dataProvider.getObjectContainers()[0].getString() + "\"");
						CustomClipboard.set(dataProvider.getObjectContainers()[0].getObject(), dataProvider.getObjectContainers()[0].getString());
					}
				});
				if (invisibleWhenFalse != null)
				{
					copyContentItem.visibleProperty().bind(invisibleWhenFalse);
				} else {
					if (dataProvider == null || dataProvider.getObjectContainers() == null || dataProvider.getObjectContainers().length != 1 || dataProvider.getObjectContainers()[0] == null) {
						copyContentItem.setVisible(false);
					}
				}

				menuItems.add(copyContentItem);
			}
		}

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
		MenuItem copySctIdMenuItem = new MenuItem(CommonMenuItems.COPY_SCTID.getText());
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
		if (invisibleWhenFalse != null)
		{
			copySctIdMenuItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (sctIds == null || sctIds.length != 1 || sctIds[0] == null) {
				copySctIdMenuItem.setVisible(false);
			}
		}

		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (getNumVisibleMenuItems(menuItems) > 0 && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copySctIdMenuItem);


		// Menu item to copy UUID.
		MenuItem copyUuidMenuItem = new MenuItem(CommonMenuItems.COPY_UUID.getText());
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
		if (invisibleWhenFalse != null)
		{
			copyUuidMenuItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (uuids == null || uuids.length != 1 || uuids[0] == null) {
				copyUuidMenuItem.setVisible(false);
			}
		}

		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (getNumVisibleMenuItems(menuItems) > 0 && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copyUuidMenuItem);


		MenuItem copyNidMenuItem = new MenuItem(CommonMenuItems.COPY_NID.getText());
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
		if (invisibleWhenFalse != null)
		{
			copyNidMenuItem.visibleProperty().bind(invisibleWhenFalse);
		} else {
			if (nids == null || nids.length != 1) {
				copyNidMenuItem.setVisible(false);
			}
		}

		// Add menu separator IFF there were non-ID items AND this is the first ID item
		if (getNumVisibleMenuItems(menuItems) > 0 && ! menuItems.contains(separator)) {
			menuItems.add(separator);
		}
		menuItems.add(copyNidMenuItem);


		return menuItems;
	}
}
