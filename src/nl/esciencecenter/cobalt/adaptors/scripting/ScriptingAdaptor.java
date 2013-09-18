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

package nl.esciencecenter.cobalt.adaptors.scripting;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltPropertyDescription;
import nl.esciencecenter.cobalt.credentials.Credentials;
import nl.esciencecenter.cobalt.engine.Adaptor;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.engine.util.ImmutableArray;
import nl.esciencecenter.cobalt.files.Files;

/**
 * ScriptingAdaptor is a generic adaptor implementation. It server as a parent class for adaptors that are based on running
 * scripts over an SSH connection.
 * 
 * @see nl.esciencecenter.cobalt.adaptors.gridengine.GridEngineAdaptor
 * @see nl.esciencecenter.cobalt.adaptors.slurm.SlurmAdaptor
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 * 
 */
public abstract class ScriptingAdaptor extends Adaptor {

    private final ScriptingJobs jobsAdaptor;
    private final ForwardingCredentials credentialsAdaptor;

    protected ScriptingAdaptor(CobaltEngine cobaltEngine, String name, String description, 
            ImmutableArray<String> supportedSchemes, ImmutableArray<String> supportedLocations, 
            ImmutableArray<CobaltPropertyDescription> validProperties, 
            CobaltProperties properties, SchedulerConnectionFactory factory) throws CobaltException {

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
    public Files filesAdaptor() throws CobaltException {
        throw new CobaltException(getName(), "Adaptor does not support files.");
    }

    @Override
    public Credentials credentialsAdaptor() throws CobaltException {
        return credentialsAdaptor;
    }
}
