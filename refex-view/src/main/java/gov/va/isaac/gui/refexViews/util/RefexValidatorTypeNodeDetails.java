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
package gov.va.isaac.gui.refexViews.util;

import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * {@link RefexValidatorTypeNodeDetails}
 *
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexValidatorTypeNodeDetails
{
	protected Node nodeForDisplay;
	protected ObservableList<ReadOnlyStringProperty> boundToAllValid = new ObservableListWrapper<>(new ArrayList<>());
	protected ObjectProperty<RefexDynamicDataBI> validatorData = new SimpleObjectProperty<RefexDynamicDataBI>();
	
	/**
	 * @return the nodeForDisplay
	 */
	public Node getNodeForDisplay()
	{
		return nodeForDisplay;
	}
	/**
	 * @return things that were bound to the allValid parameter.
	 */
	public ObservableList<ReadOnlyStringProperty> getBoundToAllValid()
	{
		return boundToAllValid;
	}
	/**
	 * @return the validatorData
	 */
	public ObjectProperty<RefexDynamicDataBI> getValidatorDataProperty()
	{
		return validatorData;
	}
}