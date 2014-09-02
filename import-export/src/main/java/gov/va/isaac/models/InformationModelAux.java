package gov.va.isaac.models;

import gov.va.isaac.model.InformationModelType;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Like {@link TermAux}, this has concepts specs related to information models.
 * For UUIDs related to information model types, see
 * {@link InformationModelType}.
 *
 * @author bcarlsenca
 */
public class InformationModelAux {

  // Hierarchy
  
  /**  The information model root. */
  public static ConceptSpec INFORMATION_MODEL_ROOT = new ConceptSpec(
      "Information Models",
      UUID.fromString("ab09b185-b93d-577b-a350-622be832e6c7"));

  // Refsets and columns

  /**  The information model properties refset. */
  public static ConceptSpec INFORMATION_MODEL_PROPERTIES_REFSET =
      new ConceptSpec("Information model property refset",
          UUID.fromString("30a58969-ccb5-5a98-91aa-38df9d8bccf2"));

  // Other metadata
  
  /**  The "associated with" rel type.
   * NOTE: this creates a dependency on SNOMED being loaded
   */
  public static ConceptSpec HAS_TERMINOLOGY_CONCEPT =
      new ConceptSpec("Has terminology concept",
          UUID.fromString("5d46206a-aa37-5b9c-905a-f33990cc0209"));  
  
}
