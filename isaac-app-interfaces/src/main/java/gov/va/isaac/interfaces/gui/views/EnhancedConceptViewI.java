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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.interfaces.gui.views;

import java.util.UUID;

import javafx.scene.Node;
import javafx.stage.Stage;
/**
 * {@link EnhancedConceptViewI}
 * 
 * An interface that requests a pop up window, pane, and enables modifying existing popup window that displays concept information.
 *
 * @author <a href="mailto:jefron@apelon.com">Jesse Efron</a> 
 */

public interface EnhancedConceptViewI extends ConceptViewI {
    public enum ViewType {
    	SIMPLE_VIEW, DETAIL_VIEW, HISTORICAL_VIEW;
	}
    

	/**
	 * Update the pane  to show the selected concept.
	 * @param conceptUuid
	 * @return 
	 */
    public Node getConceptViewerPanel(UUID conceptUUID);
    
	/**
	 * Update the pane to show the selected concept.
	 * @param conceptNid
	 * @return 
	 */
    public Node getConceptViewerPanel(int conceptNid);

	/**
	 * Update the popup window to show the selected concept.
	 * @param conceptUuid
	 * @return 
	 */
    public Node changeConcept(Stage stage, UUID conceptUUID, ViewType view);

	/**
	 * Update the popup window to show the selected concept.
	 * @param conceptNid
	 * @return 
	 */
    public Node changeConcept(Stage stage, int conceptNid, ViewType view);

	Node changeViewType(Stage stage, UUID conceptUUID, ViewType view);

	Node changeViewType(Stage stage, int conceptNid, ViewType view);
}
