package gov.va.isaac.gui.util;

import com.sun.javafx.tk.Toolkit;

/**
 * Helper class encapsulating various Java FX helper utilities.
 *
 * @author ocarlsen
 */
public class FxUtils {

    // Do not instantiate.
    private FxUtils() {};

    /**
     * Makes sure thread is NOT the FX application thread.
     */
    public static void checkBackgroundThread() {
        // Throw exception if on FX user thread
        if (Toolkit.getToolkit().isFxUserThread()) {
            throw new IllegalStateException("Not on background thread; currentThread = "
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Wrapper around {@link Toolkit#isFxUserThread()} for easy access.
     */
    public static void checkFxUserThread() {
        Toolkit.getToolkit().checkFxUserThread();
    }
}
