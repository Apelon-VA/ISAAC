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
package gov.va.isaac.gui.dialog;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A {@link Stage} which can be used to show an export settings dialog.
 *
 * @author ocarlsen
 */
public class ExportFileSettingsDialog extends Stage {

    private final ExportFileSettingsDialogController controller;

    public ExportFileSettingsDialog(Window parent) throws IOException {
        super();
        setTitle("Export Settings");
        setResizable(true);

        initOwner(parent);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.DECORATED);

        // Load from FXML.
        URL resource = this.getClass().getResource("ExportFileSettingsDialog.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root);
        setScene(scene);

        this.controller = loader.getController();
        controller.setVariables(this, parent);
    }
}
