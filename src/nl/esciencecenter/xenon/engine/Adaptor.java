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
package nl.esciencecenter.xenon.engine;

import java.util.ArrayList;
import java.util.Map;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.CobaltPropertyDescription;
import nl.esciencecenter.xenon.CobaltPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more create functions of the Cobalt interface,
 * depending on which functionality they provide.
 * 
 * @author Jason Maassen
 * 
 */
public abstract class Adaptor {

    private final String name;
    private final String description;
    private final ImmutableArray<String> supportedSchemes;
    private final ImmutableArray<String> supportedLocations;
    private final ImmutableArray<CobaltPropertyDescription> validProperties;
    private final CobaltProperties properties;
    private final CobaltEngine cobaltEngine;

    protected Adaptor(CobaltEngine cobaltEngine, String name, String description, ImmutableArray<String> supportedSchemes,
            ImmutableArray<String> supportedLocations, ImmutableArray<CobaltPropertyDescription> validProperties, 
            CobaltProperties properties) throws CobaltException {

        super();

        this.cobaltEngine = cobaltEngine;
        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes;
        this.supportedLocations = supportedLocations;
        
        if (validProperties == null) {
            this.validProperties = new ImmutableArray<>();
        } else { 
            this.validProperties = validProperties;
        }
         
        this.properties = properties;
    }

    protected CobaltEngine getCobaltEngine() {
        return cobaltEngine;
    }

    public CobaltProperties getProperties() {
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

    public CobaltPropertyDescription[] getSupportedProperties() {
        return validProperties.asArray();
    }

    public ImmutableArray<CobaltPropertyDescription> getSupportedProperties(Component level) {

        ArrayList<CobaltPropertyDescription> tmp = new ArrayList<>();

        for (int i = 0; i < validProperties.length(); i++) {

            CobaltPropertyDescription d = validProperties.get(i);

            if (d.getLevels().contains(level)) {
                tmp.add(d);
            }
        }

        return new ImmutableArray<>(tmp.toArray(new CobaltPropertyDescription[tmp.size()]));
    }

    public AdaptorStatus getAdaptorStatus() {
        return new AdaptorStatusImplementation(name, description, supportedSchemes, supportedLocations, validProperties,
                getAdaptorSpecificInformation());
    }

    public String[] getSupportedSchemes() {
        return supportedSchemes.asArray();
    }

    public String[] getSupportedLocations() {
        return supportedLocations.asArray();
    }
    
    @Override
    public String toString() {
        return "Adaptor [name=" + name + "]";
    }

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract Files filesAdaptor() throws CobaltException;

    public abstract Jobs jobsAdaptor() throws CobaltException;

    public abstract Credentials credentialsAdaptor() throws CobaltException;

    public abstract void end();
}
