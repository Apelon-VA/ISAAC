
package gov.va.isaac.models.cem;

/**
 * Represents a CEM constraint.
 */
public class CEMConstraint {

  /**  The path. */
  private String path;

  /**  The value. */
  private String value;

  /**
   * Instantiates a {@link CEMConstraint}.
   */
  public CEMConstraint() {
    // do nothing
  }

  /**
   * Returns the path.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path.
   *
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
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
   * Returns the ceml tag name.
   *
   * @return the ceml tag name
   */
  public static String getCemlTagName() {
    return "constraint";
  }
  
  /**
   * Sets the value.
   *
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
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
    CEMConstraint other = (CEMConstraint) obj;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }


}
