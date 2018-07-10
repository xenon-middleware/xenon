package nl.esciencecenter.xenon.adaptors.schedulers.at;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingSchedulerAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class AtSchedulerAdaptor extends ScriptingSchedulerAdaptor {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "at";

    /** The prefix used by all properties related to this adaptor */
    public static final String PREFIX = SchedulerAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** Polling delay for jobs started by this adaptor. */
    public static final String POLL_DELAY_PROPERTY = PREFIX + "poll.delay";

    /** Human readable description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The At Adaptor submits jobs to an at scheduler. "
            + " This adaptor uses either the local or the ssh scheduler adaptor to run commands on the machine running at, "
            + " and the file or the stfp filesystem adaptor to gain access to the filesystem of that machine.";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "local://[/workdir]", "ssh://host[:port][/workdir][ via:otherhost[:port]]*" };

    /** List of all properties supported by this adaptor */
    private static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(POLL_DELAY_PROPERTY, Type.LONG, "1000", "Number of milliseconds between polling the status of a job.") };

    public AtSchedulerAdaptor() throws XenonException {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public Scheduler createScheduler(String location, Credential credential, Map<String, String> properties) throws XenonException {
        return new AtScheduler(getNewUniqueID(), location, credential, VALID_PROPERTIES, properties);
    }

}
