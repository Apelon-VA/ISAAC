
package gov.va.isaac.models.cem;

/**
 * Represents a CEM component.
 */
public class CEMComponent {

  /** The component type. */
  private CEMComponentType componentType;

  /** The data type reference - this is a pointer to another model. */
  private String dateTypeRef;

  /** The name. */
  private String name;

  /** The value */
  private String value;

  /** The name. */
  private String cardinalityMin;

  /** The name. */
  private String cardinalityMax;

  /**
   * Instantiates a {@link CEMComponent}.
   */
  public CEMComponent() {
    // do nothing
  }

  /**
   * Returns the component type.
   *
   * @return the component type
   */
  public CEMComponentType getComponentType() {
    return componentType;
  }

  /**
   * Sets the component type.
   *
   * @param componentType the componentType to set
   */
  public void setComponentType(CEMComponentType componentType) {
    this.componentType = componentType;
  }

  /**
   * Returns the date type ref.
   *
   * @return the dateTypeRef
   */
  public String getDateTypeRef() {
    return dateTypeRef;
  }

  /**
   * Sets the date type ref.
   *
   * @param dateTypeRef the dateTypeRef to set
   */
  public void setDateTypeRef(String dateTypeRef) {
    this.dateTypeRef = dateTypeRef;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns the cardinality min.
   *
   * @return the cardinalityMin
   */
  public String getCardinalityMin() {
    return cardinalityMin;
  }

  /**
   * Sets the cardinality min.
   *
   * @param cardinalityMin the cardinalityMin to set
   */
  public void setCardinalityMin(String cardinalityMin) {
    this.cardinalityMin = cardinalityMin;
  }

  /**
   * Returns the cardinality max.
   *
   * @return the cardinalityMax
   */
  public String getCardinalityMax() {
    return cardinalityMax;
  }

  /**
   * Sets the cardinality max.
   *
   * @param cardinalityMax the cardinalityMax to set
   */
  public void setCardinalityMax(String cardinalityMax) {
    this.cardinalityMax = cardinalityMax;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((cardinalityMax == null) ? 0 : cardinalityMax.hashCode());
    result =
        prime * result
            + ((cardinalityMin == null) ? 0 : cardinalityMin.hashCode());
    result =
        prime * result
            + ((componentType == null) ? 0 : componentType.hashCode());
    result =
        prime * result + ((dateTypeRef == null) ? 0 : dateTypeRef.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEMComponent other = (CEMComponent) obj;
    if (cardinalityMax == null) {
      if (other.cardinalityMax != null)
        return false;
    } else if (!cardinalityMax.equals(other.cardinalityMax))
      return false;
    if (cardinalityMin == null) {
      if (other.cardinalityMin != null)
        return false;
    } else if (!cardinalityMin.equals(other.cardinalityMin))
      return false;
    if (componentType != other.componentType)
      return false;
    if (dateTypeRef == null) {
      if (other.dateTypeRef != null)
        return false;
    } else if (!dateTypeRef.equals(other.dateTypeRef))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
