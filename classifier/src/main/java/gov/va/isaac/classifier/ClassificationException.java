package gov.va.isaac.classifier;

/**
 * Classification exception.
 *
 * @author bcarlsenca
 */
@SuppressWarnings("serial")
public class ClassificationException extends Exception {

  /**
   * Instantiates a {@link ClassificationException} from the specified
   * parameters.
   *
   * @param message the message
   */
  public ClassificationException(String message) {
    super(message);
  }
}
