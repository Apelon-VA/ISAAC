package gov.va.isaac.ie.exporter;

import gov.va.isaac.util.ProgressReporter;


/**
 * Generically represents an Exporter.
 */
public interface Exporter extends ProgressReporter {

  /**
   * Exports the nid.
   *
   * @param pathNid the path nid
   * @throws Exception 
   */
  public void export(int pathNid) throws Exception;
  
  /**
   * Cancel.
   */
  public void cancel();
}
