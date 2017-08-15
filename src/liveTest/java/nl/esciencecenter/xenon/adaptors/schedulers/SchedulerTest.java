package nl.esciencecenter.xenon.adaptors.schedulers;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import org.junit.BeforeClass;

import java.util.Map;

import static nl.esciencecenter.xenon.adaptors.Utils.buildCredential;
import static nl.esciencecenter.xenon.adaptors.Utils.buildProperties;
import static org.junit.Assume.assumeFalse;

public class SchedulerTest extends SchedulerTestParent {
    @BeforeClass
    static public void skipIfNotRequested() {
        String name = System.getProperty("xenon.scheduler");
        assumeFalse("Ignoring scheduler test, 'xenon.scheduler' system property not set", name == null);
    }

    @Override
    protected SchedulerLocationConfig setupLocationConfig() {
        return new LiveLocationConfig();
    }

    @Override
    public Scheduler setupScheduler() throws XenonException {
        String name = System.getProperty("xenon.scheduler");
        String location = System.getProperty("xenon.scheduler.location");
        Credential cred = buildCredential();
        Map<String, String> props = buildProperties(SchedulerAdaptor.ADAPTORS_PREFIX + System.getProperty("xenon.scheduler"));
        return Scheduler.create(name, location, cred, props);
    }
}
