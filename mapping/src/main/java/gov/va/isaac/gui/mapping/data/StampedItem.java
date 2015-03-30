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
import gov.va.isaac.util.OTFUtility;
import gov.va.isaac.util.Utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

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
	
	private SimpleStringProperty authorSSP = new SimpleStringProperty("-");
	private SimpleStringProperty moduleSSP = new SimpleStringProperty("-");;
	private SimpleStringProperty pathSSP   = new SimpleStringProperty("-");;
	
	private int authorNid;
	private int moduleNid;
	private int pathNid;
	
	protected void readStampDetails(ComponentVersionBI componentVersion) throws IOException
	{
		authorNid = componentVersion.getAuthorNid();
		moduleNid = componentVersion.getModuleNid();
		pathNid   = componentVersion.getPathNid();
		
		authorUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(authorNid);
		creationTime = componentVersion.getTime();
		isActive = componentVersion.getStatus() == Status.ACTIVE;
		moduleUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(moduleNid);
		pathUUID = ExtendedAppContext.getDataStore().getUuidPrimordialForNid(pathNid);
		
		Utility.execute(() ->
		{
			String authorName = OTFUtility.getDescription(authorUUID);
			String moduleName = OTFUtility.getDescription(moduleUUID);
			String pathName =   OTFUtility.getDescription(pathUUID);
			Platform.runLater(() -> {
				authorSSP.set(authorName);
				moduleSSP.set(moduleName);
				pathSSP.set(pathName);
			});
		});
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

	public SimpleStringProperty getStatusProperty() { return new SimpleStringProperty(isActive? "Active" : "Inactive"); }
	public SimpleStringProperty getTimeProperty()   {
		SimpleStringProperty property = new SimpleStringProperty();
		try {
			property.set(new SimpleDateFormat("MM/dd/yy HH:mm").format(creationTime));
		} catch (Exception e) {
			//TODO something
		}
		return property;
	}
	public SimpleStringProperty getAuthorProperty() { return authorSSP; }
	public SimpleStringProperty getModuleProperty() { return moduleSSP; }
	public SimpleStringProperty getPathProperty()   { return pathSSP; }
	
	public int getAuthorNid() { return authorNid; }
	public int getModuleNid() { return moduleNid; }
	public int getPathNid()   { return pathNid; }
	

}
