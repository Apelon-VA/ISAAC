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
package gov.va.isaac.gui.util;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TreeItem;

/**
 * 
 * {@link ExpandedNode} Just a silly class to store the hierarchy of an expanded tree for later replication.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExpandedNode
{
	ArrayList<ExpandedNode> items = new ArrayList<>();
	
	public void addExpanded(ExpandedNode children)
	{
		items.add(children);
	}
	
	public void addCollapsed()
	{
		items.add(null);
	}
	
	public List<ExpandedNode> getItems()
	{
		return items;
	}
	
	public static ExpandedNode buildExpandedNodeHierarchy(TreeItem<?> treeItem)
	{
		//If this is called, assume the called node is expanded.  Only care about children.
		ExpandedNode en = new ExpandedNode();
		for (TreeItem<?> ti : treeItem.getChildren())
		{
			if (ti.isExpanded())
			{
				en.addExpanded(buildExpandedNodeHierarchy(ti));
			}
			else
			{
				en.addCollapsed();
			}
		}
		return en;
	}
	
	public static void setExpandedStates(ExpandedNode expandedNode, TreeItem<?> treeItem)
	{
		for (int i = 0; i < expandedNode.getItems().size(); i++)
		{
			//If the structure changed, just have to ignore
			//maybe in the future, try to keep track of ids or something, so we can better align
			if (treeItem.getChildren().size() >  i)
			{
				if (expandedNode.getItems().get(i) != null)
				{
					treeItem.getChildren().get(i).setExpanded(true);
					setExpandedStates(expandedNode.getItems().get(i), treeItem.getChildren().get(i));
				}
				else
				{
					treeItem.getChildren().get(i).setExpanded(false);
				}
			}
		}
	}
}
