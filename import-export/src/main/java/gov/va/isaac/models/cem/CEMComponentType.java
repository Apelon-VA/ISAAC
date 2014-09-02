package gov.va.isaac.models.cem;

/**
 * Enumeration of CEM component types.
 */
public enum CEMComponentType {

  /**  The qualifier. */
  QUALIFIER("qual"),
  
  /**  The modifier. */
  MODIFIER("mod"),
  
  /**  The attribution. */
  ATTRIBUTION("att");
  
  /**  The ceml tag name. */
  private String cemlTagName;
  
  /**
   * CEM component type.
   *
   * @param cemlTagName the ceml tag name
   */
  private CEMComponentType(String cemlTagName) {
    this.cemlTagName = cemlTagName;
  }
  
  /**
   * Returns the ceml tag name.
   *
   * @return the ceml tag name
   */
  public String getCemlTagName() {
    return cemlTagName;
  }
}