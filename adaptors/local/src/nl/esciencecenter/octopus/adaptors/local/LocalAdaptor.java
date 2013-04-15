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
package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

public class LocalAdaptor extends Adaptor {

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = OctopusEngine.LOCAL_ADAPTOR_NAME;

    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The local adaptor implements all functionality with "
            + " standard java classes such as java.lang.Process and java.nio.file.Files.";

    /** The schemes supported by the adaptor */
    public static final String[] ADAPTOR_SCHEME = new String[] { "local", "file" };

    /** Local properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "local.";

    /** Local queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Property for maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** Local multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Property for the maximum number of concurrent jobs in the multi queue. */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] {
            { MAX_HISTORY, "1000", "Int: the maximum history length for finished jobs." },
            { MULTIQ_MAX_CONCURRENT, null, "Int: the maximum number of concurrent jobs in the multiq." } };

    /** Local implementation for Files */
    private final LocalFiles localFiles;

    /** Local implementation for Jobs */
    private final LocalJobs localJobs;

    public LocalAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, properties);

        localFiles = new LocalFiles(getProperties(), this);
        localJobs = new LocalJobs(getProperties(), this, octopusEngine);
    }

    void checkURI(URI location) throws OctopusException {

        if (location == null) {
            return;
        }

        String scheme = location.getScheme();

        if (scheme != null && !supports(scheme)) {
            throw new OctopusException(ADAPTOR_NAME, "Adaptor does not support scheme " + scheme);
        }

        String host = location.getHost();

        if (host != null && !host.equals("localhost")) {
            throw new OctopusException(ADAPTOR_NAME, "Adaptor only supports URI with empty host or \"localhost\", not \""
                    + location.getHost() + "\"");
        }
    }

    @Override
    public boolean supports(String scheme) {

        if (scheme == null) {
            return true;
        }

        return super.supports(scheme);
    }

    public Map<String, String> getSupportedProperties() {
        return new HashMap<String, String>();
    }

    @Override
    public void end() {
        // TODO: implement!
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Files filesAdaptor() {
        return localFiles;
    }

    @Override
    public Jobs jobsAdaptor() {
        return localJobs;
    }

    @Override
    public Credentials credentialsAdaptor() throws OctopusException {
        throw new OctopusException(ADAPTOR_NAME, "Adaptor does not need or understand credentials.");
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO: supply some info.
        return null;
    }
}
