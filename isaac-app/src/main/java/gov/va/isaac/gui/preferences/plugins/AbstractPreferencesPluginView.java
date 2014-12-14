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

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.config.profiles.UserProfile;
import gov.va.isaac.config.profiles.UserProfileManager;
import gov.va.isaac.config.users.InvalidUserException;
import gov.va.isaac.gui.preferences.properties.PreferencesPluginProperty;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractPreferencesPluginView
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */

public abstract class AbstractPreferencesPluginView implements PreferencesPluginViewI {
	private Logger logger = LoggerFactory.getLogger(AbstractPreferencesPluginView.class);

	protected final String name;
	
	private GridPane gridPane = null;
	protected ValidBooleanBinding allValid_ = null;

	private final Set<PreferencesPluginProperty<?, ? extends Control>> properties = new HashSet<>();

	protected AbstractPreferencesPluginView(String name, PreferencesPluginProperty<?, ? extends Control>...properties) {
		this.name = name;
		this.properties.addAll(Arrays.asList(properties));
	}

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
	public Region getContent() {
		if (gridPane == null) {
			gridPane = new GridPane();

			int row = 0;
			for (PreferencesPluginProperty property : properties) {
				property.getLabel().setPadding(new Insets(5, 5, 5, 5));
				property.getControl().setTooltip(new Tooltip("Default is " + property.getStringConverter().convertToString(property.readFromDefaults())));

				property.getBinder().apply(property);
				property.getControlCurrentPreferenceValueSetter().apply(property);
			
				gridPane.setMaxWidth(Double.MAX_VALUE);
				
				gridPane.addRow(row++, property.getLabel(), property.getControl());
				
				property.getGuiFormattingApplicator().apply(property);
			}

			allValid_ = new ValidBooleanBinding() {
				{
					List<ValidBooleanBinding> bindings = new ArrayList<>();
					for (PreferencesPluginProperty<?, ? extends Control> property : properties) {
						logger.debug("Binding plugin {} ValidBooleanBinding to \"{}\"", getName(), property.getName());
						bindings.add(property.getValidator());
					}
					bind(bindings.toArray(new ValidBooleanBinding[bindings.size()]));
					setComputeOnInvalidate(true);
				}
				
				@Override
				protected boolean computeValue() {
					for (PreferencesPluginProperty<?, ? extends Control> property : properties) {
						//logger.debug("Plugin \"{}\" checking bound property \"{}\"", getName(), property.getName());
						if (! property.getValidator().get()) {
							this.setInvalidReason(property.getValidator().getReasonWhyInvalid().get());

							logger.debug("Plugin \"{}\" invalidated because {}", getName(), property.getValidator().getReasonWhyInvalid().get());

							return false;
						} else {
							//logger.debug("Plugin \"{}\" bound property \"{}\" is OK", getName(), property.getName());
						}
					}

					this.clearInvalidReason();
					return true;
				}
			};
		}
		
		return gridPane;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#getName()
	 */
	@Override
	public final String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.PreferencesPluginViewI#save()
	 */
	@Override
	public void save() throws IOException {
		logger.debug("Saving {} preferences", getName());
		
		for (PreferencesPluginProperty property : properties) {
			property.writeToPreferences();
		}
		try {
			UserProfile loggedIn = ExtendedAppContext.getCurrentlyLoggedInUserProfile();
			AppContext.getService(UserProfileManager.class).saveChanges(loggedIn);
		} catch (InvalidUserException e) {
			String msg = "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage() + " attempting to save UserProfile";
			
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
}
