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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.util;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * {@link Utility}
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Utility {

    private static final ThreadPoolExecutor EXECUTOR = buildExecutor();
    private static final ScheduledExecutorService scheduledExecutor_ = Executors.newScheduledThreadPool(1, new BackgroundThreadFactory());

    private static ThreadPoolExecutor buildExecutor() {

        // Thread pool settings.
        int corePoolSize = 10;
        int maximumPoolSize = 60;
        int keepAliveTime = 60;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        BackgroundThreadFactory threadFactory = new BackgroundThreadFactory();

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                workQueue,
                threadFactory);
    }

    public static void execute(Runnable command) {
        EXECUTOR.execute(command);
    }
    
    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }
    
    public static Future<?> submit(Runnable task) {
        return EXECUTOR.submit(task);
    }
    
    public static <T> Future<?> submit(Runnable task, T result) {
        return EXECUTOR.submit(task, result);
    }
    
    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutor_.schedule(command, delay, unit);
    }
    
    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledExecutor_.schedule(callable, delay, unit);
    }

    public static UUID getUUID(String string) {
        if (string == null)
        {
            return null;
        }
        if (string.length() != 36) {
            return null;
        }
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static boolean isUUID(String string) {
        return (getUUID(string) != null);
    }

    public static boolean isLong(String string) {
        if (string == null)
        {
            return false;
        }
        try {
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isInt(String string) {
        return (getInt(string) != null);
    }
    
    public static Integer getInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static void shutdownThreadPools()
    {
        EXECUTOR.shutdownNow();
        scheduledExecutor_.shutdownNow();
    }
    
    public static int compareStringsIgnoreCase(String s1, String s2) {
    	int rval = 0;
    	
    	if (s1 != null || s2 != null) {
    		if (s1 == null) {
    			rval = -1;
    		} else if (s2 == null) {
    			rval = 1;
    		} else {
    			rval = s1.trim().toLowerCase().compareTo(s2.trim().toLowerCase());
    		}
    	}
    	
    	return rval;
    }
}
