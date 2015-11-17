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
package nl.esciencecenter.xenon.adaptors.local;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.util.Utils;

/**
 * LocalAdaptor implements an Xenon adaptor for local operations.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalAdaptor extends Adaptor {

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = XenonEngine.LOCAL_ADAPTOR_NAME;

    /** Local properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS + "local.";

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
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("local", "file");

    /** The locations supported by the adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("(null)", "(empty string)", "/");
    
    /** The properties supported by this adaptor */
    private static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>(
                    new XenonPropertyDescriptionImplementation(POLLING_DELAY, Type.INTEGER, EnumSet.of(Component.XENON), 
                            "1000", "The polling delay for monitoring running jobs (in milliseconds)."),
                    new XenonPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER, EnumSet.of(Component.XENON), 
                            "4", "The maximum number of concurrent jobs in the multiq.."));

    /** Local implementation for Files */
    private final LocalFiles localFiles;

    /** Local implementation for Jobs */
    private final LocalJobs localJobs;

    /** Local implementation for Credentials */
    private final LocalCredentials localCredentials;

    public LocalAdaptor(XenonEngine xenonEngine, Map<String, String> properties) throws XenonException {
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES, 
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties));

        localFiles = new LocalFiles(this, xenonEngine.getCopyEngine());
        localJobs = new LocalJobs(this, getProperties(), Utils.getLocalCWD(localFiles), xenonEngine);
        localCredentials = new LocalCredentials();
    }

    protected void checkCredential(Credential credential) throws XenonException {

        if (credential == null) {
            return;
        }

        if (credential instanceof LocalCredential) {
            return;
        }

        throw new InvalidCredentialException(ADAPTOR_NAME, "Adaptor does not support this credential!");
    }
   
    @Override
    public boolean supports(String scheme) {
        return scheme == null || super.supports(scheme);
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
    public Credentials credentialsAdaptor() throws XenonException {
        return localCredentials;
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        Map<String,String> result = new HashMap<>();
        localJobs.getAdaptorSpecificInformation(result);
        return result;
    }
}
