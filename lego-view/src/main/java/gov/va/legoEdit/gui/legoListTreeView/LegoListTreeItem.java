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

import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItemComparator;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;

/**
 * {@link LegoListTreeItem} The actual data item for each node in the tree
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoListTreeItem extends LegoTreeItem
{
	public LegoListTreeItem()
	{
		super();
	}

	public LegoListTreeItem(LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		setValue(null);
	}

	public LegoListTreeItem(String value, LegoTreeNodeType tct)
	{
		super(value, tct);
	}
	
	public LegoListTreeItem(String value, LegoTreeNodeType tct, Object extraData)
	{
		super(value, tct, extraData);
	}

	public LegoListTreeItem(LegoListByReference llbr)
	{
		setValue(llbr.getGroupName());
		this.extraData_ = llbr;
		this.ltnt_ = LegoTreeNodeType.legoListByReference;

		// Going to reorganize these under the LEGO list by introducing a PNCS NAME / value hierarchy in-between the
		// LegoList and the individual legos.

		buildPNCSChildren();
	}

	public void buildPNCSChildren()
	{
		if (!(getExtraData() instanceof LegoListByReference))
		{
			throw new IllegalArgumentException();
		}
		LegoListByReference llbr = (LegoListByReference) getExtraData();
		Hashtable<String, Hashtable<String, List<LegoReference>>> pncsHier = new Hashtable<>();

		for (LegoReference lr : llbr.getLegoReference())
		{
			String pncsName = lr.getPncs().getName();
			Hashtable<String, List<LegoReference>> pncsValueTable = pncsHier.get(pncsName);
			if (pncsValueTable == null)
			{
				pncsValueTable = new Hashtable<String, List<LegoReference>>();
				pncsHier.put(pncsName, pncsValueTable);
			}
			String pncsValue = lr.getPncs().getValue();
			List<LegoReference> legoList = pncsValueTable.get(pncsValue);
			if (legoList == null)
			{
				legoList = new ArrayList<LegoReference>();
				pncsValueTable.put(pncsValue, legoList);
			}
			legoList.add(lr);
		}

		for (Entry<String, Hashtable<String, List<LegoReference>>> items : pncsHier.entrySet())
		{
			Integer pncsId = null;
			if (items.getValue().values().size() > 0)
			{
				pncsId = items.getValue().values().iterator().next().get(0).getPncs().getId();
			}
			
			LegoListTreeItem pncsNameTI = new LegoListTreeItem(items.getKey(), LegoTreeNodeType.pncsName, pncsId);
			getChildren().add(pncsNameTI);
			for (Entry<String, List<LegoReference>> nestedItems : items.getValue().entrySet())
			{
				LegoListTreeItem pncsValueTI = new LegoListTreeItem(nestedItems.getKey(), LegoTreeNodeType.pncsValue);
				pncsNameTI.getChildren().add(pncsValueTI);
				for (LegoReference lr : nestedItems.getValue())
				{
					pncsValueTI.getChildren().add(new LegoListTreeItem(lr));
				}
			}
		}
		FXCollections.sort(getChildren(), new LegoTreeItemComparator(true));
		for (TreeItem<String> item : getChildren())
		{
			FXCollections.sort(item.getChildren(), new LegoTreeItemComparator(true));
		}
	}

	public LegoListTreeItem(LegoReference lr)
	{
		setValue("Lego");
		extraData_ = lr;
		ltnt_ = LegoTreeNodeType.legoReference;
	}

	@Override
	protected void validate()
	{
		//Note, no real validation necessary on the LegoListView... this could probably become a no-op.
		if (LegoTreeNodeType.pncsName  == ltnt_ || LegoTreeNodeType.pncsValue == ltnt_ || LegoTreeNodeType.legoListByReference == ltnt_
				|| LegoTreeNodeType.blankLegoEndNode == ltnt_ || LegoTreeNodeType.blankLegoListEndNode == ltnt_ 
				|| LegoTreeNodeType.legoReference == ltnt_)
		{
			invalidReason_ = null;
		}
		else if (extraData_ == null)
		{
			invalidReason_ = null;
		}
		else
		{
			throw new RuntimeException("Unexpected tree node type " + ltnt_);
		}
		isValid = invalidReason_ == null;
		validationTimestamp = System.currentTimeMillis();
	}
}
