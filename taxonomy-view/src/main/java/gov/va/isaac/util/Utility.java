package gov.va.isaac.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utility {

    private static final ThreadPoolExecutor EXECUTOR = buildExecutor();

    private static ThreadPoolExecutor buildExecutor() {

        // Thread pool settings.
        int corePoolSize = 10;
        int maximumPoolSize = 30;
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
}
