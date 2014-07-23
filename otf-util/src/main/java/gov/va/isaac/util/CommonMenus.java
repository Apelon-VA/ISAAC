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
import gov.va.isaac.gui.dragAndDrop.ConceptIdProvider;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.TaxonomyViewI;
import gov.va.isaac.interfaces.gui.views.ConceptWorkflowViewI;
import gov.va.isaac.interfaces.gui.views.ListBatchViewI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

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
		default public String getString() { return null; }

		default public Number getNumber() { return null; }

		default public ObjectContainer getObjectContainer() { return null; }
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
	
	
	public static void addCommonMenus(ContextMenu cm, BooleanProperty invisibleWhenFalse, final ConceptIdProvider idProvider)
	{
		addCommonMenus(cm, MergeMode.ADD_TO_EXISTING, invisibleWhenFalse, null, idProvider);
	}
	public static void addCommonMenus(ContextMenu cm, MergeMode mergeMode, BooleanProperty invisibleWhenFalse, final ConceptIdProvider idProvider)
	{
		addCommonMenus(cm, mergeMode, invisibleWhenFalse, null, idProvider);
	}
	public static void addCommonMenus(ContextMenu cm, BooleanProperty invisibleWhenFalse, final DataProvider dataProvider, final ConceptIdProvider idProvider)
	{
		addCommonMenus(cm, MergeMode.ADD_TO_EXISTING, invisibleWhenFalse, dataProvider, idProvider);
	}
	public static void addCommonMenus(ContextMenu existingMenu, MergeMode mergeMode, BooleanProperty invisibleWhenFalse, final DataProvider dataProvider, final ConceptIdProvider idProvider)
	{
		List<MenuItem> menuItems = getCommonMenus(invisibleWhenFalse, dataProvider, idProvider);
		
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
	public static List<MenuItem> getCommonMenus(BooleanProperty invisibleWhenFalse, final DataProvider dataProvider, final ConceptIdProvider idProvider)
	{
		List<MenuItem> menuItems = new ArrayList<>();
		
		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		// Menu item to show concept details.
//		// This concept viewer is obsolete
//		if (idProvider != null && idProvider.getUUID() != null) {
//			MenuItem viewConceptMenuItem = new MenuItem("View Concept (simple)");
//			viewConceptMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
//			viewConceptMenuItem.setOnAction(new EventHandler<ActionEvent>() {
//				@Override
//				public void handle(ActionEvent event)
//				{
//					UUID id = idProvider.getUUID();
//					if (id != null)
//					{
//						AppContext.getCommonDialogs().showConceptDialog(id);
//					}
//					else
//					{
//						AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't display an invalid concept");
//					}
//				}
//			});
//			if (invisibleWhenFalse != null)
//			{
//				viewConceptMenuItem.visibleProperty().bind(invisibleWhenFalse);
//			}
//			cm.getItems().add(viewConceptMenuItem);
//		}

		if (idProvider != null && idProvider.getNid() != null) {
			// Menu item to show concept details.
			MenuItem enhancedConceptViewMenuItem = new MenuItem(CommonMenuItems.CONCEPT_VIEW.getText());
			enhancedConceptViewMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
			enhancedConceptViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					Integer id = idProvider.getNid();
					if (id != null)
					{
						LOG.debug("Using \"" + CommonMenuItems.CONCEPT_VIEW.getText() + "\" menu item to display concept with id \"" + id + "\"");

						AppContext.getService(EnhancedConceptView.class).setConcept(id);
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
			}
			menuItems.add(enhancedConceptViewMenuItem);
		}

		if (idProvider != null && idProvider.getUUID() != null) {
			// Menu item to find concept in tree.
			MenuItem findInTaxonomyViewMenuItem = new MenuItem(CommonMenuItems.TAXONOMY_VIEW.getText());
			findInTaxonomyViewMenuItem.setGraphic(Images.ROOT.createImageView());
			findInTaxonomyViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0)
				{
					UUID id = idProvider.getUUID();
					if (id != null)
					{
						AppContext.getService(TaxonomyViewI.class).locateConcept(id, null);
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
			}
			menuItems.add(findInTaxonomyViewMenuItem);
		}

		List<MenuItem> sendToMenuItems = getSendToMenuItems(invisibleWhenFalse, dataProvider, idProvider);
		// ContextMenu already has view actions, so possibly add submenu
		if (sendToMenuItems.size() > 0) {
			// Copy menu items exist, so add in submenu
			Menu sendToMenu = new Menu(CommonMenuItems.SEND_TO.getText());
			sendToMenu.getItems().addAll(sendToMenuItems);
			menuItems.add(sendToMenu);
		}
		
		// Get copy menu items
		// If no copyable data avail, then don't add menu
		// If no other ContextMenu items, then add as items, not as submenu
		List<MenuItem> copyMenuItems = getCopyMenuItems(invisibleWhenFalse, dataProvider, idProvider);
		
		if (menuItems.size() > 0) {
			// ContextMenu already has view actions, so possibly add submenu
			if (copyMenuItems.size() > 0) {
				// Copy menu items exist, so add in submenu
				Menu copyMenu = new Menu(CommonMenuItems.COPY.getText());
				copyMenu.getItems().addAll(copyMenuItems);
				menuItems.add(copyMenu);
			}
		} else {
			// ContextMenu has no view actions, so possibly add items directly
			if (copyMenuItems.size() > 0) {
				// Copy menu items exist, so add directly to ContextMenu
				menuItems.addAll(copyMenuItems);
			}
		}
		
		return menuItems;
	}
	
	private static List<MenuItem> getSendToMenuItems(BooleanProperty invisibleWhenFalse, DataProvider dataProvider, final ConceptIdProvider idProvider) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();
		
		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		if (idProvider != null && idProvider.getNid() != null) {
			// Menu item to show concept details.
			MenuItem listViewMenuItem = new MenuItem(CommonMenuItems.LIST_VIEW.getText());
			listViewMenuItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
			listViewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					Integer id = idProvider.getNid();
					if (id != null)
					{
						LOG.debug("Using \"List View\" menu item to list concept with id \"" + id + "\"");

						ListBatchViewI lv = AppContext.getService(ListBatchViewI.class);
						
						AppContext.getMainApplicationWindow().ensureDockedViewIsVisble(lv);
						
						lv.addConcept(id);		
					}
					else
					{
						AppContext.getCommonDialogs().showInformationDialog("Invalid Concept", "Can't list an invalid concept");
					}
				}
			});
			if (invisibleWhenFalse != null)
			{
				listViewMenuItem.visibleProperty().bind(invisibleWhenFalse);
			}
			menuItems.add(listViewMenuItem);
		}

		if (idProvider != null && idProvider.getNid() != null) {
			// Menu item to generate New Workflow Instance.
			MenuItem newWorkflowInstanceItem = new MenuItem(CommonMenuItems.WORKFLOW_VIEW.getText());
			newWorkflowInstanceItem.setGraphic(Images.CONCEPT_VIEW.createImageView());
			newWorkflowInstanceItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					Integer id = idProvider.getNid();
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
			}
			menuItems.add(newWorkflowInstanceItem);
		}

		return menuItems;
	}
	
	private static List<MenuItem> getCopyMenuItems(BooleanProperty invisibleWhenFalse, DataProvider dataProvider, final ConceptIdProvider idProvider) {
		// The following code is for the Copy submenu

		List<MenuItem> menuItems = new ArrayList<>();
		
		//ConceptIdProvider idProvider = passedIdProvider != null ? ConceptIdProviderHelper.getPopulatedConceptIdProvider(passedIdProvider) : null;

		SeparatorMenuItem separator = new SeparatorMenuItem();
		
		if (dataProvider != null) {
			if (dataProvider.getString() != null) {
				MenuItem copyTextItem = new MenuItem(CommonMenuItems.COPY_TEXT.getText());
				copyTextItem.setGraphic(Images.COPY.createImageView());
				copyTextItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						LOG.debug("Using \"" + CommonMenuItems.CONCEPT_VIEW.getText() + "\" menu item to copy text \"" + dataProvider.getString() + "\"");
						CustomClipboard.set(dataProvider.getString());
					}
				});
				if (invisibleWhenFalse != null)
				{
					copyTextItem.visibleProperty().bind(invisibleWhenFalse);
				}

				menuItems.add(copyTextItem);
			}
			
			if (dataProvider.getObjectContainer() != null) {
				MenuItem copyContentItem = new MenuItem(CommonMenuItems.COPY_CONTENT.getText());
				copyContentItem.setGraphic(Images.COPY.createImageView());
				copyContentItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event)
					{
						LOG.debug("Using \"" + CommonMenuItems.COPY_CONTENT.getText() + "\" menu item to copy " + dataProvider.getObjectContainer().getObject().getClass() + " object \"" + dataProvider.getObjectContainer().getString() + "\"");
						CustomClipboard.set(dataProvider.getObjectContainer().getObject(), dataProvider.getObjectContainer().getString());
					}
				});
				if (invisibleWhenFalse != null)
				{
					copyContentItem.visibleProperty().bind(invisibleWhenFalse);
				}

				menuItems.add(copyContentItem);
			}
		}
		
		// The following are ID-related and will be under a separator
	
		// Menu item to copy UUID.
		if (idProvider != null && idProvider.getSctId() != null) {
			MenuItem copySctIdMenuItem = new MenuItem(CommonMenuItems.COPY_SCTID.getText());
			copySctIdMenuItem.setGraphic(Images.COPY.createImageView());
			copySctIdMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					if (idProvider.getSctId() != null)
					{
						CustomClipboard.set(idProvider.getSctId());
					}
				}
			});
			if (invisibleWhenFalse != null)
			{
				copySctIdMenuItem.visibleProperty().bind(invisibleWhenFalse);
			}

			// Add menu separator IFF there were non-ID items AND this is the first ID item
			if (menuItems.size() > 0 && ! menuItems.contains(separator)) {
				menuItems.add(separator);
			}
			menuItems.add(copySctIdMenuItem);
		}
		
		// Menu item to copy UUID.
		if (idProvider != null && idProvider.getUUID() != null) {

			MenuItem copyUuidMenuItem = new MenuItem(CommonMenuItems.COPY_UUID.getText());
			copyUuidMenuItem.setGraphic(Images.COPY.createImageView());
			copyUuidMenuItem.setOnAction(new EventHandler<ActionEvent>()
					{
				@Override
				public void handle(ActionEvent event)
				{
					if (idProvider.getUUID() != null)
					{
						CustomClipboard.set(idProvider.getUUID().toString());
					}
				}
					});
			if (invisibleWhenFalse != null)
			{
				copyUuidMenuItem.visibleProperty().bind(invisibleWhenFalse);
			}
			
			// Add menu separator IFF there were non-ID items AND this is the first ID item
			if (menuItems.size() > 0 && ! menuItems.contains(separator)) {
				menuItems.add(separator);
			}
			menuItems.add(copyUuidMenuItem);
		}

		if (idProvider != null && idProvider.getNid() != null) {
			MenuItem copyNidMenuItem = new MenuItem(CommonMenuItems.COPY_NID.getText());
			copyNidMenuItem.setGraphic(Images.COPY.createImageView());
			copyNidMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event)
				{
					if (idProvider.getNid() != null)
					{
						CustomClipboard.set(idProvider.getNid(), idProvider.getNid().toString());
					}
				}
			});
			if (invisibleWhenFalse != null)
			{
				copyNidMenuItem.visibleProperty().bind(invisibleWhenFalse);
			}
			
			// Add menu separator IFF there were non-ID items AND this is the first ID item
			if (menuItems.size() > 0 && ! menuItems.contains(separator)) {
				menuItems.add(separator);
			}
			menuItems.add(copyNidMenuItem);
		}
		
		return menuItems;
	}
}
