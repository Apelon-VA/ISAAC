package gov.va.isaac.util;


import java.util.ArrayList;
import java.util.List;

/**
 * Reference implementation of {@link ProgressReporter}
 */
public abstract class AbstractProgressReporter implements ProgressReporter {

  /** Listeners */
  private List<ProgressListener> listeners = new ArrayList<>();

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
  }

  /**
   * Adds a {@link ProgressListener}.
   * @param l thef{@link ProgressListener}
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /**
   * Removes a {@link ProgressListener}.
   * @param l thef{@link ProgressListener}
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }
}