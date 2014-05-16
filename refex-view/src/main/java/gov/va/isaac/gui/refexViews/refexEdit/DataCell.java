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
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.WBUtility;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;

/**
 * {@link DataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DataCell extends TreeTableCell<RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>, RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>>>
{
	private Collection<RefexDynamicColumnInfo> columnInfo_;

	public DataCell(Collection<RefexDynamicColumnInfo> columnInfo)
	{
		super();
		columnInfo_ = columnInfo;
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
			for (RefexDynamicColumnInfo colInfo : columnInfo_)
			{
				try
				{
					if (item.getAssemblageNid() == UUIDToNid(colInfo.getAssemblageConcept()))
					{
						RefexDynamicDataBI data = item.getData()[colInfo.getColumnOrder()];
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
						return;
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Not applicable, for the current row.
			setText("");
			setGraphic(null);
		}
	}
	
	private void conceptLookup(RefexDynamicDataBI data)
	{
		setGraphic(new ProgressBar());
		ContextMenu cm = new ContextMenu();
		Utility.execute(() ->
		{
			String value;
			
			MenuItem mi = new MenuItem("View Concept", Images.CONCEPT_VIEW.createImageView());
			
			if (data instanceof RefexDynamicNidBI)
			{
				value = WBUtility.getDescription(((RefexDynamicNidBI)data).getDataNid());
				mi.setOnAction((event) ->
				{
					AppContext.getCommonDialogs().showConceptDialog(((RefexDynamicNidBI)data).getDataNid());
				});
				cm.getItems().add(mi);
			}
			else if (data instanceof RefexDynamicUUIDBI)
			{
				value = WBUtility.getDescription(((RefexDynamicUUIDBI)data).getDataUUID());
				mi.setOnAction((event) ->
				{
					AppContext.getCommonDialogs().showConceptDialog(((RefexDynamicUUIDBI)data).getDataUUID());
				});
				cm.getItems().add(mi);
			}
			else
			{
				value = null;
			}

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
