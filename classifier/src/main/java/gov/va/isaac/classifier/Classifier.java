package gov.va.isaac.classifier;
import gov.va.isaac.util.ProgressReporter;

import org.ihtsdo.otf.tcc.api.nid.IntSet;

/**
 * Generically represents a classifier.
 */
public interface Classifier extends ProgressReporter {

  /**
   * Classify.
   * @param rootNid the root nid
   * @throws Exception 
   */
  public void classify(int rootNid) throws Exception;

  /**
   * Incremental classify.
   *   This can only handle new concepts or relationships.
   *   Retirements will require a full classification.
   *
   * @param conceptSet the set of concepts that is new or has been added to
   * @throws Exception the exception
   */
  public void incrementalClassify(IntSet conceptSet) throws Exception;

  /**
   * Cancel.
   */
  public void cancel();
}
