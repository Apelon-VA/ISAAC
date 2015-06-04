package gov.va.isaac.request;

import java.io.File;


/**
 * Generically represents a handler for content request submission.
 *
 * @author bcarlsenca
 */
public interface ContentRequestHandler {

  /**
   * Submit content request.
   *
   * @param nid the concept nid
   * @return the request tracking info
   * @throws Exception 
   */
  public ContentRequestTrackingInfo submitContentRequest(int nid) throws Exception;
	
//  public ContentRequestTrackingInfo submitContentRequest(int nid, File file) throws Exception;

  
  /**
   * Returns the content request status.
   * PLACEHOLDER for future functionality to allow polling of an
   * automated request system to get the current status so it can be
   * effectively updated in ISAAC.  Presumably, this would involve
   * storing information about the tracking info in the DB somewhere
   * (e.g. dynamic refex).
   * 
   * @param info the tracking info
   * @return the content request status
   */
  public ContentRequestTrackingInfo getContentRequestStatus(ContentRequestTrackingInfo info);

}
