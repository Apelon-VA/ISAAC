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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class Relationship.
 * Represents a relationship.
 */
public class Relationship implements Comparable<Object> {

    /** The relationship Id. */
    public String relationshipId;
    
    /** The source Id. */
    public int sourceId; 
    
    /** The destination Id. */
    public int destinationId;
    
    /** The type id. */
    public int typeId;
    
    /** The group. */
    public int group;

    // Relationship form a versioned "new" database perspective
    /**
     * Instantiates a new sno rel.
     *
     * @param sourceId the c1 id
     * @param destinationId the c2 id
     * @param roleTypeId the role type id
     * @param group the group
     * @param relationshipId the rel nid
     */
    public Relationship(int sourceId, int destinationId, int roleTypeId, int group, String relationshipId) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.typeId = roleTypeId;
        this.group = group;
        this.relationshipId = relationshipId;
    }

    // Relationship from a SnoRocket perspective
    /**
     * Instantiates a new sno rel.
     *
     * @param sourceId the c1 id
     * @param destinationId the c2 id
     * @param roleTypeId the role type id
     * @param group the group
     */
    public Relationship(int sourceId, int destinationId, int roleTypeId, int group) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.typeId = roleTypeId;
        this.group = group;
        this.relationshipId = "null";
    }

    /**
     * Gets the rel id.
     *
     * @return the rel id
     */
    public String getRelId() {
        return relationshipId;
    }

    /**
     * Sets the nid.
     *
     * @param nid the new nid
     */
    public void setNid(String nid) {
        this.relationshipId = nid;
    }

    // default sort order [c1-group-type-c2]
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        Relationship other = (Relationship) o;
        int thisMore = 1;
        int thisLess = -1;
        if (this.sourceId > other.sourceId) {
            return thisMore;
        } else if (this.sourceId < other.sourceId) {
            return thisLess;
        } else {
            if (this.group > other.group) {
                return thisMore;
            } else if (this.group < other.group) {
                return thisLess;
            } else {
                if (this.typeId > other.typeId) {
                    return thisMore;
                } else if (this.typeId < other.typeId) {
                    return thisLess;
                } else {
                    if (this.destinationId > other.destinationId) {
                        return thisMore;
                    } else if (this.destinationId < other.destinationId) {
                        return thisLess;
                    } else {
                        return 0; // this == received
                    }
                }
            }
        }
    } // Relationship.compareTo()

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sourceId);
        sb.append(": ");
        sb.append(typeId);
        sb.append(": ");
        sb.append(destinationId);
        return sb.toString();
    }

    /**
     * To string c1.
     *
     * @return the string
     */
    public String toStringC1() {
       
        return Integer.toString(sourceId);
    }

    /**
     * To string type.
     *
     * @return the string
     */
    public String toStringType() {
       
        return Integer.toString(typeId);
    }

    /**
     * To string c2.
     *
     * @return the string
     */
    public String toStringC2() {
       
        return Integer.toString(destinationId);
    }

    /**
     * To string group.
     *
     * @return the string
     */
    public String toStringGroup() {
        return Integer.toString(group);
    }


    /**
     * To string4 file.
     *
     * @return the string
     */
    public String toString4File() {
        return "relId     \t" + "sourceId      \t" + "destinationId      \t" + "typeId    \t" + "group";
    }

    /**
     * Dump to file.
     *
     * @param srl the srl
     * @param fName the f name
     * @param format the format
     */
    public static void dumpToFile(List<Relationship> srl, String fName, int format) {

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            if (format == 1) { // RAW NIDs
                for (Relationship sr : srl) {
                    bw.write(sr.sourceId + "\t" + sr.typeId + "\t" + sr.destinationId + "\t" + sr.group + "\r\n");
                }
            }
            
            if (format == 6) { // Distribution Form
                int index = 0;
                bw.write("RELATIONSHIPID\t" + "CONCEPTID1\t" + "RELATIONSHIPTYPE\t"
                        + "CONCEPTID2\t" + "CHARACTERISTICTYPE\t" + "REFINABILITY\t"
                        + "RELATIONSHIPGROUP\r\n");
                for (Relationship sr : srl) {
                    // RELATIONSHIPID + CONCEPTID1 + RELATIONSHIPTYPE +
                    // CONCEPTID2
                    bw.write("#" + index + "\t" + sr.sourceId + "\t" + sr.typeId + "\t" + sr.destinationId
                            + "\t");
                    // CHARACTERISTICTYPE + REFINABILITY + RELATIONSHIPGROUP
                    bw.write("NA\t" + "NA\t" + sr.group + "\r\n");
                    index += 1;
                }
            }
            bw.flush();
            bw.close();
        
        } catch (IOException e) {
            // can be caused by new FileWriter
            Logger.getLogger(Relationship.class.getName()).log(Level.SEVERE, null, e);
        }
    }
} // class Relationship


