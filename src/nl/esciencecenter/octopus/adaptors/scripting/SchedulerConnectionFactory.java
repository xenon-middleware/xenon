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
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * Factory interface for scheduling connections. Implemented by adaptors.
 * 
 * @author Niels Drost
 * 
 */
public interface SchedulerConnectionFactory {

    SchedulerConnection newSchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location, Credential credential,
            OctopusProperties properties, OctopusEngine engine) throws OctopusException;
}
