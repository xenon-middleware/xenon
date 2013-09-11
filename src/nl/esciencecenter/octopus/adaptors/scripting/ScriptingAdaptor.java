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

import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.util.ImmutableArray;
import nl.esciencecenter.octopus.files.Files;

/**
 * ScriptingAdaptor is a generic adaptor implementation. It server as a parent class for adaptors that are based on running
 * scripts over an SSH connection.
 * 
 * @see nl.esciencecenter.octopus.adaptors.gridengine.GridEngineAdaptor
 * @see nl.esciencecenter.octopus.adaptors.slurm.SlurmAdaptor
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public abstract class ScriptingAdaptor extends Adaptor {

    private final ScriptingJobs jobsAdaptor;
    private final ForwardingCredentials credentialsAdaptor;

    protected ScriptingAdaptor(OctopusEngine octopusEngine, String name, String description, 
            ImmutableArray<String> supportedSchemes, ImmutableArray<String> supportedLocations, 
            ImmutableArray<OctopusPropertyDescription> validProperties, 
            OctopusProperties properties, SchedulerConnectionFactory factory) throws OctopusException {

        super(octopusEngine, name, description, supportedSchemes, supportedLocations, validProperties, properties);

        jobsAdaptor = new ScriptingJobs(this, octopusEngine, factory);
        credentialsAdaptor = new ForwardingCredentials(octopusEngine, "ssh");
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
