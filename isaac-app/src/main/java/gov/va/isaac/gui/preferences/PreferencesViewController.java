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
 * PreferencesViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.preferences;

import gov.va.isaac.AppContext;
import gov.va.isaac.config.profiles.UserProfileBindings;
import gov.va.isaac.gui.util.TextErrorColorHelper;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.IterableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PreferencesViewController
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class PreferencesViewController {
	private Logger logger = LoggerFactory.getLogger(PreferencesViewController.class);

	@Inject
	private IterableProvider<PreferencesPluginViewI> plugins_;

	private @FXML TabPane tabPane_;

	private @FXML Button okButton_;
	private @FXML Button cancelButton_;
	
	private PreferencesView stage_;
	
	private ValidBooleanBinding allValid_ = null;
	
	public PreferencesViewController() {
		AppContext.getServiceLocator().inject(this);
	}

	@FXML
	void initialize()
	{
		assert tabPane_ != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'PreferencesView.fxml'.";
		assert okButton_ != null : "fx:id=\"okButton\" was not injected: check your FXML file 'PreferencesView.fxml'.";
		assert cancelButton_ != null : "fx:id=\"cancelButton_\" was not injected: check your FXML file 'PreferencesView.fxml'.";

		tabPane_.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane_.setMaxWidth(Double.MAX_VALUE);

		okButton_.setOnAction((e) -> saveAndExitIfSuccessful());

		cancelButton_.setOnAction((e) -> stage_.close());
	}

	void setStage(PreferencesView stage) {
		this.stage_ = stage;
	}
	
	public void aboutToShow()
	{
		// Using allValid_ to prevent rerunning content of aboutToShow()
		if (allValid_ == null) {
			// These listeners are for debug and testing only. They may be removed at any time.
			UserProfileBindings  userProfileBindings = AppContext.getService(UserProfileBindings.class);
			for (Property<?> property : userProfileBindings.getAll()) 
			{
				property.addListener(new ChangeListener<Object>()
				{
					@Override
					public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue)
					{
						logger.debug("{} property changed from {} to {}", property.getName(), oldValue, newValue);
					}
				});
			}

			// load fields before initializing allValid_
			// in case plugin.validationFailureMessageProperty() initialized by getNode()
			tabPane_.getTabs().clear();
			List<PreferencesPluginViewI> sortableList = new ArrayList<>();
			Comparator<PreferencesPluginViewI> comparator = new Comparator<PreferencesPluginViewI>() {
				@Override
				public int compare(PreferencesPluginViewI o1, PreferencesPluginViewI o2) {
					if (o1.getTabOrder() == o2.getTabOrder()) {
						return o1.getName().compareTo(o2.getName());
					} else {
						return o1.getTabOrder() - o2.getTabOrder();
					}
				}
			};
			for (PreferencesPluginViewI plugin : plugins_) {
				sortableList.add(plugin);
			}
			Collections.sort(sortableList, comparator);
			for (PreferencesPluginViewI plugin : sortableList) {
				logger.debug("Adding PreferencesPluginView tab \"{}\"", plugin.getName());
				Label tabLabel = new Label(plugin.getName());
				
				tabLabel.setMaxHeight(Double.MAX_VALUE);
				tabLabel.setMaxWidth(Double.MAX_VALUE);
				Tab pluginTab = new Tab();
				pluginTab.setGraphic(tabLabel);
				Region content = plugin.getContent();
				content.setMaxWidth(Double.MAX_VALUE);
				content.setMaxHeight(Double.MAX_VALUE);
				content.setPadding(new Insets(5.0));
				
				Label errorMessageLabel = new Label();
				errorMessageLabel.textProperty().bind(plugin.validationFailureMessageProperty());
				errorMessageLabel.setAlignment(Pos.BOTTOM_CENTER);
				TextErrorColorHelper.setTextErrorColor(errorMessageLabel);

				VBox vBox = new VBox();
				vBox.getChildren().addAll(errorMessageLabel, content);
				vBox.setMaxWidth(Double.MAX_VALUE);
				vBox.setAlignment(Pos.TOP_CENTER);

				plugin.validationFailureMessageProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue,
							String newValue) {
						if (newValue != null && ! StringUtils.isEmpty(newValue)) {
							TextErrorColorHelper.setTextErrorColor(tabLabel);
						} else {
							TextErrorColorHelper.clearTextErrorColor(tabLabel);
						}
					}
				});
				//Initialize, if stored value is wrong
				if (StringUtils.isNotEmpty(plugin.validationFailureMessageProperty().getValue()))
				{
					TextErrorColorHelper.setTextErrorColor(tabLabel);
				}
				pluginTab.setContent(vBox);
				tabPane_.getTabs().add(pluginTab);
			}

			allValid_ = new ValidBooleanBinding() {
				{
					ArrayList<ReadOnlyStringProperty> pluginValidationFailureMessages = new ArrayList<>();
					for (PreferencesPluginViewI plugin : plugins_) {
						pluginValidationFailureMessages.add(plugin.validationFailureMessageProperty());
					}
					bind(pluginValidationFailureMessages.toArray(new ReadOnlyStringProperty[pluginValidationFailureMessages.size()]));
					setComputeOnInvalidate(true);
				}

				@Override
				protected boolean computeValue() {
					for (PreferencesPluginViewI plugin : plugins_) {
						if (plugin.validationFailureMessageProperty().get() != null && plugin.validationFailureMessageProperty().get().length() > 0) {
							this.setInvalidReason(plugin.validationFailureMessageProperty().get());

							logger.debug("Setting PreferencesView allValid_ to false because \"{}\"", this.getReasonWhyInvalid().get());
							return false;
						}
					}

					logger.debug("Setting PreferencesView allValid_ to true");

					this.clearInvalidReason();
					return true;
				}
			};

			okButton_.disableProperty().bind(allValid_.not());
			// set focus on default
			// Platform.runLater(...);
		}
		
		// Reload persisted values every time view opened
		for (PreferencesPluginViewI plugin : plugins_) {
			plugin.getContent();
		}
	}
	
	private void saveAndExitIfSuccessful() {
		logger.debug("performing save...");
		
		final Map<PreferencesPluginViewI, Exception> caughtExceptions = Collections.synchronizedMap(new WeakHashMap<>());
		
		for (PreferencesPluginViewI plugin : plugins_) {		
			try {
				plugin.save();
			} catch (IOException e) {
				caughtExceptions.put(plugin, e);
			}
		}
		
		if (caughtExceptions.size() > 0) {
			String msg = "Caught " + caughtExceptions.size() + " exceptions performing save";
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<PreferencesPluginViewI, Exception> entry : caughtExceptions.entrySet()) {
				builder.append("\n" + "Plugin " + entry.getKey().getName() + " (" + entry.getKey().getClass().getName() + ")" + " threw " + entry.getValue().getClass().getName() + " " + entry.getValue().getLocalizedMessage());
			
				logger.error("Plugin " + entry.getKey().getName() + " (" + entry.getKey().getClass().getName() + ")" + " threw " + entry.getValue().getClass().getName() + " " + entry.getValue().getLocalizedMessage());
				entry.getValue().printStackTrace();
			}
			
			AppContext.getCommonDialogs().showErrorDialog("Preferences Save Error", msg, builder.toString(), stage_);
		} else {
			stage_.close();
		}
	}
}
