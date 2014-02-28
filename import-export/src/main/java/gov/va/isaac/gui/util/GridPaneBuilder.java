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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * A utility for assembling a {@link GridPane}.
 *
 * @author ocarlsen
 */
public class GridPaneBuilder {

    private final GridPane gridPane;

    private int rowIndex = 0;

    public GridPaneBuilder(GridPane gridPane) {
        super();
        this.gridPane = gridPane;
    }

    public void addRow(String labelText, Node fxNode) {

        // Column 0.
        Label label = new Label(labelText);
        gridPane.add(label, 0, rowIndex);

        // Column 1.
        gridPane.add(fxNode, 1, rowIndex);

        // Increment row index.
        ++rowIndex;
    }

    /**
     * @param fxNode A component to span two columns.
     */
    public void addRow(Node fxNode) {
        gridPane.add(fxNode, 0, rowIndex++, 2, 1);
    }
}
