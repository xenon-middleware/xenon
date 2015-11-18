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
package nl.esciencecenter.xenon.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.InvalidSchemeException;
import nl.esciencecenter.xenon.NoSuchXenonException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.ftp.FtpAdaptor;
import nl.esciencecenter.xenon.adaptors.gridengine.GridEngineAdaptor;
import nl.esciencecenter.xenon.adaptors.local.LocalAdaptor;
import nl.esciencecenter.xenon.adaptors.slurm.SlurmAdaptor;
import nl.esciencecenter.xenon.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.xenon.adaptors.torque.TorqueAdaptor;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.credentials.CredentialsEngineImplementation;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.engine.util.CopyEngine;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XenonEngine implements the Xenon Interface class by redirecting all calls to {@link Adaptor}s.
 *
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public final class XenonEngine implements Xenon {

    private static final Logger LOGGER = LoggerFactory.getLogger(XenonEngine.class);

    /** The local adaptor is a special case, therefore we publish its name here. */
    public static final String LOCAL_ADAPTOR_NAME = "local";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = "xenon.";

    /** All our own adaptor properties start with this prefix. */
    public static final String ADAPTORS = PREFIX + "adaptors.";

    /** All XenonEngines created so far */
    private static final List<XenonEngine> XENON_ENGINES = new ArrayList<>(1);

    /**
     * Create a new Xenon using the given properties.
     *
     * @param properties
     *            the properties used to create the Xenon.
     * @return the newly created Xenon created.
     *
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws XenonException
     *             If the Xenon failed initialize.
     */
    public static synchronized Xenon newXenon(Map<String, String> properties) throws XenonException {
        XenonEngine result = new XenonEngine(properties);
        XENON_ENGINES.add(result);
        return result;
    }

    public static synchronized void closeXenon(Xenon engine) throws NoSuchXenonException {

        XenonEngine result = null;

        for (int i = 0; i < XENON_ENGINES.size(); i++) {
            if (XENON_ENGINES.get(i) == engine) {
                result = XENON_ENGINES.remove(i);
                break;
            }
        }

        if (result == null) {
            throw new NoSuchXenonException("engine", "No such XenonEngine");
        }

        result.end();
    }

    public static UUID getNextUUID() {
        return UUID.randomUUID();
    }

    public static synchronized void endAll() {
        for (XenonEngine engine : XENON_ENGINES) {
            engine.end();
        }

        XENON_ENGINES.clear();
    }

    private boolean ended = false;

    private final Map<String, String> properties;

    private final FilesEngine filesEngine;

    private final JobsEngine jobsEngine;

    private final CredentialsEngineImplementation credentialsEngine;

    private final Adaptor[] adaptors;

    private final CopyEngine copyEngine;

    /**
     * Constructs a XenonEngine.
     *
     * @param properties
     *            the properties to use. Will NOT be copied.
     *
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws IllegalPropertyException
     *             If a known property was passed with an illegal value.
     * @throws XenonException
     *             If the Xenon failed initialize.
     */
    private XenonEngine(Map<String, String> properties) throws XenonException {

        // Store the properties for later reference.
        if (properties == null) {
            this.properties = Collections.unmodifiableMap(new HashMap<String, String>(0));
        } else {
            this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
        }

        // NOTE: Order is important here! We initialize the abstract engines first, as the adaptors may want to use them!
        filesEngine = new FilesEngine(this);
        jobsEngine = new JobsEngine(this);
        credentialsEngine = new CredentialsEngineImplementation(this);
        copyEngine = new CopyEngine(filesEngine);

        adaptors = loadAdaptors(this.properties);

        LOGGER.info("Xenon engine initialized with adaptors: " + Arrays.toString(adaptors));
    }

    private Adaptor[] loadAdaptors(Map<String, String> properties) throws XenonException {

        // Copy the map so we can manipulate it.
        Map<String, String> tmp = new HashMap<>(properties);

        List<Adaptor> result = new ArrayList<>(10);

        result.add(new LocalAdaptor(this, extract(tmp, LocalAdaptor.PREFIX)));
        result.add(new SshAdaptor(this, extract(tmp, SshAdaptor.PREFIX)));
        result.add(new FtpAdaptor(this, extract(tmp, FtpAdaptor.PREFIX)));
        result.add(new GridEngineAdaptor(this, extract(tmp, GridEngineAdaptor.PREFIX)));
        result.add(new SlurmAdaptor(this, extract(tmp, SlurmAdaptor.PREFIX)));
        result.add(new TorqueAdaptor(this, extract(tmp, TorqueAdaptor.PREFIX)));

        // Check if there are any properties left. If so, this is a problem.
        if (!tmp.isEmpty()) {
            throw new UnknownPropertyException("XenonEngine", "Unknown properties: " + tmp);
        }

        return result.toArray(new Adaptor[result.size()]);
    }

    private Map<String, String> extract(Map<String, String> source, String prefix) {

        HashMap<String, String> tmp = new HashMap<>(source.size());

        Iterator<Entry<String, String>> itt = source.entrySet().iterator();

        while (itt.hasNext()) {

            Entry<String, String> e = itt.next();

            if (e.getKey().startsWith(prefix)) {
                tmp.put(e.getKey(), e.getValue());
                itt.remove();
            }
        }

        return tmp;
    }

    // ************** Xenon Interface Implementation ***************\\

    @Override
    public AdaptorStatus[] getAdaptorStatuses() {

        AdaptorStatus[] status = new AdaptorStatus[adaptors.length];

        for (int i = 0; i < adaptors.length; i++) {
            status[i] = adaptors[i].getAdaptorStatus();
        }

        return status;
    }

    @Override
    public AdaptorStatus getAdaptorStatus(String adaptorName) throws XenonException {
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

    public Adaptor getAdaptor(String name) throws XenonException {
        for (Adaptor adaptor : adaptors) {
            if (adaptor.getName().equals(name)) {
                return adaptor;
            }
        }

        throw new XenonException("engine", "Could not find adaptor named " + name);
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
        return "XenonEngine [adaptors=" + Arrays.toString(adaptors) + " properties=" + properties + ",  + ended=" + ended + "]";
    }
}
