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

/**
 * ViewCoordinatePreferencesPlugin
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences.plugins;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;

import java.util.Collection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * ViewCoordinatePreferencesPlugin
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class CoordinatePreferencesPluginView implements PreferencesPluginViewI {
	private HBox hBox = null;
	protected ValidBooleanBinding allValid_ = null;
	
	private final ObjectProperty<StatedInferredOptions> currentStatedInferredOptionProperty = new SimpleObjectProperty<>();
	private final StringProperty currentPathProperty = new SimpleStringProperty();

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getValidationFailureMessage()
	 */
	@Override
	public ReadOnlyStringProperty validationFailureMessageProperty() {
		return allValid_.getReasonWhyInvalid();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getNode()
	 */
	@Override
	public Node getNode() {
		if (hBox == null) {
			allValid_ = new ValidBooleanBinding() {
				{
					bind(currentStatedInferredOptionProperty, currentPathProperty);
					setComputeOnInvalidate(true);
				}
				
				@Override
				protected boolean computeValue() {
					if (currentStatedInferredOptionProperty.get() == null) {
						this.setInvalidReason("Null/unset/unselected StatedInferredOption");

						return false;
					}
					if (currentPathProperty.get() == null || currentPathProperty.get().length() == 0) {
						this.setInvalidReason("Null/unset/unselected path");

						return false;
					}

					this.clearInvalidReason();
					return true;
				}
			};
			
			VBox statedInferredToggleGroupVBox = new VBox();
			ToggleGroup statedInferredToggleGroup = new ToggleGroup();
			for (StatedInferredOptions option : StatedInferredOptions.values()) {
				RadioButton optionButton = new RadioButton(option.value());
				optionButton.setUserData(option);
				statedInferredToggleGroup.getToggles().add(optionButton);
				statedInferredToggleGroupVBox.getChildren().add(optionButton);
			}
			statedInferredToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override
				public void changed(
						ObservableValue<? extends Toggle> observable,
						Toggle oldValue, Toggle newValue) {
					currentStatedInferredOptionProperty.set((StatedInferredOptions)newValue.getUserData());
				}	
			});

			ComboBox<String> pathComboBox = new ComboBox<>();
			pathComboBox.getItems().addAll(getPathOptions());
			currentPathProperty.bind(pathComboBox.getSelectionModel().selectedItemProperty());
			
			// TODO load/set current preferences values
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();

			final StatedInferredOptions storedStatedInferredOption = getStoredStatedInferredOption();
			for (Toggle toggle : statedInferredToggleGroup.getToggles()) {
				if (toggle.getUserData() == storedStatedInferredOption) {
					toggle.setSelected(true);
				}
			}
			
			// ComboBox
			final String storedPath = getStoredPath();
			pathComboBox.getSelectionModel().select(storedPath);
			
			hBox = new HBox();
			hBox.getChildren().addAll(pathComboBox, statedInferredToggleGroupVBox);
		}
		
		return hBox;
	}

	protected abstract Collection<String> getPathOptions();
	protected abstract String getStoredPath();
	
	protected abstract StatedInferredOptions getStoredStatedInferredOption();

	public ReadOnlyObjectProperty<StatedInferredOptions> currentStatedInferredOptionProperty() {
		return currentStatedInferredOptionProperty;
	}
	
	public ReadOnlyStringProperty currentPathProperty() {
		return currentPathProperty;
	}
}
