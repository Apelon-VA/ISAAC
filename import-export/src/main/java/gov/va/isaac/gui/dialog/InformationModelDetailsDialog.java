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

import gov.va.isaac.models.InformationModel;

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
 * A dialog for displaying an information model details.
 *
 * @author ocarlsen
 */
public class InformationModelDetailsDialog extends Stage {

    private final InformationModelDetailsDialogController controller;

    public InformationModelDetailsDialog(Window owner) throws IOException {
        super();

        setTitle("Information Model Details");
        setResizable(true);

        initOwner(owner);
        initModality(Modality.NONE);
        initStyle(StageStyle.DECORATED);

        // Load from FXML.
        URL resource = this.getClass().getResource("InformationModelDetailsDialog.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root);
        setScene(scene);

        this.controller = loader.getController();
    }

    public void displayModel(InformationModel informationModel) {
        controller.setStage(this);
        controller.displayModel(informationModel);
    }
}
