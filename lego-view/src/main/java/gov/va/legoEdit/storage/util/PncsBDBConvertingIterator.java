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
package gov.va.legoEdit.storage.util;

import gov.va.legoEdit.model.bdbModel.PncsBDB;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.CloseableIterator;
import java.util.concurrent.ScheduledExecutorService;
import com.sleepycat.persist.EntityCursor;

/**
 * 
 * PncsBDBConvertingIterator
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class PncsBDBConvertingIterator implements CloseableIterator<Pncs>
{

	private BDBIterator<PncsBDB> iter;

	public PncsBDBConvertingIterator(ScheduledExecutorService sec, EntityCursor<PncsBDB> c)
	{
		iter = new BDBIterator<>(sec, c);
	}

	@Override
	public Pncs next()
	{
		return iter.next().toSchemaPncs();
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
