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

package nl.esciencecenter.xenon.adaptors.jclouds;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.clouds.Clouds;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class JcloudsAdaptor extends Adaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JcloudsAdaptor.class);

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "jclouds";

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The JClouds adaptor implements functionality to access a range of cloud"
            + " providers (both compute and storage).";

    /** The schemes supported by this adaptor */
    /* FIXME: this is wrong. The selection on scheme may break down here? Is there a scheme for a specific cloud provider? */ 
    protected static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("cloud:", "blob:");
    
    /** The locations supported by this adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("(locations supported by jclouds)");
    
    /** List of properties supported by this JClouds adaptor */
    private static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>();
    
    /**
     * @param xenonEngine
     * @param properties
     * @throws InvalidPropertyException
     * @throws UnknownPropertyException  
     * @throws XenonException 
     */
    protected JcloudsAdaptor(XenonEngine xenonEngine, Map<String,String> properties) throws XenonException {
             
        super(xenonEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties));
        
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.Adaptor#getAdaptorSpecificInformation()
     */
    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.Adaptor#filesAdaptor()
     */
    @Override
    public Files filesAdaptor() throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.Adaptor#jobsAdaptor()
     */
    @Override
    public Jobs jobsAdaptor() throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.Adaptor#credentialsAdaptor()
     */
    @Override
    public Credentials credentialsAdaptor() throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.Adaptor#cloudsAdaptor()
     */
    @Override
    public Clouds cloudsAdaptor() throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.Adaptor#end()
     */
    @Override
    public void end() {
        // TODO Auto-generated method stub
        
    }

}
