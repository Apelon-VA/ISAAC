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
 * ComponentDescriptionHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.util;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * ComponentDescriptionHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class ComponentDescriptionHelper {
	private ComponentDescriptionHelper() {}
	
	public static String getComponentDescription(int nid) {
		return getComponentDescription(WBUtility.getComponentVersion(nid));
	}
	public static String getComponentDescription(UUID uuid) {
		return getComponentDescription(WBUtility.getComponentVersion(uuid));
	}
	public static String getComponentDescription(ComponentVersionBI component) {
		ComponentType type = ComponentTypeHelper.getComponentType(component);

		String description = null;

		switch(type) {
		case Concept: {
			ConceptChronicleBI concept = null;
			if (component instanceof ConceptAttributeVersionBI) {
				concept = ((ConceptAttributeVersionBI<?>) component).getEnclosingConcept();
			} else if (component instanceof ConceptChronicleBI) {
				concept = (ConceptChronicleBI)component;
			}

			String fsn = WBUtility.getFullySpecifiedName(concept);
			UUID uuid = concept.getPrimordialUuid();

			// Concept with FSN: <FSN> with UUID: <Concept UUID>
			description = ComponentType.Concept.name() + " \"" + fsn + "\" \nwith UUID: " + uuid;
			break;

		}

		case Description: {
			DescriptionVersionBI<?> descriptionVersion = (DescriptionVersionBI<?>)component;
			String typeName = WBUtility.getConPrefTerm(descriptionVersion.getTypeNid());
			String term = descriptionVersion.getText();
			ConceptVersionBI containingConcept = WBUtility.getConceptVersion(descriptionVersion.getConceptNid());
			String containingConceptFSN = WBUtility.getFullySpecifiedName(containingConcept);
			UUID containingConceptUuid = containingConcept.getPrimordialUuid();

			// The <TYPE> term type with text: <TERM> in Concept with FSN: <FSN> with UUID: <Description UUID>
			description = typeName + " \"" + term + "\" \nin concept with FSN \"" + containingConceptFSN + "\" \nwith UUID: " + containingConceptUuid;
			break;
		}

		case Refex:
			//The refex member with Referenced Component UUID: <REF_COMP_UUID> in Refex: <REFEX_UUID> having with UUID: <Refex MEMEBER UUID>
			RefexVersionBI<?> refexVersion = (RefexVersionBI<?>)component;
			UUID referencedComponent = WBUtility.getComponentVersion(refexVersion.getReferencedComponentNid()).getPrimordialUuid();
			int assemblageNid = refexVersion.getAssemblageNid();
			ComponentVersionBI assemblageComponentVersion = WBUtility.getComponentVersion(assemblageNid);
			UUID assemblageUuid = assemblageComponentVersion.getPrimordialUuid();
			description = ComponentType.Refex.name() + " member " + refexVersion.getPrimordialUuid() + " \nwith referenced component " + referencedComponent + " \nin refex " + assemblageUuid;
			break;

		case RefexDynamic:
			//The refex Dynamic member with Referenced Component UUID: <REF_COMP_UUID> in Refex: <REFEX_UUID> having with UUID: <Refex MEMEBER UUID>
			RefexDynamicVersionBI<?> refexDynamicVersion = (RefexDynamicVersionBI<?>)component;
			UUID dynamicReferencedComponent = WBUtility.getComponentVersion(refexDynamicVersion.getReferencedComponentNid()).getPrimordialUuid();
			int assemblageDynamicNid = refexDynamicVersion.getAssemblageNid();
			ConceptVersionBI assemblageDynamicComponentVersion = WBUtility.getConceptVersion(assemblageDynamicNid);
			UUID assemblageDynamicUuid = assemblageDynamicComponentVersion.getPrimordialUuid();
			try {
				if (assemblageDynamicComponentVersion.isAnnotationStyleRefex()) {
					description = ComponentType.RefexDynamic.name() + " annotated member " + refexDynamicVersion.getPrimordialUuid() + " \nwith referenced component " + dynamicReferencedComponent + " \nin refex " + assemblageDynamicUuid;
				} else {
					description = ComponentType.RefexDynamic.name() + " regular member " + refexDynamicVersion.getPrimordialUuid() + " \nwith referenced component " + dynamicReferencedComponent + " \nin refex " + assemblageDynamicUuid;
				}
			} catch (Exception e) {
				description = "Error accessing Refex Annotation type\n" + ComponentType.RefexDynamic.name() + " member " + refexDynamicVersion.getPrimordialUuid() + " \nwith referenced component " + dynamicReferencedComponent + " \nin refex " + assemblageDynamicUuid;
			}
			break;

		case Relationship: {
			RelationshipVersionBI<?> relationshipVersion = (RelationshipVersionBI<?>)component;
			UUID relationshipUuid = relationshipVersion.getPrimordialUuid();
			String typeName = WBUtility.getConPrefTerm(relationshipVersion.getTypeNid());
			ConceptVersionBI sourceConcept = WBUtility.getConceptVersion(relationshipVersion.getOriginNid());
			String sourceConceptFSN = WBUtility.getFullySpecifiedName(sourceConcept);
			ConceptVersionBI destinationConcept = WBUtility.getConceptVersion(relationshipVersion.getDestinationNid());
			String destinationConceptFSN = WBUtility.getFullySpecifiedName(destinationConcept);

			// The <TYPE> relationship type with source FSN: <SOURCE_FSN> and destination FSN: <DESTINATION_FSN> with UUID: <Relationship UUID>
			description = ComponentType.Relationship.name() + " type \"" + typeName + "\" \nwith source FSN: \"" + sourceConceptFSN + "\" \nand destination FSN: \"" + destinationConceptFSN + "\" \nwith UUID: " + relationshipUuid;
			break;
		}

		default:
			throw new IllegalArgumentException("Unsupported ComponentVersionBI: " + component.getClass().getName());
		}

		return description;
	}
}
