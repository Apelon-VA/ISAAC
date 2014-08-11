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
import javafx.scene.layout.Region;
import org.jvnet.hk2.annotations.Contract;

//TODO get rid of this confusing contract... need to not go back and forth between Refset and Refex.
//This is actually used as an info-model viewer, which is kind of a different animal.
/**
 * RefsetViewI
 * 
 * An interface that allows the creation of a RefsetView implementation, which 
 * will be a JavaFX component that extends {@link Region} that can be embedded
 * into other views 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface RefsetViewI extends ViewI
{
	/**
	 * Tell this view to display a particular refset
	 */
	public void setRefsetAndComponent(UUID refsetUUID, UUID componentUUID);
	
	/**
	 * Tell this view to display activeOnly in the current view coordinate, or 
	 * all values.  Implementations should default this value to true.
	 * @param activeOnly
	 */
	public void setViewActiveOnly(boolean activeOnly);
}
