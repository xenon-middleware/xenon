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

package nl.esciencecenter.xenon.adaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public abstract class GenericTestConfig {

    private final String adaptorName;

    protected final Properties p;

    protected GenericTestConfig(String adaptorName, String configfile) throws FileNotFoundException, IOException {
        this.adaptorName = adaptorName;
        this.p = getTestProperties(configfile);
    }

    public static Properties getTestProperties(String configFilename) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        if (configFilename == null) {
            configFilename = System.getProperty("xenon.test.properties");
        }

        if (configFilename == null) {
            configFilename = System.getProperty("user.dir") + File.separator + "xenon.test.properties";
            
            if (!new File(configFilename).exists()) {
                configFilename = null;
            }
        }

        if (configFilename == null) {
            configFilename = System.getProperty("user.home") + File.separator + "xenon.test.properties";
            
            if (!new File(configFilename).exists()) {
                configFilename = null;
            }
        }

        if (configFilename == null) { 
            props.putAll(System.getProperties());
        } else { 
            props.load(new FileInputStream(configFilename));
        }
        return props;
    }

    public String getProperty(String name, String defaultValue) {
        String prop = p.getProperty(name);
        return prop != null ? prop : defaultValue;
    }

    public static String getPropertyOrFail(String adaptorName, Properties props, String name) throws UnknownPropertyException {
        String value = props.getProperty(name);
        if (value == null) {
            throw new UnknownPropertyException(adaptorName, "Failed to retrieve property " + name);
        }
        return value;
    }

    public final String getPropertyOrFail(String name) throws UnknownPropertyException {
        return getPropertyOrFail(adaptorName, p, name);
    }

    public String getAdaptorName() {
        return adaptorName;
    }

    public abstract String getScheme() throws Exception;
    
    public abstract String getCorrectLocation() throws Exception;
    
    public abstract String getWrongLocation() throws Exception;
    
    public abstract String getCorrectLocationWithUser() throws Exception;
    
    public abstract String getCorrectLocationWithWrongUser() throws Exception;
    
    public boolean supportUserInUri() {
        return false;
    }

    public boolean supportLocation() {
        return false;
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

    public abstract Map<String, String> getDefaultProperties() throws Exception;

    public Map<String, String> getUnknownProperties() throws Exception {
        throw new Exception("Adaptor " + adaptorName + " does not support unknown properties!");
    }

    public Map<String, String>[] getInvalidProperties() throws Exception {
        throw new Exception("Adaptor " + adaptorName + " does not support invalid properties!");
    }

    public Map<String, String> getCorrectProperties() throws Exception {
        throw new Exception("Adaptor " + adaptorName + " does not support properties!");
    }
}
