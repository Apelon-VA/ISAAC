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
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.Utility;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

import javax.inject.Inject;

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

		okButton_.setOnAction((e) -> saveAll());

		cancelButton_.setOnAction((e) -> stage_.close());
	}

	void setStage(PreferencesView stage) {
		this.stage_ = stage;
	}
	
	public void aboutToShow()
	{
		// load fields before initializing allValid_
		// in case plugin.validationFailureMessageProperty() initialized by getNode()
		tabPane_.getTabs().clear();
		for (PreferencesPluginViewI plugin : plugins_) {
			Tab pluginTab = new Tab(plugin.getName());
			pluginTab.setGraphic(plugin.getNode());
		}

		allValid_ = new ValidBooleanBinding() {
			{
				ArrayList<ReadOnlyStringProperty> pluginValidationFailureMessages = new ArrayList<>();
				for (PreferencesPluginViewI plugin : plugins_) {
					pluginValidationFailureMessages.add(plugin.validationFailureMessageProperty());
				}
				bind(pluginValidationFailureMessages.toArray(new StringProperty[pluginValidationFailureMessages.size()]));
				setComputeOnInvalidate(true);
			}
			
			@Override
			protected boolean computeValue() {
				for (PreferencesPluginViewI plugin : plugins_) {
					if (plugin.validationFailureMessageProperty().get() != null) {
						this.setInvalidReason(plugin.validationFailureMessageProperty().get());
						return false;
					}
				}
				
				this.clearInvalidReason();
				return true;
			}
		};

		okButton_.disableProperty().bind(allValid_.not());
		// set focus on default
		// Platform.runLater(...);
	}
	
	private void saveAll() {
		System.out.println("performing save...");
		
		final Map<PreferencesPluginViewI, Exception> caughtExceptions = Collections.synchronizedMap(new WeakHashMap<>());
		
		for (PreferencesPluginViewI plugin : plugins_) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						plugin.save();
					} catch (IOException e) {
						caughtExceptions.put(plugin, e);
					}
				}
			};
			
			Utility.execute(r);
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
		}
	}
}
