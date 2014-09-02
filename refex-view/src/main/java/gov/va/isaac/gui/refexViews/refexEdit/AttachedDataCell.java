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

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusNIdProvider;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AttachedDataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AttachedDataCell extends TreeTableCell<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>
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
	protected void updateItem(RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> item, boolean empty)
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
					if (UUIDToNid(uuid) == item.getAssemblageNid())
					{
						List<RefexDynamicColumnInfo> colInfo =  columnInfo_.get(uuid);
						RefexDynamicDataBI data = (colInfo.size() > listItem_ ? 
								(item.getData().length <= colInfo.get(listItem_).getColumnOrder() ? null : item.getData()[colInfo.get(listItem_).getColumnOrder()]) 
								: null);
						if (data != null)
						{
							if (data instanceof RefexDynamicByteArrayBI)
							{
								
								setText("[Binary]");
								setGraphic(null);
							}
							else if (data instanceof RefexDynamicNidBI)
							{
								conceptLookup(data);
							}
							else if (data instanceof RefexDynamicUUIDBI)
							{
								conceptLookup(data);
							}
							else
							{
								setText(data.getDataObject().toString());
								setGraphic(null);
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
	
	private void conceptLookup(RefexDynamicDataBI data)
	{
		setGraphic(new ProgressBar());
		setText(null);
		ContextMenu cm = new ContextMenu();
		Utility.execute(() ->
		{
			String value;
			
			if (data instanceof RefexDynamicNidBI)
			{
				value = WBUtility.getDescription(((RefexDynamicNidBI)data).getDataNid());
			}
			else if (data instanceof RefexDynamicUUIDBI)
			{
				value = WBUtility.getDescription(((RefexDynamicUUIDBI)data).getDataUUID());
			}
			else
			{
				value = null;
			}
			
				
			CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
			{
				
				@Override
				public Collection<Integer> getNIds()
				{
					Integer nid = null;
					
					if (data instanceof RefexDynamicNidBI)
					{
						nid = ((RefexDynamicNidBI)data).getDataNid();
					}
					else if (data instanceof RefexDynamicUUIDBI)
					{
						ConceptVersionBI c = WBUtility.getConceptVersion(((RefexDynamicUUIDBI)data).getDataUUID());
						if (c != null)
						{
							nid = c.getNid();
						}
					}
					ArrayList<Integer> nids = new ArrayList<>();
					if (nid != null)
					{
						nids.add(nid);
					}
					return nids;
				}
			});

			Platform.runLater(() ->
			{
				if (value == null)
				{
					setText(data.getDataObject().toString());
				}
				else
				{
					setText(value);
					setTooltip(new Tooltip(data.getDataObject().toString()));
					if (cm.getItems().size() > 0)
					{
						setContextMenu(cm);
					}
				}
				setGraphic(null);
			});
		});
	}

	private int UUIDToNid(UUID uuid) throws IOException
	{
		return ExtendedAppContext.getDataStore().getNidForUuids(uuid);
	}
}
