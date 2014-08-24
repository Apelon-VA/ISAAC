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
package gov.va.isaac.gui.refexViews.refexCreation;

import javafx.scene.Parent;

/**
 * 
 * {@link PanelControllers}
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public interface PanelControllers {
	//This method will allow the injection of the Parent ScreenPane
	void finishInit(ScreensController screenController, Parent parent); 
	Parent getParent();
	void initialize();  // for FXML
}
