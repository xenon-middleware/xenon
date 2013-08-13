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
import java.util.EnumSet;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Type;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Component;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusPropertyDescriptionImplementation;
import nl.esciencecenter.octopus.engine.util.ImmutableArray;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

/**
 * LocalAdaptor implements an Octopus adaptor for local operations.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalAdaptor extends Adaptor {

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = OctopusEngine.LOCAL_ADAPTOR_NAME;

    /** Local properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "local.";

    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The local adaptor implements all functionality with "
            + " standard java classes such as java.lang.Process and java.nio.file.Files.";

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

    /** The schemes supported by the adaptor */
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<String>("local", "file");

    /** The properties supported by this adaptor */
    private static final ImmutableArray<OctopusPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<OctopusPropertyDescription>(
                    new OctopusPropertyDescriptionImplementation(POLLING_DELAY, Type.INTEGER, EnumSet.of(Component.OCTOPUS), 
                            "1000", "The polling delay for monitoring running jobs (in milliseconds)."),
                    new OctopusPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER, EnumSet.of(Component.OCTOPUS), 
                            "4", "The maximum number of concurrent jobs in the multiq.."));

    /** Local implementation for Files */
    private final LocalFiles localFiles;

    /** Local implementation for Jobs */
    private final LocalJobs localJobs;

    /** Local implementation for Credentials */
    private final LocalCredentials localCredentials;

    public LocalAdaptor(OctopusEngine octopusEngine, Map<String, String> properties) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, new OctopusProperties(
                VALID_PROPERTIES, Component.OCTOPUS, properties));

        localFiles = new LocalFiles(this, octopusEngine.getCopyEngine());
        localJobs = new LocalJobs(getProperties(), this, localFiles.getLocalCWDFileSystem(), octopusEngine);
        localCredentials = new LocalCredentials();
    }

    void checkCredential(Credential credential) throws OctopusException {

        if (credential == null) {
            return;
        }

        if (credential instanceof LocalCredential) {
            return;
        }

        throw new InvalidCredentialException(ADAPTOR_NAME, "Adaptor does not support this credential!");
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
            throw new InvalidLocationException(ADAPTOR_NAME, "Adaptor only supports URI with empty host or \"localhost\", not \""
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

    @Override
    public void end() {
        localJobs.end();
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
        return localCredentials;
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO: supply some info.
        return null;
    }
}
