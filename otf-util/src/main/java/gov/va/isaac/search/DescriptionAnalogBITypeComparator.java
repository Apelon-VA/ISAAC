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
package gov.va.isaac.search;

import gov.va.isaac.util.OTFUtility;

import java.util.Comparator;

import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Comparator} for {@link SearchResult} objects.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DescriptionAnalogBITypeComparator implements Comparator<DescriptionAnalogBI<?>> {
	protected static final Logger LOG = LoggerFactory.getLogger(DescriptionAnalogBITypeComparator.class);

    @Override
    public int compare(DescriptionAnalogBI<?> o1, DescriptionAnalogBI<?> o2) {
        String o1matchingComponentType = OTFUtility.getConPrefTerm(o1.getTypeNid()).trim();
        String o2matchingComponentType = OTFUtility.getConPrefTerm(o2.getTypeNid()).trim();

        final String fullySpecifiedNameStr = "Fully specified name";
        if (o1matchingComponentType.equalsIgnoreCase(fullySpecifiedNameStr)) {
        	return -1;
        } else if (o2matchingComponentType.equalsIgnoreCase(fullySpecifiedNameStr)) {
        	return 1;
        }
        
        // else both FSNs
        final String synonymStr = "Synonym";
        if (o1matchingComponentType.equalsIgnoreCase(synonymStr)) {
        	return -1;
        } else if (o2matchingComponentType.equalsIgnoreCase(synonymStr)) {
        	return 1;
        }
        
        // else same score and FSN and preferred description and both Synonyms
        return 0;
    }
}
