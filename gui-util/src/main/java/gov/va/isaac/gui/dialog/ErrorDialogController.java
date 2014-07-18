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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controller class for {@link ErrorDialog}.
 *
 * @author ocarlsen
 */
public class ErrorDialogController {

    @FXML private AnchorPane rootPane;
    @FXML private TextArea detailsTextArea;
    @FXML private Label messageLabel;
    @FXML private Button okButton;

    @FXML
    public void initialize() {

        // Bind detailMessage layout to whether or not it is visible.
        detailsTextArea.managedProperty().bind(detailsTextArea.visibleProperty());

        okButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
    }

    public void setMessageText(String message) {
        messageLabel.setText(message);
    }

    public void setDetailsText(String details) {
        if (details == null || details.length() == 0) {
            this.detailsTextArea.setVisible(false);
        } else {
            this.detailsTextArea.setText(details);
            this.detailsTextArea.setVisible(true);
        }
    }
}
