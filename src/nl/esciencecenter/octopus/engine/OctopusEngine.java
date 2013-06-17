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
package nl.esciencecenter.octopus.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.adaptors.gridengine.GridengineAdaptor;
import nl.esciencecenter.octopus.adaptors.local.LocalAdaptor;
import nl.esciencecenter.octopus.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.credentials.CredentialsEngineImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsEngine;
import nl.esciencecenter.octopus.engine.util.CopyEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.IllegalPropertyException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OctopusEngine implements the Octopus Interface class by redirecting all calls to {@link Adaptor}s.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class OctopusEngine implements Octopus {

    /** The local adaptor is a special case, therefore we publish its name here. */
    public static final String LOCAL_ADAPTOR_NAME = "local";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = "octopus.";

    /** All our own queue properties start with this prefix. */
    public static final String ADAPTORS = PREFIX + "adaptors.";

    /** All our own queue properties start with this prefix. */
    public static final String LOAD = ADAPTORS + "load";

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] { { LOAD, null,
            "List: comma separated list of the adaptors to load." } };

    private static final Logger logger = LoggerFactory.getLogger(OctopusEngine.class);

    /** All OctopusEngines created so far */
    private static final ArrayList<OctopusEngine> octopusEngines = new ArrayList<OctopusEngine>();

    /**
     * Create a new Octopus using the given properties.
     * 
     * @param properties
     *            the properties used to create the Octopus.
     * @return the newly created Octopus created.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws IllegalPropertyException
     *             If a known property was passed with an illegal value.
     * @throws OctopusException
     *             If the Octopus failed initialize.
     */
    public static synchronized Octopus newOctopus(Properties properties) throws OctopusException {
        OctopusEngine result = new OctopusEngine(properties);
        octopusEngines.add(result);
        return result;
    }

    public static synchronized void closeOctopus(Octopus engine) throws OctopusException {

        OctopusEngine result = null;

        for (int i = 0; i < octopusEngines.size(); i++) {
            if (octopusEngines.get(i) == engine) {
                result = octopusEngines.remove(i);
                break;
            }
        }
        
        if (result == null) {
            throw new OctopusException("engine", "No such OctopusEngine");
        }

        result.end();
    }

    public static UUID getNextUUID() { 
        return UUID.randomUUID();
    }
    
    public static synchronized void endAll() {
        for (OctopusEngine octopusEngine : octopusEngines) {
            octopusEngine.end();
        }
    }

    private boolean ended = false;

    private OctopusProperties octopusProperties;

    private final FilesEngine filesEngine;

    private final JobsEngine jobsEngine;

    private final CredentialsEngineImplementation credentialsEngine;

    private final Adaptor[] adaptors;
    
    private final CopyEngine copyEngine;

    /**
     * Constructs a OctopusEngine.
     * 
     * @param properties
     *            the properties to use. Will NOT be copied.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws IllegalPropertyException
     *             If a known property was passed with an illegal value.
     * @throws OctopusException
     *             If the Octopus failed initialize.
     */
    public OctopusEngine(Properties properties) throws OctopusException {

        octopusProperties = new OctopusProperties(VALID_PROPERTIES, properties);

        // adaptors = AdaptorLoader.loadAdaptors(octopusProperties, this);

        adaptors = loadAdaptors();
        
        filesEngine = new FilesEngine(this);

        jobsEngine = new JobsEngine(this);

        credentialsEngine = new CredentialsEngineImplementation(this);

        copyEngine = new CopyEngine(filesEngine);
        
        logger.info("Octopus engine initialized with adaptors: " + Arrays.toString(adaptors));
    }

    private Adaptor[] loadAdaptors() throws OctopusException { 
        
        Adaptor [] result = new Adaptor[3];
        
        result[0] = new LocalAdaptor(octopusProperties, this);
        result[1] = new SshAdaptor(octopusProperties, this);
        result[2] = new GridengineAdaptor(octopusProperties, this);
        
        // TODO: Add properties to extend list later.  
        return result;
    }
    
    // ************** Octopus Interface Implementation ***************\\

    @Override
    public AdaptorStatus[] getAdaptorInfos() {

        AdaptorStatus[] status = new AdaptorStatus[adaptors.length];

        for (int i = 0; i < adaptors.length; i++) {
            status[i] = adaptors[i].getAdaptorStatus();
        }

        return status;
    }

    @Override
    public AdaptorStatus getAdaptorInfo(String adaptorName) throws OctopusException {
        return getAdaptor(adaptorName).getAdaptorStatus();
    }

    /**
     * Return the adaptor that provides functionality for the given scheme.
     * 
     * @param scheme
     *            the scheme for which to get the adaptor
     * @return the adaptor
     */
    public Adaptor getAdaptorFor(String scheme) throws OctopusException {

        for (Adaptor adaptor : adaptors) {
            if (adaptor.supports(scheme)) {
                return adaptor;
            }
        }
        throw new OctopusException("engine", "Could not find adaptor for scheme " + scheme);
    }

    public Adaptor getAdaptor(String name) throws OctopusException {
        for (Adaptor adaptor : adaptors) {
            if (adaptor.getName().equals(name)) {
                return adaptor;
            }
        }

        throw new OctopusException("engine", "Could not find adaptor named " + name);
    }

    public Adaptor[] getAdaptors() {
        return adaptors;
    }

    @Override
    public synchronized Properties getProperties() {
        return octopusProperties;
    }

    @Override
    public Files files() {
        return filesEngine;
    }

    @Override
    public Jobs jobs() {
        return jobsEngine;
    }

    @Override
    public Credentials credentials() {
        return credentialsEngine;
    }

    /**
     * @return
     */
    public CopyEngine getCopyEngine() {
        return copyEngine;
    }
    
    private synchronized boolean setEnd() { 
        if (ended) {
            return false;
        }

        ended = true;
        return true;
    }
        
    @Override
    public void end() {
        if (setEnd()) { 
            for (Adaptor adaptor : adaptors) {
                adaptor.end();
            }
        }
    }

    @Override
    public String toString() {
        return "OctopusEngine [adaptors=" + Arrays.toString(adaptors) + " octopusProperties=" + octopusProperties + 
                    ",  + ended=" + ended + "]";
    }
}
