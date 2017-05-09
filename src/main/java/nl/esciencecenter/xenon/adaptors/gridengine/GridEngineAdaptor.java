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
package nl.esciencecenter.xenon.adaptors.gridengine;

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
 * Adaptor for (Sun/Ocacle/Univa) Grid Engine scheduler.
 * 
 * @version 1.0
 * @since 1.0
 */
public class GridEngineAdaptor extends ScriptingAdaptor {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "gridengine";

    /** The prefix used by all properties related to this adaptor */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + GridEngineAdaptor.ADAPTOR_NAME + ".";

    /** The schemes supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_SCHEMES = new ImmutableArray<>("ge", "sge");
    
    /** The locations supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("(locations supported by local)",
            "(locations supported by ssh)");
    
    /** Should the grid engine version on the target machine be ignored ? */
    public static final String IGNORE_VERSION_PROPERTY = PREFIX + "ignore.version";

    /** Timeout for waiting for the accounting info of a job to appear */
    public static final String ACCOUNTING_GRACE_TIME_PROPERTY = PREFIX + "accounting.grace.time";

    /** Polling delay for jobs started by this adaptor. */
    public static final String POLL_DELAY_PROPERTY = PREFIX + "poll.delay";

    /** Human readable description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler."
            + " This adaptor uses either the local or the ssh adaptor to gain access to the scheduler machine.";

    /** List of all properties supported by this adaptor */
    private static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>(
        new XenonPropertyDescriptionImplementation(IGNORE_VERSION_PROPERTY, Type.BOOLEAN, EnumSet.of(Component.SCHEDULER),
                "false", "Skip version check is skipped when connecting to remote machines. "
                        + "WARNING: it is not recommended to use this setting in production environments!"),
        new XenonPropertyDescriptionImplementation(ACCOUNTING_GRACE_TIME_PROPERTY, Type.LONG, EnumSet.of(Component.SCHEDULER),
                "60000", "Number of milliseconds a job is allowed to take going from the queue to the qacct output."),

        new XenonPropertyDescriptionImplementation(POLL_DELAY_PROPERTY, Type.LONG, EnumSet.of(Component.SCHEDULER), "1000",
                "Number of milliseconds between polling the status of a job."));

    /**
     * Create a new GridEngineAdaptor.
     * 
     * @param properties
     *            the properties to use when creating the adaptor.
     * @param xenonEngine
     *            the engine to which this adaptor belongs.
     * @throws XenonException
     *             if the adaptor creation fails.
     */
    public GridEngineAdaptor(XenonEngine xenonEngine, Map<String, String> properties) throws XenonException {
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEMES, ADAPTOR_LOCATIONS, VALID_PROPERTIES, 
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties), new GridEngineSchedulerConnectionFactory());
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return new HashMap<>();
    }
}
