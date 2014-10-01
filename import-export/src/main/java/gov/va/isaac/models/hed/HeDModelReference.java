
package gov.va.isaac.models.hed;

/**
 * Represents a HeD "modelReference" entry.
 */
public class HeDModelReference {

  /**  The description. */
  private String description = null;

  /**  The referenced model. */
  private String referencedModel = null;

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the referenced model.
   *
   * @return the referencedModel
   */
  public String getReferencedModel() {
    return referencedModel;
  }

  /**
   * Sets the referenced model.
   *
   * @param referencedModel the referencedModel to set
   */
  public void setReferencedModel(String referencedModel) {
    this.referencedModel = referencedModel;
  }
}
