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
package gov.va.legoEdit.gui.legoListTreeView;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.htmlView.NativeHTMLViewer;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.gui.views.XMLViewI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.ExportDialog;
import gov.va.legoEdit.gui.LegoGUIMasterModel;
import gov.va.legoEdit.gui.LegoSummaryPane;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeGraphic;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import gov.va.legoEdit.util.TimeConvert;
import java.util.Arrays;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LegoTreeCell - A monstrosity of a class that handles the display and user interaction of each node within the lego tree.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 * 
 */
public class LegoListTreeCell<T> extends TreeCell<T>
{
	private static Logger logger = LoggerFactory.getLogger(LegoListTreeCell.class);

	private LegoListTreeView treeView;

	public LegoListTreeCell(LegoListTreeView lltv)
	{
		// For reasons I don't understand, the getTreeView() method is unreliable, sometimes returning null. Either a bug in javafx, or really poorly
		// documented...
		this.treeView = lltv;

		addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() > 1)
				{
					final LegoListTreeItem treeItem = (LegoListTreeItem) getTreeItem();
					if (treeItem.getNodeType() != null)
					{
						if (LegoTreeNodeType.legoReference == treeItem.getNodeType())
						{
							LegoReference lr = (LegoReference) treeItem.getExtraData();
							new LegoSummaryPane(BDBDataStoreImpl.getInstance().getLego(lr.getLegoUUID(), lr.getStampUUID())).show();
						}
					}
				}
			}
		});
	}

	@Override
	public void updateItem(T item, boolean empty)
	{
		try
		{
			super.updateItem(item, empty);
			final LegoTreeItem treeItem = (LegoTreeItem) getTreeItem();
			ContextMenu cm = new ContextMenu();
			LegoTreeNodeGraphic nodeBox = new LegoTreeNodeGraphic();
			
			// This is the first time I really don't understand the JavaFX API. It appears to reuse the item values, when scrolling up and down...
			// So if the item type changes from one position to another, and you don't unset (or reset) all of the same properties that were set
			// previously see http://javafx-jira.kenai.com/browse/RT-19629
			setEditable(false);
			setText(null);
			setGraphic(null);
			getStyleClass().remove("boldLabel");
			styleProperty().unbind();
			// Clear the drop shadow and bold - workaround for non-clearing styles
			setStyle("-fx-effect: innershadow(two-pass-box , white , 0, 0.0 , 0 , 0);");
			setTooltip(null);
			if (treeItem != null)
			{
				treeItem.setTreeNodeGraphic(nodeBox);
				treeItem.updateValidityImageThreaded();
				if (!treeItem.isLeaf())
				{
					MenuItem mi = new MenuItem("Expand All");
					mi.setOnAction(new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent arg0)
						{
							FxUtils.expandAll(treeItem);
						}
					});
					mi.setGraphic(Images.EXPAND_ALL.createImageView());
					cm.getItems().add(mi);
				}
			}

			if (empty || treeItem.getNodeType() == LegoTreeNodeType.blankLegoEndNode || treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode)
			{
				if (!empty && treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode)
				{
					//TODO [LegoEdit]
//					MenuItem mi = new MenuItem("Create Lego List");
//					mi.setOnAction(new EventHandler<ActionEvent>()
//					{
//						@Override
//						public void handle(ActionEvent arg0)
//						{
//							LegoGUI.getInstance().showLegoListPropertiesDialog(null, null);
//						}
//					});
//					mi.setGraphic(Images.LEGO_ADD.createImageView());
//					cm.getItems().add(mi);
				}
			}
			else
			{
				if (treeItem.getNodeType() == LegoTreeNodeType.legoListByReference)
				{
					Tooltip tp = new Tooltip(((LegoListByReference) treeItem.getExtraData()).getGroupDescription());
					setTooltip(tp);
					treeItem.setValue(((LegoListByReference) treeItem.getExtraData()).getGroupName());
					addMenus((LegoListByReference) treeItem.getExtraData(), treeItem, cm);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.legoReference)
				{
					final LegoReference legoReference = (LegoReference) treeItem.getExtraData();
					final Label l = new Label(TimeConvert.format(legoReference.getStampTime()));

					nodeBox.getChildren().add(l);
					l.setGraphic(legoReference.isNew() ? Images.LEGO_EDIT.createImageView() : Images.LEGO.createImageView());

					addMenus(legoReference, treeItem, cm);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.pncsName)
				{
					//TODO [LegoEdit]
//					MenuItem mi = new MenuItem("Create New Lego for PNCS");
//					mi.setOnAction(new EventHandler<ActionEvent>()
//					{
//						@Override
//						public void handle(ActionEvent arg0)
//						{
//							LegoGUI.getInstance().showCreateLegoDialog((LegoListByReference) treeItem.getLegoParent().getExtraData(), 
//									treeItem, false);
//						}
//					});
//					mi.setGraphic(Images.LEGO_ADD.createImageView());
//					cm.getItems().add(mi);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.pncsValue)
				{
					//TODO [LegoEdit]
//					MenuItem mi = new MenuItem("Create New Lego for PNCS");
//					mi.setOnAction(new EventHandler<ActionEvent>()
//					{
//						@Override
//						public void handle(ActionEvent arg0)
//						{
//							createNewLego(treeItem, false);
//						}
//					});
//					mi.setGraphic(Images.LEGO_ADD.createImageView());
//					cm.getItems().add(mi);
//
//					mi = new MenuItem("Paste Lego (using this PNCS name / value)");
//					mi.setOnAction(new EventHandler<ActionEvent>()
//					{
//						@Override
//						public void handle(ActionEvent arg0)
//						{
//							createNewLego(treeItem, true);
//						}
//					});
//					mi.visibleProperty().bind(CustomClipboard.containsLego);
//					mi.setGraphic(Images.PASTE.createImageView());
//					cm.getItems().add(mi);
				}
			}
			// done with massive if/else
			// Deal with javafx memory leak (at least help)
			if (getContextMenu() != null)
			{
				for (MenuItem mi : getContextMenu().getItems())
				{
					mi.visibleProperty().unbind();
				}
				setContextMenu(null);
			}
			if (cm.getItems().size() > 0)
			{
				setContextMenu(cm);
			}
			
			if (nodeBox.getChildren().size() == 0 )
			{
				if (item != null)
				{
					nodeBox.getChildren().add(new Label(item.toString()));
				}
			}
			if (nodeBox.getChildren().size() > 0)
			{
				HBox.setHgrow(nodeBox.getChildren().get(nodeBox.getChildren().size() - 1), Priority.SOMETIMES);
			}
			setGraphic(nodeBox.getNode());
		}
		catch (Exception e)
		{
			logger.error("Unexpected", e);
			AppContext.getCommonDialogs().showErrorDialog("Unexpected Error", "There was an unexpected problem building the tree",
					"Please report this as a bug.  " + e.toString());
		}
	}

	//TODO [LEGO Edit]
//	private void createNewLego(LegoTreeItem ti, boolean fromPaste)
//	{
//		if (ti.getNodeType() == LegoTreeNodeType.pncsValue)
//		{
//			String pncsValue = ti.getValue();
//			String pncsName = ti.getParent().getValue();
//
//			// ID can be grabbed from any child lego of this treeItem.
//			// Should always be at least one child
//			int pncsId = ((LegoReference) ((LegoTreeItem) ti.getChildren().get(0)).getExtraData()).getPncs().getId();
//			LegoListByReference llbr = (LegoListByReference) ti.getLegoParent().getLegoParent().getExtraData();
//
//			Pncs pncs = new Pncs();
//			pncs.setName(pncsName);
//			pncs.setValue(pncsValue);
//			pncs.setId(pncsId);
//
//			UserPreferences up = LegoGUIModel.getInstance().getUserPreferences();
//
//			Lego l;
//			if (fromPaste)
//			{
//				l = CustomClipboard.getLego();
//				if (l == null)
//				{
//					LegoGUI.getInstance().showErrorDialog("Not a Lego", "The Clipboard does not contain a Lego", null);
//					CustomClipboard.updateBindings();
//					return;
//				}
//				Stamp s = l.getStamp();
//				if (s == null)
//				{
//					// set everything
//					s = new Stamp();
//					s.setAuthor(up.getAuthor());
//					s.setModule(up.getModule());
//					s.setPath(up.getPath());
//					s.setStatus(statusChoices_.get(0));
//					s.setTime(TimeConvert.convert(System.currentTimeMillis()));
//					s.setUuid(UUID.randomUUID().toString());
//					l.setStamp(s);
//				}
//				else
//				{
//					// just set the author, time and uuid
//					s.setAuthor(up.getAuthor());
//					s.setTime(TimeConvert.convert(System.currentTimeMillis()));
//					s.setUuid(UUID.randomUUID().toString());
//				}
//				l.setStamp(s);
//
//				// Change all the assertion UUIDs
//				for (Assertion a : l.getAssertion())
//				{
//					a.setAssertionUUID(UUID.randomUUID().toString());
//				}
//			}
//			else
//			{
//				l = new Lego();
//
//				Stamp s = new Stamp();
//				s.setAuthor(up.getAuthor());
//				s.setModule(up.getModule());
//				s.setPath(up.getPath());
//				s.setStatus(statusChoices_.get(0));
//				s.setTime(TimeConvert.convert(System.currentTimeMillis()));
//				s.setUuid(UUID.randomUUID().toString());
//				l.setStamp(s);
//
//				Assertion a = new Assertion();
//				a.setAssertionUUID(UUID.randomUUID().toString());
//				l.getAssertion().add(a);
//			}
//
//			l.setPncs(pncs);
//			l.setLegoUUID(UUID.randomUUID().toString());
//
//			LegoReference lr = new LegoReference(l);
//			lr.setIsNew(true);
//			llbr.getLegoReference().add(lr);
//			LegoTreeItem lti = new LegoTreeItem(lr);
//			ti.getChildren().add(lti);
//			LegoTreeView ltv = (LegoTreeView) getTreeView();
//			ltv.getSelectionModel().select(lti);
//			LegoGUI.getInstance().getLegoGUIController().addNewLego(llbr.getLegoListUUID(), l);
//		}
//		else
//		{
//			logger.error("Unhandled create lego request!");
//		}
//	}

	private void addMenus(final LegoListByReference llbr, final LegoTreeItem treeItem, ContextMenu cm)
	{
		MenuItem mi;
		//TODO [LEGO Edit]
//		mi = new MenuItem("Create New Lego Within Lego List");
//		mi.setOnAction(new EventHandler<ActionEvent>()
//		{
//			@Override
//			public void handle(ActionEvent arg0)
//			{
//				LegoGUI.getInstance().showCreateLegoDialog(llbr, treeItem, false);
//			}
//		});
//		mi.setGraphic(Images.LEGO_ADD.createImageView());
//		cm.getItems().add(mi);
//
//		mi = new MenuItem("Paste Lego into Lego List");
//		mi.setOnAction(new EventHandler<ActionEvent>()
//		{
//			@Override
//			public void handle(ActionEvent arg0)
//			{
//				LegoGUI.getInstance().showCreateLegoDialog(llbr, treeItem, true);
//			}
//		});
//		mi.visibleProperty().bind(CustomClipboard.containsLego);
//		mi.setGraphic(Images.PASTE.createImageView());
//		cm.getItems().add(mi);
		
		
		mi = new MenuItem("View in Web Browser");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				NativeHTMLViewer.viewInBrowser(() ->
				{
					try
					{
						return LegoXMLUtils.toXHTML(BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID()));
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				});
			}
		});
		mi.setGraphic(Images.HTML_VIEW_16.createImageView());
		cm.getItems().add(mi);

		mi = new MenuItem("Show XML View");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				AppContext.getService(XMLViewI.class).showView(treeView.getScene().getWindow(), llbr.getGroupName(), () -> 
				{
					try
					{
						LegoList ll = BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID());
						return LegoXMLUtils.toXML(ll);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}, 800, 600);
			}
		});
		mi.setGraphic(Images.XML_VIEW_16.createImageView());
		cm.getItems().add(mi);

		mi = new MenuItem("Properties");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				AppContext.getService(LegoGUIMasterModel.class).showLegoListPropertiesDialog(llbr, treeItem);
			}
		});
		mi.setGraphic(Images.PROPERTIES.createImageView());
		cm.getItems().add(mi);

		mi = new MenuItem("Export...");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				try
				{
					new ExportDialog(Arrays.asList(new LegoList[] {BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID())}),
						treeView.getParent().getScene().getWindow());
				}
				catch (Exception e)
				{
					logger.error("Unexpected error exporting LegoList ", e);
					AppContext.getCommonDialogs().showErrorDialog("Error exporting Lego Lists", e);
				}
			}
		});
		mi.setGraphic(Images.LEGO_EXPORT.createImageView());
		cm.getItems().add(mi);

		mi = new MenuItem("Delete Lego List");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				DialogResponse a = AppContext.getCommonDialogs().showYesNoDialog("Really delete Lego List?",
						"Are you sure that you want to delete the Lego List?  " + "This will delete all contained Legos.");
				if (a == DialogResponse.YES)
				{
					try
					{
						AppContext.getService(LegoGUIMasterModel.class).removeLegoList(llbr);
					}
					catch (WriteException e)
					{
						logger.error("Error deleting lego list", e);
						AppContext.getCommonDialogs().showErrorDialog("Error Removing Lego List", "Unexpected error removing lego list", e.toString());
					}
				}
			}
		});
		mi.setGraphic(Images.LEGO_DELETE.createImageView());
		cm.getItems().add(mi);
	}

	private void addMenus(final LegoReference legoReference, final LegoTreeItem lti, ContextMenu cm)
	{
		MenuItem mi;

		if (!legoReference.isNew())
		{
			mi = new MenuItem("View in Web Browser");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					NativeHTMLViewer.viewInBrowser(() ->
					{
						try
						{
							return LegoXMLUtils.toXHTML(BDBDataStoreImpl.getInstance().getLego(legoReference.getLegoUUID(), legoReference.getStampUUID()));
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					});
				}
			});
			mi.setGraphic(Images.HTML_VIEW_16.createImageView());
			cm.getItems().add(mi);
		}
		
		mi = new MenuItem("Delete Lego");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				DialogResponse a = AppContext.getCommonDialogs().showYesNoDialog("Really delete Lego?", "Are you sure that you want to delete the Lego?");
				if (a == DialogResponse.YES)
				{
					// From legoReference treeItem, go up past pncsName and pncs value to get the LegoListReference
					try
					{
						AppContext.getService(LegoGUIMasterModel.class).removeLego((LegoListByReference) lti.getLegoParent().getLegoParent().getLegoParent().getExtraData(),
								legoReference);
					}
					catch (WriteException e)
					{
						logger.error("Error deleting lego", e);
						AppContext.getCommonDialogs().showErrorDialog("Error Removing Lego", "Unexpected error removing lego", e.toString());
					}
				}

			}
		});
		mi.setGraphic(Images.LEGO_DELETE.createImageView());
		cm.getItems().add(mi);

		if (!legoReference.isNew())
		{
			mi = new MenuItem("Copy Lego");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					Lego lego = BDBDataStoreImpl.getInstance().getLego(legoReference.getLegoUUID(), legoReference.getStampUUID());
					if (lego != null)
					{
						CustomClipboard.set(lego, SchemaToString.toString(lego));
					}
					else
					{
						AppContext.getCommonDialogs().showErrorDialog("Lego Not Found", "Couldn't find the desired Lego in the Database",
								"Legos can only be copied after they have been stored to the database.");
					}
				}
			});
			mi.setGraphic(Images.COPY.createImageView());
			cm.getItems().add(mi);

			//TODO templates
//			mi = new MenuItem("Create Template...");
//			mi.setOnAction(new EventHandler<ActionEvent>()
//			{
//				@Override
//				public void handle(ActionEvent arg0)
//				{
//					Lego lego = BDBDataStoreImpl.getInstance().getLego(legoReference.getLegoUUID(), legoReference.getStampUUID());
//					if (lego != null)
//					{
//						LegoGUI.getInstance().showCreateTemplateDialog(lego);
//					}
//					else
//					{
//						AppContext.getCommonDialogs().showErrorDialog("Lego Not Found", "Couldn't find the desired Lego in the Database",
//								"Legos can only be used as a template after they have been stored to the database.");
//					}
//				}
//			});
//			mi.setGraphic(Images.TEMPLATE.createImageView());
//			cm.getItems().add(mi);
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		// Help deal with javafx memory leaks
		styleProperty().unbind();
		ContextMenu cm = getContextMenu();
		if (cm != null)
		{
			for (MenuItem mi : cm.getItems())
			{
				mi.visibleProperty().unbind();
			}
		}
		super.finalize();
	}
}
