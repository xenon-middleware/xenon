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
package nl.esciencecenter.octopus.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more create functions of the Octopus interface,
 * depending on which functionality they provide.
 * 
 * @author Jason Maassen
 * 
 */
public abstract class Adaptor {

    private final String name;
    private final String description;
    private final String[] supportedSchemes;

    protected final OctopusEngine octopusEngine;

    private final String[][] defaultProperties;

    private final Map<String, String> supportedProperties;

    private final OctopusProperties properties;

    protected Adaptor(OctopusEngine octopusEngine, String name, String description, String[] supportedSchemes,
            String[][] defaultProperties, OctopusProperties properties) throws OctopusException {

        super();

        this.octopusEngine = octopusEngine;

        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes;

        this.defaultProperties = (defaultProperties == null ? new String[0][0] : defaultProperties);

        Map<String, String> tmp = new HashMap<String, String>();

        if (defaultProperties != null) {
            for (int i = 0; i < defaultProperties.length; i++) {
                tmp.put(defaultProperties[i][0], defaultProperties[i][2]);
            }
        }

        this.supportedProperties = Collections.unmodifiableMap(tmp);
        this.properties = processProperties(properties);
    }

    private OctopusProperties processProperties(OctopusProperties properties) throws OctopusException {

        Set<String> validSet = new HashSet<String>();

        for (int i = 0; i < defaultProperties.length; i++) {
            validSet.add(defaultProperties[i][0]);
        }

        OctopusProperties p = properties.filter("octopus.adaptors." + name);

        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            if (!validSet.contains(entry.getKey())) {
                throw new OctopusException(getName(), "Unknown property " + entry);
            }
        }

        return new OctopusProperties(defaultProperties, p);
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    public boolean supports(String scheme) {

        for (String s : supportedSchemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    }

    public Map<String, String> getSupportedProperties() {
        return supportedProperties;
    }

    public AdaptorStatus getAdaptorStatus() {
        return new AdaptorStatusImplementation(name, description, supportedSchemes, supportedProperties,
                getAdaptorSpecificInformation());
    }

    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }

    @Override
    public String toString() { 
        return name;
    }

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract Files filesAdaptor() throws OctopusException;

    public abstract Jobs jobsAdaptor() throws OctopusException;

    public abstract Credentials credentialsAdaptor() throws OctopusException;

    public abstract void end();    
}
