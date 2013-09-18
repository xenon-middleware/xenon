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
package nl.esciencecenter.cobalt.adaptors.local;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.cobalt.InvalidCredentialException;
import nl.esciencecenter.cobalt.InvalidLocationException;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltPropertyDescription;
import nl.esciencecenter.cobalt.CobaltPropertyDescription.Component;
import nl.esciencecenter.cobalt.CobaltPropertyDescription.Type;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.credentials.Credentials;
import nl.esciencecenter.cobalt.engine.Adaptor;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.engine.CobaltPropertyDescriptionImplementation;
import nl.esciencecenter.cobalt.engine.util.ImmutableArray;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.jobs.Jobs;
import nl.esciencecenter.cobalt.util.Utils;

/**
 * LocalAdaptor implements an Octopus adaptor for local operations.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalAdaptor extends Adaptor {

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = CobaltEngine.LOCAL_ADAPTOR_NAME;

    /** Local properties start with this prefix. */
    public static final String PREFIX = CobaltEngine.ADAPTORS + "local.";

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

    /** Local queue information start with this prefix. */
    public static final String INFO = PREFIX + "info.";

    /** Local job information start with this prefix. */
    public static final String JOBS = INFO + "jobs.";
    
    /** How many jobs have been submitted locally. */
    public static final String SUBMITTED = JOBS + "submitted";
    
    /** The schemes supported by the adaptor */
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<String>("local", "file");

    /** The locations supported by the adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<String>("(null)", "(empty string)", "/");
    
    /** The properties supported by this adaptor */
    private static final ImmutableArray<CobaltPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<CobaltPropertyDescription>(
                    new CobaltPropertyDescriptionImplementation(POLLING_DELAY, Type.INTEGER, EnumSet.of(Component.COBALT), 
                            "1000", "The polling delay for monitoring running jobs (in milliseconds)."),
                    new CobaltPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER, EnumSet.of(Component.COBALT), 
                            "4", "The maximum number of concurrent jobs in the multiq.."));

    /** Local implementation for Files */
    private final LocalFiles localFiles;

    /** Local implementation for Jobs */
    private final LocalJobs localJobs;

    /** Local implementation for Credentials */
    private final LocalCredentials localCredentials;

    public LocalAdaptor(CobaltEngine octopusEngine, Map<String, String> properties) throws CobaltException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES, 
                new CobaltProperties(VALID_PROPERTIES, Component.COBALT, properties));

        localFiles = new LocalFiles(this, octopusEngine.getCopyEngine());
        localJobs = new LocalJobs(getProperties(), Utils.getLocalCWD(localFiles), octopusEngine);
        localCredentials = new LocalCredentials();
    }

    void checkCredential(Credential credential) throws CobaltException {

        if (credential == null) {
            return;
        }

        if (credential instanceof LocalCredential) {
            return;
        }

        throw new InvalidCredentialException(ADAPTOR_NAME, "Adaptor does not support this credential!");
    }
   
    /** 
     * Check if a location string is valid for the local scheduler. 
     * 
     * The location should -only- contain a file system root, such as "/" or "C:". 
     * 
     * @param location
     *          the location to check.
     * @throws InvalidLocationException
     *          if the location is invalid.                   
     */
    void checkLocation(String location) throws InvalidLocationException {

        if (location == null) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Location must contain a file system root! (not null)");
        }

        if (Utils.isLocalRoot(location)) { 
            return;
        }
        
        throw new InvalidLocationException(ADAPTOR_NAME, "Location must only contain a file system root! (not " + location + ")");
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
    public Credentials credentialsAdaptor() throws CobaltException {
        return localCredentials;
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        Map<String,String> result = new HashMap<String, String>();
        localJobs.getAdaptorSpecificInformation(result);
        return result;
    }
}
