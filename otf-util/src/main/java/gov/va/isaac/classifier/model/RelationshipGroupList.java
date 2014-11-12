/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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
package gov.va.isaac.classifier.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class RelationshipGroupList.
 * Represents a relationship group list
 */
public class RelationshipGroupList extends ArrayList<RelationshipGroup> {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new sno grp list.
     */
    public RelationshipGroupList() {
        super();
    }

    /**
     * Construct a ROLE_GROUP_LIST from <code>List&lt;Relationship&gt;</code><br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     *
     * @param rels the rels
     */
    public RelationshipGroupList(List<Relationship> rels) {
        super();
        // First Group
        RelationshipGroup group = new RelationshipGroup();
        this.add(group);

        // First Relationship in First Group
        Iterator<Relationship> it = rels.iterator();
        Relationship relationshipA = it.next();
        group.add(relationshipA);

        while (it.hasNext()) {
            Relationship relationshipB = it.next();
            if (relationshipB.group == relationshipA.group) {
                group.add(relationshipB); // ADD TO SAME GROUP
            } else {
                group = new RelationshipGroup(); // CREATE NEW GROUP
                this.add(group); // ADD GROUP TO GROUP LIST
                group.add(relationshipB);
            }
            relationshipA = relationshipB;
        }
    }

    /**
     * Count rels.
     *
     * @return the int
     */
    public int countRels() {
        int returnCount = 0;
        for (RelationshipGroup sg : this)
            returnCount += sg.size();
        return returnCount;
    }

   
    /**
     * Which groups in this do not have ANY equal group in groupListB?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     *
     * @param groupListB the group list b
     * @return <code>RelationshipGroupList</code>
     */
    public RelationshipGroupList whichNotEqual(RelationshipGroupList groupListB) {
        RelationshipGroupList sg = new RelationshipGroupList();
        for (RelationshipGroup groupA : this) {
            boolean foundEqual = false;
            for (RelationshipGroup groupB : groupListB) {
                if (groupA.equals(groupB)) {
                    foundEqual = true;
                    break;
                }
            }
            if (!foundEqual) {
                sg.add(groupA);
            }
        }
        return sg;
    }

}
