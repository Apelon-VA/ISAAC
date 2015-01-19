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

import gov.va.isaac.config.generated.StatedInferredOptions;
import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;
import gov.va.isaac.util.OTFUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * ViewCoordinatePreferencesPlugin
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public abstract class CoordinatePreferencesPluginView implements PreferencesPluginViewI {
	protected HBox hBox = null;
	protected ValidBooleanBinding allValid_ = null;
	
	protected ToggleGroup statedInferredToggleGroup = null;
	protected ComboBox<UUID> pathComboBox = null;
	
	protected final ObjectProperty<StatedInferredOptions> currentStatedInferredOptionProperty = new SimpleObjectProperty<>();
	protected final ObjectProperty<UUID> currentPathProperty = new SimpleObjectProperty<>();

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getValidationFailureMessage()
	 */
	@Override
	public ReadOnlyStringProperty validationFailureMessageProperty() {
		return allValid_.getReasonWhyInvalid();
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getRegion()
	 */
	@Override
	public Region getContent() {
		if (hBox == null) {
			VBox statedInferredToggleGroupVBox = new VBox();
			statedInferredToggleGroup = new ToggleGroup();
			List<RadioButton> statedInferredOptionButtons = new ArrayList<>();
			for (StatedInferredOptions option : StatedInferredOptions.values()) {
				RadioButton optionButton = new RadioButton(option.value());
				optionButton.setUserData(option);
				optionButton.setTooltip(new Tooltip("Default StatedInferredOption is " + getDefaultStatedInferredOption()));
				optionButton.setPadding(new Insets(5,5,5,5));
				statedInferredToggleGroup.getToggles().add(optionButton);
				statedInferredToggleGroupVBox.getChildren().add(optionButton);
				statedInferredOptionButtons.add(optionButton);
			}
			statedInferredToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override
				public void changed(
						ObservableValue<? extends Toggle> observable,
						Toggle oldValue, Toggle newValue) {
					currentStatedInferredOptionProperty.set((StatedInferredOptions)newValue.getUserData());
				}	
			});

			pathComboBox = new ComboBox<>();
			pathComboBox.setCellFactory(new Callback<ListView<UUID>, ListCell<UUID>> () {
				@Override
				public ListCell<UUID> call(ListView<UUID> param) {
					final ListCell<UUID> cell = new ListCell<UUID>() {
						@Override
						protected void updateItem(UUID c, boolean emptyRow) {
							super.updateItem(c, emptyRow);

							if(c == null) {
								setText(null);
							}else {
								String desc = OTFUtility.getDescription(c);
								setText(desc);
							}
						}
					};

					return cell;
				}
			});
			pathComboBox.setButtonCell(new ListCell<UUID>() {
				@Override
				protected void updateItem(UUID c, boolean emptyRow) {
					super.updateItem(c, emptyRow); 
					if (emptyRow) {
						setText("");
					} else {
						String desc = OTFUtility.getDescription(c);
						setText(desc);
					}
				}
			});
			currentPathProperty.bind(pathComboBox.getSelectionModel().selectedItemProperty());
			
			
			// ComboBox
			pathComboBox.setTooltip(new Tooltip("Default path is \"" + OTFUtility.getDescription(getDefaultPath()) + "\""));
			pathComboBox.setPadding(new Insets(5,5,5,5));

			hBox = new HBox();
			hBox.getChildren().addAll(pathComboBox, statedInferredToggleGroupVBox);

			allValid_ = new ValidBooleanBinding() {
				{
					bind(currentStatedInferredOptionProperty, currentPathProperty);
					setComputeOnInvalidate(true);
				}

				@Override
				protected boolean computeValue() {
					if (currentStatedInferredOptionProperty.get() == null) {
						this.setInvalidReason("Null/unset/unselected StatedInferredOption");
						for (RadioButton button : statedInferredOptionButtons) {
							TextErrorColorHelper.setTextErrorColor(button);
						}
						return false;
					} else {
						for (RadioButton button : statedInferredOptionButtons) {
							TextErrorColorHelper.clearTextErrorColor(button);
						}
					}
					if (currentPathProperty.get() == null) {
						this.setInvalidReason("Null/unset/unselected path");
						TextErrorColorHelper.setTextErrorColor(pathComboBox);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(pathComboBox);
					}
					if (OTFUtility.getConceptVersion(currentPathProperty.get()) == null) {
						this.setInvalidReason("Invalid path");
						TextErrorColorHelper.setTextErrorColor(pathComboBox);

						return false;
					} else {
						TextErrorColorHelper.clearTextErrorColor(pathComboBox);
					}

					this.clearInvalidReason();
					return true;
				}
			};
		}
		
		// Don't know why this should be necessary, but without this the UUID itself is displayed
		pathComboBox.setButtonCell(new ListCell<UUID>() {
			@Override
			protected void updateItem(UUID c, boolean emptyRow) {
				super.updateItem(c, emptyRow); 
				if (emptyRow) {
					setText("");
				} else {
					String desc = OTFUtility.getDescription(c);
					setText(desc);
				}
			}
		});

		// Reload persisted values every time
		final StatedInferredOptions storedStatedInferredOption = getStoredStatedInferredOption();
		for (Toggle toggle : statedInferredToggleGroup.getToggles()) {
			if (toggle.getUserData() == storedStatedInferredOption) {
				toggle.setSelected(true);
			}
		}
		
		pathComboBox.getItems().clear();
		pathComboBox.getItems().addAll(getPathOptions());
		final UUID storedPath = getStoredPath();
		pathComboBox.getSelectionModel().select(storedPath);
		
		return hBox;
	}

	protected abstract Collection<UUID> getPathOptions();
	protected abstract UUID getStoredPath();
	protected abstract UUID getDefaultPath();
	
	protected abstract StatedInferredOptions getStoredStatedInferredOption();
	protected abstract StatedInferredOptions getDefaultStatedInferredOption();

	public ReadOnlyObjectProperty<StatedInferredOptions> currentStatedInferredOptionProperty() {
		return currentStatedInferredOptionProperty;
	}
	
	public ReadOnlyObjectProperty<UUID> currentPathProperty() {
		return currentPathProperty;
	}
}
