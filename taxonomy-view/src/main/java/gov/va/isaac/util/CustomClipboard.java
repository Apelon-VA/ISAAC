package gov.va.isaac.util;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * A wrapper simplifying access to a {@link Clipboard} object.
 *
 * @author ocarlsen
 */
public class CustomClipboard {

    private final static Clipboard CLIPBOARD = Clipboard.getSystemClipboard();

    public static void set(String content) {

        // Sanity check.
        if (content == null) { return; }

        ClipboardContent cc = new ClipboardContent();
        cc.putString(content);
        CLIPBOARD.setContent(cc);
    }
}
