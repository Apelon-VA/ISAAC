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
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.gui.dragAndDrop.DragRegistry;
import gov.va.isaac.gui.dragAndDrop.SingleConceptIdProvider;
import gov.va.isaac.gui.util.CustomClipboard;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Utility;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.text.Text;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AttachedDataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AttachedDataCell extends TreeTableCell<RefexDynamicGUI, RefexDynamicGUI>
{
	private Hashtable<UUID, List<RefexDynamicColumnInfo>> columnInfo_;
	private int listItem_;
	private static Logger logger_ = LoggerFactory.getLogger(AttachedDataCell.class);

	public AttachedDataCell(Hashtable<UUID, List<RefexDynamicColumnInfo>> columnInfo, int listItem)
	{
		super();
		columnInfo_ = columnInfo;
		listItem_ = listItem;
	}

	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(RefexDynamicGUI item, boolean empty)
	{
		super.updateItem(item, empty);
		
		if (empty || item == null)
		{
			setText("");
			setGraphic(null);
		}
		else if (item != null)
		{
			try
			{
				for (UUID uuid : columnInfo_.keySet())
				{
					if (UUIDToNid(uuid) == item.getRefex().getAssemblageNid())
					{
						List<RefexDynamicColumnInfo> colInfo =  columnInfo_.get(uuid);
						Integer refexColumnOrder = (colInfo.size() > listItem_ ? 
								(item.getRefex().getData().length <= colInfo.get(listItem_).getColumnOrder() ? null 
									: colInfo.get(listItem_).getColumnOrder()): null);
						RefexDynamicDataBI data = (refexColumnOrder == null ? null : item.getRefex().getData()[refexColumnOrder]); 
						if (data != null)
						{
							if (data instanceof RefexDynamicNidBI)
							{
								conceptLookup(item, refexColumnOrder);
							}
							else if (data instanceof RefexDynamicUUIDBI)
							{
								conceptLookup(item, refexColumnOrder);
							}
							else
							{
								AbstractMap.SimpleImmutableEntry<String, String> texts = item.getDisplayStrings(DynamicRefexColumnType.ATTACHED_DATA, refexColumnOrder);
								
								if (texts == null || texts.getKey() == null)
								{
									setText("");
									setGraphic(null);
								}
								else
								{
									//default text is a label, which doesn't wrap properly.
									setText(null);
									Text textHolder = new Text(texts.getKey());
									textHolder.wrappingWidthProperty().bind(widthProperty().subtract(10));
									setGraphic(textHolder);
									ContextMenu cm = new ContextMenu();
									MenuItem mi = new MenuItem("Copy");
									mi.setGraphic(Images.COPY.createImageView());
									mi.setOnAction((action) -> 
									{
										CustomClipboard.set(texts.getKey());
									});
									cm.getItems().add(mi);
									setContextMenu(cm);
									if (texts.getValue() != null && texts.getValue().length() > 0)
									{
										setTooltip(new Tooltip(texts.getValue()));
									}
								}
							}
						}
						else
						{
							//Not applicable, for the current row.
							setText("");
							setGraphic(null);
						}
						return;
					}
				}
			}
			catch (Exception e)
			{
				logger_.error("Unexpected error rendering data cell", e);
				setText("-ERROR-");
				setGraphic(null);
			}
			//Not applicable, for the current row.
			setText("");
			setGraphic(null);
		}
	}
	
	private void conceptLookup(final RefexDynamicGUI item, final Integer refexColumnOrder)
	{
		setGraphic(new ProgressBar());
		setText(null);
		ContextMenu cm = new ContextMenu();
		Utility.execute(() ->
		{
			AbstractMap.SimpleImmutableEntry<String, String> value = item.getDisplayStrings(DynamicRefexColumnType.ATTACHED_DATA, refexColumnOrder);
				
			CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
			{
				
				@Override
				public Collection<Integer> getNIds()
				{
					int nid = item.getNidFetcher(DynamicRefexColumnType.ATTACHED_DATA, refexColumnOrder).applyAsInt(item.getRefex());

					ArrayList<Integer> nids = new ArrayList<>();
					if (nid != 0)
					{
						nids.add(nid);
					}
					return nids;
				}
			});

			Platform.runLater(() ->
			{
				if (value.getValue() != null && value.getValue().length() > 0)
				{
					setTooltip(new Tooltip(value.getValue()));
				}
				if (cm.getItems().size() > 0)
				{
					setContextMenu(cm);
				}
				Text textHolder = new Text(value.getKey());
				textHolder.wrappingWidthProperty().bind(widthProperty().subtract(10));
				setGraphic(textHolder);
				AppContext.getService(DragRegistry.class).setupDragOnly(textHolder, new SingleConceptIdProvider()
				{
					@Override
					public String getConceptId()
					{
						return item.getNidFetcher(DynamicRefexColumnType.ATTACHED_DATA, refexColumnOrder).applyAsInt(item.getRefex()) +"";
					}
				});
				setText(null);
			});
		});
	}

	private int UUIDToNid(UUID uuid) throws IOException
	{
		return ExtendedAppContext.getDataStore().getNidForUuids(uuid);
	}
}
