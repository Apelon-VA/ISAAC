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
package gov.va.isaac.models.fhim.importer;

import gov.va.isaac.models.util.MetadataBindingBase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Convenience class with constants extracted from {@link FHIMMetadataCreator} output.
 *
 * @author ocarlsen
 */
public class FHIMMetadataBinding extends MetadataBindingBase {

    public static ConceptSpec FHIM_REFSET
            = new ConceptSpec("FHIM reference sets (foundation metadata concept)",
            UUID.fromString("f62ef2c9-0f73-5435-b4ec-bdfd4deede6f"));

    public static ConceptSpec FHIM_MODELS_REFSET
            = new ConceptSpec("FHIM Models reference set (foundation metadata concept)",
            UUID.fromString("10c31e7f-942b-5513-bacc-85d8778c616c"));

    public static ConceptSpec FHIM_CLASSES_REFSET
            = new ConceptSpec("FHIM Classes reference set (foundation metadata concept)",
            UUID.fromString("8225419e-292c-5e45-b9cf-cb357620cf30"));

    public static ConceptSpec FHIM_ENUMERATIONS_REFSET
            = new ConceptSpec("FHIM Enumerations reference set (foundation metadata concept)",
            UUID.fromString("9e71077c-65c6-5622-be6b-d8fcdf439b2d"));

    public static ConceptSpec FHIM_ENUMERATIONVALUES_REFSET
            = new ConceptSpec("FHIM EnumerationValues reference set (foundation metadata concept)",
            UUID.fromString("f25d1b3a-6200-5663-af68-dc944140ef27"));

    public static ConceptSpec FHIM_ATTRIBUTES_REFSET
            = new ConceptSpec("FHIM Attributes reference set (foundation metadata concept)",
            UUID.fromString("7f47616e-206d-58f3-b386-38a41619a5cd"));

    public static ConceptSpec FHIM_DEFAULTVALUES_REFSET
            = new ConceptSpec("FHIM DefaultValues reference set (foundation metadata concept)",
            UUID.fromString("0950b1e1-bb50-57a6-94b8-c546afa9dc15"));

    public static ConceptSpec FHIM_RELATIONSHIPS_REFSET
            = new ConceptSpec("FHIM Relationships reference set (foundation metadata concept)",
            UUID.fromString("aa170d80-3c37-5e52-bb0f-4468c5bfd201"));

    public static ConceptSpec FHIM_CONSTRAINTS_REFSET
            = new ConceptSpec("FHIM Constraints reference set (foundation metadata concept)",
            UUID.fromString("48db0d60-a32a-535e-8525-b48b78ea45f4"));

    public static ConceptSpec FHIM_CODE
            = new ConceptSpec("FHIM Code (foundation metadata concept)",
            UUID.fromString("2ee93643-38d5-54c1-bd77-ce1bd8ed843a"));

    public static ConceptSpec FHIM_OBSERVATIONQUALIFIER
            = new ConceptSpec("FHIM ObservationQualifier (foundation metadata concept)",
            UUID.fromString("1acf39ba-40ec-5157-8f1c-b0849d8e11b4"));

    public static ConceptSpec FHIM_OBSERVATIONSTATEMENT
            = new ConceptSpec("FHIM ObservationStatement (foundation metadata concept)",
            UUID.fromString("46532d6a-bc7f-56b3-b130-f3df5436d9c5"));

    public static ConceptSpec FHIM_PHYSICALQUANTITY
            = new ConceptSpec("FHIM PysicalQuantity (foundation metadata concept)",
            UUID.fromString("b9565ceb-1734-5893-ade9-a706e3107ef6"));

    public static ConceptSpec FHIM_PULSEPOSITION
            = new ConceptSpec("FHIM PulsePosition (foundation metadata concept)",
            UUID.fromString("e05f10cb-09e0-5cbe-809b-eaa09d770e53"));

    public static ConceptSpec FHIM_GENERALIZATION
            = new ConceptSpec("FHIM Generalization (foundation metadata concept)",
            UUID.fromString("f326a54e-3d05-5a73-b075-9487fc324eb3"));

    public static ConceptSpec FHIM_ASSOCIATION
            = new ConceptSpec("FHIM Association (foundation metadata concept)",
            UUID.fromString("19d14f12-56de-5ced-8964-efa4f9fea1b7"));

    public static ConceptSpec FHIM_DEPENDENCY
            = new ConceptSpec("FHIM Dependency (foundation metadata concept)",
            UUID.fromString("09cfd668-0196-541c-bfda-720522c65cb6"));

    public static ConceptSpec FHIM_MULTIPLICITY
            = new ConceptSpec("FHIM Multiplicity (foundation metadata concept)",
            UUID.fromString("433e0078-c273-52bb-8e85-4ac2d22da4cc"));

    public static List<ConceptSpec> getAllRefsets()
    {
        try
        {
            ArrayList<ConceptSpec> allConceptSpec = new ArrayList<>();

            allConceptSpec.add(FHIM_MODELS_REFSET);
            allConceptSpec.add(FHIM_CLASSES_REFSET);
            allConceptSpec.add(FHIM_ATTRIBUTES_REFSET);
            allConceptSpec.add(FHIM_DEFAULTVALUES_REFSET);
            allConceptSpec.add(FHIM_RELATIONSHIPS_REFSET);
            allConceptSpec.add(FHIM_CONSTRAINTS_REFSET);

            return allConceptSpec;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unexpected!", e);
        }
    }

    public static List<ConceptSpec> getAll() {
        return getAll(FHIMMetadataBinding.class);
    }
}
