/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.schedulers.slurm;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingSchedulerAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

/**
 * Adaptor for Slurm scheduler.
 */
public class SlurmSchedulerAdaptor extends ScriptingSchedulerAdaptor {

	/** The name of this adaptor */
    public static final String ADAPTOR_NAME = "slurm";

    /** The prefix used by all properties related to this adaptor */
    public static final String PREFIX = SchedulerAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** The locations supported by this adaptor */
    public static final String [] ADAPTOR_LOCATIONS = new String [] { "(locations supported by local)",
            "(locations supported by ssh)" };
    
    /** Should the accounting usage be disabled? */
    public static final String DISABLE_ACCOUNTING_USAGE = PREFIX + "disable.accounting.usage";

    /** Polling delay for jobs started by this adaptor. */
    public static final String POLL_DELAY_PROPERTY = PREFIX + "poll.delay";

    /** Human readable description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The Slurm Adaptor submits jobs to a Slurm scheduler. This adaptor uses either the local "
            + "or the ssh adaptor to gain access to the scheduler machine.";

    /** List of all properties supported by this adaptor */
    public static final XenonPropertyDescription [] VALID_PROPERTIES = new XenonPropertyDescription [] {       
        new XenonPropertyDescription(DISABLE_ACCOUNTING_USAGE, Type.BOOLEAN,
                "false", "Do not use accounting info of slurm, even when available. Mostly for testing purposes"),
                
        new XenonPropertyDescription(POLL_DELAY_PROPERTY, Type.LONG, 
        		"1000", "Number of milliseconds between polling the status of a job.")
    };

    public static final long SLURM_UPDATE_TIMEOUT = 60L*1000L; // 30 second update timeout
    
    public static final long SLURM_UPDATE_SLEEP = 1000L; // 1 second update sleep
    
    public static final String[] SUPPORTED_VERSIONS = { "2.3.", "2.5.", "2.6.", "14.03.0", "14.11.9-Bull.1.0", "15.08.6"};
    
    public SlurmSchedulerAdaptor() {
		super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
	}
    
	@Override
	public boolean supportsInteractive() { 
		// The slurm scheduler supports interactive jobs
		return true;
	}
    
	@Override
	public Scheduler createScheduler(String location, Credential credential, Map<String, String> properties) throws XenonException {
		return new SlurmScheduler(getNewUniqueID(), location, credential, properties);
	}
}
