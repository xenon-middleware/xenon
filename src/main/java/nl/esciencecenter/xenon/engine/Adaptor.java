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
package nl.esciencecenter.xenon.engine;

import java.util.ArrayList;
import java.util.Map;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.util.Utils;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more create functions of the Xenon interface,
 * depending on which functionality they provide.
 * 
 */
public abstract class Adaptor {

    private final String name;
    private final String description;
    private final ImmutableArray<String> supportedJobSchemes;
    private final ImmutableArray<String> supportedFileSchemes;
    private final ImmutableArray<String> supportedLocations;
    private final ImmutableArray<XenonPropertyDescription> validProperties;
    private final XenonProperties properties;
    private final XenonEngine xenonEngine;

    protected Adaptor(XenonEngine xenonEngine, String name, String description, ImmutableArray<String> supportedJobSchemes,
            ImmutableArray<String> supportedFileSchemes, ImmutableArray<String> supportedLocations, 
            ImmutableArray<XenonPropertyDescription> validProperties, XenonProperties properties) {

        super();

        this.xenonEngine = xenonEngine;
        this.name = name;
        this.description = description;
        
        if (supportedJobSchemes == null && supportedFileSchemes == null) { 
            throw new IllegalArgumentException("Both Job and File schemes are null!");
        }
        
        if (supportedJobSchemes == null) { 
            this.supportedJobSchemes = new ImmutableArray<>();
        } else { 
            this.supportedJobSchemes = supportedJobSchemes;
        }
            
        if (supportedFileSchemes == null) { 
            this.supportedFileSchemes = new ImmutableArray<>();
        } else { 
            this.supportedFileSchemes = supportedFileSchemes;
        }
        
        this.supportedLocations = supportedLocations;
        
        if (validProperties == null) {
            this.validProperties = new ImmutableArray<>();
        } else { 
            this.validProperties = validProperties;
        }
         
        this.properties = properties;
    }

    protected XenonEngine getXenonEngine() {
        return xenonEngine;
    }

    public XenonProperties getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    public boolean supportsJob(String scheme) {
        for (String s : supportedJobSchemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    } 
    
    
    public boolean supportsFile(String scheme) {
        for (String s : supportedFileSchemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    } 
    
    public boolean supports(String scheme) {
        return supportsJob(scheme) || supportsFile(scheme);
    }

    public XenonPropertyDescription[] getSupportedProperties() {
        return validProperties.asArray();
    }

    public ImmutableArray<XenonPropertyDescription> getSupportedProperties(Component level) {

        ArrayList<XenonPropertyDescription> tmp = new ArrayList<>();

        for (int i = 0; i < validProperties.length(); i++) {

            XenonPropertyDescription d = validProperties.get(i);

            if (d.getLevels().contains(level)) {
                tmp.add(d);
            }
        }

        return new ImmutableArray<>(tmp.toArray(new XenonPropertyDescription[tmp.size()]));
    }

    public AdaptorStatus getAdaptorStatus() {
        return new AdaptorStatusImplementation(name, description, supportedJobSchemes, supportedFileSchemes, supportedLocations, 
                validProperties, getAdaptorSpecificInformation());
    }

    public String[] getSupportedJobSchemes() {
        return supportedJobSchemes.asArray();
    }

    public String[] getSupportedFileSchemes() {
        return supportedFileSchemes.asArray();
    }

    public String[] getSupportedSchemes() {
        return Utils.merge(supportedJobSchemes.asArray(), supportedFileSchemes.asArray());
    }

    public String[] getSupportedLocations() {
        return supportedLocations.asArray();
    }
    
    @Override
    public String toString() {
        return "Adaptor [name=" + name + "]";
    }

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract Files filesAdaptor() throws XenonException;

    public abstract Jobs jobsAdaptor() throws XenonException;

    public abstract Credentials credentialsAdaptor() throws XenonException;

    public abstract void end();
}
