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
package gov.va.legoEdit.util;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Utility
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class Utility
{
	private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	public static ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 30, 60, TimeUnit.SECONDS, workQueue, new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			t.setName("Background-Thread-" + t.getId());
			return t;
		}
	});

	public static boolean isLong(String string)
	{
		try
		{
			Long.parseLong(string);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	public static boolean isUUID(String string)
	{
		if (string.length() != 36)
		{
			return false;
		}
		try
		{
			UUID.fromString(string);
			return true;
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
	}

	public static boolean isEmpty(String string)
	{
		if (string == null || string.length() == 0)
		{
			return true;
		}
		return false;
	}

	public static boolean isEqual(String a, String b)
	{
		if (a == null)
		{
			return (b == null ? true : false);
		}
		if (b == null)
		{
			return (a == null ? true : false);
		}
		return a.equals(b);
	}
}
