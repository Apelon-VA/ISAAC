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

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * {@link LegoListTreeView} An enhanced tree view for Lego editing 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoListTreeView extends TreeView<String>
{
	public LegoListTreeView()
	{
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setCellFactory(new Callback<TreeView<String>, TreeCell<String>>()
		{
			@Override
			public TreeCell<String> call(TreeView<String> arg0)
			{
				return new LegoListTreeCell<String>(LegoListTreeView.this);
			}
		});
		// Not going to use the edit API, not reliable. Just detect doubleclick instead.
		setEditable(false);
		LegoListTreeItem treeRoot = new LegoListTreeItem();
		setShowRoot(false);
		setRoot(treeRoot);
	}
}
