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

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import com.google.common.base.Preconditions;

/**
 * Controller class for {@link InformationDialog}.
 *
 * @author ocarlsen
 */
public class InformationDialogController {

    @FXML private Label messageLabel;

    private InformationDialog informationDialog;

    @FXML
    public void initialize() {
    }

    public void setMessageText(String message) {
        messageLabel.setText(Preconditions.checkNotNull(message));
    }

    public void handleOk() {
        informationDialog.close();
    }

    public void setVariables(InformationDialog informationDialog) {
        this.informationDialog = informationDialog;
    }
}
