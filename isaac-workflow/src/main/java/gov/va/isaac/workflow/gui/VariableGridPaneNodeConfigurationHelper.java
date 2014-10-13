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
 * VariableGridPaneNodeConfigurationHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.workflow.gui;

import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.CommonMenusDataProvider;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * VariableGridPaneNodeConfigurationHelper
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
class VariableGridPaneNodeConfigurationHelper {
	private VariableGridPaneNodeConfigurationHelper() {}
	
	static Node configureNode(Node node) {
		if (node instanceof Label) {
			Label label = (Label)node;
			
			label.setPadding(new Insets(5));

			label.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.SECONDARY) {
						CommonMenusDataProvider dp = new CommonMenusDataProvider() {
							@Override
							public String[] getStrings() {
								List<String> items = new ArrayList<>();
								items.add(label.getText());

								String[] itemArray = items.toArray(new String[items.size()]);

								return itemArray;
							}
						};

						ContextMenu cm = new ContextMenu();
						CommonMenus.addCommonMenus(cm, dp);

						label.setContextMenu(cm);
					} 
				}
			});
		} else if (node instanceof TextInputControl) {
			TextInputControl textInputControl = (TextInputControl)node;
			
			textInputControl.setPadding(new Insets(5));

			textInputControl.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.SECONDARY) {
						CommonMenusDataProvider dp = new CommonMenusDataProvider() {
							@Override
							public String[] getStrings() {
								List<String> items = new ArrayList<>();
								items.add(textInputControl.getText());

								String[] itemArray = items.toArray(new String[items.size()]);

								return itemArray;
							}
						};

						ContextMenu cm = new ContextMenu();
						CommonMenus.addCommonMenus(cm, dp);

						textInputControl.setContextMenu(cm);
					} 
				}
			});
		}

		return node;
	}
}
