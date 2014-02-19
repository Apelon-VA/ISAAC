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
package gov.va.isaac.gui.importview;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;

/**
 * A utility for assembling the {@link ImportView}.
 *
 * @author ocarlsen
 */
public class ImportViewBuilder {

    private final ImportView importView;

    private int rowIndex = 0;

    public ImportViewBuilder(ImportView importView) {
        super();
        this.importView = importView;
    }

    public void addRow(String labelText, Node fxNode) {

        // Column 0.
        Label label = new Label(labelText);
        importView.add(label, 0, rowIndex);

        // Column 1.
        importView.add(fxNode, 1, rowIndex);

        // Increment row index.
        ++rowIndex;
    }

    public void addSeparator() {
        importView.add(new Separator(), 0, rowIndex++, 2, 1);
    }
}
