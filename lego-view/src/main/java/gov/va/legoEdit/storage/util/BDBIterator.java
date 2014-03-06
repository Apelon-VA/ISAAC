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

import gov.va.legoEdit.storage.CloseableIterator;
import gov.va.legoEdit.storage.IteratorClosedException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

/**
 * 
 * BDBIterator
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class BDBIterator<T> implements CloseableIterator<T>
{
	public static int timeoutInSeconds = 15;
	Logger logger = LoggerFactory.getLogger(BDBIterator.class);

	private EntityCursor<T> entityCursor;
	private Iterator<T> iterator;
	private ScheduledExecutorService scheduledExecutorService;
	private Runnable runnable;
	private ScheduledFuture<?> scheduledFuture;
	private long expireAt;

	public BDBIterator(ScheduledExecutorService sec, EntityCursor<T> c)
	{
		scheduledExecutorService = sec;
		expireAt = System.currentTimeMillis() + (timeoutInSeconds * 1000);  // Expire X seconds after the last read
		entityCursor = c;
		logger.debug("Iterator created for " + entityCursor.toString());
		iterator = entityCursor.iterator();
		runnable = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					logger.debug("timer task running: expireAt: " + expireAt + " now: " + System.currentTimeMillis());
					if (entityCursor != null && System.currentTimeMillis() > expireAt)
					{
						close();
					}
					else
					{
						// reschedule
						long waitFor = expireAt - System.currentTimeMillis();
						if (waitFor > 0)
						{
							scheduledFuture = scheduledExecutorService.schedule(runnable, waitFor, TimeUnit.MILLISECONDS);
							logger.debug("Close rescheduled to execute in {} ms", waitFor);
						}
						else
						{
							close();
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Unexpected error in iterator close timer task", e);
				}
			}
		};
		long waitFor = expireAt - System.currentTimeMillis();
		if (waitFor > 0)
		{
			scheduledFuture = scheduledExecutorService.schedule(runnable, waitFor, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public synchronized boolean hasNext() throws IteratorClosedException
	{
		if (iterator == null)
		{
			throw new IteratorClosedException("This iterator is already closed");
		}
		boolean result = iterator.hasNext();
		if (!result)
		{
			close();
		}
		return result;
	}

	@Override
	public synchronized T next() throws IteratorClosedException
	{
		if (iterator == null)
		{
			throw new IteratorClosedException("This iterator is already closed");
		}
		try
		{
			expireAt = System.currentTimeMillis() + (timeoutInSeconds * 1000);
			return iterator.next();
		}
		catch (NoSuchElementException e)
		{
			close();
			throw e;
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public synchronized void close()
	{
		if (entityCursor != null)
		{
			try
			{
				entityCursor.close();
			}
			catch (DatabaseException e)
			{
				logger.error("Unexpected error closing cursor", e);
			}
			scheduledFuture.cancel(false);
			logger.debug("Iterator closed for " + entityCursor.toString());
			entityCursor = null;
			iterator = null;
		}
	}
}
