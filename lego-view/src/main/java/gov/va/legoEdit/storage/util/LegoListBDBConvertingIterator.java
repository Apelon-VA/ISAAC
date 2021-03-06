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
package gov.va.legoEdit.storage.util;

import com.sleepycat.persist.EntityCursor;
import gov.va.legoEdit.model.bdbModel.LegoListBDB;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.CloseableIterator;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 
 * LegoListBDBConvertingIterator
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class LegoListBDBConvertingIterator implements CloseableIterator<LegoList>
{

	private BDBIterator<LegoListBDB> iter;

	public LegoListBDBConvertingIterator(ScheduledExecutorService sec, EntityCursor<LegoListBDB> c)
	{
		iter = new BDBIterator<>(sec, c);
	}

	@Override
	public LegoList next()
	{
		return iter.next().toSchemaLegoList();
	}

	@Override
	public boolean hasNext()
	{
		return iter.hasNext();
	}

	@Override
	public void remove()
	{
		iter.remove();
	}

	@Override
	public void close()
	{
		iter.close();
	}
}
