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

package nl.esciencecenter.octopus.adaptors.scripting;

import java.net.URI;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;

/**
 * ScriptingAdaptor is a generic adaptor implementation. It server as a parent class for adaptors that are based on running 
 * scripts over an SSH connection.     
 * 
 * @see GridEngineAdaptor
 * @see SlurmAdaptor
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public abstract class ScriptingAdaptor extends Adaptor {
   
    private final ScriptingJobs jobsAdaptor;
    private final ForwardingCredentials credentialsAdaptor;

    protected ScriptingAdaptor(OctopusEngine octopusEngine, String name, String description, String[] supportedSchemes,
            OctopusProperties properties, SchedulerConnectionFactory factory) throws OctopusException {  

        super(octopusEngine, name, description, supportedSchemes, properties);

        jobsAdaptor = new ScriptingJobs(this, octopusEngine, factory);
        credentialsAdaptor = new ForwardingCredentials(octopusEngine, "ssh");
    }
    
    public void checkLocation(URI location) throws InvalidLocationException {
        //only null or "/" are allowed as paths
        if (!(location.getPath() == null || location.getPath().length() == 0 || location.getPath().equals("/"))) {
            throw new InvalidLocationException(getName(), "Paths are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        if (location.getFragment() != null && location.getFragment().length() > 0) {
            throw new InvalidLocationException(getName(), "Fragments are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        for (String scheme : getSupportedSchemes()) {
            if (scheme.equals(location.getScheme())) {
                //alls-well
                return;
            }
        }
        throw new InvalidLocationException(getName(), "Adaptor does not support scheme: " + location.getScheme());
    }
    
    @Override
    public ScriptingJobs jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public void end() {
        jobsAdaptor.end();
    }

    @Override
    public Files filesAdaptor() throws OctopusException {
        throw new OctopusException(getName(), "Adaptor does not support files.");
    }

    @Override
    public Credentials credentialsAdaptor() throws OctopusException {
        return credentialsAdaptor;
    }
} 
