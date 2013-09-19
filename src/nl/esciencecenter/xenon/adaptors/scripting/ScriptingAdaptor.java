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

package nl.esciencecenter.xenon.adaptors.scripting;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;

/**
 * ScriptingAdaptor is a generic adaptor implementation. It server as a parent class for adaptors that are based on running
 * scripts over an SSH connection.
 * 
 * @see nl.esciencecenter.xenon.adaptors.gridengine.GridEngineAdaptor
 * @see nl.esciencecenter.xenon.adaptors.slurm.SlurmAdaptor
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public abstract class ScriptingAdaptor extends Adaptor {

    private final ScriptingJobs jobsAdaptor;
    private final ForwardingCredentials credentialsAdaptor;

    protected ScriptingAdaptor(XenonEngine cobaltEngine, String name, String description, 
            ImmutableArray<String> supportedSchemes, ImmutableArray<String> supportedLocations, 
            ImmutableArray<XenonPropertyDescription> validProperties, 
            XenonProperties properties, SchedulerConnectionFactory factory) throws XenonException {

        super(cobaltEngine, name, description, supportedSchemes, supportedLocations, validProperties, properties);

        jobsAdaptor = new ScriptingJobs(this, cobaltEngine, factory);
        credentialsAdaptor = new ForwardingCredentials(cobaltEngine, "ssh");
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
    public Files filesAdaptor() throws XenonException {
        throw new XenonException(getName(), "Adaptor does not support files.");
    }

    @Override
    public Credentials credentialsAdaptor() throws XenonException {
        return credentialsAdaptor;
    }
}
