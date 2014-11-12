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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The Class ConceptGroup.
 * Represents a concept group
 */
public class ConceptGroup extends ArrayList<Concept> {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new concept group.
     *
     * @param conceptList the concepts list
     * @param sort true if the list needs to be sorted
     */
    public ConceptGroup(List<Concept> conceptList, boolean sort) {
        super();
        if (sort)
            Collections.sort(conceptList);
        this.addAll(conceptList);
    }

    /**
     * Instantiates a new concept group.
     *
     * @param conceptCollection the concepts list a Strings collection
     */
    public ConceptGroup(Collection<String> conceptCollection) {
        super();
        for (String conceptId : conceptCollection)
            this.add(new Concept(toInteger(conceptId), false));
        Collections.sort(this);
    }

    /**
     * Instantiates a new concept group.
     *
     * @param concepts the concepts list a Integer Array
     */
    public ConceptGroup(ArrayList<Integer> concepts) {
        super();
        // :NYI: defined or not_defined is indeterminate coming from classifier
        // callback.
        for (Integer concept : concepts)
            this.add(new Concept(concept.intValue(), false));
        Collections.sort(this);
    }

    /**
     * Instantiates a new concept group.
     *
     * @param concept a concept
     */
    public ConceptGroup(Concept concept) {
        super();
        this.add(concept); //
    }

    /**
     * Instantiates a new concept group.
     */
    public ConceptGroup() {
        super();
    }

    /**
     * toInteger. Transforms the String value to Integer
     *
     * @param id the string value
     * @return the integer value
     */
    static private int toInteger(final String id) {
        return Integer.parseInt(String.valueOf(id));
    }

}

