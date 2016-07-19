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

import nl.esciencecenter.xenon.clouds.Cloud;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;

public class CloudImplementation implements Cloud {

    private final String adaptorName;
    private final String uniqueID;
    private final String scheme;
    private final String location;
    
    private final Credential credential;
    
    private final XenonProperties properties;
    
    
    public CloudImplementation(String adaptorName, String uniqueID, String scheme, String location, 
            Credential credential, XenonProperties properties) { 
     
        this.adaptorName = adaptorName;
        this.uniqueID = uniqueID;
        this.scheme = scheme;
        this.location = location;
        this.credential = credential;
        
        if (properties == null) {
            this.properties = new XenonProperties();
        } else {
            this.properties = properties;
        }        
    }
    
    public Credential getCredential() {
        return credential;
    }

    public String getUniqueID() {
        return uniqueID;
    }
    
    @Override
    public String getAdaptorName() {
        return adaptorName;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties.toMap();
    }

}
