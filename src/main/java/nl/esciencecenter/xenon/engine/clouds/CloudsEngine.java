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

package nl.esciencecenter.xenon.engine.clouds;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.clouds.Cloud;
import nl.esciencecenter.xenon.clouds.Clouds;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;

/**
 * 
 * 
 * @version 2.0
 * @since 2.0
 */
public class CloudsEngine implements Clouds {

    private final XenonEngine xenonEngine;

    public CloudsEngine(XenonEngine xenonEngine) {
        this.xenonEngine = xenonEngine;
    }
    
    private Adaptor getAdaptor(Cloud cloud) throws XenonException {
        return xenonEngine.getAdaptor(cloud.getAdaptorName());
    }
       
    @Override
    public Cloud newCloud(String scheme, String location, Credential credential, Map<String, String> properties)
            throws XenonException {
        
        Adaptor adaptor = xenonEngine.getAdaptorFor(scheme);
        return adaptor.cloudsAdaptor().newCloud(scheme, location, credential, properties);
    }

    @Override
    public void close(Cloud cloud) throws XenonException {
        getAdaptor(cloud).cloudsAdaptor().close(cloud);
    }

    @Override
    public boolean isOpen(Cloud cloud) throws XenonException {
        return getAdaptor(cloud).cloudsAdaptor().isOpen(cloud);
    }
}
