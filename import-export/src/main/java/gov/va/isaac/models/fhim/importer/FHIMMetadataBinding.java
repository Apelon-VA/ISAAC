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

import gov.va.isaac.models.util.MetadataBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Convenience class with constants extracted from {@link FHIMMetadataCreator} output.
 *
 * @author ocarlsen
 */
public class FHIMMetadataBinding extends MetadataBinding {

    public static ConceptSpec FHIM_REFSET
            = new ConceptSpec("FHIM reference sets (foundation metadata concept)",
            UUID.fromString("f62ef2c9-0f73-5435-b4ec-bdfd4deede6f"));

    public static ConceptSpec FHIM_TYPE_REFSET
            = new ConceptSpec("FHIM type reference set (foundation metadata concept)",
            UUID.fromString("f7894a3c-47e7-5d2d-ba56-74db4b16b83b"));

    public static ConceptSpec FHIM_CODE_REFSET
            = new ConceptSpec("FHIM code reference set (foundation metadata concept)",
            UUID.fromString("5a0a4b9c-18b3-5927-8408-eb98e3e338e8"));

    public static ConceptSpec FHIM_DATA_REFSET
            = new ConceptSpec("FHIM data reference set (foundation metadata concept)",
            UUID.fromString("3da771eb-1031-5857-be6b-56b704b09d63"));

    public static ConceptSpec FHIM_COMPOSITION_REFSET
            = new ConceptSpec("FHIM composition reference set (foundation metadata concept)",
            UUID.fromString("e5acfdfa-a402-5868-9c34-1bc9e15ee9c6"));

    public static ConceptSpec FHIM_CONSTRAINTS_REFSET
            = new ConceptSpec("FHIM constraints reference set (foundation metadata concept)",
            UUID.fromString("45046c7f-ec3f-56cb-b3c8-d588a4ef167f"));

    public static ConceptSpec FHIM_CONSTRAINTS_PATH_REFSET
            = new ConceptSpec("FHIM constraints path reference set (foundation metadata concept)",
            UUID.fromString("1188762f-e6c5-5396-9fdb-1c1ec1909d10"));

    public static ConceptSpec FHIM_CONSTRAINTS_VALUE_REFSET
            = new ConceptSpec("FHIM constraints value reference set (foundation metadata concept)",
            UUID.fromString("aad39bde-43e2-5ec1-aab7-3529dd793234"));

    public static ConceptSpec FHIM_PHYSICALQUANTITY
            = new ConceptSpec("FHIM PysicalQuantity data type (foundation metadata concept)",
            UUID.fromString("28b40275-fa79-5fbe-9c75-715c8d5f74b5"));

    public static List<ConceptSpec> getAllRefsets()
    {
        try
        {
            ArrayList<ConceptSpec> allConceptSpec = new ArrayList<>();

            allConceptSpec.add(FHIM_TYPE_REFSET);
            allConceptSpec.add(FHIM_CODE_REFSET);
            allConceptSpec.add(FHIM_DATA_REFSET);
            allConceptSpec.add(FHIM_COMPOSITION_REFSET);

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
