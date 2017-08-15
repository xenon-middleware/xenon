package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assume.assumeNotNull;

public class LiveLocationConfig extends SchedulerLocationConfig {
    @Override
    public String getLocation() {
        return System.getProperty("xenon.scheduler.location");
    }

    @Override
    public String[] getQueueNames() {
        String queuesAsString = System.getProperty("xenon.scheduler.queues");
        // Skip when expected queues are not given
        assumeNotNull(queuesAsString);
        return queuesAsString.split(",");
    }

    @Override
    public String getDefaultQueueName() {
        String name = System.getProperty("xenon.scheduler.queues.default");
        // Skip when expected default queue is not given
        assumeNotNull(name);
        return name;
    }
}
