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
package gov.va.isaac.gui.mapping.data;

import gov.va.isaac.ExtendedAppContext;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

/**
 * {@link StampedItem}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class StampedItem
{
	private UUID authorUUID;
	private UUID moduleUUID;
	private UUID pathUUID;
	private long creationTime;
	private boolean isActive;
	
	protected void readStampDetails(ComponentVersionBI componentVersion) throws IOException
	{
		authorUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(componentVersion.getAuthorNid());
		creationTime = componentVersion.getTime();
		isActive = componentVersion.getStatus() == Status.ACTIVE;
		moduleUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(componentVersion.getModuleNid());
		pathUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(componentVersion.getPathNid());
	}
	
	/**
	 * @return the authorName - a UUID that identifies a concept that represents the Author
	 */
	public UUID getAuthorName()
	{
		return authorUUID;
	}

	/**
	 * @return the creationDate
	 */
	public long getCreationDate()
	{
		return creationTime;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive()
	{
		return isActive;
	}

	/**
	 * @return the moduleUUID
	 */
	public UUID getModuleUUID()
	{
		return moduleUUID;
	}

	/**
	 * @return the pathUUID
	 */
	public UUID getPathUUID()
	{
		return pathUUID;
	}
}
