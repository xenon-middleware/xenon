package nl.esciencecenter.xenon.adaptors.job.local;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

public class LocalProperties {

	 /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = "local";

    /** Local properties start with this prefix. */
    public static final String PREFIX = Xenon.ADAPTORS_PREFIX + "local.";

    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The local jobs adaptor implements all functionality "
            + " by emulating a local queue.";

    /** Local queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Property for maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** Property for maximum history length for finished jobs */
    public static final String POLLING_DELAY = QUEUE + "pollingDelay";

    /** Local multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Property for the maximum number of concurrent jobs in the multi queue. */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** Local queue information start with this prefix. */
    public static final String INFO = PREFIX + "info.";

    /** Local job information start with this prefix. */
    public static final String JOBS = INFO + "jobs.";
    
    /** How many jobs have been submitted locally. */
    public static final String SUBMITTED = JOBS + "submitted";
    
    /** The schemes supported by the adaptor */
    public static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("local");

    /** The locations supported by the adaptor */
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("(null)", "(empty string)", "/");
    
    /** The properties supported by this adaptor */
    public static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>(
                    new XenonPropertyDescription(POLLING_DELAY, Type.INTEGER, 
                            "1000", "The polling delay for monitoring running jobs (in milliseconds)."),
                    new XenonPropertyDescription(MULTIQ_MAX_CONCURRENT, Type.INTEGER,  
                            "4", "The maximum number of concurrent jobs in the multiq.."));
	
}
