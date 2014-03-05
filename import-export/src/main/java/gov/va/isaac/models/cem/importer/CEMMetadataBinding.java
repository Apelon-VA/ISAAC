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
package gov.va.isaac.models.cem.importer;

import gov.va.isaac.models.util.MetadataBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Convenience class with constants extracted from {@link CEMMetadataCreator} output.
 *
 * @author alo
 * @author ocarlsen
 */
public class CEMMetadataBinding extends MetadataBinding {

    public static ConceptSpec CEM_REFSET
            = new ConceptSpec("CEM reference sets (foundation metadata concept)",
            UUID.fromString("cb6035e9-b943-5d56-bfa3-0c6f2f413dc3"));

    public static ConceptSpec CEM_DATA_REFSET
            = new ConceptSpec("CEM data reference set (foundation metadata concept)",
            UUID.fromString("1b8ce0d6-3002-5077-8679-17af5ea3f84b"));

    public static ConceptSpec CEM_TYPE_REFSET
            = new ConceptSpec("CEM type reference set (foundation metadata concept)",
            UUID.fromString("c0ef465c-b52f-58d0-a9fa-532b66924ec4"));

    public static ConceptSpec CEM_KEY_REFSET
            = new ConceptSpec("CEM key reference set (foundation metadata concept)",
            UUID.fromString("d114baab-81b5-571c-ab11-f9b5187c9115"));

    public static ConceptSpec CEM_INFO_REFSET
            = new ConceptSpec("CEM info reference set (foundation metadata concept)",
            UUID.fromString("86972cde-c300-503c-be3c-a68f775d604a"));

    public static ConceptSpec CEM_COMPOSITION_REFSET
            = new ConceptSpec("CEM composition reference set (foundation metadata concept)",
            UUID.fromString("67d71185-d10c-542e-af71-e0662304ec43"));

    public static ConceptSpec CEM_CONSTRAINTS_REFSET
            = new ConceptSpec("CEM constraints reference set (foundation metadata concept)",
            UUID.fromString("7daac070-1510-552b-9b6c-633af8a5e5fa"));

    public static ConceptSpec CEM_CONSTRAINTS_PATH_REFSET
            = new ConceptSpec("CEM constraints path reference set (foundation metadata concept)",
            UUID.fromString("f0ca1b2f-4616-5596-b6a2-d86355ef177b"));

    public static ConceptSpec CEM_CONSTRAINTS_VALUE_REFSET
    		= new ConceptSpec("CEM constraints value reference set (foundation metadata concept)",
    		UUID.fromString("46a7a28a-8fc6-5b31-8a4e-ae7b00729456"));

    public static ConceptSpec CEM_VALUE_REFSET
    		= new ConceptSpec("CEM value reference set (foundation metadata concept)",
    		UUID.fromString("AAA81c5fc48-da1f-50f5-8440-5a2fa850ad73"));

    public static ConceptSpec CEM_ATTRIBUTES
            = new ConceptSpec("CEM attributes (foundation metadata concept)",
            UUID.fromString("271fb6f9-8fe1-552d-8c8e-a7d6fa9d8119"));

    public static ConceptSpec CEM_PQ
            = new ConceptSpec("CEM PQ data type (foundation metadata concept)",
            UUID.fromString("8bc4069e-b09b-53a5-9526-6a87dc855e64"));

    public static ConceptSpec CEM_CD
            = new ConceptSpec("CEM CD data type (foundation metadata concept)",
            UUID.fromString("ed65304a-3cf8-5ef3-8e64-39be0159388a"));

    public static ConceptSpec CEM_QUAL
            = new ConceptSpec("CEM qualifier (foundation metadata concept)",
            UUID.fromString("7963d8ce-c5aa-5d8a-96ae-ee5d597ecd9b"));

    public static ConceptSpec CEM_MOD
            = new ConceptSpec("CEM modifier (foundation metadata concept)",
            UUID.fromString("7f79313c-f9df-5096-b104-2532bcbb8ad0"));

    public static ConceptSpec CEM_ATTR
            = new ConceptSpec("CEM attribution (foundation metadata concept)",
            UUID.fromString("d411d80a-54f9-5121-a5a1-0c7565bab85c"));

    public static ConceptSpec CEM_LINK
            = new ConceptSpec("CEM link (foundation metadata concept)",
            UUID.fromString("77170ffa-9b54-571a-9f54-10a44510abf4"));

     public static ConceptSpec CEM_CODE_FIELD
            = new ConceptSpec("CEM code field (foundation metadata concept)",
            UUID.fromString("d7fb3716-45b4-5b3d-bfa6-3dae69ec2b73"));

     public static ConceptSpec CEM_UNIT_FIELD
            = new ConceptSpec("CEM unit field (foundation metadata concept)",
            UUID.fromString("2f3fe6d9-6a7c-5d05-aa4f-3ebde57fff83"));

    // TODO convert the above to an enum
     
     public static List<ConceptSpec> getAllRefsets()
     {
         try
         {
             ArrayList<ConceptSpec> allConceptSpec = new ArrayList<>();
             
             allConceptSpec.add(CEM_DATA_REFSET);
    		 allConceptSpec.add(CEM_KEY_REFSET);
             allConceptSpec.add(CEM_TYPE_REFSET);
             allConceptSpec.add(CEM_INFO_REFSET);
             allConceptSpec.add(CEM_COMPOSITION_REFSET);

             return allConceptSpec;
         }
         catch (Exception e)
         {
             throw new RuntimeException("Unexpected!", e);
         }
     }


     public static List<ConceptSpec> getAll() {
         return getAll(CEMMetadataBinding.class);
     }
}
