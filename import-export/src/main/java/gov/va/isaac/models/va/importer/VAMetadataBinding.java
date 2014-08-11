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
package gov.va.isaac.models.va.importer;

import gov.va.isaac.models.util.MetadataBindingBase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Convenience class with constants extracted from {@link FHIMMetadataCreator} output.
 *
 * @author jefron
 */
public class VAMetadataBinding extends MetadataBindingBase {

    public static ConceptSpec VA_METADATA
            = new ConceptSpec("VA Metadata (foundation metadata concept)",
            UUID.fromString("40256a40-4062-5e13-b78c-14d92e62aea2"));

    // Allergens
    public static ConceptSpec VA_ALLERGEN_TYPES
            = new ConceptSpec("Allergen Type (foundation metadata concept)",
            UUID.fromString("5a80c898-299d-582f-b21b-2fa3265ee51c"));

    public static ConceptSpec FOOD_ALLERGEN
            = new ConceptSpec("Food Allergen (foundation metadata concept)",
            UUID.fromString("3203df9c-726c-5a57-b386-184ea808264a"));

    public static ConceptSpec ENVIRONMENTAL_ALLERGEN
            = new ConceptSpec("Environmental Allergen (foundation metadata concept)",
            UUID.fromString("03f95260-3aa1-5b38-b71a-f39f0dc8714f"));

    public static ConceptSpec MEDICATION_ALLERGEN
            = new ConceptSpec("Medication Allergen (foundation metadata concept)",
            UUID.fromString("92bed50a-9609-5f66-9106-eaf1e92629c0"));

	// Workflow
	public static ConceptSpec VA_WORKFLOW_CONCEPTS = new ConceptSpec(
			"VA Workflow Concepts (foundation metadata concept)",
			UUID.fromString("0d93b913-6ebc-5770-91b5-0d7da12efc13"));

	public static ConceptSpec AUTHOR_JEFRON = new ConceptSpec(
			"Jesse Efron (foundation metadata concept)",
			UUID.fromString("6258298b-db7c-5125-bb00-6d896feb5cdc"));

	public static ConceptSpec ASSIGNED_STATE = new ConceptSpec(
			"Assigned Workflow State (foundation metadata concept)",
			UUID.fromString("d28b8887-5951-5d7e-b246-54b70f463f28"));

	public static ConceptSpec DEMO_PROJECT = new ConceptSpec(
			"Demo Project (foundation metadata concept)",
			UUID.fromString("b51f5c4c-4a45-55a3-a2f1-eadf3eb3b961"));
    
    public static List<ConceptSpec> getAllRefsets()
    {
        try
        {
            ArrayList<ConceptSpec> allConceptSpec = new ArrayList<>();

            allConceptSpec.add(VA_METADATA);
            allConceptSpec.add(VA_ALLERGEN_TYPES);
            allConceptSpec.add(FOOD_ALLERGEN);
            allConceptSpec.add(ENVIRONMENTAL_ALLERGEN);
            allConceptSpec.add(MEDICATION_ALLERGEN);

            return allConceptSpec;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unexpected!", e);
        }
    }

    public static List<ConceptSpec> getAll() {
        return getAll(VAMetadataBinding.class);
    }
}
