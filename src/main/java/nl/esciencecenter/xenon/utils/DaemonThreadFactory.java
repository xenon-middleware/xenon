package nl.esciencecenter.xenon.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

    private final String name;
    private int count = 0;

    public DaemonThreadFactory(String name) {
        this.name = name;
    }

    private synchronized int getCount() {
        return count++;
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true);
        thread.setName(name + "-" + getCount());
        return thread;
    }
}
