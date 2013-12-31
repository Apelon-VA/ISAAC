package gov.va.isaac.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

/**
 * A {@link Label} that supports copying.
 *
 * @author ocarlsen
 */
public class CopyableLabel extends Label {

    public CopyableLabel() {
        super();
        addCopyMenu();
    }

    public CopyableLabel(String text) {
        super(text);
        addCopyMenu();
    }

    public CopyableLabel(String text, Node graphic) {
        super(text, graphic);
        addCopyMenu();
    }

    private void addCopyMenu() {
        addCopyMenu(this);
    }

    public static void addCopyMenu(final Label targetLabel) {
        MenuItem mi = new MenuItem("Copy");

        // Add acion handler to copy label text.
        mi.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent ignore) {
                CustomClipboard.set(targetLabel.getText());
            }
        });

        targetLabel.setContextMenu(new ContextMenu(mi));
    }
}
