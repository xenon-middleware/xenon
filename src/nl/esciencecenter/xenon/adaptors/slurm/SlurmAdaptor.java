/*
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
package nl.esciencecenter.xenon.adaptors.slurm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

/**
 * Adaptor for Slurm scheduler.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class SlurmAdaptor extends ScriptingAdaptor {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "slurm";

    /** The prefix used by all properties related to this adaptor */
    public static final String PREFIX = XenonEngine.ADAPTORS + SlurmAdaptor.ADAPTOR_NAME + ".";

    /** The schemes supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_SCHEMES = new ImmutableArray<String>("slurm");
    
    /** The locations supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<String>("(locations supported by local)", 
            "(locations supported by ssh)");
    
    /** Should the slurm version on the target machine be ignored ? */
    public static final String IGNORE_VERSION_PROPERTY = PREFIX + "ignore.version";

    /** Should the accounting usage be disabled? */
    public static final String DISABLE_ACCOUNTING_USAGE = PREFIX + "disable.accounting.usage";

    /** Polling delay for jobs started by this adaptor. */
    public static final String POLL_DELAY_PROPERTY = PREFIX + "poll.delay";

    /** Human readable description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The Slurm Adaptor submits jobs to a Slurm scheduler. This adaptor uses either the local "
            + "or the ssh adaptor to gain access to the scheduler machine.";

    /** List of all properties supported by this adaptor */
    private static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>(
        new XenonPropertyDescriptionImplementation(IGNORE_VERSION_PROPERTY, Type.BOOLEAN, EnumSet.of(Component.SCHEDULER),
                "false", "Skip version check is skipped when connecting to remote machines. "
                 + "WARNING: it is not recommended to use this setting in production environments!"),
        
        new XenonPropertyDescriptionImplementation(DISABLE_ACCOUNTING_USAGE, Type.BOOLEAN, EnumSet.of(Component.SCHEDULER),
                "false", "Do not used accounting info of slurm, even when available. Mostly for testing purposes"),
                
        new XenonPropertyDescriptionImplementation(POLL_DELAY_PROPERTY, Type.LONG, EnumSet.of(Component.SCHEDULER), "1000",
                "Number of milliseconds between polling the status of a job."));

    /**
     * Create a new SlurmAdaptor.
     * 
     * @param properties
     *            the properties to use when creating the adaptor.
     * @param cobaltEngine
     *            the engine to which this adaptor belongs.
     * @throws XenonException
     *             if the adaptor creation fails.
     */
    public SlurmAdaptor(XenonEngine cobaltEngine, Map<String, String> properties) throws XenonException {
        super(cobaltEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEMES, ADAPTOR_LOCATIONS, VALID_PROPERTIES, 
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties), new SlurmSchedulerConnectionFactory());
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return new HashMap<String, String>();
    }
}
