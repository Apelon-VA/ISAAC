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
    
//TODO come back and figure out what is actually neeeded here.  Can't figure out why we need two new getters below.
    //The setConcept methods from the parent concept aren't doing the right thing in implementation, as they are currently
    //putting up a popup, which isn't part of the spec.  If this wants to be both popup and non-popup, it should extends PopupConceptView, and use those APIs.
    //In fact, the only API call I can think of that even belongs here is a all to change the view type.  Everything else just duplicates and confuses already existing APIs.
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

    
    //TODO Dan notes:  The 4 interface methods below don't make any sense to me.  Why would we pass in a Stage?  
    //And why are two of the protected, rather than public?  The implementation of a EnhancedConceptView is PerLookup, 
    //not a singleton.  So any caller that requests one, is going to be handed and instance.  Why should they have 
    //to pass back in the stage of the view that contains the instance to simply change the concept?  Stage doesn't
    //belong here as a parameter at all.  If we were to have the case where you want to declare a public method
    //that allows changing the concept of any artibrary enhanced concept view... then it should be a static method, 
    //which takes in an enhancedconceptview, not a stage... but I have no idea why you would do that... 
    
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
