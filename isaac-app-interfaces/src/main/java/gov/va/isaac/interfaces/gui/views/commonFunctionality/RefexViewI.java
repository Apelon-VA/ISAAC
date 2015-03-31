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
package gov.va.isaac.interfaces.gui.views.commonFunctionality;

import gov.va.isaac.interfaces.gui.views.EmbeddableViewI;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.layout.Region;
import org.jvnet.hk2.annotations.Contract;

/**
 * RefsetViewI
 * 
 * An interface that allows the creation of a RefexView implementation, which 
 * will be a JavaFX component that extends {@link Region} that can be embedded
 * into other views, the purpose of which is to display Refex information for 
 * a component.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface RefexViewI extends EmbeddableViewI
{
	/**
	 * Tell this view to display the refexes for a particular component (typically, a concept, but could also 
	 * be any valid thing that supports refexes)
	 * 
	 * With this call - multiple assemblages will be shown - for a single component (concept)
	 * 
	 * @param componentNid - the component to show.
	 * @param showStampColumns - (optional) if provided, don't show toggle buttons for show/hiding the stamp, instead bind to this.
	 * @param showActiveOnly - (optional) if provided, don't show toggle buttons for activeOnly / all, instead bind to this.
	 * @param showFullHistory - (optional) if provided, don't show toggle buttons for show current / show all, instead bind to this.
	 * @param displayFSNButton - if true, shows a button for toggling FSN / preferred, otherwise, doesn't show the button.
	 */
	public void setComponent(int componentNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton);
	
	/**
	 * Tell this view to display the refexes for a particular assemblage concept.
	 * 
	 * With this call - only a single assemblage will be shown - but multiple components may be shown (typically concepts, 
	 * but could also be any valid thing that supports refexes, such as descriptions)
	 * 
	 * @param assemblageConceptNid - the assemblage to show
	 * @param showStampColumns - (optional) if provided, don't show toggle buttons for show/hiding the stamp, instead bind to this.
	 * @param showActiveOnly - (optional) if provided, don't show toggle buttons for activeOnly / all, instead bind to this.
	 * @param showFullHistory - (optional) if provided, don't show toggle buttons for show current / show all, instead bind to this.
	 * @param displayFSNButton - if true, shows a button for toggling FSN / preferred, otherwise, doesn't show the button.
	 */
	public void setAssemblage(int assemblageConceptNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton);
}
