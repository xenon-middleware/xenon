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
package nl.esciencecenter.cobalt.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import nl.esciencecenter.cobalt.AdaptorStatus;
import nl.esciencecenter.cobalt.InvalidSchemeException;
import nl.esciencecenter.cobalt.NoSuchCobaltException;
import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.UnknownPropertyException;
import nl.esciencecenter.cobalt.adaptors.gridengine.GridEngineAdaptor;
import nl.esciencecenter.cobalt.adaptors.local.LocalAdaptor;
import nl.esciencecenter.cobalt.adaptors.slurm.SlurmAdaptor;
import nl.esciencecenter.cobalt.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.cobalt.credentials.Credentials;
import nl.esciencecenter.cobalt.engine.credentials.CredentialsEngineImplementation;
import nl.esciencecenter.cobalt.engine.files.FilesEngine;
import nl.esciencecenter.cobalt.engine.jobs.JobsEngine;
import nl.esciencecenter.cobalt.engine.util.CopyEngine;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OctopusEngine implements the Octopus Interface class by redirecting all calls to {@link Adaptor}s.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public final class CobaltEngine implements Cobalt {

    private static final Logger LOGGER = LoggerFactory.getLogger(CobaltEngine.class);

    /** The local adaptor is a special case, therefore we publish its name here. */
    public static final String LOCAL_ADAPTOR_NAME = "local";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = "octopus.";

    /** All our own queue properties start with this prefix. */
    public static final String ADAPTORS = PREFIX + "adaptors.";

    /** All OctopusEngines created so far */
    private static final List<CobaltEngine> OCTOPUS_ENGINES = new ArrayList<CobaltEngine>();

    /**
     * Create a new Octopus using the given properties.
     * 
     * @param properties
     *            the properties used to create the Octopus.
     * @return the newly created Octopus created.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws CobaltException
     *             If the Octopus failed initialize.
     */
    public static synchronized Cobalt newCobalt(Map<String, String> properties) throws CobaltException {
        CobaltEngine result = new CobaltEngine(properties);
        OCTOPUS_ENGINES.add(result);
        return result;
    }

    public static synchronized void closeCobalt(Cobalt engine) throws NoSuchCobaltException {

        CobaltEngine result = null;

        for (int i = 0; i < OCTOPUS_ENGINES.size(); i++) {
            if (OCTOPUS_ENGINES.get(i) == engine) {
                result = OCTOPUS_ENGINES.remove(i);
                break;
            }
        }

        if (result == null) {
            throw new NoSuchCobaltException("engine", "No such OctopusEngine");
        }

        result.end();
    }

    public static UUID getNextUUID() {
        return UUID.randomUUID();
    }

    public static synchronized void endAll() {
        for (int i = 0; i < OCTOPUS_ENGINES.size(); i++) {
            OCTOPUS_ENGINES.get(i).end();
        }

        OCTOPUS_ENGINES.clear();
    }

    private boolean ended = false;

    private final Map<String, String> properties;

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
     * @throws CobaltException
     *             If the Octopus failed initialize.
     */
    private CobaltEngine(Map<String, String> properties) throws CobaltException {

        // Store the properties for later reference.
        if (properties == null) {
            this.properties = Collections.unmodifiableMap(new HashMap<String, String>());
        } else {
            this.properties = Collections.unmodifiableMap(new HashMap<String, String>(properties));
        }

        // NOTE: Order is important here! We initialize the abstract engines first, as the adaptors may want to use them!
        filesEngine = new FilesEngine(this);
        jobsEngine = new JobsEngine(this);
        credentialsEngine = new CredentialsEngineImplementation(this);
        copyEngine = new CopyEngine(filesEngine);

        adaptors = loadAdaptors(this.properties);

        LOGGER.info("Octopus engine initialized with adaptors: " + Arrays.toString(adaptors));
    }

    private Adaptor[] loadAdaptors(Map<String, String> properties) throws CobaltException {

        // Copy the map so we can manipulate it. 
        Map<String, String> tmp = new HashMap<>(properties);

        List<Adaptor> result = new ArrayList<>();

        result.add(new LocalAdaptor(this, extract(tmp, LocalAdaptor.PREFIX)));
        result.add(new SshAdaptor(this, extract(tmp, SshAdaptor.PREFIX)));
        result.add(new GridEngineAdaptor(this, extract(tmp, GridEngineAdaptor.PREFIX)));
        result.add(new SlurmAdaptor(this, extract(tmp, SlurmAdaptor.PREFIX)));

        // Check if there are any properties left. If so, this is a problem. 
        if (tmp.size() != 0) {
            throw new UnknownPropertyException("OctopusEngine", "Unknown properties: " + tmp);
        }

        return result.toArray(new Adaptor[result.size()]);
    }

    private Map<String, String> extract(Map<String, String> source, String prefix) {

        HashMap<String, String> tmp = new HashMap<>();

        Iterator<Entry<String, String>> itt = source.entrySet().iterator();

        while (itt.hasNext()) {

            Entry<String,String> e = itt.next();
            
            if (e.getKey().startsWith(prefix)) {
                tmp.put(e.getKey(), e.getValue());
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
    public AdaptorStatus getAdaptorStatus(String adaptorName) throws CobaltException {
        return getAdaptor(adaptorName).getAdaptorStatus();
    }

    /**
     * Return the adaptor that provides functionality for the given scheme.
     * 
     * @param scheme
     *            the scheme for which to get the adaptor
     * @return the adaptor
     */
    public Adaptor getAdaptorFor(String scheme) throws InvalidSchemeException {

        if (scheme == null || scheme.isEmpty()) { 
            throw new InvalidSchemeException("engine", "Invalid scheme " + scheme);
        }
        
        for (Adaptor adaptor : adaptors) {
            if (adaptor.supports(scheme)) {
                return adaptor;
            }
        }
        throw new InvalidSchemeException("engine", "Could not find adaptor for scheme " + scheme);
    }

    public Adaptor getAdaptor(String name) throws CobaltException {
        for (Adaptor adaptor : adaptors) {
            if (adaptor.getName().equals(name)) {
                return adaptor;
            }
        }

        throw new CobaltException("engine", "Could not find adaptor named " + name);
    }

    @Override
    public synchronized Map<String, String> getProperties() {
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

    private void end() {
        if (setEnd()) {
            copyEngine.done();
            for (Adaptor adaptor : adaptors) {
                adaptor.end();
            }
        }
    }

    @Override
    public String toString() {
        return "OctopusEngine [adaptors=" + Arrays.toString(adaptors) + " properties=" + properties + ",  + ended=" + ended + "]";
    }
}
