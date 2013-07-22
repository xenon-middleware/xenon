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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * Simple Factory class to create scheduler connections
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(URI location, Credential credential, Properties properties,
            OctopusEngine engine) throws OctopusIOException, OctopusException {
        return new GridEngineSchedulerConnection(location, credential, properties, engine);
    }

}
