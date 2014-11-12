package gov.va.isaac.classifier;
import gov.va.isaac.util.ProgressReporter;

/**
 * Generically represents a classifier.
 */
public interface Classifier extends ProgressReporter {

  /**
   * Classify.
   * @param pathNid 
   * @throws Exception 
   */
  public void classify(int pathNid) throws Exception;
  
  /**
   * Cancel.
   */
  public void cancel();
}
