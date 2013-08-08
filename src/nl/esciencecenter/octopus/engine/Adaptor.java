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

import java.util.ArrayList;
import java.util.Map;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Level;
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
    private final OctopusPropertyDescription[] validProperties;
    private final OctopusProperties properties;
    private final OctopusEngine octopusEngine;

    protected Adaptor(OctopusEngine octopusEngine, String name, String description, String[] supportedSchemes,
            OctopusPropertyDescription[] validProperties, OctopusProperties properties) throws OctopusException {

        super();

        this.octopusEngine = octopusEngine;
        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes.clone();
        
        if (validProperties == null) {
            this.validProperties = new OctopusPropertyDescription[0];
        } else { 
            this.validProperties = validProperties.clone();
        } 
        
        this.properties = properties;
    }

    protected OctopusEngine getOctopusEngine() {
        return octopusEngine;
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

    public OctopusPropertyDescription[] getSupportedProperties() {
        return validProperties.clone();
    }

    public OctopusPropertyDescription[] getSupportedProperties(Level level) {

        ArrayList<OctopusPropertyDescription> tmp = new ArrayList<>();

        for (int i = 0; i < validProperties.length; i++) {

            OctopusPropertyDescription d = validProperties[i];

            if (d.getLevels().contains(level)) {
                tmp.add(d);
            }
        }

        return tmp.toArray(new OctopusPropertyDescription[tmp.size()]);
    }

    public AdaptorStatus getAdaptorStatus() {
        return new AdaptorStatusImplementation(name, description, supportedSchemes, getSupportedProperties(),
                getAdaptorSpecificInformation());
    }

    public String[] getSupportedSchemes() {
        return supportedSchemes.clone();
    }

    @Override
    public String toString() {
        return "Adaptor [name=" + name + "]";
    }

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract Files filesAdaptor() throws OctopusException;

    public abstract Jobs jobsAdaptor() throws OctopusException;

    public abstract Credentials credentialsAdaptor() throws OctopusException;

    public abstract void end();
}
