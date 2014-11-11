package gov.va.isaac.request.uscrs;

import gov.va.isaac.request.ContentRequestTrackingInfo;

/**
 * USCRS implementation of a {@link ContentRequestTrackingInfo}.
 *
 * @author bcarlsenca
 */
public class UscrsContentRequestTrackingInfo implements
    ContentRequestTrackingInfo {

  /** The name. */
  private String name;

  /** The detail. */
  private String detail;

  /** The is successful. */
  private boolean isSuccessful;
  
  /**  The file. */
  private String file;

  /**
   * Instantiates an empty {@link UscrsContentRequestTrackingInfo}.
   */
  public UscrsContentRequestTrackingInfo() {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.request.ContentRequestTrackingInfo#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.request.ContentRequestTrackingInfo#getType()
   */
  @Override
  public String getType() {
    return "USCRS";
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.request.ContentRequestTrackingInfo#getUrl()
   */
  @Override
  public String getUrl() {
    return "https://uscrs.nlm.nih.gov/";
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.request.ContentRequestTrackingInfo#getDetail()
   */
  @Override
  public String getDetail() {
    return detail;
  }

  /**
   * Sets the detail.
   *
   * @param detail the detail
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.request.ContentRequestTrackingInfo#isSuccessful()
   */
  @Override
  public boolean isSuccessful() {
    return isSuccessful;
  }

  /**
   * Sets the is successful.
   *
   * @param isSuccessful the is successful
   */
  public void setIsSuccessful(boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
  }

  /**
   * @return the file
   */
  public String getFile() {
    return file;
  }

  /**
   * @param file the file to set
   */
  public void setFile(String file) {
    this.file = file;
  }

}
