/**
 * Copyright 2013
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
package gov.va.legoEdit.model.bdbModel;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.util.TimeConvert;
import java.util.UUID;

/**
 * 
 * StampBDB
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */

@Entity
public class StampBDB
{
	@SuppressWarnings("unused")
	private StampBDB()
	{
		// required by BDB
	}

	/**
	 * This should only be called by LegoBDB, otherwise there will be issues with the generating consistent unique IDs for the stamp.
	 * Since nothing about the stamp itself can be used to create a unique ID.
	 * 
	 * @param stamp
	 */
	protected StampBDB(Stamp stamp)
	{
		status = stamp.getStatus();
		time = TimeConvert.convert(stamp.getTime());
		author = stamp.getAuthor();
		module = stamp.getModule();
		path = stamp.getPath();
		stampId = stamp.getUuid();
		if (stampId == null || stampId.length() == 0)
		{
			stampId = UUID.randomUUID().toString();
		}
	}

	@PrimaryKey private String stampId;
	protected String status;
	protected long time;
	protected String author;
	protected String module;
	protected String path;

	public String getStatus()
	{
		return status;
	}

	public long getTime()
	{
		return time;
	}

	public String getAuthor()
	{
		return author;
	}

	public String getModule()
	{
		return module;
	}

	public String getPath()
	{
		return path;
	}

	public String getStampId()
	{
		return this.stampId;
	}

	public Stamp toSchemaStamp()
	{
		Stamp stamp = new Stamp();
		stamp.setAuthor(author);
		stamp.setModule(module);
		stamp.setPath(path);
		stamp.setStatus(status);
		stamp.setTime(TimeConvert.convert(time));
		stamp.setUuid(stampId);
		return stamp;
	}
}
