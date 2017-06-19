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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.InvalidAdaptorException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.NoSuchXenonException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

/**
 * XenonEngine implements the Xenon Interface class by redirecting all calls to {@link Adaptor}s.
 *
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
    public static final String ADAPTORS_PREFIX = PREFIX + "adaptors.";

    /** The name of this component, for use in exceptions */
    private static final String COMPONENT_NAME = "XenonEngine";
    
//    // TODO: make this configurable!!
//    /** Factories for all supported adaptors */
//    private static final AdaptorFactory [] ADAPTOR_FACTORIES = new AdaptorFactory [] { 
//            new LocalFileAdaptorFactory(),
//            new FtpFileAdaptorFactory(), 
//            new WebdavFileAdaptorFactory(),
//            
//            
//            new LocalJobAdaptorFactory(), 
//            
//            new SshAdaptorFactory(),
//            
//            new GridEngineAdaptorFactory(),
//            new SlurmAdaptorFactory(),
//            new TorqueAdaptorFactory(),
//            
//    };
   
    /** All XenonEngines created so far */
    private static final List<XenonEngine> XENON_ENGINES = new ArrayList<>(1);

    /**
     * Return the list of all properties that can to be set at Xenon creation time. 
     * 
     * These properties will be extracted from the different AdaptorFactories. 
     * 
     * @return
     *          the list of all properties that can to be set at Xenon creation time.
     */
    public static XenonPropertyDescription [] getSupportedProperties() {
   
    	ArrayList<XenonPropertyDescription> tmp = new ArrayList<>();
//   
//    	
//    	
//    	
//        for (AdaptorFactory a : ADAPTOR_FACTORIES) {
//            XenonPropertyDescription [] properties = a.getSupportedProperties();
//            
//            for (XenonPropertyDescription p : properties) { 
//                if (p.getLevels().contains(XenonPropertyDescription.Component.XENON)) { 
//                    tmp.add(p);
//                }
//            }
//        }
//        
        return tmp.toArray(new XenonPropertyDescription[tmp.size()]);
    }
    
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
            throw new NoSuchXenonException(COMPONENT_NAME, "No such XenonEngine");
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

    //private final Adaptor[] adaptors;

    /**
     * Constructs a XenonEngine.
     *
     * @param properties
     *            the properties to use. Will NOT be copied.
     *
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
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
       
        // Copy the map so we can manipulate it.
        Map<String, String> tmp = new HashMap<>(this.properties);
        
        filesEngine = new FilesEngine(this, tmp);
        jobsEngine = new JobsEngine(this, tmp);
        
        // Check if there are any properties left. If so, this is a problem.
        if (!tmp.isEmpty()) {
            throw new UnknownPropertyException(COMPONENT_NAME, "Unknown properties: " + tmp);
        }
        
       // LOGGER.debug("Xenon engine initialized with adaptors: {}", Arrays.toString(adaptors));
    }

//    private Adaptor[] loadAdaptors(Map<String, String> properties) throws XenonException {
//
//        // Copy the map so we can manipulate it.
//        Map<String, String> unprocesedProperties = new HashMap<>(properties);
//        
//        List<Adaptor> result = new ArrayList<>(10);
//
//        for (AdaptorFactory a : ADAPTOR_FACTORIES) { 
//            result.add(a.createAdaptor(this, extract(unprocesedProperties, a.getPropertyPrefix())));
//        }
//
//        // Check if there are any properties left. If so, this is a problem.
//        if (!unprocesedProperties.isEmpty()) {
//            throw new UnknownPropertyException(COMPONENT_NAME, "Unknown properties: " + unprocesedProperties);
//        }
//
//        return result.toArray(new Adaptor[result.size()]);
//    }


    // ************** Xenon Interface Implementation ***************\\

//    @Override
//    public AdaptorDescription[] getAdaptorStatuses() {
//
//        AdaptorDescription[] status = new AdaptorDescription[adaptors.length];
//
//        for (int i = 0; i < adaptors.length; i++) {
//            status[i] = adaptors[i].getAdaptorStatus();
//        }
//
//        return status;
//    }
//
//    @Override
//    public AdaptorDescription getAdaptorStatus(String adaptorName) throws XenonException {
//        return getAdaptor(adaptorName).getAdaptorStatus();
//    }

    /**
     * Return the adaptor that provides functionality for the given scheme.
     *
     * @param scheme
     *            the scheme for which to get the adaptor
     * @return the adaptor
     * @throws InvalidAdaptorException
     *          if the scheme is not known
     */
    
//    public Adaptor getAdaptorFor(String scheme) throws InvalidAdaptorException {
//
//        if (scheme == null || scheme.isEmpty()) {
//            throw new InvalidAdaptorException(COMPONENT_NAME, "Invalid scheme " + scheme);
//        }
//
//        for (Adaptor adaptor : adaptors) {
//            if (adaptor.supports(scheme)) {
//                return adaptor;
//            }
//        }
//        throw new InvalidAdaptorException(COMPONENT_NAME, "Could not find adaptor for scheme " + scheme);
//    }
//
//    public Adaptor getAdaptor(String name) throws XenonException {
//        for (Adaptor adaptor : adaptors) {
//            if (adaptor.getName().equals(name)) {
//                return adaptor;
//            }
//        }
//
//        throw new XenonException(COMPONENT_NAME, "Could not find adaptor named " + name);
//    }

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

    private synchronized boolean setEnd() {
        if (ended) {
            return false;
        }

        ended = true;
        return true;
    }

    private void end() {
        
        filesEngine.end();
        
        if (setEnd()) {
//            for (Adaptor adaptor : adaptors) {
//                adaptor.end();
//            }
        }
    }

    @Override
    public String toString() {
    	return "XenonEngine [properties=" + properties + ",  + ended=" + ended + "]";
    }

   
}
