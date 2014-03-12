/**
 * Copyright Notice
 * 
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.util;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import com.sun.javafx.tk.Toolkit;

/**
 * Helper class encapsulating various Java FX helper utilities.
 *
 * @author ocarlsen
 */
public class FxUtils {

    public static DropShadow redDropShadow = new DropShadow();
    public static DropShadow greenDropShadow = new DropShadow();
    public static DropShadow lightGreenDropShadow = new DropShadow();
    static
    {
        redDropShadow.setColor(Color.RED);
        greenDropShadow.setColor(Color.GREEN);
        lightGreenDropShadow.setColor(Color.LIGHTGREEN);
    }
    
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
