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
package nl.esciencecenter.xenon.adaptors.torque;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;

/**
 * Simple Factory class to create scheduler connections
 * 
 * @author Niels Drost
 * 
 */
public class TorqueSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location,
            Credential credential, XenonProperties properties, XenonEngine engine) throws XenonException {
        return new TorqueSchedulerConnection(adaptor, scheme, location, credential, properties, engine);
    }

}
