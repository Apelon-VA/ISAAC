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
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.util;

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
