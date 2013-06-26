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

package nl.esciencecenter.octopus.adaptors;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public abstract class TestConfig {

    private final String adaptorName;
    
    protected TestConfig(String adaptorName) { 
        this.adaptorName = adaptorName;
    }
    
    public String getAdaptorName() { 
        return adaptorName;
    }
    
    public abstract URI getCorrectURI() throws Exception;
    public abstract URI getCorrectURIWithPath() throws Exception;
    public abstract URI getURIWrongPath() throws Exception; 
    
    public boolean supportURIUser() { 
        return false;
    }
    
    public URI getURIWrongUser() throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support user names!");
    }
    
    public boolean supportURILocation() { 
        return false;
    }
    
    public URI getURIWrongLocation() throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support locations!");
    }
    
    public boolean supportsCredentials() { 
        return false;
    }
    
    public boolean supportNonDefaultCredential() { 
        return false;
    }
    
    public boolean supportNullCredential() { 
        return false;
    }

    public abstract Credential getDefaultCredential(Credentials c) throws Exception;
    
    public Credential getNonDefaultCredential(Credentials c) throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support non-default credential!");
    }
    
    public Credential getPasswordCredential(Credentials c) throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support password credential!");
    }
    
    public Credential getInvalidCredential(Credentials c) throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support invalid credential!");
    }
    
    public boolean supportsProperties() throws Exception { 
        return false;
    }
    
    public abstract Properties getDefaultProperties() throws Exception;  

    public Properties getUnknownProperties() throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support unknown properties!");
    }
    
    public Properties [] getInvalidProperties() throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support invalid properties!");
    }
    
    public Properties getCorrectProperties() throws Exception { 
        throw new Exception("Adaptor " + adaptorName + " does not support properties!");
    }
}
