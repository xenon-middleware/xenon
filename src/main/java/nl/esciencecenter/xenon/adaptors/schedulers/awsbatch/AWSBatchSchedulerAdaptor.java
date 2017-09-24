package nl.esciencecenter.xenon.adaptors.schedulers.awsbatch;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class AWSBatchSchedulerAdaptor extends SchedulerAdaptor {
    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "awsbatch";

    /** The prefix used by all properties related to this adaptor */
    public static final String PREFIX = SchedulerAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** Human readable description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The AWS Batch submits jobs to AWS Batch";

    /** The locations supported by this adaptor */
    public static final String [] ADAPTOR_LOCATIONS = new String [] { "region",
            "(empty string for default region)" };

    /** Polling delay for jobs started by this adaptor. */
    public static final String POLL_DELAY_PROPERTY = PREFIX + "poll.delay";

    /** List of all properties supported by this adaptor */
    public static final XenonPropertyDescription [] VALID_PROPERTIES = new XenonPropertyDescription [] {
            new XenonPropertyDescription(POLL_DELAY_PROPERTY, XenonPropertyDescription.Type.LONG,
                    "1000", "Number of milliseconds between polling the status of a job.")
    };

    public AWSBatchSchedulerAdaptor() {
        super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public Scheduler createScheduler(String location, Credential credential, Map<String, String> properties) throws XenonException {
        return new AWSBatchScheduler(getNewUniqueID(), location, credential, properties);
    }
}
