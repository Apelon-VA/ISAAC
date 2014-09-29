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

/**
 * ComponentTypeHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow;

import java.util.UUID;

import gov.va.isaac.util.WBUtility;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * ComponentTypeHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class ComponentTypeHelper {
	private ComponentTypeHelper() {}
	
	public static ComponentType getComponentType(ComponentVersionBI component) {
		if (component instanceof DescriptionVersionBI) {
			return ComponentType.Description;
		} else if (component instanceof ConceptVersionBI || component instanceof ConceptAttributeVersionBI) {
			return ComponentType.Concept;
		} else if (component instanceof RefexVersionBI) {
			return ComponentType.Refex;
		} else if  (component instanceof RelationshipVersionBI) {
			return ComponentType.Relationship;
		} else {
			throw new IllegalArgumentException("Unsupported ComponentVersionBI: " + component.getClass().getName());
		}
	}
	
	public static ComponentType getComponentType(int nid) {
		return getComponentType(WBUtility.getComponentVersion(nid));
	}
	
	public static ComponentType getComponentType(UUID uuid) {
		return getComponentType(WBUtility.getComponentVersion(uuid));
	}
}
