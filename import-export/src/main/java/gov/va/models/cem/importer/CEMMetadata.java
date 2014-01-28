/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.models.cem.importer;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 *
 * @author alo
 */
public class CEMMetadata {

    public static ConceptSpec CEM_DATA_REFSET
            = new ConceptSpec("CEM data reference set (foundation metadata concept)",
            UUID.fromString("1b8ce0d6-3002-5077-8679-17af5ea3f84b"));
    
    public static ConceptSpec CEM_TYPE_REFSET
            = new ConceptSpec("CEM type reference set (foundation metadata concept)",
            UUID.fromString("c0ef465c-b52f-58d0-a9fa-532b66924ec4"));
    
    public static ConceptSpec CEM_KEY_REFSET
            = new ConceptSpec("CEM key reference set (foundation metadata concept)",
            UUID.fromString("d114baab-81b5-571c-ab11-f9b5187c9115"));
    
    public static ConceptSpec CEM_IFO_REFSET
            = new ConceptSpec("CEM info reference set (foundation metadata concept)",
            UUID.fromString("86972cde-c300-503c-be3c-a68f775d604a"));

    public static ConceptSpec CEM_COMPOSITION_REFSET
            = new ConceptSpec("CEM composition reference set (foundation metadata concept)",
            UUID.fromString("67d71185-d10c-542e-af71-e0662304ec43"));

    public static ConceptSpec CEM_CONSTRAINTS_REFSET
            = new ConceptSpec("CEM constraints reference set (foundation metadata concept)",
            UUID.fromString("7daac070-1510-552b-9b6c-633af8a5e5fa"));
    
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
    
    public static ConceptSpec CEM_ITEM
            = new ConceptSpec("CEM item (foundation metadata concept)",
            UUID.fromString("40efbde4-dd8b-5eda-b2ff-b35e25a961d3"));
    
    public static ConceptSpec CEM_LINK
            = new ConceptSpec("CEM link (foundation metadata concept)",
            UUID.fromString("77170ffa-9b54-571a-9f54-10a44510abf4"));
    
    public static ConceptSpec CEM_CARDINALITY_CONSTRAINT
            = new ConceptSpec("CEM cardinality constraint (foundation metadata concept)",
            UUID.fromString("64308168-4fb7-5acf-8aa5-a36bfe9aee7f"));
    
    public static ConceptSpec CEM_NORMAL_CONSTRAINT
            = new ConceptSpec("CEM normal constraint (foundation metadata concept)",
            UUID.fromString("49afac2f-cca1-5e9c-8339-d02076c038c4"));
    
     public static ConceptSpec CEM_DOMAIN_CONSTRAINT
            = new ConceptSpec("CEM domain constraint (foundation metadata concept)",
            UUID.fromString("e34f77cf-9c8c-5d3e-bfd9-cc4af025764b"));
     
     public static ConceptSpec CEM_CODE_FIELD
            = new ConceptSpec("CEM code field (foundation metadata concept)",
            UUID.fromString("d7fb3716-45b4-5b3d-bfa6-3dae69ec2b73"));
     
     public static ConceptSpec CEM_UNIT_FIELD
            = new ConceptSpec("CEM unit field (foundation metadata concept)",
            UUID.fromString("2f3fe6d9-6a7c-5d05-aa4f-3ebde57fff83"));
    
}
