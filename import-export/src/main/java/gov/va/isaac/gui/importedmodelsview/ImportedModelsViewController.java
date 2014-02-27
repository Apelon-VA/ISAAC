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
package gov.va.isaac.gui.importedmodelsview;

import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.util.InformationModelTypeStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

/**
 * Controller class for the {@link ImportedModelsView}.
 *
 * @author ocarlsen
 */
public class ImportedModelsViewController {

    private static final String ALL = "All";

    /**
     * A {@link StringConverter} for the internal {@link ComboBox}.
     * Interprets a {@code null} as the {@link #ALL} value.
     */
    private static final class MyStringConverter extends InformationModelTypeStringConverter {

        @Override
        public String toString(InformationModelType modelType) {
            if (modelType == null) {
                return ALL;
            }
            return super.toString(modelType);
        }
    }

    @FXML private BorderPane borderPane;
    @FXML private ComboBox<InformationModelType> modelTypeCombo;
    @FXML private ProgressIndicator lookupProgress;
    @FXML private ListView importedModels;

    private final StringConverter<InformationModelType> converter = new MyStringConverter();
    private final BooleanProperty lookupRunning = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {

        // Populate modelTypeCombo.
        modelTypeCombo.setConverter(converter);
        modelTypeCombo.setItems(gatherComboBoxItems());

        // Bind progress indicator visibility to whether lookup task is running or not.
        lookupProgress.visibleProperty().bind(lookupRunning);

        // TODO: Implement real handler.
        modelTypeCombo.valueProperty().addListener(new ChangeListener<InformationModelType>() {
            @Override
            public void changed(ObservableValue<? extends InformationModelType> observable,
                    InformationModelType oldValue,
                    InformationModelType newValue) {
                System.out.println("oldValue="+oldValue);
                System.out.println("newValue="+newValue);
            }
        });
    }

    public BorderPane getRoot() {
        return borderPane;
    }

    private ObservableList<InformationModelType> gatherComboBoxItems() {
        ObservableList<InformationModelType> items = FXCollections.observableArrayList();
        items.addAll(InformationModelType.values());

        // We add this null to be interpreted as "All".
        // However, the ComboBox will not use the StringConverter on null values.
        // Instead, it uses the prompt text.
        items.add(null);
        modelTypeCombo.setPromptText(ALL);

        return items;
    }
}
