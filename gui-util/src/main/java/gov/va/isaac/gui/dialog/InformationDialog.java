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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A {@link Stage} which can be used to show an information dialog.
 *
 * @author ocarlsen
 */
public class InformationDialog extends Stage {

    private final InformationDialogController controller;

    public InformationDialog(Window owner) throws IOException {
        super();
        setResizable(false);

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);

        // Load from FXML.
        URL resource = InformationDialog.class.getResource("InformationDialog.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = (Parent) loader.load();
        setScene(new Scene(root));

        this.controller = loader.getController();
        controller.setVariables(this);
        
        //Problem on linux, where modal windows don't always stay on top...
        iconifiedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                Platform.runLater(() -> {toFront();});
            }
        });
    }

    public void setVariables(String title, String message) {
        this.setTitle(title);
        controller.setMessageText(message == null || message.length() == 0 ? title : message);
    }
}
