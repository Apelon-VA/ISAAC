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
package gov.va.legoEdit.gui.legoTreeView;

import gov.va.isaac.util.AlphanumComparator;
import gov.va.legoEdit.gui.legoListTreeView.LegoListTreeItem;
import java.util.Comparator;
import javafx.scene.control.TreeItem;

/**
 * 
 * {@link LegoTreeItemComparator}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LegoTreeItemComparator implements Comparator<TreeItem<String>>
{
    private AlphanumComparator ac;

    /**
     * Create a new instance of an AlphanumComparator.
     * 
     * @param caseSensitive
     */
    public LegoTreeItemComparator(boolean ignoreCase)
    {
        ac = new AlphanumComparator(ignoreCase);
    }

    @Override
    public int compare(TreeItem<String> s1, TreeItem<String> s2)
    {
        int r = 0;
        if (s1 instanceof LegoTreeItem && s2 instanceof LegoTreeItem)
        {
            r = ((LegoListTreeItem)s1).getSortOrder() - ((LegoListTreeItem)s2).getSortOrder();
        }
        if (r == 0)
        {
            return ac.compare(s1.getValue(), s2.getValue());
        }
        else
        {
            return r;
        }
    }
}
