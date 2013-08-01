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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.adaptors.gridengine.GridEngineAdaptor;
import nl.esciencecenter.octopus.adaptors.local.LocalAdaptor;
import nl.esciencecenter.octopus.adaptors.slurm.SlurmAdaptor;
import nl.esciencecenter.octopus.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.credentials.CredentialsEngineImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsEngine;
import nl.esciencecenter.octopus.engine.util.CopyEngine;
import nl.esciencecenter.octopus.exceptions.NoSuchOctopusException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(OctopusEngine.class);
    
    /** The local adaptor is a special case, therefore we publish its name here. */
    public static final String LOCAL_ADAPTOR_NAME = "local";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = "octopus.";

    /** All our own queue properties start with this prefix. */
    public static final String ADAPTORS = PREFIX + "adaptors.";

    /** All our own queue properties start with this prefix. */
    public static final String LOAD = ADAPTORS + "load";

    /** All OctopusEngines created so far */
    private static final List<OctopusEngine> octopusEngines = new ArrayList<OctopusEngine>();

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
    public static synchronized Octopus newOctopus(Map<String,String> properties) throws OctopusException {
        OctopusEngine result = new OctopusEngine(properties);
        octopusEngines.add(result);
        return result;
    }

    public static synchronized void closeOctopus(Octopus engine) throws NoSuchOctopusException {

        OctopusEngine result = null;

        for (int i = 0; i < octopusEngines.size(); i++) {
            if (octopusEngines.get(i) == engine) {
                result = octopusEngines.remove(i);
                break;
            }
        }

        if (result == null) {
            throw new NoSuchOctopusException("engine", "No such OctopusEngine");
        }

        result.end();
    }

    public static UUID getNextUUID() {
        return UUID.randomUUID();
    }

    public static synchronized void endAll() {
        for (int i = 0; i < octopusEngines.size(); i++) {
            octopusEngines.get(i).end();
        }

        octopusEngines.clear();
    }

    private boolean ended = false;

    private final Map<String,String> properties;

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
    public OctopusEngine(Map<String,String> properties) throws OctopusException {
        
        // Store the properties for later reference.
        if (properties == null) { 
            this.properties = Collections.unmodifiableMap(new HashMap<String, String>());
        } else { 
            this.properties = Collections.unmodifiableMap(new HashMap<String, String>(properties));
        }
        
        adaptors = loadAdaptors(this.properties);

        filesEngine = new FilesEngine(this);

        jobsEngine = new JobsEngine(this);

        credentialsEngine = new CredentialsEngineImplementation(this);

        copyEngine = new CopyEngine(filesEngine);

        LOGGER.info("Octopus engine initialized with adaptors: " + Arrays.toString(adaptors));
    }

    private Adaptor[] loadAdaptors(Map<String,String> properties) throws OctopusException {

        // Copy the map so we can manipulate it. 
        HashMap<String,String> tmp = new HashMap<>(properties);
        
        Adaptor[] result = new Adaptor[4];

        result[0] = new LocalAdaptor(this, extract(tmp, LocalAdaptor.PREFIX));
        result[1] = new SshAdaptor(this, extract(tmp, SshAdaptor.PREFIX));
        result[2] = new GridEngineAdaptor(this, extract(tmp, GridEngineAdaptor.PREFIX));
        result[3] = new SlurmAdaptor(this, extract(tmp, SlurmAdaptor.PREFIX));

        // Check if there are any properties left. If so, this is a problem. 
        if (tmp.size() != 0) { 
            throw new UnknownPropertyException("OctopusEngine", "Unknown properties: " + tmp);
        }
                
        return result;
    }

    private Map<String,String> extract(Map<String,String> source, String prefix) { 

        HashMap<String,String> tmp = new HashMap<>();
        
        Iterator<String> itt = source.keySet().iterator();
        
        while (itt.hasNext()) { 
            
            String key = itt.next();

            if (key.startsWith(prefix)) { 
                tmp.put(key, source.get(key));
                itt.remove();
            }
        }
        
        return tmp;
    }
    
    // ************** Octopus Interface Implementation ***************\\

    @Override
    public AdaptorStatus[] getAdaptorStatuses() {

        AdaptorStatus[] status = new AdaptorStatus[adaptors.length];

        for (int i = 0; i < adaptors.length; i++) {
            status[i] = adaptors[i].getAdaptorStatus();
        }

        return status;
    }

    @Override
    public AdaptorStatus getAdaptorStatus(String adaptorName) throws OctopusException {
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

    @Override
    public synchronized Map<String,String> getProperties() {
        return properties;
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

    public void end() {
        if (setEnd()) {
            copyEngine.done();
            for (Adaptor adaptor : adaptors) {
                adaptor.end();
            }
        }
    }

    @Override
    public String toString() {
        return "OctopusEngine [adaptors=" + Arrays.toString(adaptors) + " properties=" + properties + ",  + ended="
                + ended + "]";
    }
}
