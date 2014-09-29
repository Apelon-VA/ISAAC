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
package gov.va.isaac.workflow;

import java.util.UUID;

import gov.va.isaac.util.WBUtility;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
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
			description = ComponentType.Concept.name() + " \"" + fsn + "\" with UUID: " + uuid;
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
			description = typeName + " \"" + term + "\" in concept with FSN \"" + containingConceptFSN + "\" with UUID: " + containingConceptUuid;
			break;
		}

		case Refex:
			//The refex member with Referenced Component UUID: <REF_COMP_UUID> in Refex: <REFEX_UUID> having with UUID: <Refex MEMEBER UUID>
			RefexVersionBI<?> refexVersion = (RefexVersionBI<?>)component;
			UUID referencedComponent = WBUtility.getComponentVersion(refexVersion.getReferencedComponentNid()).getPrimordialUuid();
			int assemblageNid = refexVersion.getAssemblageNid();
			ComponentVersionBI assemblageComponentVersion = WBUtility.getComponentVersion(assemblageNid);
			UUID assemblageUuid = assemblageComponentVersion.getPrimordialUuid();
			description = ComponentType.Refex.name() + " member " + refexVersion.getPrimordialUuid() + " with referenced component " + referencedComponent + " in refex " + assemblageUuid;
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
			description = ComponentType.Relationship.name() + " type \"" + typeName + "\" with source FSN: \"" + sourceConceptFSN + "\" and destination FSN: \"" + destinationConceptFSN + "\" with UUID: " + relationshipUuid;
			break;
		}

		default:
			throw new IllegalArgumentException("Unsupported ComponentVersionBI: " + component.getClass().getName());
		}

		return description;
	}
}
