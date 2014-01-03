package gov.va.isaac.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} which creates daemon threads.
 *
 * @author ocarlsen
 */
public final class BackgroundThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        t.setName("Background-Thread-" + t.getId());
        return t;
    }
}