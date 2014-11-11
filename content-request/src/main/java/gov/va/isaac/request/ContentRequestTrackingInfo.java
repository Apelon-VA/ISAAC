package gov.va.isaac.request;

/**
 * Generically represents tracking info about a content request.
 *
 * Later, this can carry more information about the request once
 * it has been submitted.  e.g. "getTrackingId()" and that kind
 * of thing.  Until the request submission is dynamically linked
 * to a system, this isn't really possible.
 * 
 * @author bcarlsenca
 */
public interface ContentRequestTrackingInfo {

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();
  
  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType();
  
  /**
   * Returns the url.
   *
   * @return the url
   */
  public String getUrl();
  
  /**
   * Returns the detail.
   *
   * @return the detail
   */
  public String getDetail();
  
  /**
   * Indicates whether or not successful is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSuccessful();
}
